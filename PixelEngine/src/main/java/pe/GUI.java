package pe;

import org.jetbrains.annotations.NotNull;
import pe.color.Color;
import pe.color.Colorc;
import pe.event.*;
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
    
    static final List<GuiWindow> Windows = new LinkedList<>(); // Windows, sorted in display order, back to front
    
    static GuiWindow hoveredWindow = null;
    static GuiWindow focusedWindow = null;
    
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
    
    @SuppressWarnings("StatementWithEmptyBody")
    static void handleEvents()
    {
        for (Event event : Engine.Events.get())
        {
            if (event instanceof EventMouseMoved mMoved)
            {
                int x = (int) mMoved.x();
                int y = (int) mMoved.y();
    
                int hoveredIndex = -1;
                for (int i = 0; i < Windows.size(); i++)
                {
                    GuiWindow window = Windows.get(i);
                    if (hoveredIndex < 0 && window.bounds.contains(x, y)) hoveredIndex = i;
                    window.hovered = false;
                }
                if (hoveredIndex >= 0)
                {
                    hoveredWindow = Windows.get(hoveredIndex);
                    hoveredWindow.hovered = true;
                    mMoved.consume();
                }
                else
                {
                    hoveredWindow = null;
                }
            }
            else if (event instanceof EventMouseScrolled)
            {
                // NO-OP
            }
            else if (event instanceof EventMouseButtonDown mbDown)
            {
                int x = (int) mbDown.x();
                int y = (int) mbDown.y();
    
                int focusedIndex = -1;
                for (int i = 0; i < Windows.size(); i++)
                {
                    GuiWindow window = Windows.get(i);
                    if (focusedIndex < 0 && window.bounds.contains(x, y)) focusedIndex = i;
                    window.focused = false;
                }
                if (focusedIndex >= 0)
                {
                    focusedWindow = Windows.remove(focusedIndex);
                    focusedWindow.focused = true;
                    Windows.add(0, focusedWindow);
                    mbDown.consume();
                }
                else
                {
                    focusedWindow = null;
                }
            }
            else if (event instanceof EventMouseButtonUp mbUp)
            {
                // NO-OP
            }
            else if (event instanceof EventMouseButtonDragged mbDragged)
            {
                if (hoveredWindow != null)
                {
                    int dx = (int) mbDragged.dx();
                    int dy = (int) mbDragged.dy();
                    hoveredWindow.bounds.pos.add(dx, dy);
                    mbDragged.consume();
                }
            }
            else if (event instanceof EventKeyboardKeyDown kkDown)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardKeyUp kkUp)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardKeyRepeated kkRepeated)
            {
                // NO-OP
            }
            else if (event instanceof EventKeyboardTyped kTyped)
            {
                // NO-OP
            }
        }
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
        
        for (int i = Windows.size() - 1; i >= 0; i--)
        {
            GuiWindow window = Windows.get(i);
            
            int x0 = window.bounds.pos.x;
            int y0 = window.bounds.pos.y;
            int x1 = x0 + window.bounds.size.x;
            int y1 = y0 + window.bounds.size.y;
            
            Colorc color = window.focused ? Color.WHITE : window.hovered ? Color.DARK_GRAY : Color.GRAY;
            Engine.Draw.fillRect2D().corners(x0, y0, x1, y1).color(color).draw();
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
