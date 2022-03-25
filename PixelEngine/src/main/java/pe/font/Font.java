package pe.font;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import pe.texture.Texture;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Objects;

import static org.lwjgl.stb.STBTruetype.*;
import static rutils.IOUtil.readFromFile;

@SuppressWarnings("unused")
public class Font
{
    private static final Logger LOGGER = new Logger();
    
    private static final HashMap<String, Font> CACHE = new HashMap<>();
    
    public static final String  DEFAULT_NAME    = "PressStart2P";
    public static final Weight  DEFAULT_WEIGHT  = Weight.REGULAR;
    public static final boolean DEFAULT_ITALICS = false;
    
    public static final int DEFAULT_SIZE = 24;
    
    static
    {
        register("font/PressStart2P/PressStart2P.ttf", Font.DEFAULT_NAME, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS, true);
        // register("fonts/Arial-Regular.ttf", "Arial", Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS, true);
    }
    
    public static final @NotNull Font DEFAULT = Font.get(Font.DEFAULT_NAME, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS);
    
    private final String  name;
    private final Weight  weight;
    private final boolean italicized;
    private final boolean kerning;
    
    private final String id;
    
    final STBTTFontinfo info;
    final ByteBuffer    fileData;
    
    final int ascentUnscaled;
    final int descentUnscaled;
    final int lineGapUnscaled;
    
    final CharData[] charData;
    
    final HashMap<@NotNull Integer, SizeData> sizeData;
    
    private Font(String filePath, String name, Weight weight, boolean italicized, boolean kerning)
    {
        this.name       = name;
        this.weight     = weight;
        this.italicized = italicized;
        this.kerning    = kerning;
        
        this.id = getID(this.name, this.weight, this.italicized);
        
        this.info     = STBTTFontinfo.create();
        this.fileData = readFromFile(filePath);
        
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
            
            IntBuffer advanceWidth    = stack.mallocInt(1);
            IntBuffer leftSideBearing = stack.mallocInt(1);
            
            IntBuffer x0 = stack.mallocInt(1);
            IntBuffer y0 = stack.mallocInt(1);
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            
            this.charData = new CharData[0xFFFF];
            for (int i = 0; i < 0xFFFF; i++) this.charData[i] = new CharData(this, i, advanceWidth, leftSideBearing, x0, y0, x1, y1);
        }
        
        this.sizeData = new HashMap<>();
        
        getSizeData(Font.DEFAULT_SIZE);
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
    
    /**
     * Destroys the Font and free's its memory.
     */
    public void destroy()
    {
        Font.LOGGER.fine("Destroying Font: %s", this);
        
        this.sizeData.forEach((i, sizeData) -> sizeData.texture.delete());
        this.sizeData.clear();
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
     * @return Gets the scale of the size data.
     */
    public double scale(int size)
    {
        return getSizeData(size).scale;
    }
    
    /**
     * @return Gets the ascent of the size data.
     */
    public double ascent(int size)
    {
        return getSizeData(size).ascent;
    }
    
    /**
     * @return Gets the descent of the size data.
     */
    public double descent(int size)
    {
        return getSizeData(size).descent;
    }
    
    /**
     * @return Gets the lineGap of the size data.
     */
    public double lineGap(int size)
    {
        return getSizeData(size).lineGap;
    }
    
    /**
     * @return Gets the texture map of the size data.
     */
    public Texture texture(int size)
    {
        return getSizeData(size).texture;
    }
    
    /**
     * Method to getBytes the character data for a specific size, generating it if necessary.
     *
     * @param size The size in pixels.
     * @return The size data.
     */
    public SizeData getSizeData(int size)
    {
        return this.sizeData.computeIfAbsent(size, s -> {
            Font.LOGGER.finest("Generating SizeData for font \"%s\" for size \"%s\"", this, s);
            
            return new SizeData(this, s);
        });
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
        
        SizeData sizeData = getSizeData(size);
        
        double width = 0;
        
        String[] lines = text.split("\n");
        if (lines.length == 1)
        {
            CharData currChar, prevChar = null;
            for (int i = 0, n = text.length(); i < n; i++)
            {
                currChar = getCharData(text.charAt(i));
                width += currChar.advanceWidthUnscaled + getKernAdvance(prevChar, currChar, size);
                
                prevChar = currChar;
            }
            return width * sizeData.scale;
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
        return lines.length * size;
    }
    
    /**
     * Gets the kerning between two characters. If kerning is disabled then this offset is zero.
     *
     * @param ch1  The first character.
     * @param ch2  The second character.
     * @param size The size of the font.
     * @return The number of pixels to offset ch2 when rendering.
     */
    public double getKernAdvance(CharData ch1, CharData ch2, int size)
    {
        if (ch1 == null) return 0.0;
        if (ch2 == null) return 0.0;
        if (!this.kerning) return 0.0;
        return stbtt_GetGlyphKernAdvance(this.info, ch1.index, ch2.index) * getSizeData(size).scale;
    }
    
    public double getAdvance(CharData ch, int size)
    {
        if (ch == null) return 0.0;
        return ch.advanceWidthUnscaled * getSizeData(size).scale;
    }
    
    public PackedQuad getPackedQuad(char character, int size)
    {
        return getSizeData(size).getPackedQuad(character);
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
     */
    public static void register(@NotNull String filePath, @NotNull String name, @NotNull Weight weight, boolean italicized, boolean kerning)
    {
        String fontID = getID(name, weight, italicized);
        
        if (Font.CACHE.containsKey(fontID))
        {
            Font.LOGGER.warning("Font already registered: " + fontID);
            return;
        }
        
        Font.LOGGER.fine("Registering Font: " + fontID);
        
        Font.CACHE.put(fontID, new Font(filePath, name, weight, italicized, kerning));
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
     * Gets a font with the specified properties.
     *
     * @param name   The font name.
     * @param weight The weight of the font.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull String name, @NotNull Weight weight)
    {
        return get(name, weight, Font.DEFAULT_ITALICS);
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param name       The font name.
     * @param italicized Whether the font is italic styled or not.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull String name, boolean italicized)
    {
        return get(name, Font.DEFAULT_WEIGHT, italicized);
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param weight     The weight of the font.
     * @param italicized Whether the font is italic styled or not.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull Weight weight, boolean italicized)
    {
        return get(Font.DEFAULT_NAME, weight, italicized);
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param name The font name.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull String name)
    {
        return get(name, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS);
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param weight The weight of the font.
     * @return The font object.
     */
    public static @NotNull Font get(@NotNull Weight weight)
    {
        return get(Font.DEFAULT_NAME, weight, Font.DEFAULT_ITALICS);
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param italicized Whether the font is italic styled or not.
     * @return The font object.
     */
    public static @NotNull Font get(boolean italicized)
    {
        return get(Font.DEFAULT_NAME, Font.DEFAULT_WEIGHT, italicized);
    }
    
    /**
     * Checks if the font is registered.
     *
     * @param name       The font name.
     * @param weight     The weight of the font.
     * @param italicized Whether the font is italic styled or not.
     * @return {@code true} if the font is registered.
     */
    public static boolean exists(@NotNull String name, @NotNull Weight weight, boolean italicized)
    {
        return Font.CACHE.containsKey(getID(name, weight, italicized));
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
}
