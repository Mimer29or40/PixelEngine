package org.lwjgl.demo.nuklear;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;

public class Canvas
{
    /* nuklear - v1.05 - public domain */
    static class NkCanvas
    {
        NkCommandBuffer painter;
        NkVec2          item_spacing;
        NkVec2          panel_padding;
        NkStyleItem     window_background;
        
        NkCanvas(MemoryStack stack)
        {
            item_spacing      = NkVec2.malloc(stack);
            panel_padding     = NkVec2.malloc(stack);
            window_background = NkStyleItem.malloc(stack);
        }
    }
    
    boolean canvas_begin(NkContext ctx, NkCanvas canvas, int flags, int x, int y, int width, int height, NkColor background_color)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkVec2      vec2      = NkVec2.malloc(stack);
            NkStyleItem styleItem = NkStyleItem.malloc(stack);
            NkRect      rect      = NkRect.malloc(stack);
            
            /* save style properties which will be overwritten */
            canvas.panel_padding.set(ctx.style().window().padding());
            canvas.item_spacing.set(ctx.style().window().spacing());
            canvas.window_background.set(ctx.style().window().fixed_background());
            
            /* use the complete window space and set background */
            ctx.style().window().spacing().set(nk_vec2(0, 0, vec2));
            ctx.style().window().padding().set(nk_vec2(0, 0, vec2));
            ctx.style().window().fixed_background().set(nk_style_item_color(background_color, styleItem));
            
            /* create/update window and set position + size */
            if (!nk_begin(ctx, "Canvas", nk_rect(x, y, width, height, rect), NK_WINDOW_NO_SCROLLBAR | flags)) return false;
            
            /* allocate the complete window space for drawing */
            {
                NkRect total_space = nk_window_get_content_region(ctx, rect);
                nk_layout_row_dynamic(ctx, total_space.h(), 1);
                nk_widget(total_space, ctx);
                canvas.painter = nk_window_get_canvas(ctx);
            }
        }
        return true;
    }
    
    void canvas_end(NkContext ctx, NkCanvas canvas)
    {
        nk_end(ctx);
        ctx.style().window().spacing().set(canvas.panel_padding);
        ctx.style().window().padding().set(canvas.item_spacing);
        ctx.style().window().fixed_background().set(canvas.window_background);
    }
    
    
    void layout(NkContext ctx, int windowX, int windowY)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // NkColor color  = NkColor.malloc(stack);
            // NkColor color1 = NkColor.malloc(stack);
            // NkRect  rect   = NkRect.malloc(stack);
            
            NkCanvas canvas = new NkCanvas(stack);
            int      flags  = NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_CLOSABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE;
            if (canvas_begin(ctx, canvas, flags, windowX, windowY, 500, 550, nk_rgb(250, 250, 250, NkColor.malloc(stack))))
            {
                float x = canvas.painter.clip().x(), y = canvas.painter.clip().y();
                
                nk_fill_rect(canvas.painter, nk_rect(x + 15, y + 15, 210, 210, NkRect.malloc(stack)), 5, nk_rgb(247, 230, 154, NkColor.malloc(stack)));
                nk_fill_rect(canvas.painter, nk_rect(x + 20, y + 20, 200, 200, NkRect.malloc(stack)), 5, nk_rgb(188, 174, 118, NkColor.malloc(stack)));
                nk_draw_text(canvas.painter, nk_rect(x + 30, y + 30, 150, 20, NkRect.malloc(stack)), "Text to draw", ctx.style().font(), nk_rgb(188, 174, 118, NkColor.malloc(stack)), nk_rgb(0, 0, 0, NkColor.malloc(stack)));
                nk_fill_rect(canvas.painter, nk_rect(x + 250, y + 20, 100, 100, NkRect.malloc(stack)), 0, nk_rgb(0, 0, 255, NkColor.malloc(stack)));
                nk_fill_circle(canvas.painter, nk_rect(x + 20, y + 250, 100, 100, NkRect.malloc(stack)), nk_rgb(255, 0, 0, NkColor.malloc(stack)));
                nk_fill_triangle(canvas.painter, x + 250, y + 250, x + 350, y + 250, x + 300, y + 350, nk_rgb(0, 255, 0, NkColor.malloc(stack)));
                nk_fill_arc(canvas.painter, x + 300, y + 420, 50, 0, 3.141592654f * 3.0f / 4.0f, nk_rgb(255, 255, 0, NkColor.malloc(stack)));
                
                {
                    float[] points = {
                            x + 200,
                            y + 250,
                            x + 250,
                            y + 350,
                            x + 225,
                            y + 350,
                            x + 200,
                            y + 300,
                            x + 175,
                            y + 350,
                            x + 150,
                            y + 350,
                            };
                    nk_fill_polygon(canvas.painter, points, nk_rgb(0, 0, 0, NkColor.malloc(stack)));
                }
                
                {
                    float[] points = {
                            x + 200,
                            y + 370,
                            x + 250,
                            y + 470,
                            x + 225,
                            y + 470,
                            x + 200,
                            y + 420,
                            x + 175,
                            y + 470,
                            x + 150,
                            y + 470
                    };
                    nk_stroke_polygon(canvas.painter, points, 4, nk_rgb(0, 0, 0, NkColor.malloc(stack)));
                }
                
                {
                    float[] points = {
                            x + 250,
                            y + 200,
                            x + 275,
                            y + 220,
                            x + 325,
                            y + 170,
                            x + 350,
                            y + 200
                    };
                    nk_stroke_polyline(canvas.painter, points, 2, nk_rgb(255, 128, 0, NkColor.malloc(stack)));
                }
                
                nk_stroke_line(canvas.painter, x + 15, y + 10, x + 200, y + 10, 2.0f, nk_rgb(189, 45, 75, NkColor.malloc(stack)));
                nk_stroke_rect(canvas.painter, nk_rect(x + 370, y + 20, 100, 100, NkRect.malloc(stack)), 10, 3, nk_rgb(0, 0, 255, NkColor.malloc(stack)));
                nk_stroke_curve(canvas.painter, x + 380, y + 200, x + 405, y + 270, x + 455, y + 120, x + 480, y + 200, 2, nk_rgb(0, 150, 220, NkColor.malloc(stack)));
                nk_stroke_circle(canvas.painter, nk_rect(x + 20, y + 370, 100, 100, NkRect.malloc(stack)), 5, nk_rgb(0, 255, 120, NkColor.malloc(stack)));
                nk_stroke_triangle(canvas.painter, x + 370, y + 250, x + 470, y + 250, x + 420, y + 350, 6, nk_rgb(255, 0, 143, NkColor.malloc(stack)));
                nk_stroke_arc(canvas.painter, x + 420, y + 420, 50, 0, 3.141592654f * 3.0f / 4.0f, 5, nk_rgb(0, 255, 255, NkColor.malloc(stack)));
            }
            canvas_end(ctx, canvas);
        }
    }
}
