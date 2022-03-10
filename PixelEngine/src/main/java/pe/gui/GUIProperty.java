package pe.gui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class GUIProperty extends GUIElement
{
    protected Supplier<String> name;
    
    public GUIProperty(@NotNull Supplier<@NotNull String> name)
    {
        this.name = name;
    }
    
    public GUIProperty(@NotNull String name)
    {
        this.name = () -> name;
    }
    
    @NotNull
    public String name()
    {
        return this.name.get();
    }
    
    @NotNull
    public GUIProperty name(@NotNull Supplier<@NotNull String> name)
    {
        this.name = name;
        return this;
    }
    
    @NotNull
    public GUIProperty name(@NotNull String name)
    {
        this.name = () -> name;
        return this;
    }
}
