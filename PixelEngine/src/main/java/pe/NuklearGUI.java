package pe;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4d;
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
import pe.gui.GUIWindow;
import pe.render.*;
import rutils.IOUtil;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.stb.STBTruetype.*;

public class NuklearGUI
{
    private static final Logger LOGGER = new Logger();
    
    private static GLProgram     program;
    private static GLVertexArray vertexArray;
    private static Matrix4d      pv;
    
    private static NkAllocator allocator;
    
    private static NkDrawVertexLayoutElement.Buffer vertexLayout;
    private static NkDrawNullTexture                nullTexture;
    private static NkConvertConfig                  config;
    private static NkBuffer                         commandBuffer;
    
    private static NkContext  ctx;
    private static NkUserFont defaultFont;
    
    private static final Set<GUIWindow> windows = new LinkedHashSet<>();
    
    static ByteBuffer ttf;
    
    static int BITMAP_W = 1024;
    static int BITMAP_H = 1024;
    
    static int FONT_HEIGHT = 12;
    static int fontTexID   = GL33.glGenTextures();
    
    static STBTTFontinfo          fontInfo = STBTTFontinfo.create();
    static STBTTPackedchar.Buffer cdata    = STBTTPackedchar.create(95);
    
    static float scale;
    static float descent;
    
    static void setup()
    {
        NuklearGUI.LOGGER.fine("Setup");
        
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
        NuklearGUI.program = GLProgram.loadFromCode(vert, null, frag);
        
        GLProgram.Uniform.int1("tex", 0);
        
        int vertexSize  = Float.BYTES * 2 + Float.BYTES * 2 + Byte.BYTES * 4;
        int elementSize = Short.BYTES;
        
        ByteBuffer vertexBuffer  = MemoryUtil.memAlloc((1 << 15) * vertexSize);
        ByteBuffer elementBuffer = MemoryUtil.memAlloc((1 << 15) * elementSize);
    
        NuklearGUI.vertexArray = GLVertexArray.builder()
                                              .buffer(vertexBuffer, Usage.DYNAMIC_DRAW,
                                                      new GLAttribute(GLType.FLOAT, 2, false),
                                                      new GLAttribute(GLType.FLOAT, 2, false),
                                                      new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                              .indexBuffer(elementBuffer, Usage.DYNAMIC_DRAW, GLType.UNSIGNED_SHORT)
                                              .build();
        
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(elementBuffer);
    
        NuklearGUI.pv = new Matrix4d();
    
        NuklearGUI.allocator = NkAllocator.calloc();
        NuklearGUI.allocator.alloc((handle, old, size) -> MemoryUtil.nmemAllocChecked(size))
                            .mfree((handle, ptr) -> MemoryUtil.nmemFree(ptr));
    
        NuklearGUI.vertexLayout = NkDrawVertexLayoutElement.calloc(4);
        NuklearGUI.vertexLayout.position(0).attribute(NK_VERTEX_POSITION).format(NK_FORMAT_FLOAT).offset(0)
                               .position(1).attribute(NK_VERTEX_TEXCOORD).format(NK_FORMAT_FLOAT).offset(8)
                               .position(2).attribute(NK_VERTEX_COLOR).format(NK_FORMAT_R8G8B8A8).offset(16)
                               .position(3).attribute(NK_VERTEX_ATTRIBUTE_COUNT).format(NK_FORMAT_COUNT).offset(0)
                               .flip();
    
        NuklearGUI.nullTexture = NkDrawNullTexture.calloc();
        NuklearGUI.nullTexture.texture().id(GLTexture.getDefault().id());
        NuklearGUI.nullTexture.uv().set(0.5f, 0.5f);
    
        NuklearGUI.config = NkConvertConfig.calloc();
        NuklearGUI.config.vertex_layout(NuklearGUI.vertexLayout)
                         .vertex_size(20)
                         .vertex_alignment(4)
                         .null_texture(NuklearGUI.nullTexture)
                         .circle_segment_count(22) // TODO - Have setter methods
                         .curve_segment_count(22) // TODO - Have setter methods
                         .arc_segment_count(22) // TODO - Have setter methods
                         .global_alpha(1.0f) // TODO - Have setter methods
                         .shape_AA(NK_ANTI_ALIASING_ON) // TODO - Have setter methods
                         .line_AA(NK_ANTI_ALIASING_ON); // TODO - Have setter methods
    
        NuklearGUI.commandBuffer = NkBuffer.calloc();
        nk_buffer_init(NuklearGUI.commandBuffer, NuklearGUI.allocator, 4 * 1024);
        
        ttf = IOUtil.readFromFile("demo/ProggyClean/ProggyClean.ttf");
        
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
    
        NuklearGUI.defaultFont = NkUserFont.calloc();
        NuklearGUI.defaultFont.width((handle, h, text, len) -> {
            float text_width = 0;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer unicode = stack.mallocInt(1);
                
                int glyph_len = nnk_utf_decode(text, MemoryUtil.memAddress(unicode), len);
                int text_len  = glyph_len;
                
                if (glyph_len == 0) return 0;
                
                IntBuffer advance = stack.mallocInt(1);
                while (text_len <= len && glyph_len != 0)
                {
                    if (unicode.get(0) == NK_UTF_INVALID) break;
                    
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
        NuklearGUI.defaultFont.height(FONT_HEIGHT);
        NuklearGUI.defaultFont.query((handle, font_height, glyph, codepoint, next_codepoint) -> {
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
        NuklearGUI.defaultFont.texture(it -> it.id(fontTexID));
    
        NuklearGUI.ctx = NkContext.calloc();
        
        nk_init(NuklearGUI.ctx, NuklearGUI.allocator, NuklearGUI.defaultFont);
        NuklearGUI.ctx.clip().copy((handle, text, len) -> {
            if (len == 0) return;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer str = stack.malloc(len + 1);
                MemoryUtil.memCopy(text, MemoryUtil.memAddress(str), len);
                str.put(len, (byte) 0);
                
                Window.setClipboard(str);
            }
        });
        NuklearGUI.ctx.clip().paste((handle, edit) -> {
            long text = Window.getClipboardRaw();
            if (text != MemoryUtil.NULL)
            {
                nnk_textedit_paste(edit, text, nnk_strlen(text));
            }
        });
    }
    
    static void destroy()
    {
        NuklearGUI.LOGGER.fine("Destroy");
        
        nk_free(NuklearGUI.ctx);
        NuklearGUI.ctx.free();
        
        NuklearGUI.defaultFont.free();
        
        NuklearGUI.commandBuffer.free();
        NuklearGUI.config.free();
        NuklearGUI.nullTexture.free();
        NuklearGUI.vertexLayout.free();
        
        NuklearGUI.allocator.free();
        
        NuklearGUI.vertexArray.delete();
        
        NuklearGUI.program.delete();
    }
    
    static void handleEvents()
    {
        nk_input_begin(NuklearGUI.ctx);
        
        Set<Event> mouseEvents    = new LinkedHashSet<>();
        Set<Event> keyboardEvents = new LinkedHashSet<>();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (Event event : Engine.Events.get())
            {
                if (event instanceof EventMouse) mouseEvents.add(event);
                if (event instanceof EventKeyboard) keyboardEvents.add(event);
                
                if (event instanceof EventMouseMoved)
                {
                    nk_input_motion(NuklearGUI.ctx, (int) Mouse.absX(), (int) Mouse.absY());
                }
                else if (event instanceof EventMouseScrolled)
                {
                    NkVec2 scroll = NkVec2.malloc(stack).x((float) Mouse.scrollX()).y((float) Mouse.scrollY());
                    nk_input_scroll(NuklearGUI.ctx, scroll);
                }
                else if (event instanceof EventMouseButtonDown mbDown)
                {
                    mouseButtonInput(mbDown.button(), Mouse.absX(), Mouse.absY(), mbDown.downCount());
                }
                else if (event instanceof EventMouseButtonUp mbUp)
                {
                    mouseButtonInput(mbUp.button(), Mouse.absX(), Mouse.absY(), 0);
                }
                else if (event instanceof EventKeyboardKeyDown kkDown)
                {
                    keyboardKeyInput(kkDown.key(), true);
                }
                else if (event instanceof EventKeyboardKeyUp kkUp)
                {
                    keyboardKeyInput(kkUp.key(), false);
                }
                else if (event instanceof EventKeyboardKeyRepeated kkRepeated)
                {
                    keyboardKeyInput(kkRepeated.key(), true);
                }
                else if (event instanceof EventKeyboardTyped kTyped)
                {
                    nk_input_unicode(NuklearGUI.ctx, kTyped.codePoint());
                }
            }
        }
        
        NkMouse mouse = NuklearGUI.ctx.input().mouse();
        if (mouse.grab())
        {
            Mouse.hide();
        }
        else if (mouse.grabbed())
        {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            Mouse.absPos(prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        }
        else if (mouse.ungrab())
        {
            Mouse.show();
        }
        
        nk_input_end(NuklearGUI.ctx);
        
        for (GUIWindow window : NuklearGUI.windows) window.layout(NuklearGUI.ctx);
        
        if (nk_window_is_any_hovered(NuklearGUI.ctx)) mouseEvents.forEach(Event::consume);
        if (nk_item_is_any_active(NuklearGUI.ctx)) keyboardEvents.forEach(Event::consume);
    }
    
    private static void mouseButtonInput(@NotNull Mouse.Button button, double x, double y, int downCount)
    {
        switch (button)
        {
            case LEFT -> nk_input_button(NuklearGUI.ctx, NK_BUTTON_LEFT, (int) x, (int) y, downCount > 0);
            case RIGHT -> nk_input_button(NuklearGUI.ctx, NK_BUTTON_RIGHT, (int) x, (int) y, downCount > 0);
            case MIDDLE -> nk_input_button(NuklearGUI.ctx, NK_BUTTON_MIDDLE, (int) x, (int) y, downCount > 0);
        }
        nk_input_button(NuklearGUI.ctx, NK_BUTTON_DOUBLE, (int) x, (int) y, downCount > 1);
    }
    
    private static void keyboardKeyInput(@NotNull Keyboard.Key key, boolean down)
    {
        // NK_KEY_TEXT_INSERT_MODE
        // NK_KEY_TEXT_REPLACE_MODE
        // NK_KEY_TEXT_RESET_MODE
        
        switch (key)
        {
            case L_SHIFT, R_SHIFT -> nk_input_key(NuklearGUI.ctx, NK_KEY_SHIFT, down);
            case L_CONTROL, R_CONTROL -> nk_input_key(NuklearGUI.ctx, NK_KEY_CTRL, down);
            case DELETE -> nk_input_key(NuklearGUI.ctx, NK_KEY_DEL, down);
            case ENTER, KP_ENTER -> nk_input_key(NuklearGUI.ctx, NK_KEY_ENTER, down);
            case TAB -> nk_input_key(NuklearGUI.ctx, NK_KEY_TAB, down);
            case BACKSPACE -> nk_input_key(NuklearGUI.ctx, NK_KEY_BACKSPACE, down);
            case UP -> nk_input_key(NuklearGUI.ctx, NK_KEY_UP, down);
            case DOWN -> nk_input_key(NuklearGUI.ctx, NK_KEY_DOWN, down);
            case LEFT -> nk_input_key(ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_LEFT : NK_KEY_LEFT, down);
            case RIGHT -> nk_input_key(NuklearGUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_RIGHT : NK_KEY_RIGHT, down);
            case HOME -> {
                nk_input_key(NuklearGUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_START : NK_KEY_TEXT_LINE_START, down);
                nk_input_key(NuklearGUI.ctx, NK_KEY_SCROLL_START, down);
            }
            case END -> {
                nk_input_key(NuklearGUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_END : NK_KEY_TEXT_LINE_END, down);
                nk_input_key(NuklearGUI.ctx, NK_KEY_SCROLL_END, down);
            }
            case PAGE_UP -> nk_input_key(NuklearGUI.ctx, NK_KEY_SCROLL_UP, down);
            case PAGE_DOWN -> nk_input_key(NuklearGUI.ctx, NK_KEY_SCROLL_DOWN, down);
            case C -> nk_input_key(NuklearGUI.ctx, NK_KEY_COPY, Modifier.only(Modifier.CONTROL) && down);
            case X -> nk_input_key(NuklearGUI.ctx, NK_KEY_CUT, Modifier.only(Modifier.CONTROL) && down);
            case V -> nk_input_key(NuklearGUI.ctx, NK_KEY_PASTE, Modifier.only(Modifier.CONTROL) && down);
            case Z -> nk_input_key(NuklearGUI.ctx, NK_KEY_TEXT_UNDO, Modifier.only(Modifier.CONTROL) && down);
            case Y -> nk_input_key(NuklearGUI.ctx, NK_KEY_TEXT_REDO, Modifier.only(Modifier.CONTROL) && down);
            case A -> nk_input_key(NuklearGUI.ctx, NK_KEY_TEXT_SELECT_ALL, Modifier.only(Modifier.CONTROL) && down);
        }
    }
    
    static void draw()
    {
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
        
        GLProgram.bind(NuklearGUI.program);
        GLProgram.Uniform.mat4("pv", NuklearGUI.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
        
        GLVertexArray.bind(NuklearGUI.vertexArray);
        ByteBuffer vertices = Objects.requireNonNull(NuklearGUI.vertexArray.buffer(0).map(GLBuffer.Access.WRITE_ONLY));
        ByteBuffer elements = Objects.requireNonNull(NuklearGUI.vertexArray.indexBuffer().map(GLBuffer.Access.WRITE_ONLY));
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // setup buffers to load vertices and elements
            NkBuffer vbuf = NkBuffer.malloc(stack);
            NkBuffer ebuf = NkBuffer.malloc(stack);
            
            nk_buffer_init_fixed(vbuf, vertices);
            nk_buffer_init_fixed(ebuf, elements);
            nk_convert(NuklearGUI.ctx, NuklearGUI.commandBuffer, vbuf, ebuf, NuklearGUI.config);
        }
        NuklearGUI.vertexArray.buffer(0).unmap();
        NuklearGUI.vertexArray.indexBuffer().unmap();
        
        // iterate over and execute each draw command
        float fb_scale_x = (float) fbWidth / (float) width;
        float fb_scale_y = (float) fbHeight / (float) height;
        
        long offset = 0L;
        for (NkDrawCommand cmd = nk__draw_begin(NuklearGUI.ctx, NuklearGUI.commandBuffer);
             cmd != null;
             cmd = nk__draw_next(cmd, NuklearGUI.commandBuffer, NuklearGUI.ctx))
        {
            if (cmd.elem_count() == 0) continue;
            GLTexture.bindRaw(GL33.GL_TEXTURE_2D, cmd.texture().id(), 0);
            
            int x = (int) (cmd.clip_rect().x() * fb_scale_x);
            int y = (int) ((height - (cmd.clip_rect().y() + cmd.clip_rect().h())) * fb_scale_y);
            int w = (int) (cmd.clip_rect().w() * fb_scale_x);
            int h = (int) (cmd.clip_rect().h() * fb_scale_y);
            
            GLState.scissor(x, y, w, h);
            NuklearGUI.vertexArray.drawElements(DrawMode.TRIANGLES, offset, cmd.elem_count());
            
            offset += cmd.elem_count();
        }
        nk_clear(ctx);
        nk_buffer_clear(commandBuffer);
    }
    
    public static void addWindow(@NotNull GUIWindow window)
    {
        boolean result = NuklearGUI.windows.add(window);
        if (!result) throw new IllegalStateException("Window already present.");
    }
}
