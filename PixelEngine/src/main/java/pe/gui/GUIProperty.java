package pe.gui;

import org.jetbrains.annotations.NotNull;
import pe.util.Property;

import java.util.function.Supplier;

public abstract class GUIProperty extends GUIElement
{
    public final Property<String> name;
    
    public GUIProperty(@NotNull Supplier<@NotNull String> name)
    {
        this.name = new Property<>("name", name);
    }
}
