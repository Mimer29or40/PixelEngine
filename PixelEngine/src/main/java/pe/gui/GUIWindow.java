package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import rutils.Math;

import java.nio.IntBuffer;
import java.util.EnumSet;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUIWindow extends GUIContainer
{
    private String title;
    private int    flags;
    
    public final Vector2i pos, size;
    private final Vector2i _pos, _size;
    
    // TODO - Need to have public Vector2ic
    public final Vector2i contentPos, contentSize;
    
    public final  Vector2i scroll;
    private final Vector2i _scroll;
    
    private boolean focused;
    private boolean hovered;
    private boolean collapsed;
    private boolean closed;
    private boolean hidden;
    private boolean active;
    
    private boolean shouldClose     = false;
    private boolean shouldFocus     = false;
    private int     collapseChanged = -1;
    private int     hiddenChanged   = -1;
    
    public GUIWindow(@Nullable String title, int x, int y, int width, int height)
    {
        this.title = title == null ? "" : title;
        
        this.pos   = new Vector2i(x, y);
        this._pos  = new Vector2i(x, y);
        this.size  = new Vector2i(width, height);
        this._size = new Vector2i(width, height);
        
        this.contentPos  = new Vector2i();
        this.contentSize = new Vector2i();
        
        this.scroll  = new Vector2i();
        this._scroll = new Vector2i(width, height);
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
    
    public boolean focused()
    {
        return this.focused;
    }
    
    @NotNull
    public GUIWindow focus()
    {
        this.shouldFocus = true;
        return this;
    }
    
    public boolean hovered()
    {
        return this.hovered;
    }
    
    public boolean collapsed()
    {
        return this.collapsed;
    }
    
    @NotNull
    public GUIWindow collapse()
    {
        this.collapseChanged = NK_MINIMIZED;
        return this;
    }
    
    @NotNull
    public GUIWindow expand()
    {
        this.collapseChanged = NK_MAXIMIZED;
        return this;
    }
    
    @NotNull
    public GUIWindow toggleCollapsed()
    {
        this.collapseChanged = collapsed() ? NK_MAXIMIZED : NK_MINIMIZED;
        return this;
    }
    
    public boolean closed()
    {
        return this.closed;
    }
    
    @NotNull
    public GUIWindow close()
    {
        this.shouldClose = true;
        return this;
    }
    
    public boolean hidden()
    {
        return this.hidden;
    }
    
    @NotNull
    public GUIWindow hide()
    {
        this.hiddenChanged = NK_HIDDEN;
        return this;
    }
    
    @NotNull
    public GUIWindow shown()
    {
        this.hiddenChanged = NK_SHOWN;
        return this;
    }
    
    @NotNull
    public GUIWindow toggleHidden()
    {
        this.hiddenChanged = hidden() ? NK_SHOWN : NK_HIDDEN;
        return this;
    }
    
    public boolean active()
    {
        return this.active;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkRect rect = NkRect.malloc(stack);
            rect.set((float) this.pos.x, (float) this.pos.y, (float) this.size.x, (float) this.size.y);
            if (this.pos.x != this._pos.x || this.pos.y != this._pos.y ||
                this.size.x != this._size.x || this.size.y != this._size.y)
            {
                this._pos.set(this.pos);
                this._size.set(this.size);
                nk_window_set_bounds(ctx, this.uuid, rect);
            }
            
            IntBuffer int1 = stack.mallocInt(1);
            IntBuffer int2 = stack.mallocInt(1);
            
            if (this.shouldFocus) nk_window_set_focus(ctx, this.uuid);
            this.shouldFocus = false;
            
            this.collapsed = nk_window_is_collapsed(ctx, this.uuid);
            this.closed    = nk_window_is_closed(ctx, this.uuid);
            this.hidden    = nk_window_is_hidden(ctx, this.uuid);
            this.active    = nk_window_is_active(ctx, this.uuid);
            
            if (this.collapseChanged > 0) nk_window_collapse(ctx, this.uuid, this.collapseChanged);
            this.collapseChanged = -1;
            
            if (this.hiddenChanged > 0) nk_window_show(ctx, this.uuid, this.hiddenChanged);
            this.hiddenChanged = -1;
            
            if (this.shouldClose) nk_window_close(ctx, this.uuid);
            this.shouldClose = false;
            
            if (nk_begin_titled(ctx, this.uuid, this.title, rect, this.flags))
            {
                if (this.scroll.x != this._scroll.x || this.scroll.y != this._scroll.y)
                {
                    this._scroll.set(this.scroll);
                    nk_window_set_scroll(ctx, Math.fastFloor(this.scroll.x), Math.fastFloor(this.scroll.y));
                }
                
                nk_window_get_bounds(ctx, rect);
                this._pos.set(this.pos.set((int) rect.x(), (int) rect.y()));
                this._size.set(this.size.set((int) rect.w(), (int) rect.h()));
                
                nk_window_get_content_region(ctx, rect);
                this.contentPos.set((int) rect.x(), (int) rect.y());
                this.contentSize.set((int) rect.w(), (int) rect.h());
                
                nk_window_get_scroll(ctx, int1, int2);
                this._scroll.set(this.scroll.set(int1.get(), int2.get()));
                
                this.focused = nk_window_has_focus(ctx);
                this.hovered = nk_window_is_hovered(ctx);
                
                // TODO - nk_window_get_canvas
                
                for (GUIElement child : this.children) child.layout(ctx);
                
                // if (nk_tree_state_push(ctx, NK_TREE_TAB, "Mouse", tree_state))
                // {
                //     nk_layout_row_dynamic(ctx, 0, 1);
                //     nk_label(ctx, String.format("Mouse Pos: [%.3f, %.3f]", Mouse.x(), Mouse.y()), NK_TEXT_LEFT);
                //     nk_label(ctx, String.format("Mouse Abs Pos: [%.3f, %.3f]", Mouse.absX(), Mouse.absY()), NK_TEXT_LEFT);
                //
                //     if (nk_tree_state_push(ctx, NK_TREE_NODE, "InnerMouse", tree_state1))
                //     {
                //         nk_layout_row_dynamic(ctx, 0, 1);
                //         nk_label(ctx, String.format("Mouse Pos: [%.3f, %.3f]", Mouse.x(), Mouse.y()), NK_TEXT_LEFT);
                //         nk_label(ctx, String.format("Mouse Abs Pos: [%.3f, %.3f]", Mouse.absX(), Mouse.absY()), NK_TEXT_LEFT);
                //         if (nk_tree_state_push(ctx, NK_TREE_NODE, "InnerMouse2", tree_state2))
                //         {
                //             nk_layout_row_dynamic(ctx, 0, 1);
                //             nk_label(ctx, String.format("Mouse Pos: [%.3f, %.3f]", Mouse.x(), Mouse.y()), NK_TEXT_LEFT);
                //             nk_label(ctx, String.format("Mouse Abs Pos: [%.3f, %.3f]", Mouse.absX(), Mouse.absY()), NK_TEXT_LEFT);
                //             nk_tree_pop(ctx);
                //         }
                //         nk_tree_pop(ctx);
                //     }
                //     nk_tree_pop(ctx);
                // }
            }
            nk_end(ctx);
        }
    }
    
}
