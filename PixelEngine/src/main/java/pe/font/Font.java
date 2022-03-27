package pe.font;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryUtil;
import pe.render.DrawMode;
import pe.render.GL;
import pe.render.GLBatch;
import rutils.IOUtil;
import rutils.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;

public abstract class Font
{
    private static final Logger LOGGER = new Logger();
    
    public static final String  DEFAULT_FAMILY  = "Press Start 2P";
    public static final Weight  DEFAULT_WEIGHT  = Weight.REGULAR;
    public static final boolean DEFAULT_ITALICS = false;
    
    public static final int DEFAULT_SIZE = 24;
    
    static final HashMap<String, FontSingle> FONT_CACHE   = new HashMap<>();
    static final HashMap<String, FontFamily> FAMILY_CACHE = new HashMap<>();
    
    static FontSingle DEFAULT_FONT_INST;
    static FontFamily DEFAULT_FAMILY_INST;
    
    public static void setup()
    {
        if (Font.DEFAULT_FONT_INST != null)
        {
            Font.LOGGER.warning("Font already setup");
            return;
        }
        
        Font.LOGGER.fine("Setup");
        
        Font.DEFAULT_FONT_INST = register("font/PressStart2P/PressStart2P.ttf",
                                          true,
                                          false,
                                          false);
        
        Font.DEFAULT_FAMILY_INST = registerFamily("font/PressStart2P",
                                                  Font.DEFAULT_FAMILY,
                                                  true,
                                                  false,
                                                  false);
    }
    
    public static void destroy()
    {
        Font.LOGGER.fine("Destroy");
        
        Font.DEFAULT_FAMILY_INST = null;
        List.copyOf(Font.FAMILY_CACHE.values()).forEach(FontFamily::delete);
        
        Font.DEFAULT_FONT_INST = null;
        List.copyOf(Font.FONT_CACHE.values()).forEach(FontSingle::delete);
    }
    
    private static @NotNull FontSingle register(@NotNull String filePath, boolean kerning, boolean alignToInt, boolean interpolated, boolean warn)
    {
        STBTTFontinfo info     = STBTTFontinfo.malloc();
        ByteBuffer    fileData = IOUtil.readFromFile(filePath, new int[1], MemoryUtil::memAlloc);
        
        if (fileData == null || !stbtt_InitFont(info, fileData)) throw new RuntimeException("Font Data could not be loaded: " + filePath);
        
        String fontFamilyName    = FontSingle.nameString(info, 1);  // Font Family name
        String fontSubfamilyName = FontSingle.nameString(info, 2);  // Font Subfamily name
        String typoFamilyName    = FontSingle.nameString(info, 16); // Typographic Family name
        String typoSubfamilyName = FontSingle.nameString(info, 17); // Typographic Subfamily name
        
        String family = typoFamilyName != null ? typoFamilyName : fontFamilyName != null ? fontFamilyName : Font.DEFAULT_FAMILY;
        
        Weight  weight;
        boolean italicized;
        
        String subfamily = typoSubfamilyName != null ? typoSubfamilyName : fontSubfamilyName;
        if (subfamily != null)
        {
            subfamily = subfamily.toLowerCase();
            
            Weight possibleWeight = Weight.get(subfamily);
            weight     = possibleWeight != null ? possibleWeight : Font.DEFAULT_WEIGHT;
            italicized = subfamily.contains("italic");
        }
        else
        {
            weight     = Font.DEFAULT_WEIGHT;
            italicized = Font.DEFAULT_ITALICS;
        }
        
        String fontID = FontSingle.getID(family, weight, italicized);
        
        if (Font.FONT_CACHE.containsKey(fontID))
        {
            info.free();
            MemoryUtil.memFree(fileData);
            
            if (warn) Font.LOGGER.warning("Font already registered: " + fontID);
            return Font.FONT_CACHE.get(fontID);
        }
        
        Font.LOGGER.fine("Loading Font \"%s\" from file: %s", fontID, filePath);
        
        FontSingle font = new FontSingle(info, fileData, family, weight, italicized, kerning, alignToInt, interpolated);
        
        Font.FONT_CACHE.put(fontID, font);
        return font;
    }
    
    /**
     * Registers a font to be used. All fonts need to be registered before they can be used. Font instances are owned by this class.
     * <p>
     * There is no checking if the characteristics provided actually match the font.
     *
     * @param filePath     The path to the .ttf file
     * @param kerning      If kerning should be used when rendering.
     * @param alignToInt   If each character should align to integer values.
     * @param interpolated If the texture should be linear interpolated.
     */
    public static @NotNull FontSingle register(@NotNull String filePath, boolean kerning, boolean alignToInt, boolean interpolated)
    {
        return register(filePath, kerning, alignToInt, interpolated, true);
    }
    
    /**
     * Registers a font family to be used.
     *
     * @param directory    The path to the directory where the .ttf files are located.
     * @param name         The registry name for the family.
     * @param kerning      If kerning should be used when rendering.
     * @param alignToInt   If each character should align to integer values.
     * @param interpolated If the texture should be linear interpolated.
     */
    public static @NotNull FontFamily registerFamily(@NotNull String directory, @NotNull String name, boolean kerning, boolean alignToInt, boolean interpolated)
    {
        String familyID = FontFamily.getID(name);
        
        if (Font.FAMILY_CACHE.containsKey(familyID))
        {
            Font.LOGGER.warning("Font Family already registered: " + familyID);
            return Font.FAMILY_CACHE.get(familyID);
        }
        
        Font.LOGGER.fine("Registering Font Family \"%s\" in directory: %s", familyID, directory);
        
        Path familyPath = IOUtil.getPath(directory);
        
        try
        {
            Files.list(familyPath).forEach(path -> {
                String fileName = FontFamily.getID(path.getFileName().toString());
                if (fileName.startsWith(familyID) && fileName.endsWith(".ttf"))
                {
                    FontSingle font = Font.register(path.toString(), kerning, alignToInt, interpolated, false);
                    Font.LOGGER.fine("Added %s to Font Family: %s", font, familyID);
                }
            });
        }
        catch (IOException e)
        {
            Font.LOGGER.warning("Fonts could not be created.", e);
        }
        
        FontFamily family = new FontFamily(name);
        Font.FAMILY_CACHE.put(name, family);
        return family;
    }
    
    /**
     * Gets a font with the specified properties.
     *
     * @param family     The font family, or {@code null} for default.
     * @param weight     The weight of the font, or {@code null} for default.
     * @param italicized Whether the font is italic styled or not, or {@code null} for default.
     * @return The font object.
     */
    public static @NotNull FontSingle get(@Nullable String family, @Nullable Weight weight, @Nullable Boolean italicized)
    {
        if (family == null) family = Font.DEFAULT_FAMILY;
        if (weight == null) weight = Font.DEFAULT_WEIGHT;
        if (italicized == null) italicized = Font.DEFAULT_ITALICS;
        
        String fontID = FontSingle.getID(family, weight, italicized);
        
        if (Font.FONT_CACHE.containsKey(fontID)) return Font.FONT_CACHE.get(fontID);
        
        Font.LOGGER.warning("Font is not registered: " + fontID);
        
        return Font.DEFAULT_FONT_INST;
    }
    
    /**
     * Gets the font family for the given name.
     *
     * @param name The family name, or {@code null} for default.
     * @return The font family or the default family.
     */
    public static @NotNull FontFamily getFamily(@Nullable String name)
    {
        if (name == null) name = Font.DEFAULT_FAMILY;
        
        if (Font.FAMILY_CACHE.containsKey(name)) return Font.FAMILY_CACHE.get(name);
        
        Font.LOGGER.warning("FontFamily is not registered: " + name);
        
        return Font.DEFAULT_FAMILY_INST;
    }
    
    public static boolean isRegistered(@Nullable String family, @Nullable Weight weight, @Nullable Boolean italicized)
    {
        if (family == null) family = Font.DEFAULT_FAMILY;
        if (weight == null) weight = Font.DEFAULT_WEIGHT;
        if (italicized == null) italicized = Font.DEFAULT_ITALICS;
        
        String fontID = FontSingle.getID(family, weight, italicized);
        
        return Font.FONT_CACHE.containsKey(fontID);
    }
    
    public static boolean isFamilyRegistered(@Nullable String name)
    {
        if (name == null) name = Font.DEFAULT_FAMILY;
        
        return Font.FAMILY_CACHE.containsKey(name);
    }
    
    // -------------------- Instance -------------------- //
    
    public abstract @NotNull FontSingle withProperties(@NotNull Weight weight, boolean italicized);
    
    public abstract @NotNull FontSingle withProperties(@NotNull Weight weight);
    
    public abstract @NotNull FontSingle withProperties(boolean italicized);
    
    /**
     * Calculates the width in pixels of the string. If the string contains line breaks, then it calculates the widest line and returns it.
     *
     * @param text The text.
     * @param size The size of the text.
     * @return The width in pixels of the string.
     */
    public double getTextWidth(@NotNull String text, int size)
    {
        TextState state = new TextState(this, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS, size);
        
        String[] lines = text.split("\n");
        if (lines.length == 1) return getTextWidthImpl(text, state);
        
        double width = 0;
        for (String line : lines) width = Math.max(width, getTextWidthImpl(line, state));
        return width;
    }
    
    public double getTextWidthImpl(@NotNull String line, TextState state)
    {
        double width = 0;
        
        CharData prevChar = null, currChar;
        
        for (int i = 0, n = line.length(); i < n; i++)
        {
            char character = line.charAt(i);
            
            if (state.handleModifier(character)) continue;
            
            state.changeFont();
            
            currChar = state.currFont.charData.get(line.charAt(i));
            
            double scale = state.currFont.scale(state.size);
            
            width += currChar.advanceWidthUnscaled() * scale;
            
            if (state.prevFont == state.currFont) width += state.currFont.getKernAdvanceUnscaled(prevChar, currChar) * scale;
            
            prevChar = currChar;
        }
        return width;
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
        TextState state = new TextState(this, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS, size);
        
        String[] lines = text.split("\n");
        
        double height = 0;
        for (String line : lines) height += getTextHeightImpl(line, state);
        return height;
    }
    
    public double getTextHeightImpl(@NotNull String line, TextState state)
    {
        double height = 0;
        
        for (int i = 0, n = line.length(); i < n; i++)
        {
            char character = line.charAt(i);
            
            if (state.handleModifier(character)) continue;
            
            state.changeFont();
            
            double scale = state.currFont.scale(state.size);
            
            double charHeight = state.currFont.ascentUnscaled - state.currFont.descentUnscaled + state.currFont.lineGapUnscaled;
            
            height = Math.max(height, charHeight * scale);
        }
        
        return height;
    }
}
