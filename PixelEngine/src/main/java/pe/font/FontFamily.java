package pe.font;

import org.jetbrains.annotations.NotNull;
import rutils.Logger;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("unused")
public class FontFamily extends Font
{
    private static final Logger LOGGER = new Logger();
    
    /**
     * Builds a family tag with the provided properties.
     *
     * @param name The font family
     * @return The tag string.
     */
    public static @NotNull String getID(@NotNull String name)
    {
        return name.replace(" ", "");
    }
    
    // -------------------- Instance -------------------- //
    
    private final String name;
    
    private final FontSingle defaultFont;
    
    private final HashMap<String, FontSingle> cache;
    
    FontFamily(String name, List<FontSingle> fonts)
    {
        this.name = name;
        
        this.cache = new HashMap<>();
        for (FontSingle font : fonts)
        {
            this.cache.put(font.id, font);
        }
        
        this.defaultFont = Font.get(this.name, null, null);
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
    public FontSingle getFont(Weight weight, boolean italicized)
    {
        return this.cache.getOrDefault(FontSingle.getID(this.name, weight, italicized), this.defaultFont);
    }
    
    @Override
    public void delete()
    {
    
    }
    
    @Override
    public double getTextWidth(@NotNull String text, int size)
    {
        return 0;
    }
    
    @Override
    public double getTextHeight(@NotNull String text, int size)
    {
        return 0;
    }
    
    @Override
    public void drawText(@NotNull String text, int size, double x, double y, int r, int g, int b, int a)
    {
    
    }
}
