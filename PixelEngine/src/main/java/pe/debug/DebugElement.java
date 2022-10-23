package pe.debug;

import org.jetbrains.annotations.Nullable;
import pe.event.*;
import pe.shape.AABB2i;

public abstract class DebugElement
{
    public final AABB2i rect = new AABB2i();
    
    protected boolean hovered;
    protected boolean focused;
    
    public boolean hovered()
    {
        return this.hovered;
    }
    
    public boolean focused()
    {
        return this.focused;
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
