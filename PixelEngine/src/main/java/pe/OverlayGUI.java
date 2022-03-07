package pe;

import org.joml.Matrix4d;
import org.lwjgl.BufferUtils;
import org.lwjgl.nuklear.*;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.BlendMode;
import pe.event.*;
import pe.render.*;
import rutils.IOUtil;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Objects;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.nuklear.Nuklear.nk_layout_row_dynamic;
import static org.lwjgl.stb.STBTruetype.*;

public class OverlayGUI
{
    private static final Logger LOGGER = new Logger();
    
    private static GLProgram     program;
    private static ByteBuffer    vertexBuffer;
    private static ByteBuffer    elementBuffer;
    private static GLVertexArray vertexArray;
    private static Matrix4d      pv;
    
    private static NkAllocator allocator;
    
    private static NkDrawVertexLayoutElement.Buffer vertexLayout;
    private static NkDrawNullTexture                nullTexture;
    private static NkConvertConfig                  config;
    private static NkBuffer                         commandBuffer;
    
    private static NkContext  context;
    private static NkUserFont defaultFont;
    
    static ByteBuffer ttf;
    
    static int BITMAP_W = 1024;
    static int BITMAP_H = 1024;
    
    static int FONT_HEIGHT = 18;
    static int fontTexID   = GL33.glGenTextures();
    
    static STBTTFontinfo          fontInfo = STBTTFontinfo.create();
    static STBTTPackedchar.Buffer cdata    = STBTTPackedchar.create(95);
    
    static float scale;
    static float descent;
    
    static final int EASY = 0;
    static final int HARD = 1;
    
    static final NkColorf background = NkColorf.create().r(0.10f).g(0.18f).b(0.24f).a(1.0f);
    
    static int op = EASY;
    
    static final IntBuffer compression = BufferUtils.createIntBuffer(1).put(0, 20);
    
    static void setup()
    {
        OverlayGUI.LOGGER.fine("Setup");
        
        String vert = """
                      #version 330
                      layout(location = 0) in vec2 aPos;
                      layout(location = 1) in vec2 aTex;
                      layout(location = 2) in vec4 aCol;
                      uniform mat4 pv;
                      out vec2 uv;
                      out vec4 color;
                      void main(void) {
                          uv = aTex;
                          color = aCol;
                          gl_Position = pv * vec4(aPos, 0.0, 1.0);
                      }
                      """;
        String frag = """
                      #version 330
                      uniform sampler2D tex;
                      in vec2 uv;
                      in vec4 color;
                      out vec4 FragColor;
                      void main(void) {
                          FragColor = color * texture(tex, uv);
                      }
                      """;
        OverlayGUI.program = GLProgram.loadFromCode(vert, null, frag);
        
        GLProgram.Uniform.int1("tex", 0);
        
        int vertexSize  = Float.BYTES * 2 + Float.BYTES * 2 + Byte.BYTES * 4;
        int elementSize = Short.BYTES;
        
        OverlayGUI.vertexBuffer  = MemoryUtil.memAlloc((1 << 15) * vertexSize);
        OverlayGUI.elementBuffer = MemoryUtil.memAlloc((1 << 15) * elementSize);
        
        OverlayGUI.vertexArray = GLVertexArray.builder()
                                              .buffer(OverlayGUI.vertexBuffer, Usage.DYNAMIC_DRAW,
                                                      new GLAttribute(GLType.FLOAT, 2, false),
                                                      new GLAttribute(GLType.FLOAT, 2, false),
                                                      new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                              .indexBuffer(OverlayGUI.elementBuffer, Usage.DYNAMIC_DRAW, GLType.UNSIGNED_SHORT)
                                              .build();
        
        OverlayGUI.pv = new Matrix4d();
        
        OverlayGUI.allocator = NkAllocator.calloc();
        OverlayGUI.allocator.alloc((handle, old, size) -> MemoryUtil.nmemAllocChecked(size))
                            .mfree((handle, ptr) -> MemoryUtil.nmemFree(ptr));
        
        OverlayGUI.vertexLayout = NkDrawVertexLayoutElement.calloc(4);
        OverlayGUI.vertexLayout.position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                               .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                               .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                               .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                               .flip();
        
        OverlayGUI.nullTexture = NkDrawNullTexture.calloc();
        OverlayGUI.nullTexture.texture().id(GLTexture.getDefault().id());
        OverlayGUI.nullTexture.uv().set(0.5f, 0.5f);
        
        OverlayGUI.config = NkConvertConfig.calloc();
        OverlayGUI.config.vertex_layout(OverlayGUI.vertexLayout)
                         .vertex_size(20)
                         .vertex_alignment(4)
                         .null_texture(OverlayGUI.nullTexture)
                         .circle_segment_count(22) // TODO - Have setter methods
                         .curve_segment_count(22) // TODO - Have setter methods
                         .arc_segment_count(22) // TODO - Have setter methods
                         .global_alpha(1.0f) // TODO - Have setter methods
                         .shape_AA(NK_ANTI_ALIASING_ON) // TODO - Have setter methods
                         .line_AA(NK_ANTI_ALIASING_ON); // TODO - Have setter methods
        
        OverlayGUI.commandBuffer = NkBuffer.calloc();
        nk_buffer_init(OverlayGUI.commandBuffer, OverlayGUI.allocator, 4 * 1024);
        
        ttf = IOUtil.readFromFile("demo/FiraSans-Regular.ttf");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            stbtt_InitFont(fontInfo, ttf);
            scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_HEIGHT);
            
            IntBuffer d = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, null, d, null);
            descent = d.get(0) * scale;
            
            ByteBuffer bitmap = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H);
            
            STBTTPackContext pc = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pc, bitmap, BITMAP_W, BITMAP_H, 0, 1, MemoryUtil.NULL);
            stbtt_PackSetOversampling(pc, 4, 4);
            stbtt_PackFontRange(pc, ttf, 0, FONT_HEIGHT, 32, cdata);
            stbtt_PackEnd(pc);
            
            // Convert R8 to RGBA8
            ByteBuffer texture = MemoryUtil.memAlloc(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < bitmap.capacity(); i++)
            {
                texture.putInt((bitmap.get(i) << 24) | 0x00FFFFFF);
            }
            texture.flip();
            
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, fontTexID);
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA8, BITMAP_W, BITMAP_H, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, texture);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
            GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
            
            MemoryUtil.memFree(texture);
            MemoryUtil.memFree(bitmap);
        }
        
        OverlayGUI.defaultFont = NkUserFont.calloc();
        OverlayGUI.defaultFont.width((handle, h, text, len) -> {
            float text_width = 0;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer unicode = stack.mallocInt(1);
                
                int glyph_len = nnk_utf_decode(text, MemoryUtil.memAddress(unicode), len);
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
                    glyph_len = nnk_utf_decode(text + text_len, MemoryUtil.memAddress(unicode), len - text_len);
                    text_len += glyph_len;
                }
            }
            return text_width;
        });
        OverlayGUI.defaultFont.height(FONT_HEIGHT);
        OverlayGUI.defaultFont.query((handle, font_height, glyph, codepoint, next_codepoint) -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                FloatBuffer x = stack.floats(0.0f);
                FloatBuffer y = stack.floats(0.0f);
                
                STBTTAlignedQuad q       = STBTTAlignedQuad.malloc(stack);
                IntBuffer        advance = stack.mallocInt(1);
                
                stbtt_GetPackedQuad(cdata, BITMAP_W, BITMAP_H, codepoint - 32, x, y, q, false);
                stbtt_GetCodepointHMetrics(fontInfo, codepoint, advance, null);
                
                NkUserFontGlyph ufg = NkUserFontGlyph.create(glyph);
                
                ufg.width(q.x1() - q.x0());
                ufg.height(q.y1() - q.y0());
                ufg.offset().set(q.x0(), q.y0() + (FONT_HEIGHT + descent));
                ufg.xadvance(advance.get(0) * scale);
                ufg.uv(0).set(q.s0(), q.t0());
                ufg.uv(1).set(q.s1(), q.t1());
            }
        });
        OverlayGUI.defaultFont.texture(it -> it.id(fontTexID));
        
        OverlayGUI.context = NkContext.calloc();
        
        nk_init(OverlayGUI.context, OverlayGUI.allocator, OverlayGUI.defaultFont);
        OverlayGUI.context.clip().copy((handle, text, len) -> {
            if (len == 0) return;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer str = stack.malloc(len + 1);
                MemoryUtil.memCopy(text, MemoryUtil.memAddress(str), len);
                str.put(len, (byte) 0);
                
                Window.setClipboard(str);
            }
        });
        OverlayGUI.context.clip().paste((handle, edit) -> {
            long text = Window.getClipboardRaw();
            if (text != MemoryUtil.NULL)
            {
                nnk_textedit_paste(edit, text, nnk_strlen(text));
            }
        });
    }
    
    static void destroy()
    {
        OverlayGUI.LOGGER.fine("Destroy");
        
        OverlayGUI.context.free();
        
        OverlayGUI.defaultFont.free();
        
        OverlayGUI.commandBuffer.free();
        OverlayGUI.config.free();
        OverlayGUI.nullTexture.free();
        OverlayGUI.vertexLayout.free();
        
        OverlayGUI.allocator.free();
        
        OverlayGUI.vertexArray.delete();
        
        MemoryUtil.memFree(OverlayGUI.vertexBuffer);
        MemoryUtil.memFree(OverlayGUI.elementBuffer);
        
        OverlayGUI.program.delete();
    }
    
    static void handleEvents()
    {
        nk_input_begin(OverlayGUI.context);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (Event event : Engine.Events.get())
            {
                if (event instanceof EventMouseMoved)
                {
                    nk_input_motion(OverlayGUI.context, (int) Mouse.absX(), (int) Mouse.absY());
                }
                else if (event instanceof EventMouseScrolled)
                {
                    NkVec2 scroll = NkVec2.malloc(stack).x((float) Mouse.scrollX()).y((float) Mouse.scrollX());
                    nk_input_scroll(OverlayGUI.context, scroll);
                }
                else if (event instanceof EventMouseButtonDown mbDown)
                {
                    mouseButtonInput(mbDown.button(), Mouse.absX(), Mouse.absY(), true);
                }
                else if (event instanceof EventMouseButtonUp mbUp)
                {
                    mouseButtonInput(mbUp.button(), Mouse.absX(), Mouse.absY(), false);
                    nk_input_button(OverlayGUI.context, NK_BUTTON_DOUBLE, (int) Mouse.absX(), (int) Mouse.absY(), false);
                }
                else if (event instanceof EventMouseButtonPressed mbPressed)
                {
                    if (mbPressed.doublePressed())
                    {
                        nk_input_button(OverlayGUI.context, NK_BUTTON_DOUBLE, (int) Mouse.absX(), (int) Mouse.absY(), true);
                    }
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
                    nk_input_unicode(OverlayGUI.context, kTyped.codePoint());
                }
            }
        }
        
        // TODO - Mouse Position needs to be transformed before its passed
        
        NkMouse mouse = OverlayGUI.context.input().mouse();
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
        
        nk_input_end(OverlayGUI.context);
    }
    
    private static void mouseButtonInput(Mouse.Button button, double x, double y, boolean down)
    {
        switch (button)
        {
            case LEFT -> nk_input_button(OverlayGUI.context, NK_BUTTON_LEFT, (int) x, (int) y, down);
            case RIGHT -> nk_input_button(OverlayGUI.context, NK_BUTTON_RIGHT, (int) x, (int) y, down);
            case MIDDLE -> nk_input_button(OverlayGUI.context, NK_BUTTON_MIDDLE, (int) x, (int) y, down);
        }
    }
    
    private static void keyboardKeyInput(Keyboard.Key key, boolean down)
    {
        // NK_KEY_TEXT_INSERT_MODE
        // NK_KEY_TEXT_REPLACE_MODE
        // NK_KEY_TEXT_RESET_MODE
        
        switch (key)
        {
            case L_SHIFT, R_SHIFT -> nk_input_key(OverlayGUI.context, NK_KEY_SHIFT, down);
            case L_CONTROL, R_CONTROL -> nk_input_key(OverlayGUI.context, NK_KEY_CTRL, down);
            case DELETE -> nk_input_key(OverlayGUI.context, NK_KEY_DEL, down);
            case ENTER, KP_ENTER -> nk_input_key(OverlayGUI.context, NK_KEY_ENTER, down);
            case TAB -> nk_input_key(OverlayGUI.context, NK_KEY_TAB, down);
            case BACKSPACE -> nk_input_key(OverlayGUI.context, NK_KEY_BACKSPACE, down);
            case UP -> nk_input_key(OverlayGUI.context, NK_KEY_UP, down);
            case DOWN -> nk_input_key(OverlayGUI.context, NK_KEY_DOWN, down);
            case LEFT -> nk_input_key(context, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_LEFT : NK_KEY_LEFT, down);
            case RIGHT -> nk_input_key(OverlayGUI.context, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_RIGHT : NK_KEY_RIGHT, down);
            case HOME -> {
                nk_input_key(OverlayGUI.context, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_START : NK_KEY_TEXT_LINE_START, down);
                nk_input_key(OverlayGUI.context, NK_KEY_SCROLL_START, down);
            }
            case END -> {
                nk_input_key(OverlayGUI.context, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_END : NK_KEY_TEXT_LINE_END, down);
                nk_input_key(OverlayGUI.context, NK_KEY_SCROLL_END, down);
            }
            case PAGE_UP -> nk_input_key(OverlayGUI.context, NK_KEY_SCROLL_UP, down);
            case PAGE_DOWN -> nk_input_key(OverlayGUI.context, NK_KEY_SCROLL_DOWN, down);
            case C -> nk_input_key(OverlayGUI.context, NK_KEY_COPY, Modifier.only(Modifier.CONTROL) && down);
            case X -> nk_input_key(OverlayGUI.context, NK_KEY_CUT, Modifier.only(Modifier.CONTROL) && down);
            case V -> nk_input_key(OverlayGUI.context, NK_KEY_PASTE, Modifier.only(Modifier.CONTROL) && down);
            case Z -> nk_input_key(OverlayGUI.context, NK_KEY_TEXT_UNDO, Modifier.only(Modifier.CONTROL) && down);
            case Y -> nk_input_key(OverlayGUI.context, NK_KEY_TEXT_REDO, Modifier.only(Modifier.CONTROL) && down);
            case A -> nk_input_key(OverlayGUI.context, NK_KEY_TEXT_SELECT_ALL, Modifier.only(Modifier.CONTROL) && down);
        }
    }
    
    private static void layout()
    {
        int x = 30;
        int y = 30;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            NkRect rect = nk_rect(x, y, 230, 250, NkRect.malloc(stack));
            
            int flags = NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE;
            // int flags = 0;
            if (nk_begin(context, "Demo", rect, flags))
            {
                nk_layout_row_static(context, 30, 80, 1);
                if (nk_button_label(context, "button")) System.out.println("button pressed");
    
                nk_layout_row_dynamic(context, 30, 1);
                nk_label(context, String.format("Mouse Pos: [%.3f, %.3f]", Mouse.x(), Mouse.y()), NK_TEXT_LEFT);
                nk_label(context, String.format("Mouse Abs Pos: [%.3f, %.3f]", Mouse.absX(), Mouse.absY()), NK_TEXT_LEFT);
                
                nk_layout_row_dynamic(context, 30, 2);
                if (nk_option_label(context, "easy", op == EASY)) op = EASY;
                if (nk_option_label(context, "hard", op == HARD)) op = HARD;
                
                nk_layout_row_dynamic(context, 25, 1);
                nk_property_int(context, "Compression:", 0, compression, 100, 10, 1);
                
                nk_layout_row_dynamic(context, 20, 1);
                nk_label(context, "background:", NK_TEXT_LEFT);
                nk_layout_row_dynamic(context, 25, 1);
                if (nk_combo_begin_color(context, nk_rgb_cf(background, NkColor.malloc(stack)), NkVec2.malloc(stack).set(nk_widget_width(context), 400)))
                {
                    nk_layout_row_dynamic(context, 120, 1);
                    nk_color_picker(context, background, NK_RGBA);
                    nk_layout_row_dynamic(context, 25, 1);
                    background.r(nk_propertyf(context, "#R:", 0, background.r(), 1.0f, 0.01f, 0.005f))
                              .g(nk_propertyf(context, "#G:", 0, background.g(), 1.0f, 0.01f, 0.005f))
                              .b(nk_propertyf(context, "#B:", 0, background.b(), 1.0f, 0.01f, 0.005f))
                              .a(nk_propertyf(context, "#A:", 0, background.a(), 1.0f, 0.01f, 0.005f));
                    nk_combo_end(context);
                }
            }
            nk_end(context);
        }
    }
    
    static void draw()
    {
        layout();
        
        // setup global state
        GLState.blendMode(BlendMode.ALPHA);
        GLState.depthMode(DepthMode.NONE);
        
        GLState.cullFace(CullFace.NONE);
        
        int fbWidth  = Window.framebufferWidth();
        int fbHeight = Window.framebufferHeight();
        
        int width  = Window.width();
        int height = Window.height();
        
        GLFramebuffer.bind(null);
        GLState.viewport(0, 0, fbWidth, fbHeight);
        
        GLProgram.bind(OverlayGUI.program);
        GLProgram.Uniform.mat4("pv", OverlayGUI.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
        
        GLVertexArray.bind(OverlayGUI.vertexArray);
        ByteBuffer vertices = Objects.requireNonNull(OverlayGUI.vertexArray.buffer(0).map(GLBuffer.Access.WRITE_ONLY));
        ByteBuffer elements = Objects.requireNonNull(OverlayGUI.vertexArray.indexBuffer().map(GLBuffer.Access.WRITE_ONLY));
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.malloc(stack);
            NkBuffer ebuf = NkBuffer.malloc(stack);
            
            nk_buffer_init_fixed(vbuf, vertices);
            nk_buffer_init_fixed(ebuf, elements);
            nk_convert(OverlayGUI.context, OverlayGUI.commandBuffer, vbuf, ebuf, OverlayGUI.config);
        }
        OverlayGUI.vertexArray.buffer(0).unmap();
        OverlayGUI.vertexArray.indexBuffer().unmap();
        
        // iterate over and execute each draw command
        float fb_scale_x = (float) fbWidth / (float) width;
        float fb_scale_y = (float) fbHeight / (float) height;
        
        long offset = 0L;
        for (NkDrawCommand cmd = nk__draw_begin(OverlayGUI.context, OverlayGUI.commandBuffer);
             cmd != null;
             cmd = nk__draw_next(cmd, OverlayGUI.commandBuffer, OverlayGUI.context))
        {
            if (cmd.elem_count() == 0) continue;
            GLTexture.bindRaw(GL33.GL_TEXTURE_2D, cmd.texture().id(), 0);
            
            int x = (int) (cmd.clip_rect().x() * fb_scale_x);
            int y = (int) ((height - (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y);
            int w = (int) (cmd.clip_rect().w() * fb_scale_x);
            int h = (int) (cmd.clip_rect().h() * fb_scale_y);
            
            GLState.scissor(x, y, w, h);
            OverlayGUI.vertexArray.drawElements(DrawMode.TRIANGLES, offset, cmd.elem_count());
            
            offset += cmd.elem_count();
        }
        nk_clear(context);
        nk_buffer_clear(commandBuffer);
    }
}
