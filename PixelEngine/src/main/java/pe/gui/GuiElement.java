package pe.gui;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class GuiElement
{
    protected final String id;
    
    public GuiElement()
    {
        this.id = UUID.randomUUID().toString();
    }
    
    @Override
    public String toString()
    {
        return getClass().getName() + '{' + "id='" + this.id + '\'' + '}';
    }
    
    @NotNull
    public String id()
    {
        return this.id;
    }
    
    public abstract void layout();
    
    public abstract void draw();
}
