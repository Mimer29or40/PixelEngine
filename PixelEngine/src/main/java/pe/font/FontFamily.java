package pe.font;

import org.jetbrains.annotations.NotNull;
import rutils.Logger;

import java.util.Objects;

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
    
    FontFamily(@NotNull String name)
    {
        this.name = name;
        
        this.defaultFont = Font.get(this.name, null, null);
    }
    
    @Override
    public String toString()
    {
        return "FontFamily{" + this.name + '}';
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FontFamily font = (FontFamily) o;
        return this.name.equals(font.name);
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.name);
    }
    
    public void delete()
    {
        if (Font.DEFAULT_FAMILY_INST != this)
        {
            FontFamily.LOGGER.fine("Deleting:", this);
            
            Font.FAMILY_CACHE.remove(this.name);
        }
    }
    
    /**
     * @return The name of the family.
     */
    public String name()
    {
        return this.name;
    }
    
    @Override
    public @NotNull FontSingle withProperties(@NotNull Weight weight, boolean italicized)
    {
        if (Font.isRegistered(this.name, weight, italicized))
        {
            return Font.get(this.name, weight, italicized);
        }
        return this.defaultFont;
    }
    
    @Override
    public @NotNull FontSingle withProperties(@NotNull Weight weight)
    {
        if (Font.isRegistered(this.name, weight, null))
        {
            return Font.get(this.name, weight, null);
        }
        return this.defaultFont;
    }
    
    @Override
    public @NotNull FontSingle withProperties(boolean italicized)
    {
        if (Font.isRegistered(this.name, null, italicized))
        {
            return Font.get(this.name, null, italicized);
        }
        return this.defaultFont;
    }
}
