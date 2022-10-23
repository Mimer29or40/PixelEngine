package pe.debug;

import pe.Debug2;
import pe.Mouse;
import pe.event.*;
import pe.render.ScissorMode;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugContainer extends DebugElement
{
    protected final List<DebugElement> children = new ArrayList<>();
    
    public void addElements(DebugElement elements)
    {
        this.children.add(elements);
    }
    
    public void removeElements(DebugElement elements)
    {
        this.children.remove(elements);
    }
    
    @Override
    public void onMouseMoved(EventMouseMoved mMoved)
    {
        int x = (int) mMoved.x();
        int y = (int) mMoved.y();
        
        for (DebugElement child : this.children)
        {
            if (child.rect.contains(x, y)) child.onMouseMoved(mMoved);
            if (mMoved.consumed()) return;
        }
        super.onMouseMoved(mMoved);
    }
    
    @Override
    public void onMouseScrolled(EventMouseScrolled mScrolled)
    {
        int x = (int) Mouse.x();
        int y = (int) Mouse.y();
    
        for (DebugElement child : this.children)
        {
            if (child.rect.contains(x, y)) child.onMouseScrolled(mScrolled);
            if (mScrolled.consumed()) return;
        }
        super.onMouseScrolled(mScrolled);
    }
    
    @Override
    public void onMouseButtonDown(EventMouseButtonDown mbDown)
    {
        int x = (int) Mouse.x();
        int y = (int) Mouse.y();
    
        for (DebugElement child : this.children)
        {
            if (child.rect.contains(x, y)) child.onMouseButtonDown(mbDown);
            if (mbDown.consumed()) return;
        }
        super.onMouseButtonDown(mbDown);
    }
    
    @Override
    public void onMouseButtonUp(EventMouseButtonUp mbUp)
    {
        int x = (int) Mouse.x();
        int y = (int) Mouse.y();
    
        for (DebugElement child : this.children)
        {
            if (child.rect.contains(x, y)) child.onMouseButtonUp(mbUp);
            if (mbUp.consumed()) return;
        }
        super.onMouseButtonUp(mbUp);
    }
    
    @Override
    public void onMouseButtonDragged(EventMouseButtonDragged mbDragged)
    {
        int x = (int) Mouse.x();
        int y = (int) Mouse.y();
    
        for (DebugElement child : this.children)
        {
            if (child.rect.contains(x, y)) child.onMouseButtonDragged(mbDragged);
            if (mbDragged.consumed()) return;
        }
        super.onMouseButtonDragged(mbDragged);
    }
    
    @Override
    public void onKeyboardKeyDown(EventKeyboardKeyDown kkDown)
    {
        super.onKeyboardKeyDown(kkDown);
    }
    
    @Override
    public void onKeyboardKeyUp(EventKeyboardKeyUp kkUp)
    {
        super.onKeyboardKeyUp(kkUp);
    }
    
    @Override
    public void onKeyboardKeyRepeated(EventKeyboardKeyRepeated kkRepeated)
    {
        super.onKeyboardKeyRepeated(kkRepeated);
    }
    
    @Override
    public void onKeyboardTyped(EventKeyboardTyped kTyped)
    {
        super.onKeyboardTyped(kTyped);
    }
    
    @Override
    public void draw(int contentX, int contentY, int contentW, int contentH)
    {
        Debug2.scissor(contentX, contentY, contentW, contentH);
        for (DebugElement child : this.children)
        {
            child.draw(contentX, contentY, contentW, contentH);
            contentY += child.rect.height() + DebugWindow.ELEMENT_SPACING_SIZE;
        }
        Debug2.scissor(ScissorMode.NONE);
    }
    
    @Override
    public void unhover()
    {
        for (DebugElement element : this.children) element.unhover();
        super.unhover();
    }
    
    @Override
    public void unfocus()
    {
        for (DebugElement element : this.children) element.unfocus();
        super.unfocus();
    }
}
