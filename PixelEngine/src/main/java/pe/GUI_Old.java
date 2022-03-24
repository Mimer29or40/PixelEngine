package pe;

import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.event.*;
import pe.render.GL;
import rutils.IOUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13C.glActiveTexture;
import static org.lwjgl.opengl.GL14C.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL14C.glBlendEquation;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public final class GUI_Old
{
    static NkAllocator                      allocator;
    static NkDrawVertexLayoutElement.Buffer vertex;
    
    static ByteBuffer vertexBuffer;
    static ByteBuffer elementBuffer;
    
    static NkContext         ctx;
    static NkBuffer          cmds;
    static NkUserFont        font;
    static NkDrawNullTexture texture;
    
    static ByteBuffer ttf;
    
    static int BITMAP_W = 1024;
    static int BITMAP_H = 1024;
    
    static int FONT_HEIGHT = 18;
    static int fontTexID   = glGenTextures();
    
    static STBTTFontinfo          fontInfo = STBTTFontinfo.create();
    static STBTTPackedchar.Buffer cdata    = STBTTPackedchar.create(95);
    
    static float scale;
    static float descent;
    
    static void setup()
    {
        GUI_Old.allocator = NkAllocator.create();
        GUI_Old.allocator.alloc(GUI_Old::alloc);
        GUI_Old.allocator.mfree(GUI_Old::memFree);
    
        GUI_Old.vertex = NkDrawVertexLayoutElement.create(4);
        GUI_Old.vertex.position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0);
        GUI_Old.vertex.position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8);
        GUI_Old.vertex.position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16);
        GUI_Old.vertex.position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0);
        GUI_Old.vertex.flip();
    
        GUI_Old.vertexBuffer  = MemoryUtil.memCalloc(512 * 1024);
        GUI_Old.elementBuffer = MemoryUtil.memCalloc(128 * 1024);
    
        GUI_Old.ctx = NkContext.create();
        
        nk_init(GUI_Old.ctx, GUI_Old.allocator, null);
        
        GUI_Old.ctx.clip().copy(GUI_Old::copy).paste(GUI_Old::paste);
    
        GUI_Old.cmds = NkBuffer.create();
        
        nk_buffer_init(GUI_Old.cmds, GUI_Old.allocator, 4 * 1024);
        
        ttf = IOUtil.readFromFile("demo/FiraSans-Regular.ttf");
        
        try (MemoryStack stack = stackPush())
        {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
            
            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;
            
            ByteBuffer bitmap = memAlloc(BITMAP_W * BITMAP_H);
            
            STBTTPackContext pc = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);
            
            // Convert R8 to RGBA8
            ByteBuffer texture = memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++)
            {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();
            
            glBindTexture(GL_TEXTURE_2D, fontTexID);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL_RGBA, GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            
            MemoryUtil.memFree(texture);
            MemoryUtil.memFree(bitmap);
        }
    
        GUI_Old.font = NkUserFont.create();
        GUI_Old.font.width(GUI_Old::width);
        GUI_Old.font.height(FONT_HEIGHT);
        GUI_Old.font.query(GUI_Old::query);
        GUI_Old.font.texture(GUI_Old::texture);
        
        nk_style_set_font(GUI_Old.ctx, GUI_Old.font);
    
        GUI_Old.texture = NkDrawNullTexture.create();
        GUI_Old.texture.texture().id(GL.defaultTexture().id());
        GUI_Old.texture.uv().set(0.5F, 0.5F);
        
        // This is here because sometimes the mouse started grabbed.
        nk_input_begin(GUI_Old.ctx);
        nk_input_end(GUI_Old.ctx);
    }
    
    private static long alloc(long handle, long old, long size)
    {
        return MemoryUtil.nmemAllocChecked(size);
    }
    
    private static void memFree(long handle, long ptr)
    {
        MemoryUtil.nmemFree(ptr);
    }
    
    private static void copy(long handle, long text, int len)
    {
        if (len == 0) return;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer str = stack.malloc(len + 1);
            MemoryUtil.memCopy(text, MemoryUtil.memAddress(str), len);
            str.put(len, (byte) 0);
            
            Window.setClipboard(str);
        }
    }
    
    private static void paste(long handle, long edit)
    {
        long text = Window.getClipboardRaw();
        if (text != MemoryUtil.NULL) nnk_textedit_paste(edit, text, nnk_strlen(text));
    }
    
    private static float width(long handle, float h, long text, int len)
    {
        float text_width = 0;
        try (MemoryStack stack = stackPush())
        {
            IntBuffer unicode = stack.mallocInt(1);
            
            int glyph_len = nnk_utf_decode(text, memAddress(unicode), len);
            int text_len  = glyph_len;
            
            if (glyph_len == 0)
            {
                return 0;
            }
            
            IntBuffer advance = stack.mallocInt(1);
            while (text_len <= len && glyph_len != 0)
            {
                if (unicode.get(0) == NK_UTF_INVALID)
                {
                    break;
                }
                
                /* query currently drawn glyph information */
                stbtt_GetCodepointHMetrics(fontInfo, unicode.get(0), advance, null);
                text_width += advance.get(0) * scale;
                
                /* offset next glyph */
                glyph_len = nnk_utf_decode(text + text_len, memAddress(unicode), len - text_len);
                text_len += glyph_len;
            }
        }
        return text_width;
    }
    
    private static void query(long handle, float h, long glyph, int codePoint, int nextCodePoint)
    {
        try (MemoryStack stack = stackPush())
        {
            FloatBuffer x = stack.floats(0.0f);
            FloatBuffer y = stack.floats(0.0f);
            
            STBTTAlignedQuad q       = STBTTAlignedQuad.malloc(stack);
            IntBuffer        advance = stack.mallocInt(1);
            
            stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codePoint - 32, x, y, q, false);
            stbtt_GetCodepointHMetrics(fontInfo, codePoint, advance, null);
            
            NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
            
            ufg.width(q.x1() - q.x0());
            ufg.height(q.y1() - q.y0());
            ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
            ufg.xadvance(advance.get(0) * scale);
            ufg.uv(0).set(q.s0(), q.t0());
            ufg.uv(1).set(q.s1(), q.t1());
        }
    }
    
    private static void texture(NkHandle handle)
    {
        handle.id(fontTexID);
    }
    
    static void destroy()
    {
        GUI_Old.texture.free();
        GUI_Old.font.free();
        GUI_Old.cmds.free();
        GUI_Old.ctx.free();
        
        GUI_Old.allocator.free();
        GUI_Old.vertex.free();
    }
    
    static void handleEvents()
    {
        nk_input_begin(GUI_Old.ctx);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (Event event : Engine.Events.get())
            {
                if (event instanceof EventMouseMoved mMoved)
                {
                    nk_input_motion(GUI_Old.ctx, (int) mMoved.x(), (int) mMoved.y());
                }
                else if (event instanceof EventMouseScrolled mScrolled)
                {
                    NkVec2 scroll = NkVec2.malloc(stack).x((float) mScrolled.dx()).y((float) mScrolled.dx());
                    nk_input_scroll(GUI_Old.ctx, scroll);
                }
                else if (event instanceof EventMouseButtonDown mbDown)
                {
                    mouseButtonInput(mbDown.button(), mbDown.x(), mbDown.y(), true);
                    if (mbDown.downCount() > 1)
                    {
                        nk_input_button(GUI_Old.ctx, NK_BUTTON_DOUBLE, (int) mbDown.x(), (int) mbDown.y(), true);
                    }
                }
                else if (event instanceof EventMouseButtonUp mbUp)
                {
                    mouseButtonInput(mbUp.button(), mbUp.x(), mbUp.y(), false);
                    nk_input_button(GUI_Old.ctx, NK_BUTTON_DOUBLE, (int) mbUp.x(), (int) mbUp.y(), false);
                }
                else if (event instanceof EventKeyboardKeyDown kkDown)
                {
                    keyboardKeyInput(kkDown.key(), true);
                }
                else if (event instanceof EventKeyboardKeyUp kkUp)
                {
                    keyboardKeyInput(kkUp.key(), false);
                }
                else if (event instanceof EventKeyboardTyped kTyped)
                {
                    nk_input_unicode(GUI_Old.ctx, kTyped.codePoint());
                }
            }
        }
        
        NkMouse mouse = GUI_Old.ctx.input().mouse();
        if (mouse.grab())
        {
            Mouse.hide();
        }
        else if (mouse.grabbed())
        {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            Mouse.pos(prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        }
        else if (mouse.ungrab())
        {
            Mouse.show();
        }
        
        nk_input_end(GUI_Old.ctx);
    }
    
    private static void mouseButtonInput(Mouse.Button button, double x, double y, boolean down)
    {
        switch (button)
        {
            case LEFT -> nk_input_button(GUI_Old.ctx, NK_BUTTON_LEFT, (int) x, (int) y, down);
            case RIGHT -> nk_input_button(GUI_Old.ctx, NK_BUTTON_RIGHT, (int) x, (int) y, down);
            case MIDDLE -> nk_input_button(GUI_Old.ctx, NK_BUTTON_MIDDLE, (int) x, (int) y, down);
        }
    }
    
    private static void keyboardKeyInput(Keyboard.Key key, boolean down)
    {
        // NK_KEY_TEXT_INSERT_MODE
        // NK_KEY_TEXT_REPLACE_MODE
        // NK_KEY_TEXT_RESET_MODE
        
        switch (key)
        {
            case L_SHIFT, R_SHIFT -> nk_input_key(GUI_Old.ctx, NK_KEY_SHIFT, down);
            case L_CONTROL, R_CONTROL -> nk_input_key(GUI_Old.ctx, NK_KEY_CTRL, down);
            case DELETE -> nk_input_key(GUI_Old.ctx, NK_KEY_DEL, down);
            case ENTER, KP_ENTER -> nk_input_key(GUI_Old.ctx, NK_KEY_ENTER, down);
            case TAB -> nk_input_key(GUI_Old.ctx, NK_KEY_TAB, down);
            case BACKSPACE -> nk_input_key(GUI_Old.ctx, NK_KEY_BACKSPACE, down);
            case UP -> nk_input_key(GUI_Old.ctx, NK_KEY_UP, down);
            case DOWN -> nk_input_key(GUI_Old.ctx, NK_KEY_DOWN, down);
            case LEFT -> nk_input_key(ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_LEFT : NK_KEY_LEFT, down);
            case RIGHT -> nk_input_key(GUI_Old.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_RIGHT : NK_KEY_RIGHT, down);
            case HOME -> {
                nk_input_key(GUI_Old.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_START : NK_KEY_TEXT_LINE_START, down);
                nk_input_key(GUI_Old.ctx, NK_KEY_SCROLL_START, down);
            }
            case END -> {
                nk_input_key(GUI_Old.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_END : NK_KEY_TEXT_LINE_END, down);
                nk_input_key(GUI_Old.ctx, NK_KEY_SCROLL_END, down);
            }
            case PAGE_UP -> nk_input_key(GUI_Old.ctx, NK_KEY_SCROLL_UP, down);
            case PAGE_DOWN -> nk_input_key(GUI_Old.ctx, NK_KEY_SCROLL_DOWN, down);
            case C -> nk_input_key(GUI_Old.ctx, NK_KEY_COPY, Modifier.only(Modifier.CONTROL) && down);
            case X -> nk_input_key(GUI_Old.ctx, NK_KEY_CUT, Modifier.only(Modifier.CONTROL) && down);
            case V -> nk_input_key(GUI_Old.ctx, NK_KEY_PASTE, Modifier.only(Modifier.CONTROL) && down);
            case Z -> nk_input_key(GUI_Old.ctx, NK_KEY_TEXT_UNDO, Modifier.only(Modifier.CONTROL) && down);
            case Y -> nk_input_key(GUI_Old.ctx, NK_KEY_TEXT_REDO, Modifier.only(Modifier.CONTROL) && down);
            case A -> nk_input_key(GUI_Old.ctx, NK_KEY_TEXT_SELECT_ALL, Modifier.only(Modifier.CONTROL) && down);
        }
    }
    
    static int EASY = 0;
    static int HARD = 1;
    
    static NkColorf background = NkColorf.create().r(0.10f).g(0.18f).b(0.24f).a(1.0f);
    
    static int op = EASY;
    
    static IntBuffer compression = BufferUtils.createIntBuffer(1).put(0, 20);
    
    private static void testDraw()
    {
        int x = 50;
        int y = 50;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkRect rect = nk_rect(x, y, 230, 250, NkRect.malloc(stack));
            
            int flags = NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE;
            if (nk_begin(ctx, "Demo", rect, flags))
            {
                nk_layout_row_static(ctx, 30, 80, 1);
                if (nk_button_label(ctx, "button")) System.out.println("button pressed");
                
                nk_layout_row_dynamic(ctx, 30, 2);
                if (nk_option_label(ctx, "easy", op == EASY)) op = EASY;
                if (nk_option_label(ctx, "hard", op == HARD)) op = HARD;
                
                nk_layout_row_dynamic(ctx, 25, 1);
                nk_property_int(ctx, "Compression:", 0, compression, 100, 10, 1);
                
                nk_layout_row_dynamic(ctx, 20, 1);
                nk_label(ctx, "background:", NK_TEXT_LEFT);
                nk_layout_row_dynamic(ctx, 25, 1);
                if (nk_combo_begin_color(ctx, nk_rgb_cf(background, NkColor.malloc(stack)), NkVec2.malloc(stack).set(nk_widget_width(ctx), 400)))
                {
                    nk_layout_row_dynamic(ctx, 120, 1);
                    nk_color_picker(ctx, background, NK_RGBA);
                    nk_layout_row_dynamic(ctx, 25, 1);
                    background.r(nk_propertyf(ctx, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
                              .g(nk_propertyf(ctx, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
                              .b(nk_propertyf(ctx, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
                              .a(nk_propertyf(ctx, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
                    nk_combo_end(ctx);
                }
            }
            nk_end(ctx);
        }
    }
    
    static void draw()
    {
        testDraw();
        
        // vertexBuffer();
        // drawCommands();
        // Engine.stop();
    }
    
    private static void vertexBuffer()
    {
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glActiveTexture(GL_TEXTURE0);
        
        vertexBuffer.clear();
        elementBuffer.clear();
        // convert from command queue into draw list and draw to screen
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // fill convert configuration
            NkConvertConfig config = NkConvertConfig.calloc(stack)
                                                    .vertex_layout(GUI_Old.vertex)
                                                    .vertex_size(20)
                                                    .vertex_alignment(4)
                                                    .null_texture(GUI_Old.texture)
                                                    .circle_segment_count(22)
                                                    .curve_segment_count(22)
                                                    .arc_segment_count(22)
                                                    .global_alpha(1.0f)
                                                    .shape_AA(NK_ANTI_ALIASING_ON)
                                                    .line_AA(NK_ANTI_ALIASING_ON);
            
            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.malloc(stack);
            NkBuffer ebuf = NkBuffer.malloc(stack);
            
            nk_buffer_init_fixed(vbuf, GUI_Old.vertexBuffer);
            nk_buffer_init_fixed(ebuf, GUI_Old.elementBuffer);
            nk_convert(ctx, cmds, vbuf, ebuf, config);
        }
        
        // GLBatch batch = GLBatch.get();
        
        float fb_scale_x = (float) Window.framebufferWidth() / (float) Window.width();
        float fb_scale_y = (float) Window.framebufferHeight() / (float) Window.height();
        
        // iterate over and execute each draw command
        long offset = MemoryUtil.NULL;
        for (NkDrawCommand cmd = nk__draw_begin(GUI_Old.ctx, GUI_Old.cmds);
             cmd != null;
             cmd = nk__draw_next(cmd, GUI_Old.cmds, GUI_Old.ctx))
        {
            if (cmd.elem_count() == 0) continue;
            
            // batch.checkBuffer(cmd.elem_count() * 3);
            //
            // batch.begin(DrawMode.TRIANGLES);
            // batch.setTexture(cmd.texture().id());
            //
            // // System.out.printf("elem_count=%s, rect=[x=%s, y=%s, w=%s, h=%s]%n",
            // //                   cmd.elem_count(),
            // //                   cmd.clip_rect().x(),
            // //                   cmd.clip_rect().y(),
            // //                   cmd.clip_rect().w(),
            // //                   cmd.clip_rect().h()
            // //                  );
            // elementBuffer.position((int) offset);
            // for (int i = 0, n = cmd.elem_count(); i < n; i++)
            // {
            //     int element = elementBuffer.getShort() & 0xFFFF;
            //
            //     vertexBuffer.position(element * 20);
            //     float x = vertexBuffer.getFloat();
            //     float y = vertexBuffer.getFloat();
            //     float u = vertexBuffer.getFloat();
            //     float v = vertexBuffer.getFloat();
            //     int   r = vertexBuffer.get() & 0xFF;
            //     int   g = vertexBuffer.get() & 0xFF;
            //     int   b = vertexBuffer.get() & 0xFF;
            //     int   a = vertexBuffer.get() & 0xFF;
            //
            //     // System.out.printf("element=%s Vertex[pos=(%.3f,%.3f), uv=(%.3f,%.3f), color=(%s,%s,%s,%s)]%n",
            //     //                   element,
            //     //                   x, y,
            //     //                   u, v,
            //     //                   r, g, b, a);
            //
            //     batch.pos(x, y);
            //     batch.texCoord(u, v);
            //     batch.color(r, g, b, a);
            // }
            // vertexBuffer.clear();
            // elementBuffer.clear();
            //
            // batch.end();
            glBindTexture(GL_TEXTURE_2D, cmd.texture().id());
            glScissor(
                    (int) (cmd.clip_rect().x() * fb_scale_x),
                    (int) ((Window.height() - (int) (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y),
                    (int) (cmd.clip_rect().w() * fb_scale_x),
                    (int) (cmd.clip_rect().h() * fb_scale_y)
                     );
            glDrawElements(GL_TRIANGLES, cmd.elem_count(), GL_UNSIGNED_SHORT, offset);
            
            offset += Integer.toUnsignedLong(cmd.elem_count() * Short.BYTES);
        }
        
        nk_clear(GUI_Old.ctx);
        nk_buffer_clear(GUI_Old.cmds);
        
        glDisable(GL_SCISSOR_TEST);
    }
    
    private static void drawCommands()
    {
        for (NkCommand cmd = nk__begin(ctx); cmd != null; cmd = nk__next(ctx, cmd))
        {
            switch (cmd.type())
            {
                case NK_COMMAND_NOP -> System.out.println("NK_COMMAND_NOP");
                case NK_COMMAND_SCISSOR -> System.out.println("NK_COMMAND_SCISSOR");
                case NK_COMMAND_LINE -> System.out.println("NK_COMMAND_LINE");
                case NK_COMMAND_CURVE -> System.out.println("NK_COMMAND_CURVE");
                case NK_COMMAND_RECT -> System.out.println("NK_COMMAND_RECT");
                case NK_COMMAND_RECT_FILLED -> System.out.println("NK_COMMAND_RECT_FILLED");
                case NK_COMMAND_RECT_MULTI_COLOR -> System.out.println("NK_COMMAND_RECT_MULTI_COLOR");
                case NK_COMMAND_CIRCLE -> System.out.println("NK_COMMAND_CIRCLE");
                case NK_COMMAND_CIRCLE_FILLED -> System.out.println("NK_COMMAND_CIRCLE_FILLED");
                case NK_COMMAND_ARC -> System.out.println("NK_COMMAND_ARC");
                case NK_COMMAND_ARC_FILLED -> System.out.println("NK_COMMAND_ARC_FILLED");
                case NK_COMMAND_TRIANGLE -> System.out.println("NK_COMMAND_TRIANGLE");
                case NK_COMMAND_TRIANGLE_FILLED -> System.out.println("NK_COMMAND_TRIANGLE_FILLED");
                case NK_COMMAND_POLYGON -> System.out.println("NK_COMMAND_POLYGON");
                case NK_COMMAND_POLYGON_FILLED -> System.out.println("NK_COMMAND_POLYGON_FILLED");
                case NK_COMMAND_POLYLINE -> System.out.println("NK_COMMAND_POLYLINE");
                case NK_COMMAND_TEXT -> System.out.println("NK_COMMAND_TEXT");
                case NK_COMMAND_IMAGE -> System.out.println("NK_COMMAND_IMAGE");
                case NK_COMMAND_CUSTOM -> System.out.println("NK_COMMAND_CUSTOM");
                // case NK_COMMAND_LINE:
                //     your_draw_line_function(...)
                //     break;
                // case NK_COMMAND_RECT
                //         your_draw_rect_function(...)
                //     break;
                // case ...:
                //     // [...]
            }
        }
        nk_clear(ctx);
    }
}
