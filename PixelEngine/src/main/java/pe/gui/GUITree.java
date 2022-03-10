package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nuklear.NkContext;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUITree extends GUIContainer
{
    protected Type   type;
    protected String title;
    
    protected final int[] state = new int[] {NK_MAXIMIZED};
    
    public GUITree(@NotNull Type type, @Nullable String title)
    {
        this.type  = type;
        this.title = title == null ? "" : title;
    }
    
    // ---------- Properties ---------- //
    
    @NotNull
    public Type type()
    {
        return this.type;
    }
    
    @NotNull
    public GUITree type(@NotNull Type type)
    {
        this.type = type;
        return this;
    }
    
    @NotNull
    public String title()
    {
        return this.title;
    }
    
    public void title(@Nullable String title)
    {
        this.title = title == null ? "" : title;
    }
    
    public boolean collapsed()
    {
        return this.state[0] == NK_MINIMIZED;
    }
    
    @NotNull
    public GUITree collapse()
    {
        this.state[0] = NK_MINIMIZED;
        return this;
    }
    
    @NotNull
    public GUITree expand()
    {
        this.state[0] = NK_MAXIMIZED;
        return this;
    }
    
    @NotNull
    public GUITree toggleCollapsed()
    {
        this.state[0] = collapsed() ? NK_MAXIMIZED : NK_MINIMIZED;
        return this;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        // TODO - nk_tree_state_image_push   Start a collapsable UI section with image and label header and external state management
        
        if (nk_tree_state_push(ctx, this.type.value, this.title, this.state))
        {
            for (GUILayout child : this.children) child.layout(ctx);
            nk_tree_pop(ctx);
        }
    }
    
    public enum Type
    {
        /**
         * Highlighted tree header to mark a collapsible UI section
         */
        NODE(NK_TREE_NODE),
        
        /**
         * Non-highlighted tree header closer to tree representations
         */
        TAB(NK_TREE_TAB),
        ;
        
        final int value;
        
        Type(int value)
        {
            this.value = value;
        }
    }
}
