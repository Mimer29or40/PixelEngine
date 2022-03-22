package pe.guiOld;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.EnumSet;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUIGroup extends GUIContainer
{
    protected String title;
    protected int    flags;
    
    protected final Vector2i scroll = new Vector2i();
    
    private boolean scrollChanged = false;
    
    public GUIGroup(@Nullable String title)
    {
        this.title = title == null ? "" : title;
    }
    
    // ---------- Properties ---------- //
    
    @NotNull
    public String title()
    {
        return this.title;
    }
    
    public void title(@Nullable String title)
    {
        this.title = title == null ? "" : title;
    }
    
    public boolean isEnabled(@NotNull WindowFlag flag)
    {
        return (this.flags & flag.value) > 0;
    }
    
    public void enable(@NotNull WindowFlag flag)
    {
        this.flags |= flag.value;
    }
    
    public void enable(@NotNull WindowFlag @NotNull ... flags)
    {
        for (WindowFlag flag : flags) this.flags |= flag.value;
    }
    
    public void enable(@NotNull EnumSet<WindowFlag> flags)
    {
        for (WindowFlag flag : flags) this.flags |= flag.value;
    }
    
    public void disable(@NotNull WindowFlag flag)
    {
        this.flags ^= flag.value;
    }
    
    public void disable(@NotNull WindowFlag @NotNull ... flags)
    {
        for (WindowFlag flag : flags) this.flags ^= flag.value;
    }
    
    public void disable(@NotNull EnumSet<WindowFlag> flags)
    {
        for (WindowFlag flag : flags) this.flags ^= flag.value;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        // nk_group_scrolled_offset_begin  Start a new group with manual separated handling of scrollbar x- and y-offset
        // nk_group_scrolled_begin         Start a new group with manual scrollbar handling
        // nk_group_scrolled_end           Ends a group with manual scrollbar handling. Should only be called if nk_group_begin returned non-zero
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer xOffset = stack.mallocInt(1);
            IntBuffer yOffset = stack.mallocInt(1);
            
            if (this.scrollChanged) nk_group_set_scroll(ctx, this.uuid, this.scroll.x, this.scroll.y);
            this.scrollChanged = false;
            
            if (nk_group_begin_titled(ctx, this.uuid, this.title, this.flags))
            {
                nk_group_get_scroll(ctx, this.uuid, xOffset, yOffset);
                this.scroll.set(xOffset.get(), yOffset.get());
                
                for (GUILayout child : this.children) child.layout(ctx);
                
                nk_group_end(ctx);
            }
        }
    }
}
