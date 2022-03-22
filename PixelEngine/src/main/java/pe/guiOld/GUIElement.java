package pe.guiOld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nuklear.NkContext;

import java.util.UUID;

public abstract class GUIElement
{
    protected final String uuid;
    
    protected GUIElement parent;
    
    public GUIElement()
    {
        this.uuid = UUID.randomUUID().toString();
    }
    
    @Override
    public String toString()
    {
        return getClass().getName() + '{' + "uuid='" + this.uuid + '\'' + '}';
    }
    
    @NotNull
    public String uuid()
    {
        return this.uuid;
    }
    
    @Nullable
    public GUIElement parent()
    {
        return this.parent;
    }
    
    public abstract void layout(@NotNull NkContext ctx);
}
