package pe.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.event.*;
import pe.shape.AABB2i;
import rutils.Logger;

public abstract class Element
{
    private static final Logger LOGGER = new Logger();
    
    protected Element parent;
    
    public final AABB2i rect = new AABB2i();
    
    protected boolean hovered;
    protected boolean focused;
    
    public @Nullable Element getParent()
    {
        return this.parent;
    }
    
    public @NotNull Element getRoot()
    {
        if (this.parent == null) return this;
        return this.parent.getRoot();
    }
    
    public @Nullable Element getTopElementAt(int x, int y)
    {
        if (this.rect.contains(x, y)) return this;
        return null;
    }
    
    public boolean isHovered()
    {
        return this.hovered;
    }
    
    public boolean isFocused()
    {
        return this.focused;
    }
    
    protected void layout(int contentX, int contentY, int contentW, int contentH)
    {
        this.rect.pos.set(contentX, contentY);
        this.rect.size.set(contentW, contentH);
    }
    
    protected abstract void draw();
    
    public void onMouseMoved(EventMouseMoved mMoved)
    {
    
    }
    
    public void onMouseScrolled(EventMouseScrolled mScrolled)
    {
    
    }
    
    public void onMouseButtonDown(EventMouseButtonDown mbDown)
    {
    
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
    
    public void onKeyboardKeyHeld(EventKeyboardKeyRepeated kkRepeated)
    {
    
    }
    
    public void onKeyboardKeyRepeated(EventKeyboardTyped kTyped)
    {
    
    }
}
