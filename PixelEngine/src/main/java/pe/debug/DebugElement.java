package pe.debug;

import org.jetbrains.annotations.Nullable;
import pe.event.*;
import pe.shape.AABB2i;

public abstract class DebugElement
{
    public final AABB2i rect = new AABB2i();
    
    protected boolean hovered;
    protected boolean focused;
    
    public @Nullable DebugElement getHoveredElement(int x, int y)
    {
        if (this.rect.contains(x, y))
        {
            this.hovered = true;
            return this;
        }
        this.hovered = false;
        return null;
    }
    
    public @Nullable DebugElement getFocusedElement(int x, int y)
    {
        if (this.rect.contains(x, y))
        {
            this.focused = true;
            return this;
        }
        this.focused = false;
        return null;
    }
    
    public void onMouseMoved(EventMouseMoved mMoved)
    {
        this.hovered = true;
    }
    
    public void onMouseScrolled(EventMouseScrolled mScrolled)
    {
    
    }
    
    public void onMouseButtonDown(EventMouseButtonDown mbDown)
    {
        this.focused = true;
    }
    
    public void onMouseButtonUp(EventMouseButtonUp mbUp)
    {
    
    }
    
    public void onMouseButtonDragged(EventMouseButtonDragged mbDragged)
    {
    
    }
    
    public void onKeyboardKeyDown(EventKeyboardKeyDown kkDown)
    {
    
    }
    
    public void onKeyboardKeyUp(EventKeyboardKeyUp kkUp)
    {
    
    }
    
    public void onKeyboardKeyRepeated(EventKeyboardKeyRepeated kkRepeated)
    {
    
    }
    
    public void onKeyboardTyped(EventKeyboardTyped kTyped)
    {
    
    }
    
    public abstract void draw(int contentX, int contentY, int contentW, int contentH);
    
    public void unhover()
    {
        this.hovered = false;
    }
    
    public void unfocus()
    {
        this.focused = false;
    }
}
