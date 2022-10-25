package pe.debug;

import pe.Debug2;
import pe.color.Color;
import pe.color.Colorc;
import pe.event.EventMouseButton;
import pe.event.EventMouseButtonDown;
import pe.event.EventMouseButtonDragged;
import pe.event.EventMouseButtonUp;

public class DebugButton extends Element
{
    public static final Colorc TEXT_COLOR               = Color.WHITE;
    public static final Colorc HOVERED_TEXT_COLOR       = Color.WHITE;
    public static final Colorc PRESSED_TEXT_COLOR       = Color.LIGHT_GRAY;
    public static final Colorc TOGGLED_TEXT_COLOR       = Color.WHITE;
    public static final Colorc BACKGROUND_COLOR         = Color.GRAY;
    public static final Colorc HOVERED_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    public static final Colorc PRESSED_BACKGROUND_COLOR = Color.DARK_GRAY;
    public static final Colorc TOGGLED_BACKGROUND_COLOR = Color.LIGHT_BLUE;
    
    public String  text;
    public boolean toggleable;
    
    private boolean mouseDown;
    private boolean toggled;
    
    public DebugButton(String text, boolean toggleable)
    {
        this.text       = text;
        this.toggleable = toggleable;
    }
    
    @Override
    public void onMouseButtonDown(EventMouseButtonDown mbDown)
    {
        super.onMouseButtonDown(mbDown);
        
        this.mouseDown = true;
    
        mbDown.consume();
    }
    
    @Override
    public void onMouseButtonUp(EventMouseButtonUp mbUp)
    {
        super.onMouseButtonUp(mbUp);
        
        if (getTopElementAt((int) mbUp.x(), (int) mbUp.y()) != null)
        {
            if (this.toggleable)
            {
                this.toggled = !this.toggled;
            }
            onButtonPressed(mbUp);
        }
        
        this.mouseDown = false;
    
        mbUp.consume();
    }
    
    @Override
    public void onMouseButtonDragged(EventMouseButtonDragged mbDragged)
    {
        super.onMouseButtonDragged(mbDragged);
    
        mbDragged.consume();
    }
    
    public void onButtonPressed(EventMouseButton event)
    {
    
    }
    
    @Override
    protected void layout(int contentX, int contentY, int contentW, int contentH)
    {
        if (this.text == null) this.text = "";
        
        int textW = Debug2.textWidth(this.text);
        int textH = Debug2.textHeight(this.text);
        
        this.rect.pos.set(contentX, contentY);
        if (this.rect.size.x < textW) this.rect.size.x = textW;
        if (this.rect.size.y < textH) this.rect.size.y = textH;
    }
    
    @Override
    protected void draw()
    {
        Colorc textColor       = DebugButton.TEXT_COLOR;
        Colorc backgroundColor = DebugButton.BACKGROUND_COLOR;
        if (this.mouseDown)
        {
            textColor       = DebugButton.PRESSED_TEXT_COLOR;
            backgroundColor = DebugButton.PRESSED_BACKGROUND_COLOR;
        }
        else if (this.hovered)
        {
            textColor       = DebugButton.HOVERED_TEXT_COLOR;
            backgroundColor = DebugButton.HOVERED_BACKGROUND_COLOR;
        }
        else if (this.toggled)
        {
            textColor       = DebugButton.TOGGLED_TEXT_COLOR;
            backgroundColor = DebugButton.TOGGLED_BACKGROUND_COLOR;
        }
        
        Debug2.drawTextWithBackground(this.rect.x(), this.rect.y(), this.text, textColor, backgroundColor);
    }
}
