package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
import pe.event.EventKeyboardKeyDown;
import pe.render.*;
import rutils.Math;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.stb.STBEasyFont.*;

public final class Debug
{
    private static String notification;
    private static long   notificationTime;
    private static long   notificationDur; // Time in seconds to display a notification.
    
    private static boolean enabled;
    
    private static GLProgram     program;
    private static ByteBuffer    vertexBuffer;
    private static GLVertexArray vertexArray;
    private static Matrix4d      pv;
    
    private static final int headerSize = textHeight("TEXT") + 2;
    
    private static final List<Menu> menus       = new ArrayList<>();
    private static       int        currentMenu = -1;
    
    private static final Queue<Renderable> renderables = new LinkedList<>();
    
    private static final Colorc defaultBackColor = Color_RGBA.create().set(Color.GRAY).a(180);
    
    static void setup()
    {
        Debug.enabled = true;
        
        Debug.notificationDur = (long) (1_000_000_000L * 2.0);
        
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
        Debug.program = GLProgram.loadFromCode(vert, null, frag);
        
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
        
        Debug.vertexBuffer = MemoryUtil.memAlloc(vertexLimit * (Float.BYTES * 3 + Byte.BYTES * 4));
        
        Debug.vertexArray = GLVertexArray.builder()
                                         .buffer(Debug.vertexBuffer, Usage.DYNAMIC_DRAW,
                                                 new GLAttribute(GLType.FLOAT, 3, false),
                                                 new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                         .indexBuffer(indices.clear(), Usage.STATIC_DRAW)
                                         .build();
        MemoryUtil.memFree(indices);
        
        Debug.pv = new Matrix4d();
        
        Debug.addMenu(new Debug.Menu("Engine")
        {
            @Override
            protected void drawImpl()
            {
                String[] lines = {
                        String.format("Frame: %s", Time.frameCount()),
                        String.format("Time: %.3f", Time.get()),
                        String.format("Wireframe: %s", Engine.wireframe),
                        String.format("Vertex Count: %s", Engine.vertices),
                        String.format("Draw Calls: %s", Engine.draws),
                        };
                
                int x = 0, y = 0;
                for (String line : lines)
                {
                    drawTextWithBackground(x, y, line, Color.WHITE, null);
                    y += Debug.textHeight(line);
                }
            }
        });
        
        EnumMap<Mouse.Button, Layout> mouseMap    = new EnumMap<>(Mouse.Button.class);
        EnumMap<Keyboard.Key, Layout> keyboardMap = new EnumMap<>(Keyboard.Key.class);
        EnumMap<Modifier, Layout>     modifierMap = new EnumMap<>(Modifier.class);
        
        {
            mouseMap.put(Mouse.Button.LEFT, new Layout(26, 26, 24, 24, ""));
            mouseMap.put(Mouse.Button.MIDDLE, new Layout(52, 26, 12, 24, ""));
            mouseMap.put(Mouse.Button.RIGHT, new Layout(66, 26, 24, 24, ""));
            mouseMap.put(Mouse.Button.FOUR, new Layout(26, 52, 64, 12, "4"));
            mouseMap.put(Mouse.Button.FIVE, new Layout(26, 64, 64, 12, "5"));
            mouseMap.put(Mouse.Button.SIX, new Layout(26, 76, 64, 12, "6"));
            mouseMap.put(Mouse.Button.SEVEN, new Layout(26, 88, 64, 12, "7"));
            mouseMap.put(Mouse.Button.EIGHT, new Layout(26, 100, 64, 12, "8"));
        }
        
        {
            keyboardMap.put(Keyboard.Key.ESCAPE, new Layout(0, 0, 24, 24, "Esc"));
            keyboardMap.put(Keyboard.Key.F1, new Layout(48, 0, 24, 24, "F1"));
            keyboardMap.put(Keyboard.Key.F2, new Layout(74, 0, 24, 24, "F2"));
            keyboardMap.put(Keyboard.Key.F3, new Layout(100, 0, 24, 24, "F3"));
            keyboardMap.put(Keyboard.Key.F4, new Layout(126, 0, 24, 24, "F4"));
            keyboardMap.put(Keyboard.Key.F5, new Layout(172, 0, 24, 24, "F5"));
            keyboardMap.put(Keyboard.Key.F6, new Layout(198, 0, 24, 24, "F6"));
            keyboardMap.put(Keyboard.Key.F7, new Layout(224, 0, 24, 24, "F7"));
            keyboardMap.put(Keyboard.Key.F8, new Layout(250, 0, 24, 24, "F8"));
            keyboardMap.put(Keyboard.Key.F9, new Layout(296, 0, 24, 24, "F9"));
            keyboardMap.put(Keyboard.Key.F10, new Layout(322, 0, 24, 24, "F10"));
            keyboardMap.put(Keyboard.Key.F11, new Layout(348, 0, 24, 24, "F11"));
            keyboardMap.put(Keyboard.Key.F12, new Layout(374, 0, 24, 24, "F12"));
            keyboardMap.put(Keyboard.Key.PRINT_SCREEN, new Layout(402, 0, 24, 24, "Prt\nScn"));
            keyboardMap.put(Keyboard.Key.SCROLL_LOCK, new Layout(428, 0, 24, 24, "Scrl"));
            keyboardMap.put(Keyboard.Key.PAUSE, new Layout(454, 0, 24, 24, "Pse"));
            modifierMap.put(Modifier.CAPS_LOCK, new Layout(482, 0, 24, 24, "Caps\nLock"));
            modifierMap.put(Modifier.NUM_LOCK, new Layout(508, 0, 24, 24, "Num\nLock"));
            
            keyboardMap.put(Keyboard.Key.GRAVE, new Layout(0, 28, 24, 24, "~\n`"));
            keyboardMap.put(Keyboard.Key.K1, new Layout(26, 28, 24, 24, "1"));
            keyboardMap.put(Keyboard.Key.K2, new Layout(52, 28, 24, 24, "2"));
            keyboardMap.put(Keyboard.Key.K3, new Layout(78, 28, 24, 24, "3"));
            keyboardMap.put(Keyboard.Key.K4, new Layout(104, 28, 24, 24, "4"));
            keyboardMap.put(Keyboard.Key.K5, new Layout(130, 28, 24, 24, "4"));
            keyboardMap.put(Keyboard.Key.K6, new Layout(156, 28, 24, 24, "6"));
            keyboardMap.put(Keyboard.Key.K7, new Layout(182, 28, 24, 24, "7"));
            keyboardMap.put(Keyboard.Key.K8, new Layout(208, 28, 24, 24, "8"));
            keyboardMap.put(Keyboard.Key.K9, new Layout(234, 28, 24, 24, "9"));
            keyboardMap.put(Keyboard.Key.K0, new Layout(260, 28, 24, 24, "0"));
            keyboardMap.put(Keyboard.Key.MINUS, new Layout(286, 28, 24, 24, "_\n-"));
            keyboardMap.put(Keyboard.Key.EQUAL, new Layout(312, 28, 24, 24, "+\n="));
            keyboardMap.put(Keyboard.Key.BACKSPACE, new Layout(338, 28, 60, 24, "Back"));
            keyboardMap.put(Keyboard.Key.INSERT, new Layout(402, 28, 24, 24, "Ins"));
            keyboardMap.put(Keyboard.Key.HOME, new Layout(428, 28, 24, 24, "Hme"));
            keyboardMap.put(Keyboard.Key.PAGE_UP, new Layout(454, 28, 24, 24, "PgUp"));
            keyboardMap.put(Keyboard.Key.NUM_LOCK, new Layout(482, 28, 24, 24, "Num"));
            keyboardMap.put(Keyboard.Key.KP_DIVIDE, new Layout(508, 28, 24, 24, "/"));
            keyboardMap.put(Keyboard.Key.KP_MULTIPLY, new Layout(534, 28, 24, 24, "*"));
            keyboardMap.put(Keyboard.Key.KP_SUBTRACT, new Layout(560, 28, 24, 24, "-"));
            
            keyboardMap.put(Keyboard.Key.TAB, new Layout(0, 54, 42, 24, "Tab"));
            keyboardMap.put(Keyboard.Key.Q, new Layout(44, 54, 24, 24, "Q"));
            keyboardMap.put(Keyboard.Key.W, new Layout(70, 54, 24, 24, "W"));
            keyboardMap.put(Keyboard.Key.E, new Layout(96, 54, 24, 24, "E"));
            keyboardMap.put(Keyboard.Key.R, new Layout(122, 54, 24, 24, "R"));
            keyboardMap.put(Keyboard.Key.T, new Layout(148, 54, 24, 24, "T"));
            keyboardMap.put(Keyboard.Key.Y, new Layout(174, 54, 24, 24, "Y"));
            keyboardMap.put(Keyboard.Key.U, new Layout(200, 54, 24, 24, "U"));
            keyboardMap.put(Keyboard.Key.I, new Layout(226, 54, 24, 24, "I"));
            keyboardMap.put(Keyboard.Key.O, new Layout(252, 54, 24, 24, "O"));
            keyboardMap.put(Keyboard.Key.P, new Layout(278, 54, 24, 24, "P"));
            keyboardMap.put(Keyboard.Key.L_BRACKET, new Layout(304, 54, 24, 24, "{\n["));
            keyboardMap.put(Keyboard.Key.R_BRACKET, new Layout(330, 54, 24, 24, "}\n}"));
            keyboardMap.put(Keyboard.Key.BACKSLASH, new Layout(356, 54, 42, 24, "|\n\\"));
            keyboardMap.put(Keyboard.Key.DELETE, new Layout(402, 54, 24, 24, "Del"));
            keyboardMap.put(Keyboard.Key.END, new Layout(428, 54, 24, 24, "End"));
            keyboardMap.put(Keyboard.Key.PAGE_DOWN, new Layout(454, 54, 24, 24, "PgDn"));
            keyboardMap.put(Keyboard.Key.KP_7, new Layout(482, 54, 24, 24, "7"));
            keyboardMap.put(Keyboard.Key.KP_8, new Layout(508, 54, 24, 24, "8"));
            keyboardMap.put(Keyboard.Key.KP_9, new Layout(534, 54, 24, 24, "9"));
            keyboardMap.put(Keyboard.Key.KP_ADD, new Layout(560, 54, 24, 50, "+"));
            
            keyboardMap.put(Keyboard.Key.CAPS_LOCK, new Layout(0, 80, 46, 24, "Caps"));
            keyboardMap.put(Keyboard.Key.A, new Layout(48, 80, 24, 24, "A"));
            keyboardMap.put(Keyboard.Key.S, new Layout(74, 80, 24, 24, "S"));
            keyboardMap.put(Keyboard.Key.D, new Layout(100, 80, 24, 24, "D"));
            keyboardMap.put(Keyboard.Key.F, new Layout(126, 80, 24, 24, "F"));
            keyboardMap.put(Keyboard.Key.G, new Layout(152, 80, 24, 24, "G"));
            keyboardMap.put(Keyboard.Key.H, new Layout(178, 80, 24, 24, "H"));
            keyboardMap.put(Keyboard.Key.J, new Layout(204, 80, 24, 24, "J"));
            keyboardMap.put(Keyboard.Key.K, new Layout(230, 80, 24, 24, "K"));
            keyboardMap.put(Keyboard.Key.L, new Layout(256, 80, 24, 24, "L"));
            keyboardMap.put(Keyboard.Key.SEMICOLON, new Layout(282, 80, 24, 24, ":\n;"));
            keyboardMap.put(Keyboard.Key.APOSTROPHE, new Layout(308, 80, 24, 24, "\"\n'"));
            keyboardMap.put(Keyboard.Key.ENTER, new Layout(334, 80, 64, 24, "<-|"));
            keyboardMap.put(Keyboard.Key.KP_4, new Layout(482, 80, 24, 24, "4"));
            keyboardMap.put(Keyboard.Key.KP_5, new Layout(508, 80, 24, 24, "5"));
            keyboardMap.put(Keyboard.Key.KP_6, new Layout(534, 80, 24, 24, "6"));
            
            keyboardMap.put(Keyboard.Key.L_SHIFT, new Layout(0, 106, 64, 24, "Shift"));
            keyboardMap.put(Keyboard.Key.Z, new Layout(66, 106, 24, 24, "Z"));
            keyboardMap.put(Keyboard.Key.X, new Layout(92, 106, 24, 24, "X"));
            keyboardMap.put(Keyboard.Key.C, new Layout(118, 106, 24, 24, "C"));
            keyboardMap.put(Keyboard.Key.V, new Layout(144, 106, 24, 24, "V"));
            keyboardMap.put(Keyboard.Key.B, new Layout(170, 106, 24, 24, "B"));
            keyboardMap.put(Keyboard.Key.N, new Layout(196, 106, 24, 24, "N"));
            keyboardMap.put(Keyboard.Key.M, new Layout(222, 106, 24, 24, "M"));
            keyboardMap.put(Keyboard.Key.COMMA, new Layout(248, 106, 24, 24, "<\n,"));
            keyboardMap.put(Keyboard.Key.PERIOD, new Layout(274, 106, 24, 24, ">\n."));
            keyboardMap.put(Keyboard.Key.SLASH, new Layout(300, 106, 24, 24, "?\n/"));
            keyboardMap.put(Keyboard.Key.R_SHIFT, new Layout(326, 106, 72, 24, "Shift"));
            keyboardMap.put(Keyboard.Key.UP, new Layout(428, 106, 24, 24, "/\\"));
            keyboardMap.put(Keyboard.Key.KP_1, new Layout(482, 106, 24, 24, "1"));
            keyboardMap.put(Keyboard.Key.KP_2, new Layout(508, 106, 24, 24, "2"));
            keyboardMap.put(Keyboard.Key.KP_3, new Layout(534, 106, 24, 24, "3"));
            keyboardMap.put(Keyboard.Key.KP_ENTER, new Layout(560, 106, 24, 50, "<-|"));
            
            keyboardMap.put(Keyboard.Key.L_CONTROL, new Layout(0, 132, 42, 24, "Ctrl"));
            keyboardMap.put(Keyboard.Key.L_SUPER, new Layout(44, 132, 30, 24, "Super"));
            keyboardMap.put(Keyboard.Key.L_ALT, new Layout(76, 132, 30, 24, "Alt"));
            keyboardMap.put(Keyboard.Key.SPACE, new Layout(108, 132, 150, 24, "Space"));
            keyboardMap.put(Keyboard.Key.R_ALT, new Layout(260, 132, 30, 24, "Alt"));
            keyboardMap.put(Keyboard.Key.UNKNOWN, new Layout(292, 132, 30, 24, "Func"));
            keyboardMap.put(Keyboard.Key.MENU, new Layout(324, 132, 30, 24, "Menu"));
            keyboardMap.put(Keyboard.Key.R_CONTROL, new Layout(356, 132, 42, 24, "Ctrl"));
            keyboardMap.put(Keyboard.Key.LEFT, new Layout(402, 132, 24, 24, "{-"));
            keyboardMap.put(Keyboard.Key.DOWN, new Layout(428, 132, 24, 24, "\\/"));
            keyboardMap.put(Keyboard.Key.RIGHT, new Layout(454, 132, 24, 24, "-}"));
            keyboardMap.put(Keyboard.Key.KP_0, new Layout(482, 132, 50, 24, "0"));
            keyboardMap.put(Keyboard.Key.KP_DECIMAL, new Layout(534, 132, 24, 24, "."));
        }
        
        Color keyInactive = Color_RGBA.create().set(Color.GRAY).a(180);
        Color keyActive   = Color_RGBA.create().set(Color.LIGHT_GRAY).a(180);
        Color modActive   = Color_RGBA.create().set(Color.DARK_RED).a(180);
        
        Debug.addMenu(new Debug.Menu("Input")
        {
            final int spacing = 5;
            final int msWidth = 116;
            final int msHeight = 140;
            final int kbWidth = 584;
            final int kbHeight = 156;
            
            int lastDirH = -1;
            long lastDirHTime = -1;
            int lastDirV = -1;
            long lastDirVTime = -1;
            int lastScroll = -1;
            long lastScrollTime = -1;
            
            @Override
            protected void drawImpl()
            {
                int mx = (this.width - this.msWidth) / 2;
                int my = (this.height - (this.msHeight + this.spacing + this.kbHeight)) / 2;
                
                int kx = (this.width - this.kbWidth) / 2;
                int ky = my + (this.msHeight + this.spacing);
                
                Mouse    mouse    = Mouse.get();
                Keyboard keyboard = Keyboard.get();
                
                for (Mouse.Button button : mouseMap.keySet())
                {
                    Layout l = mouseMap.get(button);
                    Colorc c = mouse.held(button) ? keyActive : keyInactive;
                    
                    drawQuad(mx + l.x, my + l.y, l.width, l.height, c);
                    drawText(mx + l.tx, my + l.ty, l.text, Color.WHITE);
                }
                
                long delay = 100_000_000L;
                
                if (mouse.dx() < 0.0)
                {
                    this.lastDirH     = 0;
                    this.lastDirHTime = Time.getNS();
                }
                else if (mouse.dx() > 0.0)
                {
                    this.lastDirH     = 1;
                    this.lastDirHTime = Time.getNS();
                }
                
                if (this.lastDirH >= 0 && Time.getNS() - this.lastDirHTime < delay)
                {
                    switch (this.lastDirH)
                    {
                        case 0 -> {
                            String text = "{-";
                            int    x    = (26 - Debug.textWidth(text)) / 2;
                            int    y    = (this.msHeight - Debug.textHeight(text)) / 2;
                            drawTextWithBackground(mx + x, my + y, text, Color.WHITE, null);
                        }
                        case 1 -> {
                            String text = "-}";
                            int    x    = 90 + (26 - Debug.textWidth(text)) / 2;
                            int    y    = (this.msHeight - Debug.textHeight(text)) / 2;
                            drawTextWithBackground(mx + x, my + y, text, Color.WHITE, null);
                        }
                    }
                }
                else
                {
                    this.lastDirH = -1;
                }
                
                if (mouse.dy() > 0.0)
                {
                    this.lastDirV     = 0;
                    this.lastDirVTime = Time.getNS();
                }
                else if (mouse.dy() < 0.0)
                {
                    this.lastDirV     = 1;
                    this.lastDirVTime = Time.getNS();
                }
                
                if (this.lastDirV >= 0 && Time.getNS() - this.lastDirVTime < delay)
                {
                    switch (this.lastDirV)
                    {
                        case 0 -> {
                            String text = "\\/";
                            int    x    = (this.msWidth - Debug.textWidth(text)) / 2;
                            int    y    = 114 + (26 - Debug.textHeight(text)) / 2;
                            drawTextWithBackground(mx + x, my + y, text, Color.WHITE, keyInactive);
                        }
                        case 1 -> {
                            String text = "/\\";
                            int    x    = (this.msWidth - Debug.textWidth(text)) / 2;
                            int    y    = (26 - Debug.textHeight(text)) / 2;
                            drawTextWithBackground(mx + x, my + y, text, Color.WHITE, keyInactive);
                        }
                    }
                }
                else
                {
                    this.lastDirV = -1;
                }
                
                if (mouse.scrollY() < 0.0)
                {
                    this.lastScroll     = 0;
                    this.lastScrollTime = Time.getNS();
                }
                else if (mouse.scrollY() > 0.0)
                {
                    this.lastScroll     = 1;
                    this.lastScrollTime = Time.getNS();
                }
                
                if (this.lastScroll >= 0 && Time.getNS() - this.lastScrollTime < delay)
                {
                    switch (this.lastScroll)
                    {
                        case 0 -> {
                            String text = "\\/";
                            int    x    = 52 + 2 + (12 - Debug.textWidth(text)) / 2;
                            int    y    = 38 + 2;
                            drawText(mx + x, my + y, text, Color.WHITE);
                        }
                        case 1 -> {
                            String text = "/\\";
                            int    x    = 52 + 2 + (12 - Debug.textWidth(text)) / 2;
                            int    y    = 26 + 2;
                            drawText(mx + x, my + y, text, Color.WHITE);
                        }
                    }
                }
                else
                {
                    this.lastScroll = -1;
                }
                
                for (Keyboard.Key key : keyboardMap.keySet())
                {
                    Layout l = keyboardMap.get(key);
                    Colorc c = keyboard.held(key) ? keyActive : keyInactive;
                    
                    drawQuad(kx + l.x, ky + l.y, l.width, l.height, c);
                    drawText(kx + l.tx, ky + l.ty, l.text, Color.WHITE);
                }
                
                for (Modifier mod : modifierMap.keySet())
                {
                    Layout l = modifierMap.get(mod);
                    Colorc c = mod.isActive() ? modActive : keyInactive;
                    
                    drawQuad(kx + l.x, ky + l.y, l.width, l.height, c);
                    drawText(kx + l.tx, ky + l.ty, l.text, Color.WHITE);
                }
            }
        });
        
        Debug.addMenu(new Debug.Menu("Time")
        {
            final Color chartBackground = Color_RGBA.create().set(Color.GRAY).a(180);
            final Color chartLine = Color_RGBA.create().set(Color.WHITE).a(180);
            final Color barLow = Color_RGBA.create().set(Color.GREEN);
            final Color barHigh = Color_RGBA.create().set(Color.RED);
            final Color barTemp = Color_RGBA.create().set(Color.GREEN);
            
            @Override
            protected void drawImpl()
            {
                String[] lines = {
                        String.format("Frame: %s", Time.frameCount()),
                        String.format("Time: %.3f", Time.get()),
                        };
                
                int x = 0, y = 0;
                for (String line : lines)
                {
                    drawTextWithBackground(x, y, line, Color.WHITE, null);
                    y += Debug.textHeight(line);
                }
                
                int chartX = 0;
                int chartY = this.height >> 1;
                int chartW = Math.min(this.width * 2 / 3, 512);
                int chartH = this.height >> 1;
                
                long[] timeArray = Time.frameTimesRaw;
                int    barX, barY, barW, barH;
                int    ratio, r, g, b, a;
                for (int i = 0, n = timeArray.length; i < n; i++)
                {
                    barW = 1;
                    barH = Math.max(1, (int) (chartH * 30 * timeArray[i] / 1_000_000_000L));
                    
                    barX = chartX + chartW - 1 - i;
                    barY = chartY + chartH - 1 - barH;
                    
                    ratio = 255 * barH / chartH;
                    
                    r = Math.clamp((this.barHigh.r() * ratio + this.barLow.r() * (255 - ratio)) / 255, 0, 255);
                    g = Math.clamp((this.barHigh.g() * ratio + this.barLow.g() * (255 - ratio)) / 255, 0, 255);
                    b = Math.clamp((this.barHigh.b() * ratio + this.barLow.b() * (255 - ratio)) / 255, 0, 255);
                    a = 255;
                    
                    drawQuad(barX, barY, barW, barH, this.barTemp.set(r, g, b, a));
                    
                    if (barX < 0) break;
                }
                
                int lineThickness = 1;
                
                drawQuad(chartX, chartY, chartW, chartH, this.chartBackground);
                drawQuad(chartX, chartY, chartW, lineThickness, this.chartLine);
                drawQuad(chartX, chartY + (chartH >> 1), chartW, lineThickness, this.chartLine);
    
                long min = Math.min(Time.frameTimesRaw);
                long avg = (long) Math.mean(Time.frameTimesRaw);
                long max = Math.max(Time.frameTimesRaw);
    
                String text;
                int    textW, textH;
    
                text  = String.format("Min: %s us", min / 1_000L);
                textH = textHeight(text);
                drawText(chartX + 1, chartY - textH, text, Color.WHITE);
    
                text  = String.format("Avg: %s us", avg / 1_000L);
                textW = textWidth(text);
                textH = textHeight(text);
                drawText(chartX + ((chartW - textW) >> 1), chartY - textH, text, Color.WHITE);
    
                text  = String.format("Max: %s us", max / 1_000L);
                textW = textWidth(text);
                textH = textHeight(text);
                drawText(chartX + chartW - textW, chartY - textH, text, Color.WHITE);
                
                text = "30";
                textW = textWidth(text);
                drawText(chartX + chartW - textW, chartY + lineThickness, text, Color.WHITE);
                
                text = "60";
                textW = textWidth(text);
                drawText(chartX + chartW - textW, chartY + (chartH >> 1) + lineThickness, text, Color.WHITE);
            }
        });
    }
    
    static void destroy()
    {
        Debug.program.delete();
        MemoryUtil.memFree(Debug.vertexBuffer);
        Debug.vertexArray.delete();
    }
    
    static void handleEvents()
    {
        if (Modifier.all(Modifier.SHIFT, Modifier.CONTROL, Modifier.ALT))
        {
            for (EventKeyboardKeyDown event : Engine.Events.get(EventKeyboardKeyDown.class))
            {
                switch (event.key())
                {
                    case F10 -> {
                        Engine.wireframe = !Engine.wireframe;
                        Debug.notification(Engine.wireframe ? "Wireframe Mode: On" : "Wireframe Mode: Off");
                        event.consume();
                    }
                    case LEFT -> {
                        if (Debug.currentMenu >= 0)
                        {
                            Debug.currentMenu = Math.index(Debug.currentMenu - 1, Debug.menus.size());
                            event.consume();
                        }
                    }
                    case RIGHT -> {
                        if (Debug.currentMenu >= 0)
                        {
                            Debug.currentMenu = Math.index(Debug.currentMenu + 1, Debug.menus.size());
                            event.consume();
                        }
                    }
                    case F11 -> {
                        Debug.enabled = !Debug.enabled;
                        Debug.notification(Debug.enabled ? "Debug Mode: On" : "Debug Mode: Off");
                        event.consume();
                    }
                    case F12 -> {
                        Time.paused = !Time.paused;
                        Debug.notification(Time.paused ? "Engine Paused" : "Engine Unpaused");
                        event.consume();
                    }
                }
            }
        }
    }
    
    static void draw()
    {
        if (Debug.enabled)
        {
            if (Debug.currentMenu < 0)
            {
                String text = "No Menus";
                int    x    = (Window.get().framebufferWidth() - Debug.textWidth(text)) / 2;
                drawTextWithBackground(x, 0, text, Color.WHITE, null);
            }
            else
            {
                Menu menu = Debug.menus.get(Debug.currentMenu);
                
                menu.draw();
                
                int spacing = 2;
                
                Menu prev = Debug.menus.get(Math.index(Debug.currentMenu - 1, Debug.menus.size()));
                Menu next = Debug.menus.get(Math.index(Debug.currentMenu + 1, Debug.menus.size()));
                
                int currWidth = Debug.textWidth(menu.name) + 2;
                int prevWidth = Debug.textWidth(prev.name) + 2;
                
                int currPos = (Window.get().framebufferWidth() - currWidth) / 2;
                int prevPos = currPos - spacing - prevWidth;
                int nextPos = currPos + currWidth + spacing;
                
                drawTextWithBackground(currPos, 0, menu.name, Color.WHITE, null);
                drawTextWithBackground(prevPos, 0, prev.name, Color.LIGHT_GRAY, null);
                drawTextWithBackground(nextPos, 0, next.name, Color.LIGHT_GRAY, null);
            }
        }
        if (Debug.notification != null && Time.getNS() - Debug.notificationTime < Debug.notificationDur)
        {
            int x = (Window.get().framebufferWidth() - textWidth(Debug.notification)) >> 1;
            int y = (Window.get().framebufferHeight() - textHeight(Debug.notification)) >> 1;
            
            drawTextWithBackground(x, y, Debug.notification, Color.WHITE, null);
        }
        if (!Debug.renderables.isEmpty())
        {
            int fbWidth  = Window.get().framebufferWidth();
            int fbHeight = Window.get().framebufferHeight();
            
            GL33.glViewport(0, 0, fbWidth, fbHeight);
            
            GLProgram.bind(Debug.program);
            GLProgram.Uniform.mat4("pv", Debug.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
            
            GLState.winding(Winding.CW);
            
            int quads = 0;
            // int draws = 0; // TODO - Track Draws
            
            Renderable renderable;
            while ((renderable = Debug.renderables.poll()) != null)
            {
                if (Debug.vertexBuffer.remaining() < renderable.bytesToAdd())
                {
                    Debug.vertexArray.buffer(0).set(0, Debug.vertexBuffer.clear());
                    Debug.vertexArray.draw(DrawMode.TRIANGLES, quads * 6);
                    quads = 0;
                    // draws++;
                }
                
                quads += renderable.render();
            }
            
            Debug.vertexArray.buffer(0).set(0, Debug.vertexBuffer.clear());
            Debug.vertexArray.draw(DrawMode.TRIANGLES, quads * 6);
            // draws++;
        }
    }
    
    /**
     * @return {@code true} if debug mode is enabled
     */
    public static boolean enabled()
    {
        return Debug.enabled;
    }
    
    public static void notification(String notification)
    {
        Debug.notification     = notification;
        Debug.notificationTime = Time.getNS();
    }
    
    /**
     * Draws a colored quad to the screen.
     *
     * @param x      The x coordinate of the top left point if the quad.
     * @param y      The y coordinate of the top left point if the quad.
     * @param width  The width of the quad.
     * @param height The height of the quad.
     * @param color  The color of the quad.
     */
    public static void drawQuad(int x, int y, int width, int height, @NotNull Colorc color)
    {
        Debug.renderables.offer(new Quad(x, y, width, height, color));
    }
    
    /**
     * Draws Debug text to the screen.
     *
     * @param x     The x coordinate of the top left point if the text.
     * @param y     The y coordinate of the top left point if the text.
     * @param text  The text to render.
     * @param color The color of the text.
     */
    public static void drawText(int x, int y, String text, @NotNull Colorc color)
    {
        Debug.renderables.offer(new Text(x, y + 2, text, color));
    }
    
    /**
     * Draws Debug text to the screen with a background.
     *
     * @param x         The x coordinate of the top left point if the text.
     * @param y         The y coordinate of the top left point if the text.
     * @param text      The text to render.
     * @param color     The color of the text.
     * @param backColor The color of the background.
     */
    public static void drawTextWithBackground(int x, int y, String text, @NotNull Colorc color, @Nullable Colorc backColor)
    {
        if (backColor == null) backColor = Debug.defaultBackColor;
        
        int w = Debug.textWidth(text) + 2;
        int h = Debug.textHeight(text);
        Debug.renderables.offer(new Quad(x, y, w, h, backColor));
        Debug.renderables.offer(new Text(x + 2, y + 2, text, color));
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
    
    public static void addMenu(Menu menu)
    {
        Optional<Menu> existing = Debug.menus.stream().filter(m -> menu.name.equals(m.name)).findAny();
        if (existing.isPresent())
        {
            throw new RuntimeException(String.format("Menu with name \"%s\" already exists", menu.name));
        }
        Debug.menus.add(menu);
        Debug.currentMenu = 0;
    }
    
    // /**
    //  * @return The Engine's Profiler instance.
    //  */
    // public static Profiler profiler()
    // {
    //     return Debug.profiler;
    // }
    
    private interface Renderable
    {
        int bytesToAdd();
        
        int render();
    }
    
    private static class Quad implements Renderable
    {
        private final float x1, y1, x2, y2;
        private final byte r, g, b, a;
        
        private Quad(int x, int y, int width, int height, Colorc color)
        {
            this.x1 = x;
            this.y1 = y;
            this.x2 = x + width;
            this.y2 = y + height;
            this.r  = (byte) color.r();
            this.g  = (byte) color.g();
            this.b  = (byte) color.b();
            this.a  = (byte) color.a();
        }
        
        @Override
        public int bytesToAdd()
        {
            // 64 bytes per quad.
            return 64;
        }
        
        @Override
        public int render()
        {
            Debug.vertexBuffer.putFloat(this.x1);
            Debug.vertexBuffer.putFloat(this.y1);
            Debug.vertexBuffer.putFloat(0.0F);
            Debug.vertexBuffer.put(this.r);
            Debug.vertexBuffer.put(this.g);
            Debug.vertexBuffer.put(this.b);
            Debug.vertexBuffer.put(this.a);
            Debug.vertexBuffer.putFloat(this.x2);
            Debug.vertexBuffer.putFloat(this.y1);
            Debug.vertexBuffer.putFloat(0.0F);
            Debug.vertexBuffer.put(this.r);
            Debug.vertexBuffer.put(this.g);
            Debug.vertexBuffer.put(this.b);
            Debug.vertexBuffer.put(this.a);
            Debug.vertexBuffer.putFloat(this.x2);
            Debug.vertexBuffer.putFloat(this.y2);
            Debug.vertexBuffer.putFloat(0.0F);
            Debug.vertexBuffer.put(this.r);
            Debug.vertexBuffer.put(this.g);
            Debug.vertexBuffer.put(this.b);
            Debug.vertexBuffer.put(this.a);
            Debug.vertexBuffer.putFloat(this.x1);
            Debug.vertexBuffer.putFloat(this.y2);
            Debug.vertexBuffer.putFloat(0.0F);
            Debug.vertexBuffer.put(this.r);
            Debug.vertexBuffer.put(this.g);
            Debug.vertexBuffer.put(this.b);
            Debug.vertexBuffer.put(this.a);
            
            return 1;
        }
    }
    
    private static class Text implements Renderable
    {
        private final float x, y;
        private final String t;
        private final byte   r, g, b, a;
        
        private Text(int x, int y, String text, Colorc color)
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
        public int render()
        {
            int newQuads;
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                ByteBuffer textColor = stack.malloc(4).put(this.r).put(this.g).put(this.b).put(this.a);
                newQuads = stb_easy_font_print(this.x, this.y, this.t, textColor.clear(), Debug.vertexBuffer);
                Debug.vertexBuffer.position(Debug.vertexBuffer.position() + newQuads * 64);
            }
            return newQuads;
        }
    }
    
    public static abstract class Menu
    {
        public final String name;
        
        protected int width, height;
        
        public Menu(@NotNull String name)
        {
            this.name = name;
        }
        
        protected void draw()
        {
            this.width  = Window.get().framebufferWidth();
            this.height = Window.get().framebufferHeight() - Debug.headerSize;
            drawImpl();
        }
        
        protected abstract void drawImpl();
        
        /**
         * Draws a colored quad to the screen.
         *
         * @param x      The x coordinate of the top left point if the quad.
         * @param y      The y coordinate of the top left point if the quad.
         * @param width  The width of the quad.
         * @param height The height of the quad.
         * @param color  The color of the quad.
         */
        protected void drawQuad(int x, int y, int width, int height, @NotNull Colorc color)
        {
            Debug.drawQuad(x, y + Debug.headerSize, width, height, color);
        }
        
        /**
         * Draws Debug text to the screen.
         *
         * @param x     The x coordinate of the top left point if the text.
         * @param y     The y coordinate of the top left point if the text.
         * @param text  The text to render.
         * @param color The color of the text.
         */
        protected void drawText(int x, int y, String text, @NotNull Colorc color)
        {
            Debug.drawText(x, y + Debug.headerSize, text, color);
        }
        
        /**
         * Draws Debug text to the screen with a background.
         *
         * @param x         The x coordinate of the top left point if the text.
         * @param y         The y coordinate of the top left point if the text.
         * @param text      The text to render.
         * @param color     The color of the text.
         * @param backColor The color of the background.
         */
        protected void drawTextWithBackground(int x, int y, @NotNull String text, @NotNull Colorc color, @Nullable Colorc backColor)
        {
            Debug.drawTextWithBackground(x, y + Debug.headerSize, text, color, backColor);
        }
    }
    
    private static final class Layout
    {
        private final int x, y, width, height, tx, ty;
        private final String text;
        
        private Layout(int x, int y, int width, int height, String text)
        {
            this.x      = x;
            this.y      = y;
            this.width  = width;
            this.height = height;
            this.text   = text;
            this.tx     = x + (width - Debug.textWidth(text)) / 2 + 1;
            this.ty     = y + (height - Debug.textHeight(text)) / 2 + 2;
        }
    }
}
