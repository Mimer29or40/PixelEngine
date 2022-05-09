package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import pe.Draw;
import pe.GUI;
import pe.color.Color;
import pe.color.Colorc;
import pe.shape.AABB2i;
import pe.shape.AABB2ic;
import rutils.Math;

public class GuiWindow extends GuiElement
{
    public String title;
    
    public int flags = Flag.NONE.ordinal();
    
    public final    Vector2i pos       = new Vector2i(); // Publicly accessible for setting
    protected final Vector2i posActual = new Vector2i(); // Position (always rounded-up to the nearest pixel)
    
    public final    Vector2i size       = new Vector2i(); // Publicly accessible for setting
    protected final Vector2i sizeActual = new Vector2i(); // Current size (==SizeFull or collapsed title bar size)
    protected final Vector2i sizeFull   = new Vector2i(); // Size when non collapsed
    
    protected int autoFitFramesX = -1, autoFitFramesY = -1;
    protected int     autoFitChildAxes;
    protected boolean autoFitOnlyGrows;
    
    public final    Vector2i scroll                   = new Vector2i(); // Publicly accessible for setting
    protected final Vector2i scrollActual             = new Vector2i();
    protected final Vector2i scrollMax                = new Vector2i();
    protected final Vector2i scrollTarget             = new Vector2i(); // target scroll position. stored as cursor position with scrolling canceled out, so the highest point is always 0.0f. (FLT_MAX for no change)
    protected final Vector2i scrollTargetCenterRatio  = new Vector2i(); // 0.0f = scroll so that target position is at top, 0.5f = scroll so that target position is centered
    protected final Vector2i scrollTargetEdgeSnapDist = new Vector2i(); // 0.0f = no snapping, >0.0f snapping threshold
    protected final Vector2i scrollbarSizes           = new Vector2i(); // Size taken by each scrollbars on their smaller axis. Pay attention! ScrollbarSizes.x == width of the vertical scrollbar, ScrollbarSizes.y = height of the horizontal scrollbar.
    protected       boolean  scrollbarX, scrollbarY;                    // Are scrollbars visible?
    
    public final    Vector2i contentSize         = new Vector2i(); // Publicly accessible for setting
    protected final Vector2i contentSizeActual   = new Vector2i(); // Size of contents/scrollable client area (calculated from the extents reach of the cursor) from previous frame. Does not include window decoration or window padding.
    protected final Vector2i contentSizeIdeal    = new Vector2i();
    protected final Vector2i contentSizeExplicit = new Vector2i(); // Size of contents/scrollable client area explicitly request by the user via SetNextWindowContentSize().
    
    public    boolean collapsed;          // Publicly accessible for setting
    protected boolean collapsedActual;    // Set when collapsing window to become only title-bar
    protected boolean WantCollapseToggle;
    
    protected boolean shouldFocus = false;
    
    public final Vector2i WindowPadding = new Vector2i(); // Window padding at the time of Begin().
    public       double   WindowRounding;                 // Window rounding at the time of Begin(). May be clamped lower to avoid rendering artifacts with title bar, menu bar etc.
    public       double   WindowBorderSize;               // Window border size at the time of Begin().
    
    // int              NameBufLen; // Size of buffer storing Name. May be larger than strlen(Name)!
    // ImGuiID          MoveId;     // == window->GetID("#MOVE")
    // ImGuiID          ChildId;    // ID of corresponding item in parent window (for navigation to return from child window to parent window)
    
    public boolean Active;    // Set to true on Begin(), unless Collapsed
    public boolean WasActive;
    
    public boolean WriteAccessed; // Set to true when any widget access the current window
    
    public boolean SkipItems;        // Set when items can safely be all clipped (e.g. window not visible or collapsed)
    public boolean Appearing;        // Set during the frame where the window is appearing (or re-appearing)
    public boolean Hidden;           // Do not display (== HiddenFrames*** > 0)
    public boolean IsFallbackWindow; // Set on the "Debug##Default" window.
    public boolean IsExplicitChild;  // Set when passed _ChildWindow, left to false by BeginDocked()
    public boolean HasCloseButton;   // Set when the window has a close button (p_open != NULL)
    public int     ResizeBorderHeld; // Current border being held for resize (-1: none, otherwise 0-3)
    // short       BeginCount;                         // Number of Begin() during the current frame (generally 0 or 1, 1+ if appending via multiple Begin/End pairs)
    // short       BeginOrderWithinParent;             // Begin() order within immediate parent window, if we are a child window. Otherwise 0.
    // short       BeginOrderWithinContext;            // Begin() order within entire imgui context. This is mostly used for debugging submission order related issues.
    // short       FocusOrder;                         // Order within WindowsFocusOrder[], altered when windows are focused.
    // ImGuiID     PopupId;                            // ID in the popup stack when this window is used as a popup/menu (because we use generic Name/ID for recycling)
    // ImGuiDir    AutoPosLastDirection;
    // ImS8        HiddenFramesCanSkipItems;           // Hide the window for N frames
    // ImS8        HiddenFramesCannotSkipItems;        // Hide the window for N frames while allowing items to be submitted so we can measure their size
    // ImS8        HiddenFramesForRenderOnly;          // Hide the window until frame N at Render() time only
    // ImS8        DisableInputsFrames;                // Disable window interactions for N frames
    // ImGuiCond   SetWindowPosAllowFlags :8;         // store acceptable condition flags for SetNextWindowPos() use.
    // ImGuiCond   SetWindowSizeAllowFlags :8;        // store acceptable condition flags for SetNextWindowSize() use.
    // ImGuiCond   SetWindowCollapsedAllowFlags :8;   // store acceptable condition flags for SetNextWindowCollapsed() use.
    // ImVec2      SetWindowPosVal;                    // store window position when using a non-zero Pivot (position set needs to be processed when we know the window size)
    // ImVec2      SetWindowPosPivot;                  // store window pivot for positioning. ImVec2(0, 0) when positioning from top-left corner; ImVec2(0.5f, 0.5f) for centering; ImVec2(1, 1) for bottom right.
    
    // ImVector<ImGuiID>   IDStack; // ID stack. ID are hashes seeded with the value at the top of the stack. (In theory this should be in the TempData structure)
    // ImGuiWindowTempData DC;      // Temporary per-window data, reset at the beginning of the frame. This used to be called ImGuiDrawContext, hence the "DC" variable name.
    
    // // The best way to understand what those rectangles are is to use the 'Metrics->Tools->Show Windows Rectangles' viewer.
    // // The main 'OuterRect', omitted as a field, is window->Rect().
    // ImRect   OuterRectClipped;  // == Window->Rect() just after setup in Begin(). == window->Rect() for root window.
    // ImRect   InnerRect;         // Inner rectangle (omit title bar, menu bar, scroll bar)
    // ImRect   InnerClipRect;     // == InnerRect shrunk by WindowPadding*0.5f on each side, clipped within viewport or parent clip rect.
    // ImRect   WorkRect;          // Initially covers the whole scrolling region. Reduced by containers e.g columns/tables when active. Shrunk by WindowPadding*1.0f on each side. This is meant to replace ContentRegionRect over time (from 1.71+ onward).
    // ImRect   ParentWorkRect;    // Backup of WorkRect before entering a container such as columns/tables. Used by e.g. SpanAllColumns functions to easily access. Stacked containers are responsible for maintaining this. // FIXME-WORKRECT: Could be a stack?
    // ImRect   ClipRect;          // Current clipping/scissoring rectangle, evolve as we are using PushClipRect(), etc. == DrawList->clip_rect_stack.back().
    // ImRect   ContentRegionRect; // FIXME: This is currently confusing/misleading. It is essentially WorkRect but not handling of scrolling. We currently rely on it as right/bottom aligned sizing operation need some size to rely on.
    // ImVec2ih HitTestHoleSize;   // Define an optional rectangular hole where mouse will pass-through the window.
    // ImVec2ih HitTestHoleOffset;
    
    public int   LastFrameActive;  // Last frame number the window was Active.
    public float LastTimeActive;   // Last timestamp the window was Active (using float as we don't need high precision there)
    public float ItemWidthDefault;
    // ImGuiStorage              StateStorage;
    // ImVector<ImGuiOldColumns> ColumnsStorage;
    public float FontWindowScale;  // User scale multiplier per-window, via SetWindowFontScale()
    public int   SettingsOffset;   // Offset into SettingsWindows[] (offsets are always valid as we only grow the array from the back)
    
    // ImDrawList*DrawList;                         // == &DrawListInst (for backward compatibility reason with code using imgui_internal.h we keep this a pointer)
    // ImDrawList DrawListInst;
    public GuiWindow ParentWindow;                   // If we are a child _or_ popup _or_ docked window, this is pointing to our parent. Otherwise NULL.
    public GuiWindow ParentWindowInBeginStack;
    public GuiWindow RootWindow;                     // Point to ourself or first ancestor that is not a child window. Doesn't cross through popups/dock nodes.
    public GuiWindow RootWindowPopupTree;            // Point to ourself or first ancestor that is not a child window. Cross through popups parent<>child.
    public GuiWindow RootWindowForTitleBarHighlight; // Point to ourself or first ancestor which will display TitleBgActive color when this window is active.
    public GuiWindow RootWindowForNav;               // Point to ourself or first ancestor which doesn't have the NavFlattened flag.
    
    // ImGuiWindow*NavLastChildNavWindow;       // When going to the menu bar, we remember the child window we came from. (This could probably be made implicit if we kept g.Windows sorted by last focused including child window.)
    // ImGuiID NavLastIds[ImGuiNavLayer_COUNT]; // Last known NavId for this window, per layer (0/1)
    // ImRect NavRectRel[ImGuiNavLayer_COUNT];  // Reference rectangle, in window relative space
    //
    // int  MemoryDrawListIdxCapacity; // Backup of last idx/vtx count, so when waking up the window we can preallocate and avoid iterative alloc/copy
    // int  MemoryDrawListVtxCapacity;
    // boolean MemoryCompacted;        // Set when window extraneous data have been garbage collected
    
    // -------------------- ImGuiWindowTempData --------------- //
    
    // // Layout
    // ImVec2                  CursorPos;              // Current emitting position, in absolute coordinates.
    // ImVec2                  CursorPosPrevLine;
    // ImVec2                  CursorStartPos;         // Initial position after Begin(), generally ~ window position + WindowPadding.
    // ImVec2                  CursorMaxPos;           // Used to implicitly calculate ContentSize at the beginning of next frame, for scrolling range and auto-resize. Always growing during the frame.
    // ImVec2                  IdealMaxPos;            // Used to implicitly calculate ContentSizeIdeal at the beginning of next frame, for auto-resize only. Always growing during the frame.
    // ImVec2                  CurrLineSize;
    // ImVec2                  PrevLineSize;
    // float                   CurrLineTextBaseOffset; // Baseline offset (0.0f by default on a new line, generally == style.FramePadding.y when a framed item has been added).
    // float                   PrevLineTextBaseOffset;
    // ImVec1                  Indent;                 // Indentation / start position from left of window (increased by TreePush/TreePop, etc.)
    // ImVec1                  ColumnsOffset;          // Offset to the current column (if ColumnsCurrent > 0). FIXME: This and the above should be a stack to allow use cases like Tree->Column->Tree. Need revamp columns API.
    // ImVec1                  GroupOffset;
    // ImVec2                  CursorStartPosLossyness;// Record the loss of precision of CursorStartPos due to really large scrolling amount. This is used by clipper to compensentate and fix the most common use case of large scroll area.
    
    // // Keyboard/Gamepad navigation
    // ImGuiNavLayer           NavLayerCurrent;        // Current layer, 0..31 (we currently only use 0..1)
    // short                   NavLayersActiveMask;    // Which layers have been written to (result from previous frame)
    // short                   NavLayersActiveMaskNext;// Which layers have been written to (accumulator for current frame)
    // ImGuiID                 NavFocusScopeIdCurrent; // Current focus scope ID while appending
    // boolean                 NavHideHighlightOneFrame;
    // boolean                 NavHasScroll;           // Set when scrolling can be used (ScrollMax > 0.0f)
    
    // // Miscellaneous
    // boolean                MenuBarAppending;          // FIXME: Remove this
    public final Vector2i MenuBarOffset = new Vector2i(); // MenuBarOffset.x is sort of equivalent of a per-layer CursorPos.x, saved/restored as we switch to the menu bar. The only situation when MenuBarOffset.y is > 0 if when (SafeAreaPadding.y > FramePadding.y), often used on TVs.
    // ImGuiMenuColumns       MenuColumns;               // Simplified columns storage for menu items measurement
    // int                    TreeDepth;                 // Current tree depth.
    // ImU32                  TreeJumpToParentOnPopMask; // Store a copy of !g.NavIdIsAlive for TreeDepth 0..31.. Could be turned into a ImU64 if necessary.
    // ImVector<ImGuiWindow*> ChildWindows;
    // ImGuiStorage*          StateStorage;              // Current persistent per-window storage (store e.g. tree node open/close state)
    // ImGuiOldColumns*       CurrentColumns;            // Current columns set
    // int                    CurrentTableIdx;           // Current table index (into g.Tables)
    // ImGuiLayoutType        LayoutType;
    // ImGuiLayoutType        ParentLayoutType;          // Layout type of parent window at the time of Begin()
    
    // // Local parameters stacks
    // // We store the current settings outside of the vectors to increase memory locality (reduce cache misses). The vectors are rarely modified. Also it allows us to not heap allocate for short-lived windows which are not using those settings.
    // float                   ItemWidth;              // Current item width (>0.0: width in pixels, <0.0: align xx pixels to the right of window).
    // float                   TextWrapPos;            // Current text wrap pos.
    // ImVector<float>         ItemWidthStack;         // Store item widths to restore (attention: .back() is not == ItemWidth)
    // ImVector<float>         TextWrapPosStack;       // Store text wrap pos to restore (attention: .back() is not == TextWrapPos)
    
    public void enableFlags(@NotNull Flag @NotNull ... flags)
    {
        for (Flag flag : flags) this.flags |= flag.value;
    }
    
    public void disableFlags(@NotNull Flag @NotNull ... flags)
    {
        for (Flag flag : flags) this.flags &= ~flag.value;
    }
    
    public void toggleFlags(@NotNull Flag @NotNull ... flags)
    {
        for (Flag flag : flags) this.flags ^= flag.value;
    }
    
    public boolean isEnabled(@NotNull Flag @NotNull ... flags)
    {
        int bits = 0;
        for (Flag flag : flags) bits |= flag.value;
        return (this.flags & bits) == bits;
    }
    
    private final AABB2i rect         = new AABB2i(this.posActual, this.sizeActual);
    private final AABB2i titleBarRect = new AABB2i(this.posActual, new Vector2i());
    private final AABB2i menuBarRect  = new AABB2i(new Vector2i(), new Vector2i());
    
    public @NotNull AABB2ic rect()
    {
        return this.rect;
    }
    
    public int calcFontSize()
    {
        double scale = GUI.fontBaseSize() * this.FontWindowScale;
        if (this.ParentWindow != null) scale *= this.ParentWindow.FontWindowScale;
        return (int) Math.round(scale);
    }
    
    public int titleBarHeight()
    {
        return isEnabled(Flag.NO_TITLE_BAR) ? 0 : calcFontSize() + GUI.style().FramePadding.y * 2;
    }
    
    public @NotNull AABB2ic titleBarRect()
    {
        this.titleBarRect.size.set(this.sizeFull.x, titleBarHeight());
        return this.titleBarRect;
    }
    
    public int menuBarHeight()
    {
        return isEnabled(Flag.MENU_BAR) ? this.MenuBarOffset.y + calcFontSize() + GUI.style().FramePadding.y * 2 : 0;
    }
    
    public @NotNull AABB2ic menuBarRect()
    {
        return this.menuBarRect.set(this.posActual.x, this.posActual.y + titleBarHeight(), this.sizeFull.x, menuBarHeight());
    }
    
    @Override
    public void layout()
    {
        GuiStyle style = GUI.style();
        
        boolean window_pos_set_by_api = false;
        if (this.posActual.x != this.pos.x || this.posActual.y != this.pos.y)
        {
            window_pos_set_by_api = true;
            this.posActual.set(this.pos);
        }
        
        boolean window_size_x_set_by_api = false;
        boolean window_size_y_set_by_api = false;
        if (this.sizeFull.x != this.size.x || this.sizeFull.y != this.size.y)
        {
            window_size_x_set_by_api = this.size.x > 0;
            window_size_y_set_by_api = this.size.y > 0;
            
            if (this.size.x > 0)
            {
                this.autoFitFramesX = 0;
                this.sizeFull.x     = this.size.x;
            }
            else
            {
                this.autoFitFramesX   = 2;
                this.autoFitOnlyGrows = false;
            }
            if (this.size.y > 0)
            {
                this.autoFitFramesY = 0;
                this.sizeFull.y     = this.size.y;
            }
            else
            {
                this.autoFitFramesY   = 2;
                this.autoFitOnlyGrows = false;
            }
        }
        
        if (this.scrollTarget.x != this.scroll.x || this.scrollTarget.y != this.scroll.y)
        {
            if (this.scroll.x > 0)
            {
                this.scrollTarget.x            = this.scroll.x;
                this.scrollTargetCenterRatio.x = 0;
            }
            if (this.scroll.y > 0)
            {
                this.scrollTarget.y            = this.scroll.y;
                this.scrollTargetCenterRatio.y = 0;
            }
        }
        
        if (this.contentSizeExplicit.x != this.contentSize.x || this.contentSizeExplicit.y != this.contentSize.y)
        {
            this.contentSizeExplicit.set(this.contentSize);
        }
    
        if (this.collapsedActual != this.collapsed)
        {
            this.collapsedActual = this.collapsed;
        }
        
        if (this.shouldFocus)
        {
            GUI.focusWindow(this);
            this.shouldFocus = false;
        }
    }
    
    @Override
    public void draw()
    {
        // AABB2ic rect = rect();
        // int     x0   = rect.minX();
        // int     y0   = rect.minY();
        // int     x1   = rect.maxX();
        // int     y1   = rect.maxY();
        //
        // Colorc color = this.focused ? Color.WHITE : this.hovered ? Color.DARK_GRAY : Color.GRAY;
        // Draw.fillRect2D().corners(x0, y0, x1, y1).color(color).draw();
        // // Engine.Draw.fillRect2D().point(window.pos).size(window.size).color(Color.WHITE).draw();
        
        GuiStyle style = GUI.style();
        
        final AABB2ic rect           = rect();
        final AABB2ic title_bar_rect = titleBarRect();
        
        // final boolean title_bar_is_highlight = want_focus || (window_to_highlight && window->RootWindowForTitleBarHighlight == window_to_highlight->RootWindowForTitleBarHighlight);
        final boolean title_bar_is_highlight = true;
        
        // Draw window + handle manual resize
        // As we highlight the title bar when want_focus is set, multiple reappearing windows will have have their title bar highlighted on their reappearing frame.
        final double window_rounding    = this.WindowRounding;
        final double window_border_size = this.WindowBorderSize;
        if (this.collapsedActual)
        {
            // Title bar only
            double backup_border_size = style.FrameBorderSize;
            style.FrameBorderSize = this.WindowBorderSize;
            // GuiStyle.StyleColor color = title_bar_is_highlight && !g.NavDisableHighlight ? GuiStyle.StyleColor.TitleBgActive : GuiStyle.StyleColor.TitleBgCollapsed;
            StyleColor color         = title_bar_is_highlight ? StyleColor.TitleBgActive : StyleColor.TitleBgCollapsed;
            Colorc     title_bar_col = style.Colors.get(color.ordinal());
            renderFrame(title_bar_rect.min(), title_bar_rect.max(), title_bar_col, true, window_rounding);
            style.FrameBorderSize = backup_border_size;
        }
        else
        {
            // Window background
            if (!isEnabled(Flag.NO_BACKGROUND))
            {
                Colorc bg_col = style.Colors.get(getWindowBgColorIdx().ordinal());
                // TODO
                // boolean override_alpha = false;
                // float   alpha          = 1.0f;
                // if (g.NextWindowData.Flags & ImGuiNextWindowDataFlags_HasBgAlpha)
                // {
                //     alpha          = g.NextWindowData.BgAlphaVal;
                //     override_alpha = true;
                // }
                // if (override_alpha) bg_col = (bg_col & ~IM_COL32_A_MASK) | (IM_F32_TO_INT8_SAT(alpha) << IM_COL32_A_SHIFT);
                // TODO - (flags & Flag.NoTitleBar) ? 0 : ImDrawFlags_RoundCornersBottom
                Draw.fillRect2D()
                    .corners(this.posActual.x, this.posActual.y + titleBarHeight(), rect.maxX(), rect.maxY())
                    .cornerRadius(window_rounding)
                    .color(bg_col)
                    .draw();
            }
            
            // Title bar
            if (!isEnabled(Flag.NO_TITLE_BAR))
            {
                Colorc title_bar_col = style.Colors.get((title_bar_is_highlight ? StyleColor.TitleBgActive : StyleColor.TitleBg).ordinal());
                // TODO - ImDrawFlags_RoundCornersTop
                Draw.fillRect2D()
                    .corners(title_bar_rect.min(), title_bar_rect.max())
                    .cornerRadius(window_rounding)
                    .color(title_bar_col)
                    .draw();
            }
            
            // TODO Menu bar
            if (isEnabled(Flag.MENU_BAR))
            {
                // AABB2ic menu_bar_rect = menuBarRect();
                // menu_bar_rect.ClipWith(window -> Rect());  // Soft clipping, in particular child window don't have minimum size covering the menu bar so this is useful for them.
                // window -> DrawList -> AddRectFilled(menu_bar_rect.Min + ImVec2(window_border_size, 0), menu_bar_rect.Max - ImVec2(window_border_size, 0), GetColorU32(StyleColor.MenuBarBg), (flags & Flag.NoTitleBar) ? window_rounding : 0.0f, ImDrawFlags_RoundCornersTop);
                // if (style.FrameBorderSize > 0.0f && menu_bar_rect.Max.y < window -> Pos.y + window -> Size.y)
                // {
                //     window -> DrawList -> AddLine(menu_bar_rect.GetBL(), menu_bar_rect.GetBR(), GetColorU32(StyleColor.Border), style.FrameBorderSize);
                // }
            }
            
            // TODO Scrollbars
            // if (this.ScrollbarX) Scrollbar(ImGuiAxis_X);
            // if (this.ScrollbarY) Scrollbar(ImGuiAxis_Y);
            
            // Render resize grips (after their input handling so we don't have a frame of latency)
            if (!isEnabled(Flag.NO_RESIZE))
            {
                // TODO - Config g.IO.ConfigWindowsResizeFromEdges
                boolean ConfigWindowsResizeFromEdges = true;
                
                // Handle manual resize: Resize Grips, Borders, Gamepad
                int[] border_held = {-1};
                Colorc[] resize_grip_col = {
                        Color.WHITE,
                        Color.WHITE,
                        Color.WHITE,
                        Color.WHITE
                };
                // final int resize_grip_count = g.IO.ConfigWindowsResizeFromEdges ? 2 : 1; // Allow resize from lower-left if we have the mouse cursor feedback for it.
                final int    resize_grip_count     = ConfigWindowsResizeFromEdges ? 2 : 1; // Allow resize from lower-left if we have the mouse cursor feedback for it.
                final double resize_grip_draw_size = Math.floor(Math.max(GUI.fontSize() * 1.10, this.WindowRounding + 1.0 + GUI.fontSize() * 0.2));
                if (!this.collapsedActual)
                {
                    // if (UpdateWindowManualResize(size_auto_fit, border_held, resize_grip_count, resize_grip_col, visibility_rect))
                    // {
                    //     // use_current_size_for_scrollbar_x = use_current_size_for_scrollbar_y = true;
                    // }
                }
                this.ResizeBorderHeld = border_held[0] & 0xFF;
                
                for (int resize_grip_n = 0; resize_grip_n < resize_grip_count; resize_grip_n++)
                {
                    final ResizeGrip grip    = ResizeGrip.DEF[resize_grip_n];
                    final int        cornerX = Math.lerp(this.posActual.x, this.posActual.x + this.sizeActual.x, grip.cornerPosN.x());
                    final int        cornerY = Math.lerp(this.posActual.y, this.posActual.y + this.sizeActual.y, grip.cornerPosN.y());
                    // this.DrawList.PathLineTo(corner + grip.InnerDir * ((resize_grip_n & 1) > 0 ? ImVec2(window_border_size, resize_grip_draw_size) : ImVec2(resize_grip_draw_size, window_border_size)));
                    // this.DrawList.PathLineTo(corner + grip.InnerDir * ((resize_grip_n & 1) > 0 ? ImVec2(resize_grip_draw_size, window_border_size) : ImVec2(window_border_size, resize_grip_draw_size)));
                    // this.DrawList.PathArcToFast(ImVec2(corner.x + grip.InnerDir.x * (window_rounding + window_border_size), corner.y + grip.InnerDir.y * (window_rounding + window_border_size)), window_rounding, grip.AngleMin12, grip.AngleMax12);
                    // this.DrawList.PathFillConvex(resize_grip_col[resize_grip_n]);
                    
                    Draw.line2D()
                        .point0(cornerX + grip.innerDir.x() * ((resize_grip_n & 1) > 0 ? window_border_size : resize_grip_draw_size),
                                cornerY + grip.innerDir.y() * ((resize_grip_n & 1) > 0 ? resize_grip_draw_size : window_border_size))
                        .point1(cornerX + grip.innerDir.x() * ((resize_grip_n & 1) > 0 ? resize_grip_draw_size : window_border_size),
                                cornerY + grip.innerDir.y() * ((resize_grip_n & 1) > 0 ? window_border_size : resize_grip_draw_size))
                        .color(resize_grip_col[resize_grip_n])
                        .thickness(1.0)
                        .draw();
                }
            }
            
            // Borders
            renderOuterBorders();
        }
    }
    
    private void renderFrame(Vector2ic p_min, Vector2ic p_max, Colorc fill_col, boolean border, double rounding)
    {
        GuiStyle style = GUI.style();
        
        Draw.fillRect2D()
            .corners(p_min, p_max)
            .cornerRadius(rounding)
            .color(fill_col)
            .draw();
        final double border_size = style.FrameBorderSize;
        if (border && border_size > 0.0f)
        {
            Draw.drawRect2D()
                .corners(p_min.x() + 1, p_min.y() + 1, p_max.x() + 1, p_max.y() + 1)
                .cornerRadius(rounding)
                .thickness(border_size)
                .color(style.Colors.get(StyleColor.BorderShadow.ordinal()))
                .draw();
            Draw.drawRect2D()
                .corners(p_min, p_max)
                .cornerRadius(rounding)
                .thickness(border_size)
                .color(style.Colors.get(StyleColor.Border.ordinal()))
                .draw();
        }
    }
    
    private StyleColor getWindowBgColorIdx()
    {
        if (isEnabled(Flag.TOOLTIP, Flag.POPUP)) return StyleColor.PopupBg;
        if (isEnabled(Flag.CHILD_WINDOW)) return StyleColor.ChildBg;
        return StyleColor.WindowBg;
    }
    
    private void renderOuterBorders()
    {
        final AABB2ic rect = rect();
        
        double rounding    = this.WindowRounding;
        double border_size = this.WindowBorderSize;
        if (border_size > 0.0f && !isEnabled(Flag.NO_BACKGROUND))
        {
            Draw.drawRect2D()
                .corners(rect.min(), rect.max())
                .thickness(border_size)
                .cornerRadius(rounding)
                .color(GUI.style().Colors.get(StyleColor.Border.ordinal()))
                .draw();
        }
        
        int border_held = this.ResizeBorderHeld;
        // if (border_held != -1)
        // {
        //     final ResizeBorder def = ResizeBorder.DEF[border_held];
        //     ImRect border_r = GetResizeBorderRect(window, border_held, rounding, 0.0f);
        //     this.DrawList->PathArcTo(ImLerp(border_r.Min, border_r.Max, def.SegmentN1) + ImVec2(0.5f, 0.5f) + def.InnerDir * rounding, rounding, def.OuterAngle - IM_PI * 0.25f, def.OuterAngle);
        //     this.DrawList->PathArcTo(ImLerp(border_r.Min, border_r.Max, def.SegmentN2) + ImVec2(0.5f, 0.5f) + def.InnerDir * rounding, rounding, def.OuterAngle, def.OuterAngle + IM_PI * 0.25f);
        //     this.DrawList->PathStroke(GetColorU32(ImGuiCol_SeparatorActive), 0, ImMax(2.0f, border_size)); // Thicker than usual
        // }
        // if (g.Style.FrameBorderSize > 0 && !(this.Flags & ImGuiWindowFlags_NoTitleBar))
        // {
        //     float y = this.Pos.y + this.TitleBarHeight() - 1;
        //     this.DrawList->AddLine(ImVec2(this.Pos.x + border_size, y), ImVec2(this.Pos.x + this.Size.x - border_size, y), GetColorU32(ImGuiCol_Border), g.Style.FrameBorderSize);
        // }
    }
    
    @SuppressWarnings("PointlessBitwiseExpression")
    public enum Flag
    {
        NONE(0),
        NO_TITLE_BAR(1 << 0),   // Disable title-bar
        NO_RESIZE(1 << 1),   // Disable user resizing with the lower-right grip
        NO_MOVE(1 << 2),   // Disable user moving the window
        NO_SCROLLBAR(1 << 3),   // Disable scrollbars (window can still scroll with mouse or programmatically)
        NO_SCROLL_WITH_MOUSE(1 << 4),   // Disable user vertically scrolling with mouse wheel. On child window, mouse wheel will be forwarded to the parent unless NoScrollbar is also set.
        NO_COLLAPSE(1 << 5),   // Disable user collapsing window by double-clicking on it. Also referred to as Window Menu Button (e.g. within a docking node).
        ALWAYS_AUTO_RESIZE(1 << 6),   // Resize every window to its content every frame
        NO_BACKGROUND(1 << 7),   // Disable drawing background color (WindowBg, etc.) and outside border. Similar as using SetNextWindowBgAlpha(0.0f).
        NO_SAVED_SETTINGS(1 << 8),   // Never load/save settings in .ini file
        NO_MOUSE_INPUTS(1 << 9),   // Disable catching mouse, hovering test with pass through.
        MENU_BAR(1 << 10),  // Has a menu-bar
        HORIZONTAL_SCROLLBAR(1 << 11),  // Allow horizontal scrollbar to appear (off by default). You may use SetNextWindowContentSize(ImVec2(width,0.0f)); prior to calling Begin() to specify width. Read code in imgui_demo in the "Horizontal Scrolling" section.
        NO_FOCUS_ON_APPEARING(1 << 12),  // Disable taking focus when transitioning from hidden to visible state
        NO_BRING_TO_FRONT_ON_FOCUS(1 << 13),  // Disable bringing window to front when taking focus (e.g. clicking on it or programmatically giving it focus)
        ALWAYS_VERTICAL_SCROLLBAR(1 << 14),  // Always show vertical scrollbar (even if ContentSize.y < Size.y)
        ALWAYS_HORIZONTAL_SCROLLBAR(1 << 15),  // Always show horizontal scrollbar (even if ContentSize.x < Size.x)
        ALWAYS_USE_WINDOW_PADDING(1 << 16),  // Ensure child windows without border uses style.WindowPadding (ignored by default for non-bordered child windows, because more convenient)
        NO_NAV_INPUTS(1 << 18),  // No gamepad/keyboard navigation within the window
        NO_NAV_FOCUS(1 << 19),  // No focusing toward this window with gamepad/keyboard navigation (e.g. skipped by CTRL+TAB)
        UNSAVED_DOCUMENT(1 << 20),  // Display a dot next to the title. When used in a tab/docking context, tab is selected when clicking the X + closure is not assumed (will wait for user to stop submitting the tab). Otherwise closure is assumed when pressing the X, so if you keep submitting the tab may reappear at end of tab bar.
        NO_NAV(NO_NAV_INPUTS.value | NO_NAV_FOCUS.value),
        NO_DECORATION(NO_TITLE_BAR.value | NO_RESIZE.value | NO_SCROLLBAR.value | NO_COLLAPSE.value),
        NO_INPUTS(NO_MOUSE_INPUTS.value | NO_NAV_INPUTS.value | NO_NAV_FOCUS.value),
        
        // [Internal]
        NAV_FLATTENED(1 << 23),  // [BETA] On child window: allow gamepad/keyboard navigation to cross over parent border to this child or between sibling child windows.
        CHILD_WINDOW(1 << 24),  // Don't use! For internal use by BeginChild()
        TOOLTIP(1 << 25),  // Don't use! For internal use by BeginTooltip()
        POPUP(1 << 26),  // Don't use! For internal use by BeginPopup()
        MODAL(1 << 27),  // Don't use! For internal use by BeginPopupModal()
        CHILD_MENU(1 << 28),   // Don't use! For internal use by BeginMenu()
        ;
        
        private final int value;
        
        Flag(int value)
        {
            this.value = value;
        }
    }
    
    private record ResizeGrip(Vector2ic cornerPosN, Vector2ic innerDir, int angleMin12, int angleMax12)
    {
        private static final ResizeGrip[] DEF = {
                new ResizeGrip(new Vector2i(1, 1), new Vector2i(-1, -1), 0, 3),
                new ResizeGrip(new Vector2i(0, 1), new Vector2i(+1, -1), 3, 6),
                new ResizeGrip(new Vector2i(0, 0), new Vector2i(+1, +1), 6, 9),
                new ResizeGrip(new Vector2i(1, 0), new Vector2i(-1, +1), 9, 12)
        };
    }
    
    private record ResizeBorder(Vector2ic innerDir, Vector2ic segmentN1, Vector2ic segmentN2, double outerAngle)
    {
        private static final ResizeBorder[] DEF = {
                new ResizeBorder(new Vector2i(+1, 0), new Vector2i(0, 1), new Vector2i(0, 0), Math.PI),
                new ResizeBorder(new Vector2i(-1, 0), new Vector2i(1, 0), new Vector2i(1, 1), 0.0),
                new ResizeBorder(new Vector2i(0, +1), new Vector2i(0, 0), new Vector2i(1, 0), Math.PI + Math.PI_2),
                new ResizeBorder(new Vector2i(0, -1), new Vector2i(1, 1), new Vector2i(0, 1), Math.PI_2)
        };
    }
}
