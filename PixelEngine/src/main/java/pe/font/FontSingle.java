package pe.font;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.ColorFormat;
import pe.render.DrawMode;
import pe.render.GLBatch;
import pe.texture.Texture;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.stb.STBTruetype.*;

public class FontSingle extends Font
{
    private static final Logger LOGGER = new Logger();
    
    public static final int PLATFORM_ID = STBTT_PLATFORM_ID_MICROSOFT;
    public static final int ENCODING_ID = STBTT_MS_EID_UNICODE_BMP;
    public static final int LANGUAGE_ID = STBTT_MS_LANG_ENGLISH;
    
    /**
     * Builds a font tag with the provided properties.
     *
     * @param family     The font family
     * @param weight     The weight of the font
     * @param italicized If the font is italicized.
     * @return The tag string.
     */
    public static @NotNull String getID(@NotNull String family, @NotNull Weight weight, boolean italicized)
    {
        return FontFamily.getID(family) + '_' + weight.tag() + (italicized ? "_italicized" : "");
    }
    
    static @Nullable String nameString(@NotNull STBTTFontinfo info, int nameID)
    {
        ByteBuffer value = stbtt_GetFontNameString(info, FontSingle.PLATFORM_ID, FontSingle.ENCODING_ID, FontSingle.LANGUAGE_ID, nameID);
        if (value != null)
        {
            ByteBuffer string = MemoryUtil.memAlloc(value.order(ByteOrder.BIG_ENDIAN).capacity() >> 1);
            while (value.hasRemaining())
            {
                value.get();
                string.put(value.get());
            }
            string.flip();
            String result = MemoryUtil.memUTF8(string);
            MemoryUtil.memFree(string);
            return result;
        }
        return null;
    }
    
    // -------------------- Instance -------------------- //
    
    final STBTTFontinfo info;
    final ByteBuffer    fileData;
    
    final String  family;
    final Weight  weight;
    final boolean italicized;
    
    final boolean kerning;
    final boolean alignToInt;
    
    final String id;
    
    public final int ascentUnscaled;
    public final int descentUnscaled;
    public final int lineGapUnscaled;
    
    final CharData[] charData;
    
    final Texture texture;
    
    FontSingle(STBTTFontinfo info, ByteBuffer fileData, String family, Weight weight, boolean italicized, boolean kerning, boolean alignToInt)
    {
        this.info     = info;
        this.fileData = fileData;
        
        this.family     = family;
        this.weight     = weight;
        this.italicized = italicized;
        
        this.kerning    = kerning;
        this.alignToInt = alignToInt;
        
        this.id = getID(this.family, this.weight, this.italicized);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer ascent  = stack.mallocInt(1);
            IntBuffer descent = stack.mallocInt(1);
            IntBuffer lineGap = stack.mallocInt(1);
            
            stbtt_GetFontVMetrics(this.info, ascent, descent, lineGap);
            
            this.ascentUnscaled  = ascent.get(0);
            this.descentUnscaled = descent.get(0);
            this.lineGapUnscaled = lineGap.get(0);
            
            int baseSize = 96;
            
            STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(0xFFFF);
            
            int width;
            int height;
            
            ByteBuffer buffer;
            
            boolean success;
            
            int textureSize = 32;
            int samples     = 2;
            while (true)
            {
                width  = baseSize * textureSize;
                height = baseSize * (textureSize >> 1);
                
                buffer = MemoryUtil.memAlloc(width * height);
                
                charData.position(32);
                try (STBTTPackContext pc = STBTTPackContext.malloc())
                {
                    stbtt_PackBegin(pc, buffer, width, height, 0, 2, MemoryUtil.NULL);
                    stbtt_PackSetOversampling(pc, samples, samples);
                    success = stbtt_PackFontRange(pc, this.fileData, 0, baseSize, charData.position(), charData);
                    stbtt_PackEnd(pc);
                }
                charData.clear();
                buffer.clear();
                
                textureSize <<= 1;
                
                if (success || textureSize >= 1000) break;
                MemoryUtil.memFree(buffer);
            }
            
            IntBuffer advanceWidth    = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);
            
            IntBuffer x0 = stack.mallocInt(1);
            IntBuffer y0 = stack.mallocInt(1);
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            
            FloatBuffer x = stack.floats(0);
            FloatBuffer y = stack.floats(0);
            
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            this.charData = new CharData[0xFFFF];
            for (int i = 0, n = this.charData.length; i < n; i++)
            {
                int index = stbtt_FindGlyphIndex(this.info, i);
                
                stbtt_GetGlyphHMetrics(this.info, index, advanceWidth, leftSideBearing);
                stbtt_GetGlyphBox(this.info, index, x0, y0, x1, y1);
                stbtt_GetPackedQuad(charData, width, height, i, x, y, quad, this.alignToInt);
                
                int x0Unscaled = x0.get(0);
                int y0Unscaled = this.ascentUnscaled - y1.get(0);
                int x1Unscaled = x1.get(0);
                int y1Unscaled = this.ascentUnscaled - y0.get(0);
                
                this.charData[i] = new CharData((char) i, index,
                                                advanceWidth.get(0), leftSideBearing.get(0),
                                                x0Unscaled, y0Unscaled, x1Unscaled, y1Unscaled,
                                                quad.s0(), quad.t0(), quad.s1(), quad.t1());
            }
            
            charData.free();
            
            // Converts GL_RED to GL_RGBA
            ByteBuffer data = MemoryUtil.memAlloc(width * height * 4);
            for (int i = 0; i < buffer.capacity(); i++) data.putInt((buffer.get(i) << 24) | 0x00FFFFFF);
            data.flip();
            
            this.texture = Texture.load(data, width, height, 1, ColorFormat.RGBA);
            
            MemoryUtil.memFree(buffer);
            MemoryUtil.memFree(data);
        }
    }
    
    @Override
    public String toString()
    {
        return "Font{" + this.id + '}';
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontSingle font = (FontSingle) o;
        return this.id.equals(font.id);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.family, this.weight, this.italicized);
    }
    
    public void delete()
    {
        if (Font.DEFAULT_FONT_INST != this)
        {
            FontSingle.LOGGER.fine("Deleting Font: %s", this);
            
            this.info.free();
            MemoryUtil.memFree(this.fileData);
            
            this.texture.delete();
            
            Font.FONT_CACHE.remove(this.id);
        }
    }
    
    /**
     * @return The font name
     */
    public String name()
    {
        return this.family;
    }
    
    /**
     * @return The weight of the font
     */
    public Weight weight()
    {
        return this.weight;
    }
    
    /**
     * @return true if the font is italicized
     */
    public boolean italicized()
    {
        return this.italicized;
    }
    
    /**
     * @return true if font kerning will be respected
     */
    public boolean kerning()
    {
        return this.kerning;
    }
    
    /**
     * @return true if characters should be aligned to integer coordinates.
     */
    public boolean alignToInt()
    {
        return this.alignToInt;
    }
    
    /**
     * @return Gets the scale of the size data.
     */
    public double scale(int size)
    {
        return stbtt_ScaleForPixelHeight(this.info, size);
    }
    
    /**
     * Gets the kerning between two characters. If kerning is disabled then this offset is zero.
     *
     * @param ch1 The first character.
     * @param ch2 The second character.
     * @return The number of pixels to offset ch2 when rendering.
     */
    public double getKernAdvanceUnscaled(CharData ch1, CharData ch2)
    {
        if (ch1 == null) return 0.0;
        if (ch2 == null) return 0.0;
        if (!this.kerning) return 0.0;
        return stbtt_GetGlyphKernAdvance(this.info, ch1.index(), ch2.index());
    }
    
    /**
     * Calculates the width in pixels of the string. If the string contains line breaks, then it calculates the widest line and returns it.
     *
     * @param text The text.
     * @param size The size of the text.
     * @return The width in pixels of the string.
     */
    @Override
    public double getTextWidth(@NotNull String text, int size)
    {
        FontSingle.LOGGER.finest("Getting text width for text \"%s\" with font \"%s\" of size \"%s\"", text, this, size);
        
        double width = 0;
        
        String[] lines = text.split("\n");
        if (lines.length == 1)
        {
            CharData currChar, prevChar = null;
            for (int i = 0, n = text.length(); i < n; i++)
            {
                currChar = this.charData[text.charAt(i)];
                
                width += getKernAdvanceUnscaled(prevChar, currChar) + currChar.advanceWidthUnscaled();
                
                prevChar = currChar;
            }
            return width * scale(size);
        }
        else
        {
            for (String line : lines)
            {
                width = Math.max(width, getTextWidth(line, size));
            }
            return width;
        }
    }
    
    /**
     * Calculates the height in pixels of the string. If the string contains line breaks, then it calculates the total height of all lines.
     *
     * @param text The text.
     * @param size The size of the text.
     * @return The height in pixels of the string.
     */
    @Override
    public double getTextHeight(@NotNull String text, int size)
    {
        FontSingle.LOGGER.finest("Getting text height for text \"%s\" with font \"%s\" of size \"%s\"", text, this, size);
        
        String[] lines = text.split("\n");
        return lines.length * (this.ascentUnscaled - this.descentUnscaled + this.lineGapUnscaled) * scale(size);
    }
    
    @Override
    public void drawText(@NotNull String text, int size, double x, double y, int r, int g, int b, int a)
    {
        double scale = scale(size);
        
        CharData prevChar = null, currChar;
        for (int i = 0, n = text.length(); i < n; i++)
        {
            currChar = this.charData[text.charAt(i)];
            
            x += getKernAdvanceUnscaled(prevChar, currChar) * scale;
            
            double x0 = x + currChar.x0Unscaled() * scale;
            double y0 = y + currChar.y0Unscaled() * scale;
            double x1 = x + currChar.x1Unscaled() * scale;
            double y1 = y + currChar.y1Unscaled() * scale;
            
            GLBatch.checkBuffer(6);
            
            GLBatch.setTexture(this.texture);
            
            GLBatch.begin(DrawMode.TRIANGLES);
            
            // 0
            GLBatch.pos(x0, y0);
            GLBatch.texCoord(currChar.u0(), currChar.v0());
            GLBatch.color(r, g, b, a);
            
            // 1
            GLBatch.pos(x0, y1);
            GLBatch.texCoord(currChar.u0(), currChar.v1());
            GLBatch.color(r, g, b, a);
            
            // 2
            GLBatch.pos(x1, y1);
            GLBatch.texCoord(currChar.u1(), currChar.v1());
            GLBatch.color(r, g, b, a);
            
            // 0
            GLBatch.pos(x0, y0);
            GLBatch.texCoord(currChar.u0(), currChar.v0());
            GLBatch.color(r, g, b, a);
            
            // 2
            GLBatch.pos(x1, y1);
            GLBatch.texCoord(currChar.u1(), currChar.v1());
            GLBatch.color(r, g, b, a);
            
            // 3
            GLBatch.pos(x1, y0);
            GLBatch.texCoord(currChar.u1(), currChar.v0());
            GLBatch.color(r, g, b, a);
            
            GLBatch.end();
            
            x += currChar.advanceWidthUnscaled() * scale;
            
            prevChar = currChar;
        }
    }
}
