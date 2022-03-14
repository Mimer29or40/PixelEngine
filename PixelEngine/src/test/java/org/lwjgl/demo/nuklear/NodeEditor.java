package org.lwjgl.demo.nuklear;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import java.util.Objects;

import static org.lwjgl.nuklear.Nuklear.*;

public class NodeEditor
{
    /* nuklear - v1.00 - public domain */
    /* This is a simple node editor just to show a simple implementation and that
     * it is possible to achieve it with this library. While all nodes inside this
     * example use a simple color modifier as content you could change them
     * to have your custom content depending on the node time.
     * Biggest difference to most usual implementation is that this example does
     * not have connectors on the right position of the property that it links.
     * This is mainly done out of laziness and could be implemented as well but
     * requires calculating the position of all rows and add connectors.
     * In addition adding and removing nodes is quite limited at the
     * moment since it is based on a simple fixed array. If this is to be converted
     * into something more serious it is probably best to extend it.*/
    static class Node
    {
        int     ID;
        String  name;
        NkRect  bounds;
        float   value;
        NkColor color;
        int     input_count;
        int     output_count;
        Node    next;
        Node    prev;
    }
    
    static class NodeLink
    {
        int    input_id;
        int    input_slot;
        int    output_id;
        int    output_slot;
        NkVec2 in;
        NkVec2 out;
    }
    
    static class NodeLinking
    {
        boolean active;
        Node    node;
        int     input_id;
        int     input_slot;
    }
    
    static class NodeEditorInst
    {
        boolean     initialized;
        Node[]      node_buf = new Node[32];
        NodeLink[]  links    = new NodeLink[64];
        Node        begin;
        Node        end;
        int         node_count;
        int         link_count;
        NkRect      bounds;
        Node        selected;
        boolean     show_grid;
        NkVec2      scrolling;
        NodeLinking linking;
    }
    
    NodeEditorInst nodeEditor = new NodeEditorInst();
    
    void node_editor_push(NodeEditorInst editor, Node node)
    {
        if (editor.begin == null)
        {
            node.next    = null;
            node.prev    = null;
            editor.begin = node;
        }
        else
        {
            node.prev = editor.end;
            if (editor.end != null) editor.end.next = node;
            node.next = null;
        }
        editor.end = node;
    }
    
    void node_editor_pop(NodeEditorInst editor, Node node)
    {
        if (node.next != null) node.next.prev = node.prev;
        if (node.prev != null) node.prev.next = node.next;
        if (editor.end == node) editor.end = node.prev;
        if (editor.begin == node) editor.begin = node.next;
        node.next = null;
        node.prev = null;
    }
    
    Node node_editor_find(NodeEditorInst editor, int ID)
    {
        Node iter = editor.begin;
        while (iter != null)
        {
            if (iter.ID == ID) return iter;
            iter = iter.next;
        }
        return null;
    }
    
    int IDs = 0;
    
    void node_editor_add(NodeEditorInst editor, String name, NkRect bounds, NkColor col, int in_count, int out_count)
    {
        assert editor.node_count < editor.node_buf.length;
        Node node = editor.node_buf[editor.node_count++];
        node.ID           = IDs++;
        node.value        = 0;
        node.input_count  = in_count;
        node.output_count = out_count;
        node.color        = col;
        node.bounds       = bounds;
        node.name         = name;
        node_editor_push(editor, node);
    }
    
    void node_editor_link(NodeEditorInst editor, int in_id, int in_slot, int out_id, int out_slot)
    {
        assert editor.link_count < editor.links.length;
        NodeLink link = editor.links[editor.link_count++];
        link.input_id    = in_id;
        link.input_slot  = in_slot;
        link.output_id   = out_id;
        link.output_slot = out_slot;
    }
    
    void node_editor_init(NodeEditorInst editor)
    {
        for (int i = 0; i < editor.node_buf.length; i++) editor.node_buf[i] = new Node();
        for (int i = 0; i < editor.links.length; i++) editor.links[i] = new NodeLink();
        editor.begin     = null;
        editor.end       = null;
        editor.bounds    = NkRect.create();
        editor.scrolling = NkVec2.create();
        editor.linking   = new NodeLinking();
        node_editor_add(editor, "Source", nk_rect(40, 10, 180, 220, NkRect.create()), nk_rgb(255, 0, 0, NkColor.create()), 0, 1);
        // node_editor_add(editor, "Source", nk_rect(40, 260, 180, 220, NkRect.create()), nk_rgb(0, 255, 0, NkColor.create()), 0, 1);
        // node_editor_add(editor, "Combine", nk_rect(400, 100, 180, 220, NkRect.create()), nk_rgb(0, 0, 255, NkColor.create()), 2, 2);
        // node_editor_link(editor, 0, 0, 2, 0);
        // node_editor_link(editor, 1, 0, 2, 1);
        editor.show_grid = true;
    }
    
    boolean layout(NkContext ctx, int windowX, int windowY)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkInput in = ctx.input();
            
            Node           updated = null;
            NodeEditorInst nodedit = nodeEditor;
            
            if (!nodeEditor.initialized)
            {
                node_editor_init(nodeEditor);
                nodeEditor.initialized = true;
            }
            
            int flags = NK_WINDOW_BORDER | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_MOVABLE | NK_WINDOW_CLOSABLE | NK_WINDOW_SCALABLE;
            if (nk_begin(ctx, "NodeEdit", nk_rect(windowX, windowY, 800, 600, NkRect.malloc(stack)), flags))
            {
                /* allocate complete window space */
                NkCommandBuffer canvas      = Objects.requireNonNull(nk_window_get_canvas(ctx));
                NkRect          total_space = nk_window_get_content_region(ctx, NkRect.malloc(stack));
                nk_layout_space_begin(ctx, NK_STATIC, total_space.h(), nodedit.node_count);
                {
                    NkRect  size = nk_layout_space_bounds(ctx, NkRect.malloc(stack));
                    NkPanel node = null;
                    
                    /* display grid */
                    if (nodedit.show_grid)
                    {
                        float   grid_size  = 32.0f;
                        NkColor grid_color = nk_rgb(50, 50, 50, NkColor.malloc(stack));
                        for (float x = -nodedit.scrolling.x() % grid_size; x < size.w(); x += grid_size)
                        {
                            nk_stroke_line(canvas, size.x() + x, size.y(), size.x() + x, size.y() + size.h(), 1.0f, grid_color);
                        }
                        for (float y = -nodedit.scrolling.y() % grid_size; y < size.h(); y += grid_size)
                        {
                            nk_stroke_line(canvas, size.x(), size.y() + y, size.x() + size.w(), size.y() + y, 1.0f, grid_color);
                        }
                    }
                    
                    /* execute each node as a movable group */
                    Node it = nodedit.begin;
                    while (it != null)
                    {
                        /* calculate scrolled node window position and size */
                        // System.out.printf("[%s,%s,%s,%s]%n", it.bounds.x(), it.bounds.y(), it.bounds.w(), it.bounds.h());
                        nk_layout_space_push(ctx, nk_rect(it.bounds.x() - nodedit.scrolling.x(), it.bounds.y() - nodedit.scrolling.y(), it.bounds.w(), it.bounds.h(), NkRect.malloc(stack)));
                        
                        /* execute node window */
                        if (nk_group_begin(ctx, it.name, NK_WINDOW_MOVABLE | NK_WINDOW_NO_SCROLLBAR | NK_WINDOW_BORDER | NK_WINDOW_TITLE))
                        {
                            NkRect rect = nk_widget_bounds(ctx, NkRect.malloc(stack));
                            NkRect r = rect;
                            System.out.printf("[%s,%s,%s,%s]%n", r.x(), r.y(), r.w(), r.h());
                            
                            /* always have last selected node on top */
                            
                            // node = Objects.requireNonNull(nk_window_get_panel(ctx));
                            // if (nk_input_mouse_clicked(in, NK_BUTTON_LEFT, node.bounds()) &&
                            //     (it.prev == null || !nk_input_mouse_clicked(in, NK_BUTTON_LEFT, nk_layout_space_rect_to_screen(ctx, node.bounds()))) &&
                            //     nodedit.end != it)
                            // {
                            //     updated = it;
                            // }
                            
                            /* ================= NODE CONTENT =====================*/
                            nk_layout_row_dynamic(ctx, 25, 1);
                            nk_button_color(ctx, it.color);
                            it.color.r((byte) nk_propertyi(ctx, "#R:", 0, it.color.r() & 0xFF, 255, 1, 1));
                            it.color.g((byte) nk_propertyi(ctx, "#G:", 0, it.color.g() & 0xFF, 255, 1, 1));
                            it.color.b((byte) nk_propertyi(ctx, "#B:", 0, it.color.b() & 0xFF, 255, 1, 1));
                            it.color.a((byte) nk_propertyi(ctx, "#A:", 0, it.color.a() & 0xFF, 255, 1, 1));
                            /* ====================================================*/
                            nk_group_end(ctx);
                        }
                        // if (node != null)
                        // {
                        //     /* node connector and linking */
                        //     float  space;
                        //     NkRect bounds;
                        //     bounds = nk_layout_space_rect_to_local(ctx, node.bounds());
                        //     bounds.x(bounds.x() + nodedit.scrolling.x());
                        //     bounds.y(bounds.y() + nodedit.scrolling.y());
                        //     it.bounds = bounds;
                        //
                        //     /* output connector */
                        //     space = node.bounds().h() / (float) ((it.output_count) + 1);
                        //     for (int n = 0; n < it.output_count; ++n)
                        //     {
                        //         NkRect circle = NkRect.malloc(stack);
                        //         circle.x(node.bounds().x() + node.bounds().w() - 4);
                        //         circle.y(node.bounds().y() + space * (float) (n + 1));
                        //         circle.w(8);
                        //         circle.h(8);
                        //         nk_fill_circle(canvas, circle, nk_rgb(100, 100, 100, NkColor.malloc(stack)));
                        //
                        //         /* start linking process */
                        //         if (nk_input_has_mouse_click_down_in_rect(in, NK_BUTTON_LEFT, circle, true))
                        //         {
                        //             nodedit.linking.active     = true;
                        //             nodedit.linking.node       = it;
                        //             nodedit.linking.input_id   = it.ID;
                        //             nodedit.linking.input_slot = n;
                        //         }
                        //
                        //         /* draw curve from linked node slot to mouse position */
                        //         if (nodedit.linking.active && nodedit.linking.node == it &&
                        //             nodedit.linking.input_slot == n)
                        //         {
                        //             NkVec2 l0 = nk_vec2(circle.x() + 3, circle.y() + 3, NkVec2.malloc(stack));
                        //             NkVec2 l1 = in.mouse().pos();
                        //             nk_stroke_curve(canvas, l0.x(), l0.y(), l0.x() + 50.0f, l0.y(), l1.x() - 50.0f, l1.y(), l1.x(), l1.y(), 1.0f, nk_rgb(100, 100, 100, NkColor.malloc(stack)));
                        //         }
                        //     }
                        //
                        //     /* input connector */
                        //     space = node.bounds().h() / (float) ((it.input_count) + 1);
                        //     for (int n = 0; n < it.input_count; ++n)
                        //     {
                        //         NkRect circle = NkRect.malloc(stack);
                        //         circle.x(node.bounds().x() - 4);
                        //         circle.y(node.bounds().y() + space * (float) (n + 1));
                        //         circle.w(8);
                        //         circle.h(8);
                        //         nk_fill_circle(canvas, circle, nk_rgb(100, 100, 100, NkColor.malloc(stack)));
                        //         if (nk_input_is_mouse_released(in, NK_BUTTON_LEFT) &&
                        //             nk_input_is_mouse_hovering_rect(in, circle) &&
                        //             nodedit.linking.active && nodedit.linking.node != it)
                        //         {
                        //             nodedit.linking.active = false;
                        //             node_editor_link(nodedit, nodedit.linking.input_id,
                        //                              nodedit.linking.input_slot, it.ID, n);
                        //         }
                        //     }
                        // }
                        it = it.next;
                    }
                    
                    // /* reset linking connection */
                    // if (nodedit.linking.active && nk_input_is_mouse_released(in, NK_BUTTON_LEFT))
                    // {
                    //     nodedit.linking.active = false;
                    //     nodedit.linking.node   = null;
                    //     System.out.println("linking failed");
                    // }
                    
                    // /* draw each link */
                    // for (int n = 0; n < nodedit.link_count; ++n)
                    // {
                    //     NodeLink link   = nodedit.links[n];
                    //     Node     ni     = node_editor_find(nodedit, link.input_id);
                    //     Node     no     = node_editor_find(nodedit, link.output_id);
                    //     float    spacei = node.bounds().h() / (float) ((ni.output_count) + 1);
                    //     float    spaceo = node.bounds().h() / (float) ((no.input_count) + 1);
                    //     NkVec2   l0     = nk_layout_space_to_screen(ctx, nk_vec2(ni.bounds.x() + ni.bounds.w(), 3.0f + ni.bounds.y() + spacei * (float) (link.input_slot + 1), NkVec2.malloc(stack)));
                    //     NkVec2   l1     = nk_layout_space_to_screen(ctx, nk_vec2(no.bounds.x(), 3.0f + no.bounds.y() + spaceo * (float) (link.output_slot + 1), NkVec2.malloc(stack)));
                    //
                    //     l0.x(l0.x() - nodedit.scrolling.x());
                    //     l0.y(l0.y() - nodedit.scrolling.y());
                    //     l1.x(l1.x() - nodedit.scrolling.x());
                    //     l1.y(l1.y() - nodedit.scrolling.y());
                    //     nk_stroke_curve(canvas, l0.x(), l0.y(), l0.x() + 50.0f, l0.y(), l1.x() - 50.0f, l1.y(), l1.x(), l1.y(), 1.0f, nk_rgb(100, 100, 100, NkColor.malloc(stack)));
                    // }
                    
                    // if (updated != null)
                    // {
                    //     /* reshuffle nodes to have least recently selected node on top */
                    //     node_editor_pop(nodedit, updated);
                    //     node_editor_push(nodedit, updated);
                    // }
                    //
                    // /* node selection */
                    // if (nk_input_mouse_clicked(in, NK_BUTTON_LEFT, nk_layout_space_bounds(ctx, NkRect.malloc(stack))))
                    // {
                    //     it               = nodedit.begin;
                    //     nodedit.selected = null;
                    //     nk_rect(in.mouse().pos().x(), in.mouse().pos().y(), 100, 200, nodedit.bounds);
                    //     while (it != null)
                    //     {
                    //         NkRect b = nk_layout_space_rect_to_screen(ctx, it.bounds);
                    //         b.x(b.x() - nodedit.scrolling.x());
                    //         b.y(b.y() - nodedit.scrolling.y());
                    //         if (nk_input_is_mouse_hovering_rect(in, b)) nodedit.selected = it;
                    //         it = it.next;
                    //     }
                    // }
                    
                    /* contextual menu */
                    if (nk_contextual_begin(ctx, 0, nk_vec2(100, 220, NkVec2.malloc(stack)), nk_window_get_bounds(ctx, NkRect.malloc(stack))))
                    {
                        String[] grid_option = {"Show Grid", "Hide Grid"};
                        nk_layout_row_dynamic(ctx, 25, 1);
                        if (nk_contextual_item_label(ctx, "New", NK_TEXT_CENTERED))
                        {
                            node_editor_add(nodedit, "New", nk_rect(400, 260, 180, 220, NkRect.create()), nk_rgb(255, 255, 255, NkColor.create()), 1, 2);
                        }
                        if (nk_contextual_item_label(ctx, grid_option[nodedit.show_grid ? 1 : 0], NK_TEXT_CENTERED)) nodedit.show_grid = !nodedit.show_grid;
                        nk_contextual_end(ctx);
                    }
                }
                nk_layout_space_end(ctx);
                
                /* window content scrolling */
                if (nk_input_is_mouse_hovering_rect(in, nk_window_get_bounds(ctx, NkRect.malloc(stack))) &&
                    nk_input_is_mouse_down(in, NK_BUTTON_MIDDLE))
                {
                    nodedit.scrolling.x(nodedit.scrolling.x() - in.mouse().delta().x());
                    nodedit.scrolling.y(nodedit.scrolling.y() - in.mouse().delta().y());
                }
            }
            nk_end(ctx);
            return !nk_window_is_closed(ctx, "NodeEdit");
        }
    }
}
