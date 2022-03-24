package pe;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import pe.color.Color;
import pe.gui.GuiStyle;
import pe.gui.GuiWindow;
import pe.render.*;
import rutils.Logger;

import java.util.LinkedList;
import java.util.List;

public class GUI
{
    private static final Logger LOGGER = new Logger();
    
    static GuiStyle style;
    
    static final List<GuiWindow> Windows           = new LinkedList<>(); // Windows, sorted in display order, back to front
    static final List<GuiWindow> WindowsFocusOrder = new LinkedList<>(); // Root windows, sorted in focus order, back to front.
    
    static Vector2d  WindowsHoverPadding;            // Padding around resizable windows for which hovering on counts as hovering the window == ImMax(style.TouchExtraPadding, WINDOWS_HOVER_PADDING)
    static GuiWindow CurrentWindow;                  // Window being drawn into
    static GuiWindow HoveredWindow;                  // Window the mouse is hovering. Will typically catch mouse inputs.
    static GuiWindow HoveredWindowUnderMovingWindow; // Hovered window ignoring MovingWindow. Only set if MovingWindow is set.
    static GuiWindow MovingWindow;                   // Track the window we clicked on (in order to preserve focus). The actual window that is moved is generally MovingWindow->RootWindow.
    static GuiWindow WheelingWindow;                 // Track the window we started mouse-wheeling on. Until a timer elapse or mouse has moved, generally keep scrolling the same window even if during the course of scrolling the mouse ends up hovering a child window.
    static Vector2d  WheelingWindowRefMousePos;
    static float     WheelingWindowTimer;
    
    static void setup()
    {
        GUI.LOGGER.fine("Setup");
        
        GUI.style = new GuiStyle();
        
        // windows.remove(1)
        // windows.add();
        
        GuiWindow window;
        
        window = new GuiWindow();
        window.bounds.pos.set(10, 10);
        window.bounds.size.set(100, 100);
        Windows.add(window);
        
        window = new GuiWindow();
        window.bounds.pos.set(200, 200);
        window.bounds.size.set(100, 100);
        Windows.add(window);
    }
    
    static void destroy()
    {
        GUI.LOGGER.fine("Destroy");
    }
    
    static void handleEvents()
    {
    
    }
    
    static void draw()
    {
        GLFramebuffer.bind(null);
        GLProgram.bind(null);
        
        GL.defaultState();
        GL.depthMode(DepthMode.NONE);
        
        GLBatch.bind(null);
        
        int r = Window.framebufferWidth() >> 1;
        int l = -r;
        int b = Window.framebufferHeight() >> 1;
        int t = -b;
        
        GLBatch.matrixMode(MatrixMode.PROJECTION);
        GLBatch.loadIdentity();
        GLBatch.ortho(l, r, b, t, 1.0, -1.0);
        
        GLBatch.matrixMode(MatrixMode.VIEW);
        GLBatch.loadIdentity();
        GLBatch.translate(l, t, 0.0);
        
        GLBatch.matrixMode(MatrixMode.MODEL);
        GLBatch.loadIdentity();
        
        GLBatch.matrixMode(MatrixMode.NORMAL);
        GLBatch.loadIdentity();
        
        GLBatch.colorMode(ColorMode.DIFFUSE);
        GLBatch.loadWhite();
        
        GLBatch.colorMode(ColorMode.SPECULAR);
        GLBatch.loadWhite();
        
        GLBatch.colorMode(ColorMode.AMBIENT);
        GLBatch.loadWhite();
        
        for (GuiWindow window : Windows)
        {
            int x0 = window.bounds.pos.x;
            int y0 = window.bounds.pos.y;
            int x1 = x0 + window.bounds.size.x;
            int y1 = y0 + window.bounds.size.y;
            
            Engine.Draw.fillRect2D().corners(x0, y0, x1, y1).color(Color.WHITE).draw();
            // Engine.Draw.fillRect2D().point(window.pos).size(window.size).color(Color.WHITE).draw();
        }
        
        GLBatch.stats();
    }
    
    private GUI() {}
    
    public static @NotNull GuiStyle GetStyle()
    {
        return GUI.style;
    }
}
