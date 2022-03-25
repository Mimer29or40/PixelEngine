package pe.font;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.ColorFormat;
import pe.texture.Image;
import pe.texture.Texture;
import rutils.IOUtil;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.stb.STBTruetype.*;

public class Font
{
    private static final Logger LOGGER = new Logger();
    
    public static final String  DEFAULT_NAME    = "PressStart2P";
    public static final Weight  DEFAULT_WEIGHT  = Weight.REGULAR;
    public static final boolean DEFAULT_ITALICS = false;
    
    public static final int DEFAULT_SIZE = 24;
    
    private static final HashMap<String, Font> CACHE = new HashMap<>();
    
    private static Font DEFAULT;
    
    public static void setup()
    {
        if (Font.DEFAULT != null)
        {
            Font.LOGGER.warning("Font already setup");
            return;
        }
        
        Font.LOGGER.fine("Setup");
        
        Font.DEFAULT = register("font/PressStart2P/PressStart2P.ttf",
                                Font.DEFAULT_NAME,
                                Font.DEFAULT_WEIGHT,
                                Font.DEFAULT_ITALICS,
                                true,
                                false);
    }
    
    public static void destroy()
    {
        Font.LOGGER.fine("Destroy");
        
        Font.DEFAULT = null;
        Font.CACHE.values().forEach(Font::delete);
        Font.CACHE.clear();
    }
    
    /**
     * Registers a font to be used. All fonts need to be registered before they can be used. Font instances are owned by this class.
     * <p>
     * There is no checking if the characteristics provided actually match the font.
     *
     * @param filePath   The path to the .ttf file
     * @param name       The registry name of the font.
     * @param weight     The weight of the font.
     * @param italicized If the font is italicized
     * @param kerning    If kerning should be used when rendering.
     * @param alignToInt If each character should align to integer values.
     */
    public static Font register(@NotNull String filePath, @NotNull String name, @NotNull Weight weight, boolean italicized, boolean kerning, boolean alignToInt)
    {
        String fontID = getID(name, weight, italicized);
        
        if (Font.CACHE.containsKey(fontID))
        {
            Font.LOGGER.warning("Font already registered: " + fontID);
            return Font.CACHE.get(fontID);
        }
        
        Font.LOGGER.fine("Registering Font: " + fontID);
        
        Font font = new Font(filePath, name, weight, italicized, kerning, alignToInt);
        Font.CACHE.put(fontID, font);
        return font;
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param name       The font name.
     * @param weight     The weight of the font.
     * @param italicized Whether the font is italic styled or not.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull String name, @NotNull Weight weight, boolean italicized)
    {
        String fontID = getID(name, weight, italicized);
        
        if (Font.CACHE.containsKey(fontID)) return Font.CACHE.get(fontID);
        
        Font.LOGGER.warning("Font is not registered: " + fontID);
        
        return Font.DEFAULT;
    }
    
    /**
     * Builds a font tag with the provided properties.
     *
     * @param name       The font name
     * @param weight     The weight of the font
     * @param italicized If the font is italicized.
     * @return The tag string.
     */
    public static @NotNull String getID(@NotNull String name, @NotNull Weight weight, boolean italicized)
    {
        return name + '_' + weight.tag() + (italicized ? "_italicized" : "");
    }
    
    // -------------------- Instance -------------------- //
    
    private final String  name;
    private final Weight  weight;
    private final boolean italicized;
    private final boolean kerning;
    private final boolean alignToInt;
    
    private final String id;
    
    final STBTTFontinfo info;
    final ByteBuffer    fileData;
    
    public final int ascentUnscaled;
    public final int descentUnscaled;
    public final int lineGapUnscaled;
    
    final CharData[] charData;
    
    final Texture texture;
    
    private Font(String filePath, String name, Weight weight, boolean italicized, boolean kerning, boolean alignToInt)
    {
        this.name       = name;
        this.weight     = weight;
        this.italicized = italicized;
        this.kerning    = kerning;
        this.alignToInt = alignToInt;
        
        this.id = getID(this.name, this.weight, this.italicized);
        
        this.info     = STBTTFontinfo.malloc();
        this.fileData = IOUtil.readFromFile(filePath);
        
        if (this.fileData == null || !stbtt_InitFont(this.info, this.fileData)) throw new RuntimeException("Font Data could not be loaded: " + this.id);
        
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
            
            FloatBuffer x = stack.mallocFloat(1);
            FloatBuffer y = stack.mallocFloat(1);
            
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
            
            this.charData = new CharData[0xFFFF];
            for (int i = 0, n = this.charData.length; i < n; i++)
            {
                x.put(0, 0);
                y.put(0, 0);
                
                stbtt_GetPackedQuad(charData, width, height, i, x, y, quad, this.alignToInt);
                
                this.charData[i] = new CharData(i, quad);
            }
            
            charData.free();
            
            // Converts GL_RED to GL_RGBA
            ByteBuffer data = MemoryUtil.memAlloc(width * height * 4);
            for (int i = 0; i < buffer.capacity(); i++) data.putInt((buffer.get(i) << 24) | 0x00FFFFFF);
            data.flip();
            
            this.texture = Texture.load(data, width, height, 1, ColorFormat.RGBA);
            // this.texture.filter(TextureFilter.LINEAR, TextureFilter.LINEAR);
            
            Image image = Image.load(data, width, height, 1, ColorFormat.RGBA);
            image.export(this.id + ".png");
            
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
        Font font = (Font) o;
        return this.id.equals(font.id);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.name, this.weight, this.italicized);
    }
    
    public void delete()
    {
        if (Font.DEFAULT != this)
        {
            Font.LOGGER.fine("Deleting Font: %s", this);
            
            this.info.free();
            MemoryUtil.memFree(this.fileData);
            
            this.texture.delete();
        }
    }
    
    /**
     * @return The font name
     */
    public String name()
    {
        return this.name;
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
     * @return Gets the texture map of the font.
     */
    public Texture texture()
    {
        return this.texture;
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
        return stbtt_GetGlyphKernAdvance(this.info, ch1.index, ch2.index);
    }
    
    /**
     * Gets a read-only class with the metrics for a specific character
     *
     * @param character The character.
     * @return The character data.
     */
    public CharData getCharData(int character)
    {
        return this.charData[character];
    }
    
    /**
     * Calculates the width in pixels of the string. If the string contains line breaks, then it calculates the widest line and returns it.
     *
     * @param text The text.
     * @param size The size of the text.
     * @return The width in pixels of the string.
     */
    public double getTextWidth(@NotNull String text, int size)
    {
        Font.LOGGER.finest("Getting text width for text \"%s\" with font \"%s\" of size \"%s\"", text, this, size);
        
        double width = 0;
        
        String[] lines = text.split("\n");
        if (lines.length == 1)
        {
            CharData currChar, prevChar = null;
            for (int i = 0, n = text.length(); i < n; i++)
            {
                currChar = getCharData(text.charAt(i));
                
                width += getKernAdvanceUnscaled(prevChar, currChar) + currChar.advanceWidthUnscaled;
                
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
    public double getTextHeight(@NotNull String text, int size)
    {
        Font.LOGGER.finest("Getting text height for text \"%s\" with font \"%s\" of size \"%s\"", text, this, size);
        
        String[] lines = text.split("\n");
        return lines.length * (this.ascentUnscaled - this.descentUnscaled + this.lineGapUnscaled) * scale(size);
    }
    
    public final class CharData
    {
        public final char character;
        public final int  index;
        public final int  advanceWidthUnscaled, leftSideBearingUnscaled;
        public final int x0Unscaled, y0Unscaled, x1Unscaled, y1Unscaled;
        public final double u0, v0, u1, v1;
        
        private CharData(int character, @NotNull STBTTAlignedQuad quad)
        {
            this.character = (char) character;
            this.index     = stbtt_FindGlyphIndex(Font.this.info, this.character);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer advanceWidth    = stack.mallocInt(1);
                IntBuffer leftSideBearing = stack.mallocInt(1);
                
                stbtt_GetGlyphHMetrics(Font.this.info, this.index, advanceWidth, leftSideBearing);
                
                this.advanceWidthUnscaled    = advanceWidth.get(0);
                this.leftSideBearingUnscaled = leftSideBearing.get(0);
                
                IntBuffer x0 = stack.mallocInt(1);
                IntBuffer y0 = stack.mallocInt(1);
                IntBuffer x1 = stack.mallocInt(1);
                IntBuffer y1 = stack.mallocInt(1);
                
                stbtt_GetGlyphBox(Font.this.info, this.index, x0, y0, x1, y1);
                
                this.x0Unscaled = x0.get(0);
                this.y0Unscaled = Font.this.ascentUnscaled - y1.get(0);
                this.x1Unscaled = x1.get(0);
                this.y1Unscaled = Font.this.ascentUnscaled - y0.get(0);
                
                this.u0 = quad.s0();
                this.v0 = quad.t0();
                this.u1 = quad.s1();
                this.v1 = quad.t1();
            }
        }
        
        @Override
        public String toString()
        {
            return "CharData{" + this.character + ", font=" + Font.this + "}";
        }
    }
}
