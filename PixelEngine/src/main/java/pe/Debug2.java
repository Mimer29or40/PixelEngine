package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
import pe.debug.DebugButton;
import pe.debug.DebugWindow;
import pe.debug.DebugGUI;
import pe.render.*;
import rutils.Logger;
import rutils.Math;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import static org.lwjgl.stb.STBEasyFont.*;

public class Debug2
{
    private static final Logger LOGGER = new Logger();
    
    // TODO - Scaling System for higher density displays
    
    private static boolean enabled;
    
    private static GLProgram     program;
    private static ByteBuffer    vertexBuffer;
    private static GLVertexArray vertexArray;
    private static Matrix4d      pv;
    
    private static final Queue<Command> commands = new LinkedList<>();
    
    private static final Colorc defaultTextColor       = Color_RGBA.create().set(Color.WHITE);
    private static final Colorc defaultBackgroundColor = Color_RGBA.create().set(Color.GRAY).a(180);
    
    static void setup()
    {
        String vert = """
                      #version 330
                      layout(location = 0) in vec3 aPos;
                      layout(location = 1) in vec4 aCol;
                      uniform mat4 pv;
                      out vec4 color;
                      void main(void) {
                          color = aCol;
                          gl_Position = pv * vec4(aPos, 1.0);
                      }
                      """;
        String frag = """
                      #version 330
                      in vec4 color;
                      out vec4 FragColor;
                      void main(void) {
                          FragColor = color;
                      }
                      """;
        Debug2.program = GLProgram.loadFromCode(vert, null, frag);
        
        int vertexLimit = 4096;
        
        IntBuffer indices = MemoryUtil.memAllocInt(vertexLimit * 6); // 6 vertices per quad (indices)
        for (int i = 0; i < vertexLimit; ++i)
        {
            indices.put(4 * i);
            indices.put(4 * i + 1);
            indices.put(4 * i + 2);
            indices.put(4 * i);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
        }
        
        Debug2.vertexBuffer = MemoryUtil.memAlloc(vertexLimit * (Float.BYTES * 3 + Byte.BYTES * 4));
        
        Debug2.vertexArray = GLVertexArray.builder()
                                          .buffer(Debug2.vertexBuffer, Usage.DYNAMIC_DRAW,
                                                  new GLAttribute(GLType.FLOAT, 3, false),
                                                  new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                          .indexBuffer(indices.clear(), Usage.STATIC_DRAW)
                                          .build();
        MemoryUtil.memFree(indices);
        
        Debug2.pv = new Matrix4d();
        
        DebugWindow window;
        
        window = new DebugWindow("Window 1");
        for (int i = 0; i < 100; i++)
        {
            window.addChild(new DebugButton("Test " + i, i % 2 == 0)
            {
            
            });
        }
        DebugGUI.addChild(window);
        // Debug2.addElement(new DebugLabel("Parent Less"));
        
        window = new DebugWindow("Window 2");
        DebugGUI.addChild(window);
        
        window = new DebugWindow("Window 3");
        DebugGUI.addChild(window);
        
        // DebugGUI.addChild(new DebugLabel("Test"));
    }
    
    static void destroy()
    {
        Debug2.vertexArray.delete();
        MemoryUtil.memFree(Debug2.vertexBuffer);
        Debug2.program.delete();
    }
    
    static void handleEvents()
    {
        DebugGUI.handleEvents();
    }
    
    static void draw()
    {
        GL.defaultState();
        GLFramebuffer.bind(null);
        
        DebugGUI.draw();
        
        if (!Debug2.commands.isEmpty())
        {
            int fbWidth  = GLFramebuffer.currentWidth();
            int fbHeight = GLFramebuffer.currentHeight();
            
            GLProgram.bind(Debug2.program);
            GLProgram.Uniform.mat4("pv", Debug2.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
            
            GL.winding(Winding.CW);
            GL.depthMode(DepthMode.NONE);
            
            int quads = 0;
            // int draws = 0; // TODO - Track Draws
            
            Command command;
            while ((command = Debug2.commands.poll()) != null)
            {
                if (Debug2.vertexBuffer.remaining() < command.bytesToAdd())
                {
                    drawVAO(quads);
                    // draws++;
                    quads = 0;
                }
                
                quads += command.buildVertices(Debug2.vertexBuffer, fbWidth, fbHeight);
            }
            
            drawVAO(quads);
            // draws++;
        }
    }
    
    private static void drawVAO(int quads)
    {
        Debug2.vertexArray.buffer(0).set(0, Debug2.vertexBuffer.clear());
        Debug2.vertexArray.drawElements(DrawMode.TRIANGLES, quads * 6);
    }
    
    /**
     * Gets the width in pixels of the provided text.
     *
     * @param text The text
     * @return The width in pixels
     */
    public static int textWidth(String text)
    {
        return stb_easy_font_width(text);
    }
    
    /**
     * Gets the height in pixels of the provided text.
     *
     * @param text The text
     * @return The height in pixels
     */
    public static int textHeight(String text)
    {
        return stb_easy_font_height(text);
    }
    
    public static void drawLine(int x0, int y0, int x1, int y1, int thickness, @NotNull Colorc color)
    {
        Debug2.commands.offer(new Line(x0, y0, x1, y1, thickness, color));
    }
    
    /**
     * Draws a filled colored box to the debug screen.
     *
     * @param x      The x coordinate of the top left point of the box.
     * @param y      The y coordinate of the top left point of the box.
     * @param width  The width of the box.
     * @param height The height of the box.
     * @param color  The color of the box.
     */
    public static void drawFilledRect(int x, int y, int width, int height, @NotNull Colorc color)
    {
        Debug2.commands.offer(new Rect(x, y, width, height, color));
    }
    
    /**
     * Draws a colored box to the debug screen.
     *
     * @param x         The x coordinate of the top left point of the box.
     * @param y         The y coordinate of the top left point of the box.
     * @param width     The width of the box.
     * @param height    The height of the box.
     * @param thickness The thickness of the box.
     * @param color     The color of the box.
     */
    public static void drawRect(int x, int y, int width, int height, int thickness, @NotNull Colorc color)
    {
        Debug2.commands.offer(new Rect(x, y, thickness, height, color));
        Debug2.commands.offer(new Rect(x + width - thickness, y, thickness, height, color));
        Debug2.commands.offer(new Rect(x + thickness, y, width - (thickness * 2), thickness, color));
        Debug2.commands.offer(new Rect(x + thickness, y + height - thickness, width - (thickness * 2), thickness, color));
    }
    
    /**
     * Draws text to the debug screen.
     *
     * @param x     The x coordinate of the top left point if the text.
     * @param y     The y coordinate of the top left point if the text.
     * @param text  The text to render.
     * @param color The color of the text.
     */
    public static void drawText(int x, int y, @NotNull String text, @Nullable Colorc color)
    {
        if (color == null) color = Debug2.defaultTextColor;
        
        Debug2.commands.offer(new Text(x, y + 2, text, color));
    }
    
    /**
     * Draws text to the debug screen with a background.
     *
     * @param x               The x coordinate of the top left point if the text.
     * @param y               The y coordinate of the top left point if the text.
     * @param text            The text to render.
     * @param textColor       The color of the text.
     * @param backgroundColor The color of the background.
     */
    public static void drawTextWithBackground(int x, int y, @NotNull String text, @Nullable Colorc textColor, @Nullable Colorc backgroundColor)
    {
        if (textColor == null) textColor = Debug2.defaultTextColor;
        if (backgroundColor == null) backgroundColor = Debug2.defaultBackgroundColor;
        
        int w = textWidth(text) + 2;
        int h = textHeight(text);
        Debug2.commands.offer(new Rect(x, y, w, h, backgroundColor));
        Debug2.commands.offer(new Text(x + 2, y + 2, text, textColor));
    }
    
    public static void scissor(@NotNull ScissorMode scissorMode)
    {
        Debug2.commands.offer(new Scissor(scissorMode));
    }
    
    public static void scissor(int x, int y, int width, int height)
    {
        Debug2.commands.offer(new Scissor(x, y, width, height));
    }
    
    public static void flush()
    {
        Debug2.commands.offer(new Flush());
    }
    
    private interface Command
    {
        int bytesToAdd();
        
        int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight);
    }
    
    private static final class Line implements Command
    {
        private final float x0, y0, x1, y1;
        private final float nx, ny;
        private final byte r, g, b, a;
        
        private Line(int x0, int y0, int x1, int y1, int thickness, @NotNull Colorc color)
        {
            this.x0 = x0;
            this.y0 = y0;
            this.x1 = x1;
            this.y1 = y1;
            
            double dx = x1 - x0;
            double dy = y1 - y0;
            if (dx != 0.0 || dy != 0.0)
            {
                double l = Math.sqrt(dx * dx + dy * dy);
                double s = thickness / (2 * l);
                this.nx = (float) (-dy * s);
                this.ny = (float) (dx * s);
            }
            else
            {
                this.nx = 0.0f;
                this.ny = 0.0f;
            }
            this.r = (byte) color.r();
            this.g = (byte) color.g();
            this.b = (byte) color.b();
            this.a = (byte) color.a();
        }
        
        @Override
        public int bytesToAdd()
        {
            return 64; // 64 bytes per quad.
        }
        
        @Override
        public int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight)
        {
            buffer.putFloat(this.x1 + this.nx);
            buffer.putFloat(this.y1 + this.ny);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x0 + this.nx);
            buffer.putFloat(this.y0 + this.ny);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x0 - this.nx);
            buffer.putFloat(this.y0 - this.ny);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x1 - this.nx);
            buffer.putFloat(this.y1 - this.ny);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            
            return 1;
        }
    }
    
    private static final class Rect implements Command
    {
        private final float x0, y0, x1, y1;
        private final byte r, g, b, a;
        
        private Rect(int x, int y, int width, int height, @NotNull Colorc color)
        {
            this.x0 = x;
            this.y0 = y;
            this.x1 = x + width;
            this.y1 = y + height;
            this.r  = (byte) color.r();
            this.g  = (byte) color.g();
            this.b  = (byte) color.b();
            this.a  = (byte) color.a();
        }
        
        @Override
        public int bytesToAdd()
        {
            return 64; // 64 bytes per quad.
        }
        
        @Override
        public int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight)
        {
            buffer.putFloat(this.x0);
            buffer.putFloat(this.y0);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x1);
            buffer.putFloat(this.y0);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x1);
            buffer.putFloat(this.y1);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            buffer.putFloat(this.x0);
            buffer.putFloat(this.y1);
            buffer.putFloat(0.0F);
            buffer.put(this.r);
            buffer.put(this.g);
            buffer.put(this.b);
            buffer.put(this.a);
            
            return 1;
        }
    }
    
    private static final class Text implements Command
    {
        private final float x, y;
        private final String t;
        private final byte   r, g, b, a;
        
        private Text(int x, int y, @NotNull String text, @NotNull Colorc color)
        {
            this.x = x;
            this.y = y;
            this.t = text;
            this.r = (byte) color.r();
            this.g = (byte) color.g();
            this.b = (byte) color.b();
            this.a = (byte) color.a();
        }
        
        @Override
        public int bytesToAdd()
        {
            // 11 quads max * 64 bytes per quad.
            return this.t.length() * 704;
        }
        
        @Override
        public int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight)
        {
            int newQuads;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer color = stack.bytes(this.r, this.g, this.b, this.a);
                newQuads = stb_easy_font_print(this.x, this.y, this.t, color, buffer);
                buffer.position(buffer.position() + newQuads * 64); // 64 bytes per quad.
            }
            return newQuads;
        }
    }
    
    private static final class Scissor implements Command
    {
        private final ScissorMode scissorMode;
        private final int         x, y, width, height;
        
        private Scissor(ScissorMode scissorMode)
        {
            this.scissorMode = scissorMode;
            
            this.x      = 0;
            this.y      = 0;
            this.width  = 0;
            this.height = 0;
        }
        
        private Scissor(int x, int y, int width, int height)
        {
            this.scissorMode = null;
            
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
        }
        
        @Override
        public int bytesToAdd()
        {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight)
        {
            if (this.scissorMode != null)
            {
                GL.scissorMode(this.scissorMode);
            }
            else
            {
                GL.scissor(this.x, fbHeight - this.y - this.height, this.width, this.height);
            }
            return 0;
        }
    }
    
    private static final class Flush implements Command
    {
        @Override
        public int bytesToAdd()
        {
            return Integer.MAX_VALUE;
        }
        
        @Override
        public int buildVertices(ByteBuffer buffer, int fbWidth, int fbHeight)
        {
            return 0;
        }
    }
}
