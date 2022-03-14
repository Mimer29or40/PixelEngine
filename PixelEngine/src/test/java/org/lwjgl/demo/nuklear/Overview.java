package org.lwjgl.demo.nuklear;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Objects;

import static org.lwjgl.nuklear.Nuklear.*;

public class Overview
{
    /* window flags */
    boolean show_menu    = true;
    boolean titlebar     = true;
    boolean border       = true;
    boolean resize       = true;
    boolean movable      = true;
    boolean no_scrollbar = false;
    boolean scale_left   = false;
    boolean minimizable  = true;
    int     window_flags = 0;
    
    /* popups */
    int     header_align   = NK_HEADER_RIGHT;
    boolean show_app_about = false;
    
    /* menubar */
    enum menu_states
    {MENU_DEFAULT, MENU_WINDOWS}
    
    PointerBuffer mprog   = BufferUtils.createPointerBuffer(1).put(60).flip();
    int[]         mslider = {10};
    boolean       mcheck  = true;
    
    PointerBuffer prog   = BufferUtils.createPointerBuffer(1).put(40).flip();
    int[]         slider = {10};
    boolean       check  = true;
    
    enum MenuState
    {MENU_NONE, MENU_FILE, MENU_EDIT, MENU_VIEW, MENU_CHART}
    
    MenuState menu_state = MenuState.MENU_NONE;
    
    /* about popup */
    NkRect s = NkRect.create().set(20, 100, 300, 190);
    
    enum options
    {A, B, C}
    
    boolean checkbox;
    options option = options.A;
    
    /* window flags */
    int[] windowFlagsTree              = {NK_MINIMIZED};
    int[] widgetsTree                  = {NK_MINIMIZED};
    int[] widgetsTextTree              = {NK_MINIMIZED};
    int[] widgetsButtonTree            = {NK_MINIMIZED};
    int[] widgetsBasicTree             = {NK_MINIMIZED};
    int[] widgetsInactiveTree          = {NK_MINIMIZED};
    int[] widgetsSelectableTree        = {NK_MINIMIZED};
    int[] widgetsSelectableListTree    = {NK_MINIMIZED};
    int[] widgetsSelectableGridTree    = {NK_MINIMIZED};
    int[] widgetsComboTree             = {NK_MINIMIZED};
    int[] widgetsInputTree             = {NK_MINIMIZED};
    int[] chartTree                    = {NK_MINIMIZED};
    int[] popupTree                    = {NK_MINIMIZED};
    int[] layoutTree                   = {NK_MINIMIZED};
    int[] layoutWidgetTree             = {NK_MINIMIZED};
    int[] layoutGroupTree              = {NK_MINIMIZED};
    int[] layoutTreeTree               = {NK_MINIMIZED};
    int[] layoutNotebookTree           = {NK_MINIMIZED};
    int[] layoutSimpleTree             = {NK_MINIMIZED};
    int[] layoutComplexTree            = {NK_MINIMIZED};
    int[] layoutSplitterTree           = {NK_MINIMIZED};
    int[] layoutSplitterVerticalTree   = {NK_MINIMIZED};
    int[] layoutSplitterHorizontalTree = {NK_MINIMIZED};
    
    /* Basic widgets */
    int[]         int_slider     = {5};
    float[]       float_slider   = {2.5f};
    PointerBuffer prog_value     = BufferUtils.createPointerBuffer(1).put(40).flip();
    float[]       property_float = {2};
    int[]         property_int   = {10};
    int[]         property_neg   = {10};
    
    float[] range_float_min   = {0};
    float[] range_float_max   = {100};
    float[] range_float_value = {50};
    int[]   range_int_min     = {0};
    int[]   range_int_value   = {2048};
    int[]   range_int_max     = {4096};
    
    boolean inactive = true;
    
    boolean[] selectedList = {false, false, true, false};
    
    boolean[] selectedGrid = {true, false, false, false, false, true, false, false, false, false, true, false, false, false, false, true};
    
    float         chart_selection = 8.0f;
    int           current_weapon  = 0;
    int[]         check_values    = new int[5];
    float[][]     position        = {{0}, {0}, {0}};
    NkColor       combo_color     = NkColor.create().set((byte) 130, (byte) 50, (byte) 50, (byte) 255);
    NkColorf      combo_color2    = NkColorf.create().set(0.509f, 0.705f, 0.2f, 1.0f);
    PointerBuffer prog_a          = BufferUtils.createPointerBuffer(1).put(20).flip();
    PointerBuffer prog_b          = BufferUtils.createPointerBuffer(1).put(40).flip();
    PointerBuffer prog_c          = BufferUtils.createPointerBuffer(1).put(10).flip();
    PointerBuffer prog_d          = BufferUtils.createPointerBuffer(1).put(90).flip();
    
    final String[] weapons = {"Fist", "Pistol", "Shotgun", "Plasma", "BFG"};
    
    enum color_mode
    {COL_RGB, COL_HSV}
    
    color_mode col_mode = color_mode.COL_RGB;
    
    boolean       time_selected = false;
    boolean       date_selected = false;
    LocalDateTime sel_time      = LocalDateTime.now();
    LocalDateTime sel_date      = LocalDateTime.now();
    
    ByteBuffer   field_buffer = BufferUtils.createByteBuffer(64);
    ByteBuffer[] text         = {
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64),
            BufferUtils.createByteBuffer(64)
    };
    int[][]      text_len     = {{0}, {0}, {0}, {0}, {0}, {0}, {0}, {0}, {0}};
    ByteBuffer   box_buffer   = BufferUtils.createByteBuffer(512);
    int[]        field_len    = new int[1];
    int[]        box_len      = new int[1];
    
    int   col_index  = -1;
    int   line_index = -1;
    float step       = (2 * 3.141592654f) / 32;
    
    NkColor   popupColor = NkColor.create().set((byte) 255, (byte) 0, (byte) 0, (byte) 255);
    boolean[] select     = new boolean[4];
    boolean   popup_active;
    
    PointerBuffer popupProg   = BufferUtils.createPointerBuffer(1).put(40).flip();
    int[]         popupSlider = {10};
    NkRect        popupBounds = NkRect.create().set(20, 100, 220, 90);
    
    boolean group_titlebar     = false;
    boolean group_border       = true;
    boolean group_no_scrollbar = false;
    int[]   group_width        = {320};
    int[]   group_height       = {200};
    
    boolean[] selectedGroup = new boolean[16];
    
    int[]   root_selected = {0};
    int[][] tree_selected = {{0}, {0}, {0}, {0}, {0}, {0}, {0}, {0}};
    int[]   sel_nodes     = new int[4];
    
    enum chart_type
    {CHART_LINE, CHART_HISTO, CHART_MIXED}
    
    chart_type current_tab = chart_type.CHART_LINE;
    
    int[][] selectedComplex = new int[32][1];
    int[][] selectedGroupTR = new int[4][1];
    int[][] selectedGroupRC = new int[4][1];
    int[][] selectedGroupRB = new int[4][1];
    
    float[] va = {100}, vb = {100}, vc = {100};
    float[] ha = {100}, hb = {100}, hc = {100};
    
    public boolean layout(@NotNull NkContext ctx, int windowX, int windowY)
    {
        /* window flags */
        window_flags = 0;
        ctx.style().window().header().align(header_align);
        if (border) window_flags |= NK_WINDOW_BORDER;
        if (resize) window_flags |= NK_WINDOW_SCALABLE;
        if (movable) window_flags |= NK_WINDOW_MOVABLE;
        if (no_scrollbar) window_flags |= NK_WINDOW_NO_SCROLLBAR;
        if (scale_left) window_flags |= NK_WINDOW_SCALE_LEFT;
        if (minimizable) window_flags |= NK_WINDOW_MINIMIZABLE;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkRect      rect      = NkRect.malloc(stack);
            NkVec2      vec2      = NkVec2.malloc(stack);
            NkColor     color     = NkColor.malloc(stack);
            NkColor     color1    = NkColor.malloc(stack);
            NkStyleItem styleItem = NkStyleItem.malloc(stack);
            ByteBuffer  bytes     = stack.calloc(1);
            
            if (nk_begin(ctx, "Overview", rect.set(windowX, windowY, 400, 600), window_flags))
            {
                if (show_menu)
                {
                    nk_menubar_begin(ctx);
                    
                    /* menu #1 */
                    nk_layout_row_begin(ctx, NK_STATIC, 25, 5);
                    nk_layout_row_push(ctx, 45);
                    if (nk_menu_begin_label(ctx, "MENU", NK_TEXT_LEFT, vec2.set(120, 200)))
                    {
                        nk_layout_row_dynamic(ctx, 25, 1);
                        if (nk_menu_item_label(ctx, "Hide", NK_TEXT_LEFT)) show_menu = false;
                        if (nk_menu_item_label(ctx, "About", NK_TEXT_LEFT)) show_app_about = true;
                        nk_progress(ctx, prog, 100, true);
                        nk_slider_int(ctx, 0, slider, 16, 1);
                        nk_checkbox_label(ctx, "check", bytes.put(0, (byte) (check ? 1 : 0)));
                        check = bytes.get(0) > 0;
                        nk_menu_end(ctx);
                    }
                    /* menu #2 */
                    nk_layout_row_push(ctx, 60);
                    if (nk_menu_begin_label(ctx, "ADVANCED", NK_TEXT_LEFT, vec2.set(200, 600)))
                    {
                        int[] state = {(menu_state == MenuState.MENU_FILE) ? NK_MAXIMIZED : NK_MINIMIZED};
                        if (nk_tree_state_push(ctx, NK_TREE_TAB, "FILE", state))
                        {
                            menu_state = MenuState.MENU_FILE;
                            nk_menu_item_label(ctx, "New", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Open", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Save", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Close", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Exit", NK_TEXT_LEFT);
                            nk_tree_state_pop(ctx);
                        }
                        else
                        {
                            menu_state = (menu_state == MenuState.MENU_FILE) ? MenuState.MENU_NONE : menu_state;
                        }
                        
                        state[0] = (menu_state == MenuState.MENU_EDIT) ? NK_MAXIMIZED : NK_MINIMIZED;
                        if (nk_tree_state_push(ctx, NK_TREE_TAB, "EDIT", state))
                        {
                            menu_state = MenuState.MENU_EDIT;
                            nk_menu_item_label(ctx, "Copy", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Delete", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Cut", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Paste", NK_TEXT_LEFT);
                            nk_tree_state_pop(ctx);
                        }
                        else
                        {
                            menu_state = (menu_state == MenuState.MENU_EDIT) ? MenuState.MENU_NONE : menu_state;
                        }
                        
                        state[0] = (menu_state == MenuState.MENU_VIEW) ? NK_MAXIMIZED : NK_MINIMIZED;
                        if (nk_tree_state_push(ctx, NK_TREE_TAB, "VIEW", state))
                        {
                            menu_state = MenuState.MENU_VIEW;
                            nk_menu_item_label(ctx, "About", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Options", NK_TEXT_LEFT);
                            nk_menu_item_label(ctx, "Customize", NK_TEXT_LEFT);
                            nk_tree_state_pop(ctx);
                        }
                        else
                        {
                            menu_state = (menu_state == MenuState.MENU_VIEW) ? MenuState.MENU_NONE : menu_state;
                        }
                        
                        state[0] = (menu_state == MenuState.MENU_CHART) ? NK_MAXIMIZED : NK_MINIMIZED;
                        if (nk_tree_state_push(ctx, NK_TREE_TAB, "CHART", state))
                        {
                            final float[] values = {26.0f, 13.0f, 30.0f, 15.0f, 25.0f, 10.0f, 20.0f, 40.0f, 12.0f, 8.0f, 22.0f, 28.0f};
                            menu_state = MenuState.MENU_CHART;
                            nk_layout_row_dynamic(ctx, 150, 1);
                            nk_chart_begin(ctx, NK_CHART_COLUMN, values.length, 0, 50);
                            for (float value : values) nk_chart_push(ctx, value);
                            nk_chart_end(ctx);
                            nk_tree_state_pop(ctx);
                        }
                        else
                        {
                            menu_state = (menu_state == MenuState.MENU_CHART) ? MenuState.MENU_NONE : menu_state;
                        }
                        nk_menu_end(ctx);
                    }
                    /* menu widgets */
                    nk_layout_row_push(ctx, 70);
                    nk_progress(ctx, mprog, 100, true);
                    nk_slider_int(ctx, 0, mslider, 16, 1);
                    nk_checkbox_label(ctx, "check", bytes.put(0, (byte) (mcheck ? 1 : 0)));
                    mcheck = bytes.get(0) > 0;
                    nk_menubar_end(ctx);
                    nk_layout_row_end(ctx);
                }
                
                if (show_app_about && (show_app_about = nk_popup_begin(ctx, NK_POPUP_STATIC, "About", NK_WINDOW_CLOSABLE, s)))
                {
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_label(ctx, "Nuklear", NK_TEXT_LEFT);
                    nk_label(ctx, "By Micha Mettke", NK_TEXT_LEFT);
                    nk_label(ctx, "nuklear is licensed under the public domain License.", NK_TEXT_LEFT);
                    nk_popup_end(ctx);
                }
                
                /* window flags */
                if (nk_tree_state_push(ctx, NK_TREE_TAB, "Window", windowFlagsTree))
                {
                    nk_layout_row_dynamic(ctx, 30, 2);
                    nk_checkbox_label(ctx, "Titlebar", bytes.put(0, (byte) (titlebar ? 1 : 0)));
                    titlebar = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Menu", bytes.put(0, (byte) (show_menu ? 1 : 0)));
                    show_menu = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Border", bytes.put(0, (byte) (border ? 1 : 0)));
                    border = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Resizable", bytes.put(0, (byte) (resize ? 1 : 0)));
                    resize = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Movable", bytes.put(0, (byte) (movable ? 1 : 0)));
                    movable = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "No Scrollbar", bytes.put(0, (byte) (no_scrollbar ? 1 : 0)));
                    no_scrollbar = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Minimizable", bytes.put(0, (byte) (minimizable ? 1 : 0)));
                    minimizable = bytes.get(0) > 0;
                    nk_checkbox_label(ctx, "Scale Left", bytes.put(0, (byte) (scale_left ? 1 : 0)));
                    scale_left = bytes.get(0) > 0;
                    nk_tree_state_pop(ctx);
                }
                
                if (nk_tree_state_push(ctx, NK_TREE_TAB, "Widgets", widgetsTree))
                {
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Text", widgetsTextTree))
                    {
                        /* Text Widgets */
                        nk_layout_row_dynamic(ctx, 20, 1);
                        nk_label(ctx, "Label aligned left", NK_TEXT_LEFT);
                        nk_label(ctx, "Label aligned centered", NK_TEXT_CENTERED);
                        nk_label(ctx, "Label aligned right", NK_TEXT_RIGHT);
                        nk_label_colored(ctx, "Blue text", NK_TEXT_LEFT, nk_rgb(0, 0, 255, color));
                        nk_label_colored(ctx, "Yellow text", NK_TEXT_LEFT, nk_rgb(255, 255, 0, color));
                        // nk_text(ctx, "Text without /0", 15, NK_TEXT_RIGHT);
                        nk_text(ctx, "Text without /0", NK_TEXT_RIGHT);
                        
                        nk_layout_row_static(ctx, 100, 200, 1);
                        nk_label_wrap(ctx, "This is a very long line to hopefully get this text to be wrapped into multiple lines to show line wrapping");
                        nk_layout_row_dynamic(ctx, 100, 1);
                        nk_label_wrap(ctx, "This is another long text to show dynamic window changes on multiline text");
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Button", widgetsButtonTree))
                    {
                        /* Buttons Widgets */
                        nk_layout_row_static(ctx, 30, 100, 3);
                        if (nk_button_label(ctx, "Button")) System.out.print("Button pressed!\n");
                        nk_button_set_behavior(ctx, NK_BUTTON_REPEATER);
                        if (nk_button_label(ctx, "Repeater")) System.out.print("Repeater is being pressed!\n");
                        nk_button_set_behavior(ctx, NK_BUTTON_DEFAULT);
                        nk_button_color(ctx, nk_rgb(0, 0, 255, color));
                        
                        nk_layout_row_static(ctx, 25, 25, 8);
                        nk_button_symbol(ctx, NK_SYMBOL_CIRCLE_SOLID);
                        nk_button_symbol(ctx, NK_SYMBOL_CIRCLE_OUTLINE);
                        nk_button_symbol(ctx, NK_SYMBOL_RECT_SOLID);
                        nk_button_symbol(ctx, NK_SYMBOL_RECT_OUTLINE);
                        nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_UP);
                        nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_DOWN);
                        nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_LEFT);
                        nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_RIGHT);
                        
                        nk_layout_row_static(ctx, 30, 100, 2);
                        nk_button_symbol_label(ctx, NK_SYMBOL_TRIANGLE_LEFT, "prev", NK_TEXT_RIGHT);
                        nk_button_symbol_label(ctx, NK_SYMBOL_TRIANGLE_RIGHT, "next", NK_TEXT_LEFT);
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Basic", widgetsBasicTree))
                    {
                        final float[] ratio       = {120, 150};
                        FloatBuffer   ratioBuffer = stack.floats(ratio);
                        
                        nk_layout_row_static(ctx, 30, 100, 1);
                        nk_checkbox_label(ctx, "Checkbox", bytes.put(0, (byte) (checkbox ? 1 : 0)));
                        checkbox = bytes.get(0) > 0;
                        
                        nk_layout_row_static(ctx, 30, 80, 3);
                        option = nk_option_label(ctx, "optionA", option == options.A) ? options.A : option;
                        option = nk_option_label(ctx, "optionB", option == options.B) ? options.B : option;
                        option = nk_option_label(ctx, "optionC", option == options.C) ? options.C : option;
                        
                        nk_layout_row(ctx, NK_STATIC, 30, ratioBuffer);
                        nk_label(ctx, "Slider int", NK_TEXT_LEFT);
                        nk_slider_int(ctx, 0, int_slider, 10, 1);
                        
                        nk_label(ctx, "Slider float", NK_TEXT_LEFT);
                        nk_slider_float(ctx, 0, float_slider, 5.0f, 0.5f);
                        nk_label(ctx, "Progressbar: " + prog_value.get(0), NK_TEXT_LEFT);
                        nk_progress(ctx, prog_value, 100, true);
                        
                        nk_layout_row(ctx, NK_STATIC, 25, ratioBuffer);
                        nk_label(ctx, "Property float:", NK_TEXT_LEFT);
                        nk_property_float(ctx, "Float:", 0, property_float, 64.0f, 0.1f, 0.2f);
                        nk_label(ctx, "Property int:", NK_TEXT_LEFT);
                        nk_property_int(ctx, "Int:", 0, property_int, 100, 1, 1);
                        nk_label(ctx, "Property neg:", NK_TEXT_LEFT);
                        nk_property_int(ctx, "Neg:", -10, property_neg, 10, 1, 1);
                        
                        nk_layout_row_dynamic(ctx, 25, 1);
                        nk_label(ctx, "Range:", NK_TEXT_LEFT);
                        nk_layout_row_dynamic(ctx, 25, 3);
                        nk_property_float(ctx, "#min:", 0, range_float_min, range_float_max[0], 1.0f, 0.2f);
                        nk_property_float(ctx, "#float:", range_float_min[0], range_float_value, range_float_max[0], 1.0f, 0.2f);
                        nk_property_float(ctx, "#max:", range_float_min[0], range_float_max, 100, 1.0f, 0.2f);
                        
                        nk_property_int(ctx, "#min:", Integer.MIN_VALUE, range_int_min, range_int_max[0], 1, 10);
                        nk_property_int(ctx, "#neg:", range_int_min[0], range_int_value, range_int_max[0], 1, 10);
                        nk_property_int(ctx, "#max:", range_int_min[0], range_int_max, Integer.MAX_VALUE, 1, 10);
                        
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Inactive", widgetsInactiveTree))
                    {
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_checkbox_label(ctx, "Inactive", bytes.put(0, (byte) (inactive ? 1 : 0)));
                        inactive = bytes.get(0) > 0;
                        
                        nk_layout_row_static(ctx, 30, 80, 1);
                        if (inactive)
                        {
                            NkStyleButton button = NkStyleButton.malloc(stack).set(ctx.style().button());
                            ctx.style().button().normal(nk_style_item_color(nk_rgb(40, 40, 40, color), styleItem));
                            ctx.style().button().hover(nk_style_item_color(nk_rgb(40, 40, 40, color), styleItem));
                            ctx.style().button().active(nk_style_item_color(nk_rgb(40, 40, 40, color), styleItem));
                            ctx.style().button().border_color(nk_rgb(60, 60, 60, color));
                            ctx.style().button().text_background(nk_rgb(60, 60, 60, color));
                            ctx.style().button().text_normal(nk_rgb(60, 60, 60, color));
                            ctx.style().button().text_hover(nk_rgb(60, 60, 60, color));
                            ctx.style().button().text_active(nk_rgb(60, 60, 60, color));
                            nk_button_label(ctx, "button");
                            ctx.style().button(button);
                        }
                        else if (nk_button_label(ctx, "button"))
                        {
                            System.out.print("button pressed\n");
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Selectable", widgetsSelectableTree))
                    {
                        if (nk_tree_state_push(ctx, NK_TREE_NODE, "List", widgetsSelectableListTree))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            nk_selectable_label(ctx, "Selectable", NK_TEXT_LEFT, bytes.put(0, (byte) (selectedList[0] ? 1 : 0)));
                            selectedList[0] = bytes.get(0) > 0;
                            nk_selectable_label(ctx, "Selectable", NK_TEXT_LEFT, bytes.put(0, (byte) (selectedList[1] ? 1 : 0)));
                            selectedList[1] = bytes.get(0) > 0;
                            nk_label(ctx, "Not Selectable", NK_TEXT_LEFT);
                            nk_selectable_label(ctx, "Selectable", NK_TEXT_LEFT, bytes.put(0, (byte) (selectedList[2] ? 1 : 0)));
                            selectedList[2] = bytes.get(0) > 0;
                            nk_selectable_label(ctx, "Selectable", NK_TEXT_LEFT, bytes.put(0, (byte) (selectedList[3] ? 1 : 0)));
                            selectedList[3] = bytes.get(0) > 0;
                            nk_tree_state_pop(ctx);
                        }
                        
                        if (nk_tree_state_push(ctx, NK_TREE_NODE, "Grid", widgetsSelectableGridTree))
                        {
                            nk_layout_row_static(ctx, 50, 50, 4);
                            for (int i = 0; i < 16; ++i)
                            {
                                boolean pressed = nk_selectable_label(ctx, "Z", NK_TEXT_CENTERED, bytes.put(0, (byte) (selectedGrid[i] ? 1 : 0)));
                                selectedGrid[i] = bytes.get(0) > 0;
                                if (pressed)
                                {
                                    int x = (i % 4), y = i / 4;
                                    if (x > 0) selectedGrid[i - 1] = !selectedGrid[i - 1];
                                    if (x < 3) selectedGrid[i + 1] = !selectedGrid[i + 1];
                                    if (y > 0) selectedGrid[i - 4] = !selectedGrid[i - 4];
                                    if (y < 3) selectedGrid[i + 4] = !selectedGrid[i + 4];
                                }
                            }
                            nk_tree_state_pop(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Combo", widgetsComboTree))
                    {
                        /* Combobox Widgets
                         * In this library comboboxes are not limited to being a popup
                         * list of selectable text. Instead it is a abstract concept of
                         * having something that is *selected* or displayed, a popup window
                         * which opens if something needs to be modified and the content
                         * of the popup which causes the *selected* or displayed value to
                         * change or if wanted close the combobox.
                         *
                         * While strange at first handling comboboxes in a abstract way
                         * solves the problem of overloaded window content. For example
                         * changing a color value requires 4 value modifier (slider, property,...)
                         * for RGBA then you need a label and ways to display the current color.
                         * If you want to go fancy you even add rgb and hsv ratio boxes.
                         * While fine for one color if you have a lot of them it because
                         * tedious to look at and quite wasteful in space. You could add
                         * a popup which modifies the color but this does not solve the
                         * fact that it still requires a lot of cluttered space to do.
                         *
                         * In these kind of instance abstract comboboxes are quite handy. All
                         * value modifiers are hidden inside the combobox popup and only
                         * the color is shown if not open. This combines the clarity of the
                         * popup with the ease of use of just using the space for modifiers.
                         *
                         * Other instances are for example time and especially date picker,
                         * which only show the currently activated time/data and hide the
                         * selection logic inside the combobox popup.
                         */
                        
                        /* default combobox */
                        nk_layout_row_static(ctx, 25, 200, 1);
                        
                        PointerBuffer pointerBuffer = stack.mallocPointer(weapons.length);
                        for (String weapon : weapons) pointerBuffer.put(stack.UTF8(weapon, true));
                        current_weapon = nk_combo(ctx, pointerBuffer.flip(), current_weapon, 25, vec2.set(200, 200));
                        
                        /* slider color combobox */
                        if (nk_combo_begin_color(ctx, combo_color, vec2.set(200, 200)))
                        {
                            float[] ratios = {0.15f, 0.85f};
                            nk_layout_row(ctx, NK_DYNAMIC, 30, stack.floats(ratios));
                            nk_label(ctx, "R:", NK_TEXT_LEFT);
                            combo_color.r((byte) nk_slide_int(ctx, 0, combo_color.r() & 0xFF, 255, 5));
                            nk_label(ctx, "G:", NK_TEXT_LEFT);
                            combo_color.g((byte) nk_slide_int(ctx, 0, combo_color.g() & 0xFF, 255, 5));
                            nk_label(ctx, "B:", NK_TEXT_LEFT);
                            combo_color.b((byte) nk_slide_int(ctx, 0, combo_color.b() & 0xFF, 255, 5));
                            nk_label(ctx, "A:", NK_TEXT_LEFT);
                            combo_color.a((byte) nk_slide_int(ctx, 0, combo_color.a() & 0xFF, 255, 5));
                            nk_combo_end(ctx);
                        }
                        
                        /* complex color combobox */
                        if (nk_combo_begin_color(ctx, nk_rgb_cf(combo_color2, color), vec2.set(200, 400)))
                        {
                            nk_layout_row_dynamic(ctx, 120, 1);
                            nk_color_picker(ctx, combo_color2, NK_RGBA);
                            
                            nk_layout_row_dynamic(ctx, 25, 2);
                            col_mode = nk_option_label(ctx, "RGB", col_mode == color_mode.COL_RGB) ? color_mode.COL_RGB : col_mode;
                            col_mode = nk_option_label(ctx, "HSV", col_mode == color_mode.COL_HSV) ? color_mode.COL_HSV : col_mode;
                            
                            nk_layout_row_dynamic(ctx, 25, 1);
                            if (col_mode == color_mode.COL_RGB)
                            {
                                combo_color2.r(nk_propertyf(ctx, "#R:", 0, combo_color2.r(), 1.0f, 0.01f, 0.005f));
                                combo_color2.g(nk_propertyf(ctx, "#G:", 0, combo_color2.g(), 1.0f, 0.01f, 0.005f));
                                combo_color2.b(nk_propertyf(ctx, "#B:", 0, combo_color2.b(), 1.0f, 0.01f, 0.005f));
                                combo_color2.a(nk_propertyf(ctx, "#A:", 0, combo_color2.a(), 1.0f, 0.01f, 0.005f));
                            }
                            else
                            {
                                float[] hsva = new float[4];
                                nk_colorf_hsva_fv(hsva, combo_color2);
                                hsva[0] = nk_propertyf(ctx, "#H:", 0, hsva[0], 1.0f, 0.01f, 0.005f);
                                hsva[1] = nk_propertyf(ctx, "#S:", 0, hsva[1], 1.0f, 0.01f, 0.005f);
                                hsva[2] = nk_propertyf(ctx, "#V:", 0, hsva[2], 1.0f, 0.01f, 0.005f);
                                hsva[3] = nk_propertyf(ctx, "#A:", 0, hsva[3], 1.0f, 0.01f, 0.005f);
                                nk_hsva_colorfv(hsva, combo_color2);
                            }
                            nk_combo_end(ctx);
                        }
                        /* progressbar combobox */
                        long sum = prog_a.get(0) + prog_b.get(0) + prog_c.get(0) + prog_d.get(0);
                        if (nk_combo_begin_label(ctx, "" + sum, vec2.set(200, 200)))
                        {
                            nk_layout_row_dynamic(ctx, 30, 1);
                            nk_progress(ctx, prog_a, 100, true);
                            nk_progress(ctx, prog_b, 100, true);
                            nk_progress(ctx, prog_c, 100, true);
                            nk_progress(ctx, prog_d, 100, true);
                            nk_combo_end(ctx);
                        }
                        
                        /* checkbox combobox */
                        sum = (check_values[0] + check_values[1] + check_values[2] + check_values[3] + check_values[4]);
                        if (nk_combo_begin_label(ctx, "" + sum, vec2.set(200, 200)))
                        {
                            nk_layout_row_dynamic(ctx, 30, 1);
                            nk_checkbox_label(ctx, weapons[0], bytes.put(0, (byte) check_values[0]));
                            check_values[0] = bytes.get(0);
                            nk_checkbox_label(ctx, weapons[1], bytes.put(0, (byte) check_values[1]));
                            check_values[1] = bytes.get(0);
                            nk_checkbox_label(ctx, weapons[2], bytes.put(0, (byte) check_values[2]));
                            check_values[2] = bytes.get(0);
                            nk_checkbox_label(ctx, weapons[3], bytes.put(0, (byte) check_values[3]));
                            check_values[3] = bytes.get(0);
                            nk_combo_end(ctx);
                        }
                        
                        /* complex text combobox */
                        if (nk_combo_begin_label(ctx, String.format("%.2f, %.2f, %.2f", position[0][0], position[1][0], position[2][0]), vec2.set(200, 200)))
                        {
                            nk_layout_row_dynamic(ctx, 25, 1);
                            nk_property_float(ctx, "#X:", -1024.0f, position[0], 1024.0f, 1, 0.5f);
                            nk_property_float(ctx, "#Y:", -1024.0f, position[1], 1024.0f, 1, 0.5f);
                            nk_property_float(ctx, "#Z:", -1024.0f, position[2], 1024.0f, 1, 0.5f);
                            nk_combo_end(ctx);
                        }
                        
                        /* chart combobox */
                        if (nk_combo_begin_label(ctx, String.format("%.1f", chart_selection), vec2.set(200, 250)))
                        {
                            final float[] values = {26.0f, 13.0f, 30.0f, 15.0f, 25.0f, 10.0f, 20.0f, 40.0f, 12.0f, 8.0f, 22.0f, 28.0f, 5.0f};
                            nk_layout_row_dynamic(ctx, 150, 1);
                            nk_chart_begin(ctx, NK_CHART_COLUMN, values.length, 0, 50);
                            for (float value : values)
                            {
                                int res = nk_chart_push(ctx, value);
                                if ((res & NK_CHART_CLICKED) > 0)
                                {
                                    chart_selection = value;
                                    nk_combo_close(ctx);
                                }
                            }
                            nk_chart_end(ctx);
                            nk_combo_end(ctx);
                        }
                        
                        {
                            if (!time_selected || !date_selected)
                            {
                                /* keep time and date updated if nothing is selected */
                                if (!time_selected) sel_time = LocalDateTime.now();
                                if (!date_selected) sel_date = LocalDateTime.now();
                            }
                            
                            /* time combobox */
                            if (nk_combo_begin_label(ctx, String.format("%02d:%02d:%02d", sel_time.getHour(), sel_time.getMinute(), sel_time.getSecond()), vec2.set(200, 250)))
                            {
                                time_selected = true;
                                nk_layout_row_dynamic(ctx, 25, 1);
                                sel_time = sel_time.withSecond(nk_propertyi(ctx, "#S:", 0, sel_time.getSecond(), 59, 1, 1));
                                sel_time = sel_time.withMinute(nk_propertyi(ctx, "#M:", 0, sel_time.getMinute(), 59, 1, 1));
                                sel_time = sel_time.withHour(nk_propertyi(ctx, "#H:", 0, sel_time.getHour(), 23, 1, 1));
                                nk_combo_end(ctx);
                            }
                            
                            /* date combobox */
                            if (nk_combo_begin_label(ctx, String.format("%02d-%02d-%02d", sel_date.getDayOfMonth(), sel_date.getMonthValue(), sel_date.getYear()), vec2.set(350, 400)))
                            {
                                final String[] month = {
                                        "January", "February", "March",
                                        "April", "May", "June", "July", "August", "September",
                                        "October", "November", "December"
                                };
                                final String[] week_days  = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                                final int[]    month_days = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
                                int            year       = sel_date.getYear();
                                int            leap_year  = (year % 4) <= 0 && (year % 100) > 0 || (year % 400) <= 0 ? 1 : 0;
                                int days = (sel_date.getMonth().ordinal() == 1) ?
                                           month_days[sel_date.getMonth().ordinal()] + leap_year :
                                           month_days[sel_date.getMonth().ordinal()];
                                
                                /* header with month and year */
                                date_selected = true;
                                nk_layout_row_begin(ctx, NK_DYNAMIC, 20, 3);
                                nk_layout_row_push(ctx, 0.05f);
                                if (nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_LEFT))
                                {
                                    if (sel_date.getMonth() == Month.JANUARY)
                                    {
                                        sel_date = sel_date.withMonth(12);
                                        sel_date = sel_date.withYear(sel_date.getYear() - 1);
                                    }
                                    else
                                    {
                                        sel_date = sel_date.withMonth(sel_date.getMonthValue() - 1);
                                    }
                                }
                                nk_layout_row_push(ctx, 0.9f);
                                nk_label(ctx, String.format("%s %d", month[sel_date.getMonth().ordinal()], year), NK_TEXT_CENTERED);
                                nk_layout_row_push(ctx, 0.05f);
                                if (nk_button_symbol(ctx, NK_SYMBOL_TRIANGLE_RIGHT))
                                {
                                    if (sel_date.getMonth() == Month.DECEMBER)
                                    {
                                        sel_date = sel_date.withMonth(0);
                                        sel_date = sel_date.withYear(sel_date.getYear() + 1);
                                    }
                                    else
                                    {
                                        sel_date = sel_date.withMonth(sel_date.getMonthValue() + 1);
                                    }
                                }
                                nk_layout_row_end(ctx);
                                
                                /* good old week day formula (double because precision) */
                                {
                                    int year_n   = (sel_date.getMonthValue() < 2) ? year - 1 : year;
                                    int y        = year_n % 100;
                                    int c        = year_n / 100;
                                    int y4       = (int) ((float) y / 4);
                                    int c4       = (int) ((float) c / 4);
                                    int m        = (int) (2.6 * (double) (((sel_date.getMonthValue() + 10) % 12) + 1) - 0.2);
                                    int week_day = (((1 + m + y + y4 + c4 - 2 * c) % 7) + 7) % 7;
                                    
                                    /* weekdays  */
                                    nk_layout_row_dynamic(ctx, 35, 7);
                                    for (String weekDay : week_days) nk_label(ctx, weekDay, NK_TEXT_CENTERED);
                                    
                                    /* days  */
                                    if (week_day > 0) nk_spacing(ctx, week_day);
                                    for (int i = 1; i <= days; ++i)
                                    {
                                        if (nk_button_label(ctx, "" + i))
                                        {
                                            sel_date = sel_date.withDayOfMonth(i);
                                            nk_combo_close(ctx);
                                        }
                                    }
                                }
                                nk_combo_end(ctx);
                            }
                        }
                        
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Input", widgetsInputTree))
                    {
                        final float[] ratio       = {120, 150};
                        FloatBuffer   ratioBuffer = stack.floats(ratio);
                        
                        nk_layout_row(ctx, NK_STATIC, 25, ratioBuffer);
                        nk_label(ctx, "Default:", NK_TEXT_LEFT);
                        
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[0], text_len[0], 64, Nuklear::nnk_filter_default);
                        nk_label(ctx, "Int:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[1], text_len[1], 64, Nuklear::nnk_filter_decimal);
                        nk_label(ctx, "Float:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[2], text_len[2], 64, Nuklear::nnk_filter_float);
                        nk_label(ctx, "Hex:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[4], text_len[4], 64, Nuklear::nnk_filter_hex);
                        nk_label(ctx, "Octal:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[5], text_len[5], 64, Nuklear::nnk_filter_oct);
                        nk_label(ctx, "Binary:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_SIMPLE, text[6], text_len[6], 64, Nuklear::nnk_filter_binary);
                        
                        nk_label(ctx, "Password:", NK_TEXT_LEFT);
                        {
                            int        old_len = text_len[8][0];
                            ByteBuffer buffer  = stack.UTF8("*".repeat(Math.max(0, text_len[8][0])));
                            nk_edit_string(ctx, NK_EDIT_FIELD, buffer, text_len[8], 64, Nuklear::nnk_filter_default);
                            if (old_len < text_len[8][0])
                            {
                                MemoryUtil.memCopy(MemoryUtil.memAddress(text[8].position(old_len)),
                                                   MemoryUtil.memAddress(buffer.position(old_len)),
                                                   text_len[8][0] - old_len);
                                text[8].clear();
                            }
                        }
                        
                        nk_label(ctx, "Field:", NK_TEXT_LEFT);
                        nk_edit_string(ctx, NK_EDIT_FIELD, field_buffer, field_len, 64, Nuklear::nnk_filter_default);
                        
                        nk_label(ctx, "Box:", NK_TEXT_LEFT);
                        nk_layout_row_static(ctx, 180, 278, 1);
                        nk_edit_string(ctx, NK_EDIT_BOX, box_buffer, box_len, 512, Nuklear::nnk_filter_default);
                        
                        nk_layout_row(ctx, NK_STATIC, 25, ratioBuffer);
                        int active = nk_edit_string(ctx, NK_EDIT_FIELD | NK_EDIT_SIG_ENTER, text[7], text_len[7], 64, Nuklear::nnk_filter_ascii);
                        if (nk_button_label(ctx, "Submit") || (active & NK_EDIT_COMMITED) > 0)
                        {
                            text[7].put(text_len[7][0]++, (byte) '\n');
                            MemoryUtil.memCopy(MemoryUtil.memAddress(text[7]),
                                               MemoryUtil.memAddress(box_buffer.position(box_len[0])),
                                               text_len[7][0]);
                            box_len[0] += text_len[7][0];
                            text_len[7][0] = 0;
                            
                            box_buffer.clear();
                        }
                        nk_tree_state_pop(ctx);
                    }
                    nk_tree_state_pop(ctx);
                }
                
                if (nk_tree_state_push(ctx, NK_TREE_TAB, "Chart", chartTree))
                {
                    /* Chart Widgets
                     * This library has two different rather simple charts. The line and the
                     * column chart. Both provide a simple way of visualizing values and
                     * have a retained mode and immediate mode API version. For the retain
                     * mode version `nk_plot` and `nk_plot_function` you either provide
                     * an array or a callback to call to handle drawing the graph.
                     * For the immediate mode version you start by calling `nk_chart_begin`
                     * and need to provide min and max values for scaling on the Y-axis.
                     * and then call `nk_chart_push` to push values into the chart.
                     * Finally `nk_chart_end` needs to be called to end the process. */
                    float id = 0;
                    int   i, index;
                    
                    /* line chart */
                    id    = 0;
                    index = -1;
                    nk_layout_row_dynamic(ctx, 100, 1);
                    if (nk_chart_begin(ctx, NK_CHART_LINES, 32, -1.0f, 1.0f))
                    {
                        for (i = 0; i < 32; ++i)
                        {
                            int res = nk_chart_push(ctx, (float) Math.cos(id));
                            if ((res & NK_CHART_HOVERING) > 0) index = i;
                            if ((res & NK_CHART_CLICKED) > 0) line_index = i;
                            id += step;
                        }
                        nk_chart_end(ctx);
                    }
                    
                    if (index != -1) nk_tooltip(ctx, String.format("Value: %.2f", (float) Math.cos((float) index * step)));
                    if (line_index != -1)
                    {
                        nk_layout_row_dynamic(ctx, 20, 1);
                        nk_label(ctx, String.format("Selected value: %.2f", (float) Math.cos((float) index * step)), NK_TEXT_LEFT);
                    }
                    
                    /* column chart */
                    nk_layout_row_dynamic(ctx, 100, 1);
                    if (nk_chart_begin(ctx, NK_CHART_COLUMN, 32, 0.0f, 1.0f))
                    {
                        for (i = 0; i < 32; ++i)
                        {
                            int res = nk_chart_push(ctx, (float) Math.abs(Math.sin(id)));
                            if ((res & NK_CHART_HOVERING) > 0) index = i;
                            if ((res & NK_CHART_CLICKED) > 0) col_index = i;
                            id += step;
                        }
                        nk_chart_end(ctx);
                    }
                    if (index != -1) nk_tooltip(ctx, String.format("Value: %.2f", (float) Math.abs(Math.sin(step * (float) index))));
                    if (col_index != -1)
                    {
                        nk_layout_row_dynamic(ctx, 20, 1);
                        nk_label(ctx, String.format("Selected value: %.2f", (float) Math.abs(Math.sin(step * (float) col_index))), NK_TEXT_LEFT);
                    }
                    
                    /* mixed chart */
                    nk_layout_row_dynamic(ctx, 100, 1);
                    if (nk_chart_begin(ctx, NK_CHART_COLUMN, 32, 0.0f, 1.0f))
                    {
                        nk_chart_add_slot(ctx, NK_CHART_LINES, 32, -1.0f, 1.0f);
                        nk_chart_add_slot(ctx, NK_CHART_LINES, 32, -1.0f, 1.0f);
                        for (id = 0, i = 0; i < 32; ++i)
                        {
                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 0);
                            nk_chart_push_slot(ctx, (float) Math.cos(id), 1);
                            nk_chart_push_slot(ctx, (float) Math.sin(id), 2);
                            id += step;
                        }
                    }
                    nk_chart_end(ctx);
                    
                    /* mixed colored chart */
                    nk_layout_row_dynamic(ctx, 100, 1);
                    if (nk_chart_begin_colored(ctx, NK_CHART_LINES, color.set((byte) 255, (byte) 0, (byte) 0, (byte) 255), color1.set((byte) 150, (byte) 0, (byte) 0, (byte) 255), 32, 0.0f, 1.0f))
                    {
                        nk_chart_add_slot_colored(ctx, NK_CHART_LINES, color.set((byte) 0, (byte) 0, (byte) 255, (byte) 255), color1.set((byte) 0, (byte) 0, (byte) 150, (byte) 255), 32, -1.0f, 1.0f);
                        nk_chart_add_slot_colored(ctx, NK_CHART_LINES, color.set((byte) 0, (byte) 255, (byte) 0, (byte) 255), color1.set((byte) 0, (byte) 150, (byte) 0, (byte) 255), 32, -1.0f, 1.0f);
                        for (id = 0, i = 0; i < 32; ++i)
                        {
                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 0);
                            nk_chart_push_slot(ctx, (float) Math.cos(id), 1);
                            nk_chart_push_slot(ctx, (float) Math.sin(id), 2);
                            id += step;
                        }
                    }
                    nk_chart_end(ctx);
                    nk_tree_state_pop(ctx);
                }
                
                if (nk_tree_state_push(ctx, NK_TREE_TAB, "Popup", popupTree))
                {
                    final NkInput in     = ctx.input();
                    NkRect        bounds = NkRect.malloc(stack);
                    
                    /* menu contextual */
                    nk_layout_row_static(ctx, 30, 160, 1);
                    nk_widget_bounds(ctx, bounds);
                    nk_label(ctx, "Right click me for menu", NK_TEXT_LEFT);
                    
                    if (nk_contextual_begin(ctx, 0, vec2.set(100, 300), bounds))
                    {
                        nk_layout_row_dynamic(ctx, 25, 1);
                        nk_checkbox_label(ctx, "Menu", bytes.put(0, (byte) (show_menu ? 1 : 0)));
                        show_menu = bytes.get(0) > 0;
                        nk_progress(ctx, popupProg, 100, true);
                        nk_slider_int(ctx, 0, popupSlider, 16, 1);
                        if (nk_contextual_item_label(ctx, "About", NK_TEXT_CENTERED)) show_app_about = true;
                        for (int i = 0; i < select.length; i++)
                        {
                            nk_selectable_label(ctx, select[i] ? "Unselect" : "Select", NK_TEXT_LEFT, bytes.put(0, (byte) (select[i] ? 1 : 0)));
                            select[i] = bytes.get(0) > 0;
                        }
                        nk_contextual_end(ctx);
                    }
                    
                    /* color contextual */
                    nk_layout_row_begin(ctx, NK_STATIC, 30, 2);
                    nk_layout_row_push(ctx, 120);
                    nk_label(ctx, "Right Click here:", NK_TEXT_LEFT);
                    nk_layout_row_push(ctx, 50);
                    nk_widget_bounds(ctx, bounds);
                    nk_button_color(ctx, popupColor);
                    nk_layout_row_end(ctx);
                    
                    if (nk_contextual_begin(ctx, 0, vec2.set(350, 60), bounds))
                    {
                        nk_layout_row_dynamic(ctx, 30, 4);
                        popupColor.r((byte) nk_propertyi(ctx, "#r", 0, popupColor.r() & 0xFF, 255, 1, 1));
                        popupColor.g((byte) nk_propertyi(ctx, "#g", 0, popupColor.g() & 0xFF, 255, 1, 1));
                        popupColor.b((byte) nk_propertyi(ctx, "#b", 0, popupColor.b() & 0xFF, 255, 1, 1));
                        popupColor.a((byte) nk_propertyi(ctx, "#a", 0, popupColor.a() & 0xFF, 255, 1, 1));
                        nk_contextual_end(ctx);
                    }
                    
                    /* popup */
                    nk_layout_row_begin(ctx, NK_STATIC, 30, 2);
                    nk_layout_row_push(ctx, 120);
                    nk_label(ctx, "Popup:", NK_TEXT_LEFT);
                    nk_layout_row_push(ctx, 50);
                    if (nk_button_label(ctx, "Popup")) popup_active = true;
                    nk_layout_row_end(ctx);
                    
                    if (popup_active)
                    {
                        if (popup_active = nk_popup_begin(ctx, NK_POPUP_STATIC, "Error", 0, popupBounds))
                        {
                            nk_layout_row_dynamic(ctx, 25, 1);
                            nk_label(ctx, "A terrible error as occurred", NK_TEXT_LEFT);
                            nk_layout_row_dynamic(ctx, 25, 2);
                            if (nk_button_label(ctx, "OK"))
                            {
                                popup_active = false;
                                nk_popup_close(ctx);
                            }
                            if (nk_button_label(ctx, "Cancel"))
                            {
                                popup_active = false;
                                nk_popup_close(ctx);
                            }
                            nk_popup_end(ctx);
                        }
                    }
                    
                    /* tooltip */
                    nk_layout_row_static(ctx, 30, 150, 1);
                    nk_widget_bounds(ctx, bounds);
                    nk_label(ctx, "Hover me for tooltip", NK_TEXT_LEFT);
                    if (nk_input_is_mouse_hovering_rect(in, bounds)) nk_tooltip(ctx, "This is a tooltip");
                    
                    nk_tree_state_pop(ctx);
                }
                
                if (nk_tree_state_push(ctx, NK_TREE_TAB, "Layout", layoutTree))
                {
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Widget", layoutWidgetTree))
                    {
                        float[] ratio_two = {0.2f, 0.6f, 0.2f};
                        float[] width_two = {100, 200, 50};
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Dynamic fixed column layout with generated position and size:", NK_TEXT_LEFT);
                        nk_layout_row_dynamic(ctx, 30, 3);
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "static fixed column layout with generated position and size:", NK_TEXT_LEFT);
                        nk_layout_row_static(ctx, 30, 100, 3);
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Dynamic array-based custom column layout with generated position and custom size:", NK_TEXT_LEFT);
                        nk_layout_row(ctx, NK_DYNAMIC, 30, ratio_two);
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Static array-based custom column layout with generated position and custom size:", NK_TEXT_LEFT);
                        nk_layout_row(ctx, NK_STATIC, 30, width_two);
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Dynamic immediate mode custom column layout with generated position and custom size:", NK_TEXT_LEFT);
                        nk_layout_row_begin(ctx, NK_DYNAMIC, 30, 3);
                        nk_layout_row_push(ctx, 0.2f);
                        nk_button_label(ctx, "button");
                        nk_layout_row_push(ctx, 0.6f);
                        nk_button_label(ctx, "button");
                        nk_layout_row_push(ctx, 0.2f);
                        nk_button_label(ctx, "button");
                        nk_layout_row_end(ctx);
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Static immediate mode custom column layout with generated position and custom size:", NK_TEXT_LEFT);
                        nk_layout_row_begin(ctx, NK_STATIC, 30, 3);
                        nk_layout_row_push(ctx, 100);
                        nk_button_label(ctx, "button");
                        nk_layout_row_push(ctx, 200);
                        nk_button_label(ctx, "button");
                        nk_layout_row_push(ctx, 50);
                        nk_button_label(ctx, "button");
                        nk_layout_row_end(ctx);
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Static free space with custom position and custom size:", NK_TEXT_LEFT);
                        nk_layout_space_begin(ctx, NK_STATIC, 60, 4);
                        nk_layout_space_push(ctx, rect.set(100, 0, 100, 30));
                        nk_button_label(ctx, "button");
                        nk_layout_space_push(ctx, rect.set(0, 15, 100, 30));
                        nk_button_label(ctx, "button");
                        nk_layout_space_push(ctx, rect.set(200, 15, 100, 30));
                        nk_button_label(ctx, "button");
                        nk_layout_space_push(ctx, rect.set(100, 30, 100, 30));
                        nk_button_label(ctx, "button");
                        nk_layout_space_end(ctx);
                        
                        nk_layout_row_dynamic(ctx, 30, 1);
                        nk_label(ctx, "Row template:", NK_TEXT_LEFT);
                        nk_layout_row_template_begin(ctx, 30);
                        nk_layout_row_template_push_dynamic(ctx);
                        nk_layout_row_template_push_variable(ctx, 80);
                        nk_layout_row_template_push_static(ctx, 80);
                        nk_layout_row_template_end(ctx);
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        nk_button_label(ctx, "button");
                        
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Group", layoutGroupTree))
                    {
                        int group_flags = 0;
                        if (group_border) group_flags |= NK_WINDOW_BORDER;
                        if (group_no_scrollbar) group_flags |= NK_WINDOW_NO_SCROLLBAR;
                        if (group_titlebar) group_flags |= NK_WINDOW_TITLE;
                        
                        nk_layout_row_dynamic(ctx, 30, 3);
                        nk_checkbox_label(ctx, "Titlebar", bytes.put(0, (byte) (group_titlebar ? 1 : 0)));
                        group_titlebar = bytes.get(0) > 0;
                        nk_checkbox_label(ctx, "Border", bytes.put(0, (byte) (group_border ? 1 : 0)));
                        group_border = bytes.get(0) > 0;
                        nk_checkbox_label(ctx, "No Scrollbar", bytes.put(0, (byte) (group_no_scrollbar ? 1 : 0)));
                        group_no_scrollbar = bytes.get(0) > 0;
                        
                        nk_layout_row_begin(ctx, NK_STATIC, 22, 3);
                        nk_layout_row_push(ctx, 50);
                        nk_label(ctx, "size:", NK_TEXT_LEFT);
                        nk_layout_row_push(ctx, 130);
                        nk_property_int(ctx, "#Width:", 100, group_width, 500, 10, 1);
                        nk_layout_row_push(ctx, 130);
                        nk_property_int(ctx, "#Height:", 100, group_height, 500, 10, 1);
                        nk_layout_row_end(ctx);
                        
                        nk_layout_row_static(ctx, (float) group_height[0], group_width[0], 2);
                        if (nk_group_begin(ctx, "Group", group_flags))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (int i = 0; i < 16; ++i)
                            {
                                nk_selectable_label(ctx, selectedGroup[i] ? "Selected" : "Unselected", NK_TEXT_CENTERED, bytes.put(0, (byte) (selectedGroup[i] ? 1 : 0)));
                                selectedGroup[i] = bytes.get(0) > 0;
                            }
                            nk_group_end(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Tree", layoutTreeTree))
                    {
                        int sel = root_selected[0];
                        if (nk_tree_state_push(ctx, NK_TREE_NODE, "Root", root_selected))
                        {
                            if (sel != root_selected[0])
                            {
                                for (int i = 0; i < 8; ++i) tree_selected[i][0] = sel;
                            }
                            int node_select = tree_selected[0][0];
                            if (nk_tree_state_push(ctx, NK_TREE_NODE, "Node", tree_selected[0]))
                            {
                                if (node_select != tree_selected[0][0])
                                {
                                    for (int i = 0; i < 4; ++i) sel_nodes[i] = node_select;
                                }
                                nk_layout_row_static(ctx, 18, 100, 1);
                                for (int j = 0; j < 4; ++j)
                                {
                                    nk_selectable_symbol_label(ctx, NK_SYMBOL_CIRCLE_SOLID, sel_nodes[j] > 0 ? "Selected" : "Unselected", NK_TEXT_RIGHT, bytes.put(0, (byte) sel_nodes[j]));
                                    sel_nodes[j] = bytes.get(0);
                                }
                                nk_tree_state_pop(ctx);
                            }
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (int i = 1; i < 8; ++i)
                            {
                                nk_selectable_symbol_label(ctx, NK_SYMBOL_CIRCLE_SOLID, tree_selected[i][0] > 0 ? "Selected" : "Unselected", NK_TEXT_RIGHT, bytes.put(0, (byte) tree_selected[i][0]));
                                tree_selected[i][0] = bytes.get(0);
                            }
                            nk_tree_state_pop(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Notebook", layoutNotebookTree))
                    {
                        float          step  = (2 * 3.141592654f) / 32;
                        final String[] names = {"Lines", "Columns", "Mixed"};
                        float          id    = 0;
                        int            i;
                        
                        /* Header */
                        nk_style_push_vec2(ctx, ctx.style().window().spacing(), vec2.set(0, 0));
                        FloatBuffer buttonRounding = MemoryUtil.memFloatBuffer(ctx.style().button().address() + NkStyleButton.ROUNDING, 1);
                        nk_style_push_float(ctx, buttonRounding, 0);
                        nk_layout_row_begin(ctx, NK_STATIC, 20, 3);
                        for (chart_type type : chart_type.values())
                        {
                            i = type.ordinal();
                            /* make sure button perfectly fits text */
                            NkUserFont f            = Objects.requireNonNull(ctx.style().font());
                            float      text_width   = Objects.requireNonNull(f.width()).invoke(f.userdata().address(), f.height(), MemoryUtil.memAddress(stack.UTF8(names[i])), nk_strlen(names[i]));
                            float      widget_width = text_width + 3 * ctx.style().button().padding().x();
                            nk_layout_row_push(ctx, widget_width);
                            if (current_tab == type)
                            {
                                /* active tab gets highlighted */
                                NkStyleItem button_color = NkStyleItem.malloc(stack).set(ctx.style().button().normal());
                                ctx.style().button().normal(ctx.style().button().active());
                                current_tab = nk_button_label(ctx, names[i]) ? type : current_tab;
                                ctx.style().button().normal(button_color);
                            }
                            else
                            {
                                current_tab = nk_button_label(ctx, names[i]) ? type : current_tab;
                            }
                        }
                        nk_style_pop_float(ctx);
                        
                        /* Body */
                        nk_layout_row_dynamic(ctx, 140, 1);
                        if (nk_group_begin(ctx, "Notebook", NK_WINDOW_BORDER))
                        {
                            nk_style_pop_vec2(ctx);
                            switch (current_tab)
                            {
                                case CHART_LINE -> {
                                    nk_layout_row_dynamic(ctx, 100, 1);
                                    if (nk_chart_begin_colored(ctx, NK_CHART_LINES, nk_rgb(255, 0, 0, color), nk_rgb(150, 0, 0, color1), 32, 0.0f, 1.0f))
                                    {
                                        nk_chart_add_slot_colored(ctx, NK_CHART_LINES, nk_rgb(0, 0, 255, color), nk_rgb(0, 0, 150, color1), 32, -1.0f, 1.0f);
                                        for (i = 0, id = 0; i < 32; ++i)
                                        {
                                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 0);
                                            nk_chart_push_slot(ctx, (float) Math.cos(id), 1);
                                            id += step;
                                        }
                                    }
                                    nk_chart_end(ctx);
                                }
                                case CHART_HISTO -> {
                                    nk_layout_row_dynamic(ctx, 100, 1);
                                    if (nk_chart_begin_colored(ctx, NK_CHART_COLUMN, nk_rgb(255, 0, 0, color), nk_rgb(150, 0, 0, color1), 32, 0.0f, 1.0f))
                                    {
                                        for (i = 0, id = 0; i < 32; ++i)
                                        {
                                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 0);
                                            id += step;
                                        }
                                    }
                                    nk_chart_end(ctx);
                                }
                                case CHART_MIXED -> {
                                    nk_layout_row_dynamic(ctx, 100, 1);
                                    if (nk_chart_begin_colored(ctx, NK_CHART_LINES, nk_rgb(255, 0, 0, color), nk_rgb(150, 0, 0, color1), 32, 0.0f, 1.0f))
                                    {
                                        nk_chart_add_slot_colored(ctx, NK_CHART_LINES, nk_rgb(0, 0, 255, color), nk_rgb(0, 0, 150, color1), 32, -1.0f, 1.0f);
                                        nk_chart_add_slot_colored(ctx, NK_CHART_COLUMN, nk_rgb(0, 255, 0, color), nk_rgb(0, 150, 0, color1), 32, 0.0f, 1.0f);
                                        for (i = 0, id = 0; i < 32; ++i)
                                        {
                                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 0);
                                            nk_chart_push_slot(ctx, (float) Math.abs(Math.cos(id)), 1);
                                            nk_chart_push_slot(ctx, (float) Math.abs(Math.sin(id)), 2);
                                            id += step;
                                        }
                                    }
                                    nk_chart_end(ctx);
                                }
                            }
                            nk_group_end(ctx);
                        }
                        else
                        {
                            nk_style_pop_vec2(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Simple", layoutSimpleTree))
                    {
                        nk_layout_row_dynamic(ctx, 300, 2);
                        if (nk_group_begin(ctx, "Group_Without_Border", 0))
                        {
                            nk_layout_row_static(ctx, 18, 150, 1);
                            for (int i = 0; i < 64; ++i)
                            {
                                nk_label(ctx, String.format("0x%02x: scrollable region", i), NK_TEXT_LEFT);
                            }
                            nk_group_end(ctx);
                        }
                        if (nk_group_begin(ctx, "Group_With_Border", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_dynamic(ctx, 25, 2);
                            for (int i = 0; i < 64; ++i)
                            {
                                nk_button_label(ctx, String.format("%08d", ((((i % 7) * 10) ^ 32)) + (64 + (i % 2) * 2)));
                            }
                            nk_group_end(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Complex", layoutComplexTree))
                    {
                        int i;
                        nk_layout_space_begin(ctx, NK_STATIC, 500, 64);
                        nk_layout_space_push(ctx, nk_rect(0, 0, 150, 500, rect));
                        if (nk_group_begin(ctx, "Group_left", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (i = 0; i < 32; ++i)
                            {
                                if (selectedComplex[i] == null) selectedComplex[i] = new int[] {0};
                                nk_selectable_label(ctx, selectedComplex[i][0] > 0 ? "Selected" : "Unselected", NK_TEXT_CENTERED, bytes.put(0, (byte) selectedComplex[i][0]));
                                selectedComplex[i][0] = bytes.get(0);
                            }
                            nk_group_end(ctx);
                        }
                        
                        nk_layout_space_push(ctx, nk_rect(160, 0, 150, 240, rect));
                        if (nk_group_begin(ctx, "Group_top", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_dynamic(ctx, 25, 1);
                            nk_button_label(ctx, "#FFAA");
                            nk_button_label(ctx, "#FFBB");
                            nk_button_label(ctx, "#FFCC");
                            nk_button_label(ctx, "#FFDD");
                            nk_button_label(ctx, "#FFEE");
                            nk_button_label(ctx, "#FFFF");
                            nk_group_end(ctx);
                        }
                        
                        nk_layout_space_push(ctx, nk_rect(160, 250, 150, 250, rect));
                        if (nk_group_begin(ctx, "Group_buttom", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_dynamic(ctx, 25, 1);
                            nk_button_label(ctx, "#FFAA");
                            nk_button_label(ctx, "#FFBB");
                            nk_button_label(ctx, "#FFCC");
                            nk_button_label(ctx, "#FFDD");
                            nk_button_label(ctx, "#FFEE");
                            nk_button_label(ctx, "#FFFF");
                            nk_group_end(ctx);
                        }
                        
                        nk_layout_space_push(ctx, nk_rect(320, 0, 150, 150, rect));
                        if (nk_group_begin(ctx, "Group_right_top", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (i = 0; i < 4; ++i)
                            {
                                nk_selectable_label(ctx, selectedGroupTR[i][0] > 0 ? "Selected" : "Unselected", NK_TEXT_CENTERED, bytes.put(0, (byte) selectedGroupTR[i][0]));
                                selectedGroupTR[i][0] = bytes.get(0);
                            }
                            nk_group_end(ctx);
                        }
                        
                        nk_layout_space_push(ctx, nk_rect(320, 160, 150, 150, rect));
                        if (nk_group_begin(ctx, "Group_right_center", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (i = 0; i < 4; ++i)
                            {
                                nk_selectable_label(ctx, selectedGroupRC[i][0] > 0 ? "Selected" : "Unselected", NK_TEXT_CENTERED, bytes.put(0, (byte) selectedGroupRC[i][0]));
                                selectedGroupRC[i][0] = bytes.get(0);
                            }
                            nk_group_end(ctx);
                        }
                        
                        nk_layout_space_push(ctx, nk_rect(320, 320, 150, 150, rect));
                        if (nk_group_begin(ctx, "Group_right_bottom", NK_WINDOW_BORDER))
                        {
                            nk_layout_row_static(ctx, 18, 100, 1);
                            for (i = 0; i < 4; ++i)
                            {
                                nk_selectable_label(ctx, selectedGroupRB[i][0] > 0 ? "Selected" : "Unselected", NK_TEXT_CENTERED, bytes.put(0, (byte) selectedGroupRB[i][0]));
                                selectedGroupRB[i][0] = bytes.get(0);
                            }
                            nk_group_end(ctx);
                        }
                        nk_layout_space_end(ctx);
                        nk_tree_state_pop(ctx);
                    }
                    
                    if (nk_tree_state_push(ctx, NK_TREE_NODE, "Splitter", layoutSplitterTree))
                    {
                        final NkInput in = ctx.input();
                        nk_layout_row_static(ctx, 20, 320, 1);
                        nk_label(ctx, "Use slider and spinner to change tile size", NK_TEXT_LEFT);
                        nk_label(ctx, "Drag the space between tiles to change tile ratio", NK_TEXT_LEFT);
                        
                        if (nk_tree_state_push(ctx, NK_TREE_NODE, "Vertical", layoutSplitterVerticalTree))
                        {
                            /* header */
                            nk_layout_row_static(ctx, 30, 100, 2);
                            nk_label(ctx, "left:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, va, 200.0f, 10.0f);
                            
                            nk_label(ctx, "middle:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, vb, 200.0f, 10.0f);
                            
                            nk_label(ctx, "right:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, vc, 200.0f, 10.0f);
                            
                            float[] row_layout = {va[0], 8, vb[0], 8, vc[0]};
                            
                            /* tiles */
                            nk_layout_row(ctx, NK_STATIC, 200, row_layout);
                            
                            /* left space */
                            if (nk_group_begin(ctx, "left", NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR))
                            {
                                nk_layout_row_dynamic(ctx, 25, 1);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            
                            /* scaler */
                            nk_widget_bounds(ctx, rect);
                            nk_spacing(ctx, 1);
                            if ((nk_input_is_mouse_hovering_rect(in, rect) ||
                                 nk_input_is_mouse_prev_hovering_rect(in, rect)) &&
                                nk_input_is_mouse_down(in, NK_BUTTON_LEFT))
                            {
                                va[0] = row_layout[0] + in.mouse().delta().x();
                                vb[0] = row_layout[2] - in.mouse().delta().x();
                            }
                            
                            /* middle space */
                            if (nk_group_begin(ctx, "center", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR))
                            {
                                nk_layout_row_dynamic(ctx, 25, 1);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            
                            /* scaler */
                            nk_widget_bounds(ctx, rect);
                            nk_spacing(ctx, 1);
                            if ((nk_input_is_mouse_hovering_rect(in, rect) ||
                                 nk_input_is_mouse_prev_hovering_rect(in, rect)) &&
                                nk_input_is_mouse_down(in, NK_BUTTON_LEFT))
                            {
                                vb[0] = (row_layout[2] + in.mouse().delta().x());
                                vc[0] = (row_layout[4] - in.mouse().delta().x());
                            }
                            
                            /* right space */
                            if (nk_group_begin(ctx, "right", NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR))
                            {
                                nk_layout_row_dynamic(ctx, 25, 1);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            
                            nk_tree_state_pop(ctx);
                        }
                        
                        if (nk_tree_state_push(ctx, NK_TREE_NODE, "Horizontal", layoutSplitterHorizontalTree))
                        {
                            /* header */
                            nk_layout_row_static(ctx, 30, 100, 2);
                            nk_label(ctx, "top:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, ha, 200.0f, 10.0f);
                            
                            nk_label(ctx, "middle:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, hb, 200.0f, 10.0f);
                            
                            nk_label(ctx, "bottom:", NK_TEXT_LEFT);
                            nk_slider_float(ctx, 10.0f, hc, 200.0f, 10.0f);
                            
                            /* top space */
                            nk_layout_row_dynamic(ctx, ha[0], 1);
                            if (nk_group_begin(ctx, "top", NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER))
                            {
                                nk_layout_row_dynamic(ctx, 25, 3);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            
                            /* scaler */
                            nk_layout_row_dynamic(ctx, 8, 1);
                            nk_widget_bounds(ctx, rect);
                            nk_spacing(ctx, 1);
                            if ((nk_input_is_mouse_hovering_rect(in, rect) ||
                                 nk_input_is_mouse_prev_hovering_rect(in, rect)) &&
                                nk_input_is_mouse_down(in, NK_BUTTON_LEFT))
                            {
                                ha[0] = ha[0] + in.mouse().delta().y();
                                hb[0] = hb[0] - in.mouse().delta().y();
                            }
                            
                            /* middle space */
                            nk_layout_row_dynamic(ctx, hb[0], 1);
                            if (nk_group_begin(ctx, "middle", NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER))
                            {
                                nk_layout_row_dynamic(ctx, 25, 3);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            
                            {
                                /* scaler */
                                nk_layout_row_dynamic(ctx, 8, 1);
                                nk_widget_bounds(ctx, rect);
                                if ((nk_input_is_mouse_hovering_rect(in, rect) ||
                                     nk_input_is_mouse_prev_hovering_rect(in, rect)) &&
                                    nk_input_is_mouse_down(in, NK_BUTTON_LEFT))
                                {
                                    hb[0] = hb[0] + in.mouse().delta().y();
                                    hc[0] = hc[0] - in.mouse().delta().y();
                                }
                            }
                            
                            /* bottom space */
                            nk_layout_row_dynamic(ctx, hc[0], 1);
                            if (nk_group_begin(ctx, "bottom", NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER))
                            {
                                nk_layout_row_dynamic(ctx, 25, 3);
                                nk_button_label(ctx, "#FFAA");
                                nk_button_label(ctx, "#FFBB");
                                nk_button_label(ctx, "#FFCC");
                                nk_button_label(ctx, "#FFDD");
                                nk_button_label(ctx, "#FFEE");
                                nk_button_label(ctx, "#FFFF");
                                nk_group_end(ctx);
                            }
                            nk_tree_state_pop(ctx);
                        }
                        nk_tree_state_pop(ctx);
                    }
                    nk_tree_state_pop(ctx);
                }
            }
            nk_end(ctx);
        }
        return !nk_window_is_closed(ctx, "Overview");
    }
}
