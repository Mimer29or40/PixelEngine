package pe.debug;

import pe.Debug;
import pe.Debug2;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
import pe.shape.AABB2i;

import java.util.ArrayList;
import java.util.List;

public class DebugWindow
{
    public static final int    BORDER_SIZE            = 4;
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
        int b2 = b * 2;
        
        int textH = Debug2.textHeight(this.name);
        
        Debug2.drawBox(x, y, w, h, b, DebugWindow.BORDER_COLOR);
        Debug2.drawFilledBox(x + b, y + textH + b, w - b2, b, DebugWindow.BORDER_COLOR);
        
        Colorc headerColor = this.focused ? DebugWindow.HEADER_FOCUSED_COLOR : DebugWindow.HEADER_UNFOCUSED_COLOR;
        Debug2.drawFilledBox(x + b, y + b, w - b2, textH, headerColor);
        Debug2.drawText(x + b + 1, y + b, this.name, Color.WHITE);
        
        Debug2.drawFilledBox(x + b, y + b2 + textH, w - b2, h - b - b2 - textH, DebugWindow.BACKGROUND_COLOR);
    }
    
    public boolean hovered()
    {
        return this.hovered;
    }
    
    public boolean focused()
    {
        return this.focused;
    }
}
