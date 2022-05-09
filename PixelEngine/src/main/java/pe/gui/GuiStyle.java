package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.lwjgl.system.MemoryUtil;
import pe.GUI;
import pe.color.Color;
import pe.color.Color_RGBA;

public final class GuiStyle
{
    public static void StyleColorsDark(@Nullable GuiStyle style)
    {
        if (style == null) style = GUI.style();
        Color.Buffer colors = style.Colors;
        
        colors.put(StyleColor.Text.ordinal(), 1.00, 1.00, 1.00, 1.00);
        colors.put(StyleColor.TextDisabled.ordinal(), 0.50, 0.50, 0.50, 1.00);
        colors.put(StyleColor.WindowBg.ordinal(), 0.06, 0.06, 0.06, 0.94);
        colors.put(StyleColor.ChildBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.PopupBg.ordinal(), 0.08, 0.08, 0.08, 0.94);
        colors.put(StyleColor.Border.ordinal(), 0.43, 0.43, 0.50, 0.50);
        colors.put(StyleColor.BorderShadow.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.FrameBg.ordinal(), 0.16, 0.29, 0.48, 0.54);
        colors.put(StyleColor.FrameBgHovered.ordinal(), 0.26, 0.59, 0.98, 0.40);
        colors.put(StyleColor.FrameBgActive.ordinal(), 0.26, 0.59, 0.98, 0.67);
        colors.put(StyleColor.TitleBg.ordinal(), 0.04, 0.04, 0.04, 1.00);
        colors.put(StyleColor.TitleBgActive.ordinal(), 0.16, 0.29, 0.48, 1.00);
        colors.put(StyleColor.TitleBgCollapsed.ordinal(), 0.00, 0.00, 0.00, 0.51);
        colors.put(StyleColor.MenuBarBg.ordinal(), 0.14, 0.14, 0.14, 1.00);
        colors.put(StyleColor.ScrollbarBg.ordinal(), 0.02, 0.02, 0.02, 0.53);
        colors.put(StyleColor.ScrollbarGrab.ordinal(), 0.31, 0.31, 0.31, 1.00);
        colors.put(StyleColor.ScrollbarGrabHovered.ordinal(), 0.41, 0.41, 0.41, 1.00);
        colors.put(StyleColor.ScrollbarGrabActive.ordinal(), 0.51, 0.51, 0.51, 1.00);
        colors.put(StyleColor.CheckMark.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.SliderGrab.ordinal(), 0.24, 0.52, 0.88, 1.00);
        colors.put(StyleColor.SliderGrabActive.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.Button.ordinal(), 0.26, 0.59, 0.98, 0.40);
        colors.put(StyleColor.ButtonHovered.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.ButtonActive.ordinal(), 0.06, 0.53, 0.98, 1.00);
        colors.put(StyleColor.Header.ordinal(), 0.26, 0.59, 0.98, 0.31);
        colors.put(StyleColor.HeaderHovered.ordinal(), 0.26, 0.59, 0.98, 0.80);
        colors.put(StyleColor.HeaderActive.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.Separator.ordinal(), colors.get(StyleColor.Border.ordinal()));
        colors.put(StyleColor.SeparatorHovered.ordinal(), 0.10, 0.40, 0.75, 0.78);
        colors.put(StyleColor.SeparatorActive.ordinal(), 0.10, 0.40, 0.75, 1.00);
        colors.put(StyleColor.ResizeGrip.ordinal(), 0.26, 0.59, 0.98, 0.20);
        colors.put(StyleColor.ResizeGripHovered.ordinal(), 0.26, 0.59, 0.98, 0.67);
        colors.put(StyleColor.ResizeGripActive.ordinal(), 0.26, 0.59, 0.98, 0.95);
        colors.get(StyleColor.Header.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.80, colors.get(StyleColor.Tab.ordinal()));
        colors.put(StyleColor.TabHovered.ordinal(), colors.get(StyleColor.HeaderHovered.ordinal()));
        colors.get(StyleColor.HeaderActive.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.60, colors.get(StyleColor.TabActive.ordinal()));
        colors.get(StyleColor.Tab.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.80, colors.get(StyleColor.TabUnfocused.ordinal()));
        colors.get(StyleColor.TabActive.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.40, colors.get(StyleColor.TabUnfocusedActive.ordinal()));
        colors.put(StyleColor.PlotLines.ordinal(), 0.61, 0.61, 0.61, 1.00);
        colors.put(StyleColor.PlotLinesHovered.ordinal(), 1.00, 0.43, 0.35, 1.00);
        colors.put(StyleColor.PlotHistogram.ordinal(), 0.90, 0.70, 0.00, 1.00);
        colors.put(StyleColor.PlotHistogramHovered.ordinal(), 1.00, 0.60, 0.00, 1.00);
        colors.put(StyleColor.TableHeaderBg.ordinal(), 0.19, 0.19, 0.20, 1.00);
        colors.put(StyleColor.TableBorderStrong.ordinal(), 0.31, 0.31, 0.35, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableBorderLight.ordinal(), 0.23, 0.23, 0.25, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableRowBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.TableRowBgAlt.ordinal(), 1.00, 1.00, 1.00, 0.06);
        colors.put(StyleColor.TextSelectedBg.ordinal(), 0.26, 0.59, 0.98, 0.35);
        colors.put(StyleColor.DragDropTarget.ordinal(), 1.00, 1.00, 0.00, 0.90);
        colors.put(StyleColor.NavHighlight.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.NavWindowingHighlight.ordinal(), 1.00, 1.00, 1.00, 0.70);
        colors.put(StyleColor.NavWindowingDimBg.ordinal(), 0.80, 0.80, 0.80, 0.20);
    }
    
    public static void StyleColorsClassic(@Nullable GuiStyle style)
    {
        if (style == null) style = GUI.style();
        Color.Buffer colors = style.Colors;
        
        colors.put(StyleColor.Text.ordinal(), 0.90, 0.90, 0.90, 1.00);
        colors.put(StyleColor.TextDisabled.ordinal(), 0.60, 0.60, 0.60, 1.00);
        colors.put(StyleColor.WindowBg.ordinal(), 0.00, 0.00, 0.00, 0.85);
        colors.put(StyleColor.ChildBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.PopupBg.ordinal(), 0.11, 0.11, 0.14, 0.92);
        colors.put(StyleColor.Border.ordinal(), 0.50, 0.50, 0.50, 0.50);
        colors.put(StyleColor.BorderShadow.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.FrameBg.ordinal(), 0.43, 0.43, 0.43, 0.39);
        colors.put(StyleColor.FrameBgHovered.ordinal(), 0.47, 0.47, 0.69, 0.40);
        colors.put(StyleColor.FrameBgActive.ordinal(), 0.42, 0.41, 0.64, 0.69);
        colors.put(StyleColor.TitleBg.ordinal(), 0.27, 0.27, 0.54, 0.83);
        colors.put(StyleColor.TitleBgActive.ordinal(), 0.32, 0.32, 0.63, 0.87);
        colors.put(StyleColor.TitleBgCollapsed.ordinal(), 0.40, 0.40, 0.80, 0.20);
        colors.put(StyleColor.MenuBarBg.ordinal(), 0.40, 0.40, 0.55, 0.80);
        colors.put(StyleColor.ScrollbarBg.ordinal(), 0.20, 0.25, 0.30, 0.60);
        colors.put(StyleColor.ScrollbarGrab.ordinal(), 0.40, 0.40, 0.80, 0.30);
        colors.put(StyleColor.ScrollbarGrabHovered.ordinal(), 0.40, 0.40, 0.80, 0.40);
        colors.put(StyleColor.ScrollbarGrabActive.ordinal(), 0.41, 0.39, 0.80, 0.60);
        colors.put(StyleColor.CheckMark.ordinal(), 0.90, 0.90, 0.90, 0.50);
        colors.put(StyleColor.SliderGrab.ordinal(), 1.00, 1.00, 1.00, 0.30);
        colors.put(StyleColor.SliderGrabActive.ordinal(), 0.41, 0.39, 0.80, 0.60);
        colors.put(StyleColor.Button.ordinal(), 0.35, 0.40, 0.61, 0.62);
        colors.put(StyleColor.ButtonHovered.ordinal(), 0.40, 0.48, 0.71, 0.79);
        colors.put(StyleColor.ButtonActive.ordinal(), 0.46, 0.54, 0.80, 1.00);
        colors.put(StyleColor.Header.ordinal(), 0.40, 0.40, 0.90, 0.45);
        colors.put(StyleColor.HeaderHovered.ordinal(), 0.45, 0.45, 0.90, 0.80);
        colors.put(StyleColor.HeaderActive.ordinal(), 0.53, 0.53, 0.87, 0.80);
        colors.put(StyleColor.Separator.ordinal(), 0.50, 0.50, 0.50, 0.60);
        colors.put(StyleColor.SeparatorHovered.ordinal(), 0.60, 0.60, 0.70, 1.00);
        colors.put(StyleColor.SeparatorActive.ordinal(), 0.70, 0.70, 0.90, 1.00);
        colors.put(StyleColor.ResizeGrip.ordinal(), 1.00, 1.00, 1.00, 0.10);
        colors.put(StyleColor.ResizeGripHovered.ordinal(), 0.78, 0.82, 1.00, 0.60);
        colors.put(StyleColor.ResizeGripActive.ordinal(), 0.78, 0.82, 1.00, 0.90);
        colors.get(StyleColor.Header.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.80, colors.get(StyleColor.Tab.ordinal()));
        colors.put(StyleColor.TabHovered.ordinal(), colors.get(StyleColor.HeaderHovered.ordinal()));
        colors.get(StyleColor.HeaderActive.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.60, colors.get(StyleColor.TabActive.ordinal()));
        colors.get(StyleColor.Tab.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.80, colors.get(StyleColor.TabUnfocused.ordinal()));
        colors.get(StyleColor.TabActive.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.40, colors.get(StyleColor.TabUnfocusedActive.ordinal()));
        colors.put(StyleColor.PlotLines.ordinal(), 1.00, 1.00, 1.00, 1.00);
        colors.put(StyleColor.PlotLinesHovered.ordinal(), 0.90, 0.70, 0.00, 1.00);
        colors.put(StyleColor.PlotHistogram.ordinal(), 0.90, 0.70, 0.00, 1.00);
        colors.put(StyleColor.PlotHistogramHovered.ordinal(), 1.00, 0.60, 0.00, 1.00);
        colors.put(StyleColor.TableHeaderBg.ordinal(), 0.27, 0.27, 0.38, 1.00);
        colors.put(StyleColor.TableBorderStrong.ordinal(), 0.31, 0.31, 0.45, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableBorderLight.ordinal(), 0.26, 0.26, 0.28, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableRowBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.TableRowBgAlt.ordinal(), 1.00, 1.00, 1.00, 0.07);
        colors.put(StyleColor.TextSelectedBg.ordinal(), 0.00, 0.00, 1.00, 0.35);
        colors.put(StyleColor.DragDropTarget.ordinal(), 1.00, 1.00, 0.00, 0.90);
        colors.put(StyleColor.NavHighlight.ordinal(), colors.get(StyleColor.HeaderHovered.ordinal()));
        colors.put(StyleColor.NavWindowingHighlight.ordinal(), 1.00, 1.00, 1.00, 0.70);
        colors.put(StyleColor.NavWindowingDimBg.ordinal(), 0.80, 0.80, 0.80, 0.20);
    }
    
    public static void StyleColorsLight(@Nullable GuiStyle style)
    {
        if (style == null) style = GUI.style();
        Color.Buffer colors = style.Colors;
        
        colors.put(StyleColor.Text.ordinal(), 0.00, 0.00, 0.00, 1.00);
        colors.put(StyleColor.TextDisabled.ordinal(), 0.60, 0.60, 0.60, 1.00);
        colors.put(StyleColor.WindowBg.ordinal(), 0.94, 0.94, 0.94, 1.00);
        colors.put(StyleColor.ChildBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.PopupBg.ordinal(), 1.00, 1.00, 1.00, 0.98);
        colors.put(StyleColor.Border.ordinal(), 0.00, 0.00, 0.00, 0.30);
        colors.put(StyleColor.BorderShadow.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.FrameBg.ordinal(), 1.00, 1.00, 1.00, 1.00);
        colors.put(StyleColor.FrameBgHovered.ordinal(), 0.26, 0.59, 0.98, 0.40);
        colors.put(StyleColor.FrameBgActive.ordinal(), 0.26, 0.59, 0.98, 0.67);
        colors.put(StyleColor.TitleBg.ordinal(), 0.96, 0.96, 0.96, 1.00);
        colors.put(StyleColor.TitleBgActive.ordinal(), 0.82, 0.82, 0.82, 1.00);
        colors.put(StyleColor.TitleBgCollapsed.ordinal(), 1.00, 1.00, 1.00, 0.51);
        colors.put(StyleColor.MenuBarBg.ordinal(), 0.86, 0.86, 0.86, 1.00);
        colors.put(StyleColor.ScrollbarBg.ordinal(), 0.98, 0.98, 0.98, 0.53);
        colors.put(StyleColor.ScrollbarGrab.ordinal(), 0.69, 0.69, 0.69, 0.80);
        colors.put(StyleColor.ScrollbarGrabHovered.ordinal(), 0.49, 0.49, 0.49, 0.80);
        colors.put(StyleColor.ScrollbarGrabActive.ordinal(), 0.49, 0.49, 0.49, 1.00);
        colors.put(StyleColor.CheckMark.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.SliderGrab.ordinal(), 0.26, 0.59, 0.98, 0.78);
        colors.put(StyleColor.SliderGrabActive.ordinal(), 0.46, 0.54, 0.80, 0.60);
        colors.put(StyleColor.Button.ordinal(), 0.26, 0.59, 0.98, 0.40);
        colors.put(StyleColor.ButtonHovered.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.ButtonActive.ordinal(), 0.06, 0.53, 0.98, 1.00);
        colors.put(StyleColor.Header.ordinal(), 0.26, 0.59, 0.98, 0.31);
        colors.put(StyleColor.HeaderHovered.ordinal(), 0.26, 0.59, 0.98, 0.80);
        colors.put(StyleColor.HeaderActive.ordinal(), 0.26, 0.59, 0.98, 1.00);
        colors.put(StyleColor.Separator.ordinal(), 0.39, 0.39, 0.39, 0.62);
        colors.put(StyleColor.SeparatorHovered.ordinal(), 0.14, 0.44, 0.80, 0.78);
        colors.put(StyleColor.SeparatorActive.ordinal(), 0.14, 0.44, 0.80, 1.00);
        colors.put(StyleColor.ResizeGrip.ordinal(), 0.35, 0.35, 0.35, 0.17);
        colors.put(StyleColor.ResizeGripHovered.ordinal(), 0.26, 0.59, 0.98, 0.67);
        colors.put(StyleColor.ResizeGripActive.ordinal(), 0.26, 0.59, 0.98, 0.95);
        colors.get(StyleColor.Header.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.90, colors.get(StyleColor.Tab.ordinal()));
        colors.put(StyleColor.TabHovered.ordinal(), colors.get(StyleColor.HeaderHovered.ordinal()));
        colors.get(StyleColor.HeaderActive.ordinal()).interpolate(colors.get(StyleColor.TitleBgActive.ordinal()), 0.60, colors.get(StyleColor.TabActive.ordinal()));
        colors.get(StyleColor.Tab.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.80, colors.get(StyleColor.TabUnfocused.ordinal()));
        colors.get(StyleColor.TabActive.ordinal()).interpolate(colors.get(StyleColor.TitleBg.ordinal()), 0.40, colors.get(StyleColor.TabUnfocusedActive.ordinal()));
        colors.put(StyleColor.PlotLines.ordinal(), 0.39, 0.39, 0.39, 1.00);
        colors.put(StyleColor.PlotLinesHovered.ordinal(), 1.00, 0.43, 0.35, 1.00);
        colors.put(StyleColor.PlotHistogram.ordinal(), 0.90, 0.70, 0.00, 1.00);
        colors.put(StyleColor.PlotHistogramHovered.ordinal(), 1.00, 0.45, 0.00, 1.00);
        colors.put(StyleColor.TableHeaderBg.ordinal(), 0.78, 0.87, 0.98, 1.00);
        colors.put(StyleColor.TableBorderStrong.ordinal(), 0.57, 0.57, 0.64, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableBorderLight.ordinal(), 0.68, 0.68, 0.74, 1.00);   // Prefer using Alpha=1.0 here
        colors.put(StyleColor.TableRowBg.ordinal(), 0.00, 0.00, 0.00, 0.00);
        colors.put(StyleColor.TableRowBgAlt.ordinal(), 0.30, 0.30, 0.30, 0.09);
        colors.put(StyleColor.TextSelectedBg.ordinal(), 0.26, 0.59, 0.98, 0.35);
        colors.put(StyleColor.DragDropTarget.ordinal(), 0.26, 0.59, 0.98, 0.95);
        colors.put(StyleColor.NavHighlight.ordinal(), colors.get(StyleColor.HeaderHovered.ordinal()));
        colors.put(StyleColor.NavWindowingHighlight.ordinal(), 0.70, 0.70, 0.70, 0.70);
        colors.put(StyleColor.NavWindowingDimBg.ordinal(), 0.20, 0.20, 0.20, 0.20);
    }
    
    /**
     * Global alpha applies to everything in style.
     */
    public double Alpha = 1.0;
    
    /**
     * Additional alpha multiplier applied by BeginDisabled(). Multiply over
     * current value of {@link #Alpha}.
     */
    public double DisabledAlpha = 0.60;
    
    /**
     * Padding within a window.
     */
    public final Vector2i WindowPadding = new Vector2i(8, 8);
    
    /**
     * Radius of window corners rounding. Set to {@code 0.0} to have
     * rectangular windows. Large values tend to lead to variety of artifacts
     * and are not recommended.
     */
    public double WindowRounding = 0.0;
    
    /**
     * Thickness of border around windows. Generally set to {@code 0.0} or
     * {@code 1.0}. (Other values are not well tested and more CPU/GPU costly).
     */
    public double WindowBorderSize = 1.0;
    
    /**
     * Minimum window size. This is a global setting. If you want to constraint
     * individual windows, use SetNextWindowSizeConstraints().
     */
    public final Vector2i WindowMinSize = new Vector2i(32, 32);
    
    /**
     * Alignment for title bar text. Defaults to {@code (0.0, 0.5)} for
     * left-aligned, vertically centered.
     */
    public final Vector2d WindowTitleAlign = new Vector2d(0.0, 0.5);
    
    /**
     * Side of the collapsing/docking button in the title bar.
     * ({@link GuiDir#NONE NONE}/{@link GuiDir#LEFT LEFT}/{@link GuiDir#RIGHT RIGHT}).
     * Defaults to {@link GuiDir#LEFT LEFT}.
     */
    public GuiDir WindowMenuButtonPosition = GuiDir.LEFT;
    
    /**
     * Radius of child window corners rounding. Set to {@code 0.0} to have
     * rectangular windows.
     */
    public double ChildRounding = 0.0;
    
    /**
     * Thickness of border around child windows. Generally set to {@code 0.0}
     * or {@code 1.0}. (Other values are not well tested and more CPU/GPU
     * costly).
     */
    public double ChildBorderSize = 1.0;
    
    /**
     * Radius of popup window corners rounding. (Note that tooltip windows use
     * WindowRounding)
     */
    public double PopupRounding = 0.0;
    
    /**
     * Thickness of border around popup/tooltip windows. Generally set to
     * {@code 0.0} or {@code 1.0}. (Other values are not well tested and more
     * CPU/GPU costly).
     */
    public double PopupBorderSize = 1.0;
    
    /**
     * Padding within a framed rectangle (used by most widgets).
     */
    public final Vector2i FramePadding = new Vector2i(4, 3);
    
    /**
     * Radius of frame corners rounding. Set to {@code 0.0} to have rectangular
     * frame (used by most widgets).
     */
    public double FrameRounding = 0.0;
    
    /**
     * Thickness of border around frames. Generally set to {@code 0.0} or
     * {@code 1.0}. (Other values are not well tested and more CPU/GPU costly).
     */
    public double FrameBorderSize = 0.0;
    
    /**
     * Horizontal and vertical spacing between widgets/lines.
     */
    public final Vector2i ItemSpacing = new Vector2i(8, 4);
    
    /**
     * Horizontal and vertical spacing between within elements of a composed
     * widget (e.g. a slider and its label).
     */
    public final Vector2i ItemInnerSpacing = new Vector2i(4, 4);
    
    /**
     * Padding within a table cell
     */
    public final Vector2i CellPadding = new Vector2i(4, 2);
    
    /**
     * Expand reactive bounding box for touch-based system where touch position
     * is not accurate enough. Unfortunately we don't sort widgets so priority
     * on overlap will always be given to the first widget. So don't grow this
     * too much!
     */
    public final Vector2i TouchExtraPadding = new Vector2i(0, 0);
    
    /**
     * Horizontal indentation when e.g. entering a tree node. Generally equals
     * {@code FontSize + }{@link #FramePadding}{@code .x * 2}.
     */
    public double IndentSpacing = 21.0;
    
    /**
     * Minimum horizontal spacing between two columns. Preferably greater than
     * {@link #FramePadding}{@code .x + 1}.
     */
    public double ColumnsMinSpacing = 6.0;
    
    /**
     * Width of the vertical scrollbar, height of the horizontal scrollbar.
     */
    public double ScrollbarSize = 14.0;
    
    /**
     * Radius of grab corners for scrollbar.
     */
    public double ScrollbarRounding = 9.0;
    
    /**
     * Minimum width/height of a grab box for slider/scrollbar.
     */
    public double GrabMinSize = 10.0;
    
    /**
     * Radius of grabs corners rounding. Set to {@code 0.0} to have rectangular
     * slider grabs.
     */
    public double GrabRounding = 0.0;
    
    /**
     * The size in pixels of the dead-zone around zero on logarithmic sliders
     * that cross zero.
     */
    public double LogSliderDeadzone = 4.0;
    
    /**
     * Radius of upper corners of a tab. Set to {@code 0.0} to have rectangular
     * tabs.
     */
    public double TabRounding = 4.0;
    
    /**
     * Thickness of border around tabs.
     */
    public double TabBorderSize = 0.0;
    
    /**
     * Minimum width for close button to appear on an unselected tab when
     * hovered. Set to {@code 0.0} to always show when hovering, set to FLT_MAX
     * to never show close button unless selected.
     */
    public double TabMinWidthForCloseButton = 0.0;
    
    /**
     * Side of the color button in the ColorEdit4 widget
     * ({@link GuiDir#LEFT LEFT}/{@link GuiDir#RIGHT RIGHT}). Defaults to
     * {@link GuiDir#RIGHT RIGHT}.
     */
    public GuiDir ColorButtonPosition = GuiDir.RIGHT;
    
    /**
     * Alignment of button text when button is larger than text. Defaults to
     * {@code (0.5, 0.5)} (centered).
     */
    public final Vector2d ButtonTextAlign = new Vector2d(0.5, 0.5);
    
    /**
     * Alignment of selectable text. Defaults to {@code (0.0, 0.0)} (top-left
     * aligned). It's generally important to keep this left-aligned if you want
     * to lay multiple items on a same line.
     */
    public final Vector2d SelectableTextAlign = new Vector2d(0.0, 0.0);
    
    /**
     * Window position are clamped to be visible within the display area or
     * monitors by at least this amount. Only applies to regular windows.
     */
    public final Vector2i DisplayWindowPadding = new Vector2i(19, 19);
    
    /**
     * If you cannot see the edges of your screen (e.g. on a TV) increase the
     * safe area padding. Apply to popups/tooltips as well regular windows. NB:
     * Prefer configuring your TV sets correctly!
     */
    public final Vector2i DisplaySafeAreaPadding = new Vector2i(3, 3);
    
    /**
     * Scale software rendered mouse cursor (when io.MouseDrawCursor is
     * enabled). May be removed later.
     */
    public double MouseCursorScale = 1.0;
    
    /**
     * Enable anti-aliased lines/borders. Disable if you are really tight on
     * CPU/GPU. Latched at the beginning of the frame (copied to ImDrawList).
     */
    public boolean AntiAliasedLines = true;
    
    /**
     * Enable anti-aliased lines/borders using textures where possible. Require
     * backend to render with bi-linear filtering. Latched at the beginning of
     * the frame (copied to ImDrawList).
     */
    public boolean AntiAliasedLinesUseTex = true;
    
    /**
     * Enable anti-aliased edges around filled shapes (rounded rectangles,
     * circles, etc.). Disable if you are really tight on CPU/GPU. Latched at
     * the beginning of the frame (copied to ImDrawList).
     */
    public boolean AntiAliasedFill = true;
    
    /**
     * Tessellation tolerance when using PathBezierCurveTo() without a specific
     * number of segments. Decrease for highly tessellated curves (higher
     * quality, more polygons), increase to reduce quality.
     */
    public double CurveTessellationTol = 1.25;
    
    /**
     * Maximum error (in pixels) allowed when using
     * AddCircle()/AddCircleFilled() or drawing rounded corner rectangles with
     * no explicit segment count specified. Decrease for higher quality but
     * more geometry.
     */
    public double CircleTessellationMaxError = 0.30;
    
    public final Color.Buffer Colors = Color_RGBA.create(StyleColor.COUNT);
    
    public GuiStyle()
    {
        GuiStyle.StyleColorsDark(this);
    }
    
    public void set(@NotNull GuiStyle style)
    {
        this.Alpha         = style.Alpha;
        this.DisabledAlpha = style.DisabledAlpha;
        this.WindowPadding.set(style.WindowPadding);
        this.WindowRounding   = style.WindowRounding;
        this.WindowBorderSize = style.WindowBorderSize;
        this.WindowMinSize.set(style.WindowMinSize);
        this.WindowTitleAlign.set(style.WindowTitleAlign);
        this.WindowMenuButtonPosition = style.WindowMenuButtonPosition;
        this.ChildRounding            = style.ChildRounding;
        this.ChildBorderSize          = style.ChildBorderSize;
        this.PopupRounding            = style.PopupRounding;
        this.PopupBorderSize          = style.PopupBorderSize;
        this.FramePadding.set(style.FramePadding);
        this.FrameRounding   = style.FrameRounding;
        this.FrameBorderSize = style.FrameBorderSize;
        this.ItemSpacing.set(style.ItemSpacing);
        this.ItemInnerSpacing.set(style.ItemInnerSpacing);
        this.CellPadding.set(style.CellPadding);
        this.TouchExtraPadding.set(style.TouchExtraPadding);
        this.IndentSpacing             = style.IndentSpacing;
        this.ColumnsMinSpacing         = style.ColumnsMinSpacing;
        this.ScrollbarSize             = style.ScrollbarSize;
        this.ScrollbarRounding         = style.ScrollbarRounding;
        this.GrabMinSize               = style.GrabMinSize;
        this.GrabRounding              = style.GrabRounding;
        this.LogSliderDeadzone         = style.LogSliderDeadzone;
        this.TabRounding               = style.TabRounding;
        this.TabBorderSize             = style.TabBorderSize;
        this.TabMinWidthForCloseButton = style.TabMinWidthForCloseButton;
        this.ColorButtonPosition       = style.ColorButtonPosition;
        this.ButtonTextAlign.set(style.ButtonTextAlign);
        this.SelectableTextAlign.set(style.SelectableTextAlign);
        this.DisplayWindowPadding.set(style.DisplayWindowPadding);
        this.DisplaySafeAreaPadding.set(style.DisplaySafeAreaPadding);
        this.MouseCursorScale           = style.MouseCursorScale;
        this.AntiAliasedLines           = style.AntiAliasedLines;
        this.AntiAliasedLinesUseTex     = style.AntiAliasedLinesUseTex;
        this.AntiAliasedFill            = style.AntiAliasedFill;
        this.CurveTessellationTol       = style.CurveTessellationTol;
        this.CircleTessellationMaxError = style.CircleTessellationMaxError;
        
        MemoryUtil.memCopy(style.Colors, this.Colors);
    }
    
    public void ScaleAllSizes(double scale_factor)
    {
        this.WindowPadding.x           = (int) Math.floor(this.WindowPadding.x * scale_factor);
        this.WindowPadding.y           = (int) Math.floor(this.WindowPadding.y * scale_factor);
        this.WindowRounding            = Math.floor(this.WindowRounding * scale_factor);
        this.WindowMinSize.x           = (int) Math.floor(this.WindowMinSize.x * scale_factor);
        this.WindowMinSize.y           = (int) Math.floor(this.WindowMinSize.y * scale_factor);
        this.ChildRounding             = Math.floor(this.ChildRounding * scale_factor);
        this.PopupRounding             = Math.floor(this.PopupRounding * scale_factor);
        this.FramePadding.x            = (int) Math.floor(this.FramePadding.x * scale_factor);
        this.FramePadding.y            = (int) Math.floor(this.FramePadding.y * scale_factor);
        this.FrameRounding             = Math.floor(this.FrameRounding * scale_factor);
        this.ItemSpacing.x             = (int) Math.floor(this.ItemSpacing.x * scale_factor);
        this.ItemSpacing.y             = (int) Math.floor(this.ItemSpacing.y * scale_factor);
        this.ItemInnerSpacing.x        = (int) Math.floor(this.ItemInnerSpacing.x * scale_factor);
        this.ItemInnerSpacing.y        = (int) Math.floor(this.ItemInnerSpacing.y * scale_factor);
        this.CellPadding.x             = (int) Math.floor(this.CellPadding.x * scale_factor);
        this.CellPadding.y             = (int) Math.floor(this.CellPadding.y * scale_factor);
        this.TouchExtraPadding.x       = (int) Math.floor(this.TouchExtraPadding.x * scale_factor);
        this.TouchExtraPadding.y       = (int) Math.floor(this.TouchExtraPadding.y * scale_factor);
        this.IndentSpacing             = Math.floor(this.IndentSpacing * scale_factor);
        this.ColumnsMinSpacing         = Math.floor(this.ColumnsMinSpacing * scale_factor);
        this.ScrollbarSize             = Math.floor(this.ScrollbarSize * scale_factor);
        this.ScrollbarRounding         = Math.floor(this.ScrollbarRounding * scale_factor);
        this.GrabMinSize               = Math.floor(this.GrabMinSize * scale_factor);
        this.GrabRounding              = Math.floor(this.GrabRounding * scale_factor);
        this.LogSliderDeadzone         = Math.floor(this.LogSliderDeadzone * scale_factor);
        this.TabRounding               = Math.floor(this.TabRounding * scale_factor);
        this.TabMinWidthForCloseButton = this.TabMinWidthForCloseButton != Float.MAX_VALUE ? Math.floor(this.TabMinWidthForCloseButton * scale_factor) : Float.MAX_VALUE;
        this.DisplayWindowPadding.x    = (int) Math.floor(this.DisplayWindowPadding.x * scale_factor);
        this.DisplayWindowPadding.y    = (int) Math.floor(this.DisplayWindowPadding.y * scale_factor);
        this.DisplaySafeAreaPadding.x  = (int) Math.floor(this.DisplaySafeAreaPadding.x * scale_factor);
        this.DisplaySafeAreaPadding.y  = (int) Math.floor(this.DisplaySafeAreaPadding.y * scale_factor);
        this.MouseCursorScale          = Math.floor(this.MouseCursorScale * scale_factor);
    }
}
