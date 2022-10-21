package pe.debug;

import org.jetbrains.annotations.NotNull;
import pe.Debug;
import pe.Debug2;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
import pe.render.ScissorMode;
import pe.shape.AABB2i;
import pe.shape.AABB2ic;

import java.util.ArrayList;
import java.util.List;

public class DebugWindow
{
    public static final int    BORDER_SIZE            = 4;
    public static final int    PADDING_SIZE           = 4;
    public static final int    ELEMENT_SPACING_SIZE   = 2;
    public static final int    HEADER_SIZE            = Debug.textHeight("TEXT") + 2 * BORDER_SIZE;
    public static final Colorc HEADER_FOCUSED_COLOR   = Color_RGBA.create().set(Color.GRAY);
    public static final Colorc HEADER_UNFOCUSED_COLOR = Color_RGBA.create().set(Color.BLACK);
    public static final Colorc BORDER_COLOR           = Color_RGBA.create().set(Color.WHITE);
    public static final Colorc BACKGROUND_COLOR       = Color_RGBA.create().set(Color.BLACK).a(180);
    
    public final String name;
    
    protected final List<DebugElement> elements = new ArrayList<>();
    
    public final AABB2i rect = new AABB2i();
    
    protected boolean hovered;
    protected boolean focused;
    
    public DebugWindow(String name)
    {
        this.name = name;
        
        this.rect.size.set(400, 400);
    }
    
    public void handleEvents()
    {
    
    }
    
    public void draw()
    {
        int x = this.rect.x();
        int y = this.rect.y();
        int w = this.rect.width();
        int h = this.rect.height();
        
        int b = DebugWindow.BORDER_SIZE;
        int b2 = b << 1;
        
        int textH = Debug2.textHeight(this.name);
    
        Debug2.scissor(ScissorMode.NONE);
        Debug2.drawRect(x, y, w, h, b, DebugWindow.BORDER_COLOR);
        Debug2.drawFilledRect(x + b, y + textH + b, w - b2, b, DebugWindow.BORDER_COLOR);
        
        Colorc headerColor = this.focused ? DebugWindow.HEADER_FOCUSED_COLOR : DebugWindow.HEADER_UNFOCUSED_COLOR;
        Debug2.drawFilledRect(x + b, y + b, w - b2, textH, headerColor);
        Debug2.drawText(x + b + 1, y + b, this.name, Color.WHITE);
        
        int contentX = x + b;
        int contentY = y + b2 + textH;
        int contentW = w - b2;
        int contentH = h - b - b2 - textH;
    
        Debug2.drawFilledRect(contentX, contentY, contentW, contentH, DebugWindow.BACKGROUND_COLOR);
    
        contentX += DebugWindow.PADDING_SIZE;
        contentY += DebugWindow.PADDING_SIZE;
        contentW -= DebugWindow.PADDING_SIZE << 1;
        contentH -= DebugWindow.PADDING_SIZE << 1;
    
        Debug2.scissor(contentX, contentY, contentW, contentH);
        
        for (DebugElement element : this.elements)
        {
            element.draw(contentX, contentY, contentW, contentH);
            contentY += element.rect.height() + DebugWindow.ELEMENT_SPACING_SIZE;
        }
    }
    
    public boolean hovered()
    {
        return this.hovered;
    }
    
    public boolean focused()
    {
        return this.focused;
    }
    
    public boolean addElements(DebugElement elements)
    {
        return this.elements.add(elements);
    }
    
    public boolean removeElements(DebugElement elements)
    {
        return this.elements.remove(elements);
    }
    
    public enum ResizeMode
    {
        TOP_LEFT, TOP, TOP_RIGHT,
        LEFT, NONE, RIGHT,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT,
        ;
        
        public static ResizeMode get(int x, int y, @NotNull AABB2ic rect)
        {
            int w = rect.width();
            int h = rect.height();
            
            x -= rect.x();
            y -= rect.y();
            if (x < 0 || x >= w || y < 0 || y >= h) return NONE;
            
            if (x <= DebugWindow.BORDER_SIZE)
            {
                if (y <= DebugWindow.BORDER_SIZE)
                {
                    return TOP_LEFT;
                }
                else if (y >= h - DebugWindow.BORDER_SIZE)
                {
                    return BOTTOM_LEFT;
                }
                else
                {
                    return LEFT;
                }
            }
            else if (x >= w - DebugWindow.BORDER_SIZE)
            {
                if (y <= DebugWindow.BORDER_SIZE)
                {
                    return TOP_RIGHT;
                }
                else if (y >= h - DebugWindow.BORDER_SIZE)
                {
                    return BOTTOM_RIGHT;
                }
                else
                {
                    return RIGHT;
                }
            }
            else
            {
                if (y <= DebugWindow.BORDER_SIZE)
                {
                    return TOP;
                }
                else if (y >= h - DebugWindow.BORDER_SIZE)
                {
                    return BOTTOM;
                }
                else
                {
                    return NONE;
                }
            }
        }
    }
}
