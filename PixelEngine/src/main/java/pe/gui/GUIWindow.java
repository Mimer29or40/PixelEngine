package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2ic;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.NkColorf;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import pe.Window;
import rutils.Math;

import java.nio.IntBuffer;
import java.util.EnumSet;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUIWindow extends GUIContainer
{
    private String title;
    private int    flags;
    
    private final Vector2d pos, size;
    
    private final Vector2d contentPos, contentSize;
    
    private final Vector2d scroll;
    
    private boolean focused;
    private boolean hovered;
    private boolean collapsed;
    private boolean closed;
    private boolean hidden;
    private boolean active;
    
    private boolean boundsChanged   = false;
    private boolean scrollChanged   = false;
    private boolean shouldClose     = false;
    private boolean shouldFocus     = false;
    private int     collapseChanged = -1;
    private int     hiddenChanged   = -1;
    
    public GUIWindow(@Nullable String title, float x, float y, float width, float height)
    {
        this.title = title == null ? "" : title;
        
        this.pos  = new Vector2d(x, y);
        this.size = new Vector2d(width, height);
        
        this.contentPos  = new Vector2d();
        this.contentSize = new Vector2d();
        
        this.scroll = new Vector2d();
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
    
    @NotNull
    public Vector2dc pos()
    {
        return this.pos;
    }
    
    @NotNull
    public GUIWindow pos(double x, double y)
    {
        if (Double.compare(this.pos.x, x) + Double.compare(this.pos.y, y) != 0) this.boundsChanged = true;
        this.pos.set(x, y);
        return this;
    }
    
    @NotNull
    public GUIWindow pos(@NotNull Vector2ic pos)
    {
        return pos(pos.x(), pos.y());
    }
    
    @NotNull
    public GUIWindow pos(@NotNull Vector2dc pos)
    {
        return pos(pos.x(), pos.y());
    }
    
    public double x()
    {
        return this.pos.x;
    }
    
    @NotNull
    public GUIWindow x(double x)
    {
        return pos(x, y());
    }
    
    public double y()
    {
        return this.pos.y;
    }
    
    @NotNull
    public GUIWindow y(double y)
    {
        return pos(x(), y);
    }
    
    @NotNull
    public Vector2dc size()
    {
        return this.size;
    }
    
    @NotNull
    public GUIWindow size(double width, double height)
    {
        if (Double.compare(this.size.x, width) + Double.compare(this.size.y, height) != 0) this.boundsChanged = true;
        this.size.set(width, height);
        return this;
    }
    
    @NotNull
    public GUIWindow size(@NotNull Vector2ic size)
    {
        return size(size.x(), size.y());
    }
    
    @NotNull
    public GUIWindow size(@NotNull Vector2dc size)
    {
        return size(size.x(), size.y());
    }
    
    public double width()
    {
        return this.size.x;
    }
    
    @NotNull
    public GUIWindow width(double width)
    {
        return size(width, height());
    }
    
    public double height()
    {
        return this.size.y;
    }
    
    @NotNull
    public GUIWindow height(double height)
    {
        return size(width(), height);
    }
    
    @NotNull
    public Vector2dc contentPos()
    {
        return this.contentPos;
    }
    
    public double contentX()
    {
        return this.contentPos.x;
    }
    
    public double contentY()
    {
        return this.contentPos.y;
    }
    
    @NotNull
    public Vector2dc contentSize()
    {
        return this.contentSize;
    }
    
    public double contentW()
    {
        return this.contentSize.x;
    }
    
    public double contentH()
    {
        return this.contentSize.y;
    }
    
    @NotNull
    public Vector2dc scroll()
    {
        return this.scroll;
    }
    
    @NotNull
    public GUIWindow scroll(double x, double y)
    {
        if (Double.compare(this.scroll.x, x) + Double.compare(this.scroll.y, y) != 0) this.scrollChanged = true;
        this.scroll.set(x, y);
        return this;
    }
    
    @NotNull
    public GUIWindow scroll(@NotNull Vector2ic scroll)
    {
        return scroll(scroll.x(), scroll.y());
    }
    
    @NotNull
    public GUIWindow scroll(@NotNull Vector2dc scroll)
    {
        return scroll(scroll.x(), scroll.y());
    }
    
    public double scrollX()
    {
        return this.scroll.x;
    }
    
    @NotNull
    public GUIWindow scrollX(double x)
    {
        this.scroll.x = x;
        return this;
    }
    
    public double scrollY()
    {
        return this.scroll.y;
    }
    
    @NotNull
    public GUIWindow scrollY(double y)
    {
        this.scroll.y = y;
        return this;
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
    
    static final int EASY = 0;
    static final int HARD = 1;
    
    final NkColorf background = NkColorf.create().r(0.10f).g(0.18f).b(0.24f).a(1.0f);
    
    int op = EASY;
    
    final IntBuffer compression = BufferUtils.createIntBuffer(1).put(0, 20);
    
    int[] tree_state  = new int[1];
    int[] tree_state1 = new int[1];
    int[] tree_state2 = new int[1];
    
    int[] bound_state         = new int[1];
    int[] content_bound_state = new int[1];
    int[] scroll_state        = new int[1];
    int[] state_state         = new int[1];
    int[] flags_state         = new int[1];
    
    double[] x  = new double[] {30};
    double[] y  = new double[] {30};
    double[] w  = new double[] {230};
    double[] h  = new double[] {250};
    double[] sx = new double[] {0};
    double[] sy = new double[] {0};
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkRect rect = NkRect.malloc(stack);
            rect.set((float) this.pos.x, (float) this.pos.y,
                     (float) this.size.x, (float) this.size.y);
            if (this.boundsChanged) nk_window_set_bounds(ctx, this.uuid, rect);
            this.boundsChanged = false;
            
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
                if (this.scrollChanged) nk_window_set_scroll(ctx, Math.fastFloor(this.scroll.x), Math.fastFloor(this.scroll.y));
                this.scrollChanged = false;
                
                nk_window_get_bounds(ctx, rect);
                this.pos.set(rect.x(), rect.y());
                this.size.set(rect.w(), rect.h());
                
                nk_window_get_content_region(ctx, rect);
                this.contentPos.set(rect.x(), rect.y());
                this.contentSize.set(rect.w(), rect.h());
                
                nk_window_get_scroll(ctx, int1, int2);
                this.scroll.set(int1.get(), int2.get());
                
                this.focused = nk_window_has_focus(ctx);
                this.hovered = nk_window_is_hovered(ctx);
                
                // TODO - nk_window_get_canvas
                
                // TODO - nk_window_is_any_hovered
                // TODO - nk_item_is_any_active
                
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
                //
                //
                // nk_layout_row_dynamic(ctx, 200, 1);
                // int flags = NK_WINDOW_BORDER | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE | NK_WINDOW_NO_INPUT;
                // if (nk_group_begin(ctx, "MOUSE", flags))
                // {
                //     nk_layout_row_dynamic(ctx, 0, 1);
                //     nk_label(ctx, String.format("Mouse Pos: [%.3f, %.3f]", Mouse.x(), Mouse.y()), NK_TEXT_LEFT);
                //
                //     nk_layout_row_dynamic(ctx, 0, 1);
                //     nk_label(ctx, String.format("Mouse Abs Pos: [%.3f, %.3f]", Mouse.absX(), Mouse.absY()), NK_TEXT_LEFT);
                //
                //     nk_layout_row_static(ctx, 0, 80, 1);
                //     if (nk_button_label(ctx, "button")) System.out.println("button pressed");
                //
                // }
                // nk_group_end(ctx);
                //
                // nk_layout_row_static(ctx, 0, 80, 1);
                // if (nk_button_label(ctx, "button")) System.out.println("button pressed");
                //
                // nk_layout_row_static(ctx, 30, 80, 1);
                // if (nk_button_label(ctx, "button")) System.out.println("button pressed");
                //
                // nk_layout_row_dynamic(ctx, 30, 2);
                // if (nk_option_label(ctx, "easy", op == EASY)) op = EASY;
                // if (nk_option_label(ctx, "hard", op == HARD)) op = HARD;
                //
                // nk_layout_row_dynamic(ctx, 25, 1);
                // nk_property_int(ctx, "Compression:", 0, compression, 100, 10, 1);
                //
                // nk_layout_row_dynamic(ctx, 20, 1);
                // nk_label(ctx, "background:", NK_TEXT_LEFT);
                // nk_layout_row_dynamic(ctx, 25, 1);
                //
                // if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.malloc(stack)), NkVec2.malloc(stack).set(nk_widget_width(ctx), 400)))
                // {
                //     nk_layout_row_dynamic(ctx, 120, 1);
                //     nk_color_picker(ctx, background, NK_RGBA);
                //     nk_layout_row_dynamic(ctx, 25, 1);
                //     background.r(nk_propertyf(ctx, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
                //               .g(nk_propertyf(ctx, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
                //               .b(nk_propertyf(ctx, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
                //               .a(nk_propertyf(ctx, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
                //     nk_combo_end(ctx);
                // }
            }
            nk_end(ctx);
        }
    }
    
}
