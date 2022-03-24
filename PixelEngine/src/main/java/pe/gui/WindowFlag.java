package pe.gui;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("PointlessBitwiseExpression")
public enum WindowFlag
{
    NONE(0),
    
    /**
     * Disable title-bar
     */
    NO_TITLE_BAR(1 << 0),
    
    /**
     * Disable user resizing with the lower-right grip
     */
    NO_RESIZE(1 << 1),
    
    /**
     * Disable user moving the window
     */
    NO_MOVE(1 << 2),
    
    /**
     * Disable scrollbars (window can still scroll with mouse or
     * programmatically)
     */
    NO_SCROLLBAR(1 << 3),
    
    /**
     * Disable user vertically scrolling with mouse wheel. On child window,
     * mouse wheel will be forwarded to the parent unless {@link #NO_SCROLLBAR}
     * is also set.
     */
    NO_SCROLL_WITH_MOUSE(1 << 4),
    
    /**
     * Disable user collapsing window by double-clicking on it. Also referred
     * to as Window Menu Button (e.g. within a docking node).
     */
    NO_COLLAPSE(1 << 5),
    
    /**
     * Resize every window to its content every frame
     */
    ALWAYS_AUTO_RESIZE(1 << 6),
    
    /**
     * Disable drawing background color
     * ({@link GuiStyle.StyleColor#WindowBg WindowBg}, etc.) and outside border.
     * Similar as using SetNextWindowBgAlpha(0.0).
     */
    NO_BACKGROUND(1 << 7),
    
    /**
     * Never load/save settings in .ini file
     */
    NO_SAVED_SETTINGS(1 << 8),
    
    /**
     * Disable catching mouse, hovering test with pass through.
     */
    NO_MOUSE_INPUTS(1 << 9),
    
    /**
     * Has a menu-bar
     */
    MENU_BAR(1 << 10),
    
    /**
     * Allow horizontal scrollbar to appear (off by default). You may use
     * SetNextWindowContentSize(ImVec2(width,0.0f)); prior to calling Begin()
     * to specify width. Read code in imgui_demo in the "Horizontal Scrolling"
     * section.
     */
    HORIZONTAL_SCROLLBAR(1 << 11),
    
    /**
     * Disable taking focus when transitioning from hidden to visible state
     */
    NO_FOCUS_ON_APPEARING(1 << 12),
    
    /**
     * Disable bringing window to front when taking focus (e.g. clicking on it
     * or programmatically giving it focus)
     */
    NO_BRING_TO_FRONT_ON_FOCUS(1 << 13),
    
    /**
     * Always show vertical scrollbar (even if ContentSize.y < Size.y)
     */
    ALWAYS_VERTICAL_SCROLLBAR(1 << 14),
    
    /**
     * Always show horizontal scrollbar (even if ContentSize.x < Size.x)
     */
    ALWAYS_HORIZONTAL_SCROLLBAR(1 << 15),
    
    /**
     * Ensure child windows without border uses style.WindowPadding (ignored by
     * default for non-bordered child windows, because more convenient)
     */
    ALWAYS_USE_WINDOW_PADDING(1 << 16),
    
    /**
     * No gamepad/keyboard navigation within the window
     */
    NO_NAV_INPUTS(1 << 18),
    
    /**
     * No focusing toward this window with gamepad/keyboard navigation (e.g.
     * skipped by CTRL+TAB)
     */
    NO_NAV_FOCUS(1 << 19),
    
    /**
     * Display a dot next to the title. When used in a tab/docking context, tab
     * is selected when clicking the X + closure is not assumed (will wait for
     * user to stop submitting the tab). Otherwise closure is assumed when
     * pressing the X, so if you keep submitting the tab may reappear at end of
     * tab bar.
     */
    UNSAVED_DOCUMENT(1 << 20),
    
    NO_NAV(NO_NAV_INPUTS, NO_NAV_FOCUS),
    NO_DECORATION(NO_TITLE_BAR, NO_RESIZE, NO_SCROLLBAR, NO_COLLAPSE),
    NO_INPUTS(NO_MOUSE_INPUTS, NO_NAV_INPUTS, NO_NAV_FOCUS),
    
    // [Internal]
    
    /**
     * [BETA] On child window: allow gamepad/keyboard navigation to cross over
     * parent border to this child or between sibling child windows.
     */
    NAV_FLATTENED(1 << 23),
    
    /**
     * Don't use! For internal use by BeginChild()
     */
    CHILD_WINDOW(1 << 24),
    
    /**
     * Don't use! For internal use by BeginTooltip()
     */
    TOOLTIP(1 << 25),
    
    /**
     * Don't use! For internal use by BeginPopup()
     */
    POPUP(1 << 26),
    
    /**
     * Don't use! For internal use by BeginPopupModal()
     */
    MODAL(1 << 27),
    
    /**
     * Don't use! For internal use by BeginMenu()
     */
    CHILD_MENU(1 << 28),
    ;
    
    private final int bits;
    
    WindowFlag(int bits)
    {
        this.bits = bits;
    }
    
    WindowFlag(WindowFlag @NotNull ... flags)
    {
        int bits = 0;
        for (WindowFlag flag : flags) bits |= flag.bits;
        this.bits = bits;
    }
}
