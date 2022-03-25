package pe.font;

import rutils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static rutils.IOUtil.getPath;

@SuppressWarnings("unused")
public class FontFamily
{
    private static final Logger LOGGER = new Logger();
    
    private static final HashMap<String, FontFamily> CACHE = new HashMap<>();
    
    static
    {
        register("fonts", "Arial");
        register("fonts", "BetterPixels");
    }
    
    public static final FontFamily DEFAULT = FontFamily.get("BetterPixels");
    
    private final String name;
    
    private final Font defaultFont;
    
    private final HashMap<String, Font> cache;
    
    private FontFamily(String path, String name)
    {
        Path filePath = getPath(path);
        
        this.name = name;
        
        try
        {
            Files.list(filePath).forEach(p -> {
                String fileName = p.getFileName().toString();
                if (fileName.startsWith(name) && fileName.endsWith(".ttf"))
                {
                    fileName = fileName.replace(".ttf", "");
                    
                    String[] fileDetails = fileName.split("-");
                    
                    String  fontName       = fileDetails[0];
                    Weight  fontWeight     = fileDetails.length > 1 ? Weight.get(fileDetails[1]) : Weight.REGULAR;
                    boolean fontItalicized = fileDetails.length > 2;
                    
                    if (!Font.exists(fontName, fontWeight, fontItalicized)) Font.register(p.toString(), fontName, fontWeight, fontItalicized, true);
                }
            });
        }
        catch (IOException e)
        {
            FontFamily.LOGGER.warning("FontFamily could not be created.", e);
        }
        
        this.defaultFont = Font.get(this.name, Font.DEFAULT_WEIGHT, Font.DEFAULT_ITALICS);
        
        this.cache = new HashMap<>();
        
        String fontID;
        for (Weight weight : Weight.values())
        {
            for (boolean italicized : new boolean[] {false, true})
            {
                fontID = Font.getID(this.name, weight, italicized);
                if (Font.exists(this.name, weight, italicized)) this.cache.put(fontID, Font.get(this.name, weight, italicized));
            }
        }
    }
    
    /**
     * @return The name of the family.
     */
    public String name()
    {
        return this.name;
    }
    
    /**
     * Gets a font in this family with weight and italicized if one is available.
     *
     * @param weight     The requested weight
     * @param italicized If it is italicized.
     * @return The font if it exists or the family default.
     */
    public Font getFont(Weight weight, boolean italicized)
    {
        return this.cache.getOrDefault(Font.getID(this.name, weight, italicized), this.defaultFont);
    }
    
    /**
     * Binds the font textures to be used by a renderer.
     *
     * @param size The size of the font.
     */
    public void bindTextures(int size)
    {
        // TODO
        // Weight[] values = Weight.values();
        // for (int i = 0, n = values.length; i < n; i++)
        // {
        //     Weight weight = values[i];
        //
        //     GL.activeTexture(i);
        //     getFont(weight, false).texture(size).bind();
        //     GL.activeTexture(i + n);
        //     getFont(weight, true).texture(size).bind();
        // }
    }
    
    /**
     * Unbinds the font textures from renderers.
     *
     * @param size The size of the font.
     */
    public void unbindTextures(int size)
    {
        // TODO
        // for (Weight weight : Weight.values())
        // {
        //     getFont(weight, false).texture(size).unbind();
        //     getFont(weight, true).texture(size).unbind();
        // }
    }
    
    /**
     * Registers a font family to be used.
     *
     * @param dirPath    The path to the directory where the .ttf files are located.
     * @param familyName The registry name for the family.
     */
    public static void register(String dirPath, String familyName)
    {
        if (FontFamily.CACHE.containsKey(familyName))
        {
            FontFamily.LOGGER.warning("FontFamily already registered: " + familyName);
            return;
        }
        
        FontFamily.LOGGER.fine("Registering FontFamily \"%s\" in directory \"%s.", familyName, dirPath);
        
        FontFamily.CACHE.put(familyName, new FontFamily(dirPath, familyName));
    }
    
    /**
     * Gets the font family for the given name.
     *
     * @param familyName The family name.
     * @return The font family or the default family.
     */
    public static FontFamily get(String familyName)
    {
        if (FontFamily.CACHE.containsKey(familyName)) return FontFamily.CACHE.get(familyName);
        
        FontFamily.LOGGER.warning("FontFamily is not registered: " + familyName);
        
        return FontFamily.DEFAULT;
    }
}
