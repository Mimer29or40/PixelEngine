package pe.debug;

import pe.Debug2;
import pe.Engine;
import pe.Mouse;
import pe.color.Color;
import pe.color.Colorc;
import pe.event.*;

import java.util.LinkedList;
import java.util.List;

public class DebugGUI
{
    private final List<DebugWindow> windows = new LinkedList<>();
    private       DebugWindow       hoveredWindow;
    private       DebugWindow       focusedWindow;
    
    public void handleEvents()
    {
        for (Event event : Engine.Events.get())
        {
            if (event instanceof EventMouseMoved mMoved)
            {
                int x = (int) mMoved.x();
                int y = (int) mMoved.y();
                
                int hoveredIndex = -1;
                for (int i = 0; i < this.windows.size(); i++)
                {
                    DebugWindow window = this.windows.get(i);
                    if (hoveredIndex < 0 && window.rect.contains(x, y)) hoveredIndex = i;
                    window.hovered = false;
                }
                if (hoveredIndex >= 0)
                {
                    this.hoveredWindow = this.windows.get(hoveredIndex);
                    this.hoveredWindow.hovered = true;
                    
                    // TODO - Change Cursor
                    // x -= this.hoveredWindow.rect.x();
                    // y -= this.hoveredWindow.rect.y();
                    //
                    // int w = this.hoveredWindow.rect.width();
                    // int h = this.hoveredWindow.rect.height();
                    //
                    // if (x <= DebugWindow.BORDER_SIZE)
                    // {
                    //     if (y <= DebugWindow.BORDER_SIZE)
                    //     {
                    //         Mouse.shape(Mouse.Shape.RESIZE_NWSE_CURSOR);
                    //     }
                    //     else if (y >= h - DebugWindow.BORDER_SIZE)
                    //     {
                    //         Mouse.shape(Mouse.Shape.RESIZE_NESW_CURSOR);
                    //     }
                    //     else
                    //     {
                    //         Mouse.shape(Mouse.Shape.RESIZE_EW_CURSOR);
                    //     }
                    // }
                    // else
                    // {
                    //     Mouse.shape(Mouse.Shape.ARROW_CURSOR);
                    // }
                    
                    mMoved.consume();
                }
                else
                {
                    this.hoveredWindow = null;
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
                for (int i = 0; i < this.windows.size(); i++)
                {
                    DebugWindow window = this.windows.get(i);
                    if (focusedIndex < 0 && window.rect.contains(x, y)) focusedIndex = i;
                    window.focused = false;
                }
                if (focusedIndex >= 0)
                {
                    this.focusedWindow = this.windows.remove(focusedIndex);
                    this.focusedWindow.focused = true;
                    this.windows.add(0, this.focusedWindow);
                    mbDown.consume();
                }
                else
                {
                    this.focusedWindow = null;
                }
            }
            else if (event instanceof EventMouseButtonUp mbUp)
            {
                // NO-OP
            }
            else if (event instanceof EventMouseButtonDragged mbDragged)
            {
                if (this.focusedWindow != null)
                {
                    int dx = (int) mbDragged.dx();
                    int dy = (int) mbDragged.dy();
                    this.focusedWindow.rect.pos.add(dx, dy);
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
    
    public void draw()
    {
        for (int i = this.windows.size() - 1; i >= 0; i--)
        {
            this.windows.get(i).draw();
        }
    }
    
    public boolean addWindow(DebugWindow window)
    {
        return this.windows.add(window);
    }
    
    public boolean removeWindow(DebugWindow window)
    {
        return this.windows.remove(window);
    }
}
