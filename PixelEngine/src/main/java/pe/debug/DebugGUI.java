package pe.debug;

import pe.Engine;
import pe.event.*;

import java.util.LinkedList;
import java.util.List;

public class DebugGUI
{
    private final List<DebugElement> elements = new LinkedList<>();
    
    private DebugElement hoveredElement;
    private DebugElement focusedElement;
    
    public void handleEvents()
    {
        for (Event event : Engine.Events.get())
        {
            if (event instanceof EventMouseMoved mMoved)
            {
                int x = (int) mMoved.x();
                int y = (int) mMoved.y();
                
                this.hoveredElement = null;
                for (DebugElement element : this.elements)
                {
                    element.unhover();
                    if (element.rect.contains(x, y))
                    {
                        this.hoveredElement = element;
                        break;
                    }
                }
                if (this.hoveredElement != null)
                {
                    if (!mMoved.consumed()) this.hoveredElement.onMouseMoved(mMoved);
                }
            }
            else if (event instanceof EventMouseScrolled mScrolled)
            {
                if (this.hoveredElement != null)
                {
                    if (!mScrolled.consumed()) this.hoveredElement.onMouseScrolled(mScrolled);
                }
            }
            else if (event instanceof EventMouseButtonDown mbDown)
            {
                int x = (int) mbDown.x();
                int y = (int) mbDown.y();
                
                this.focusedElement = null;
                for (DebugElement element : this.elements)
                {
                    if (element.rect.contains(x, y))
                    {
                        this.focusedElement = element;
                        break;
                    }
                }
                if (this.focusedElement != null)
                {
                    this.elements.remove(this.focusedElement);
                    this.elements.add(0, this.focusedElement);
                    
                    if (!mbDown.consumed()) this.focusedElement.onMouseButtonDown(mbDown);
                }
            }
            else if (event instanceof EventMouseButtonUp mbUp)
            {
                if (this.focusedElement != null)
                {
                    if (!mbUp.consumed()) this.focusedElement.onMouseButtonUp(mbUp);
                }
            }
            else if (event instanceof EventMouseButtonDragged mbDragged)
            {
                if (this.focusedElement != null)
                {
                    if (!mbDragged.consumed()) this.focusedElement.onMouseButtonDragged(mbDragged);
                }
            }
            else if (event instanceof EventKeyboardKeyDown kkDown)
            {
                if (this.focusedElement != null)
                {
                    if (!kkDown.consumed()) this.focusedElement.onKeyboardKeyDown(kkDown);
                }
            }
            else if (event instanceof EventKeyboardKeyUp kkUp)
            {
                if (this.focusedElement != null)
                {
                    if (!kkUp.consumed()) this.focusedElement.onKeyboardKeyUp(kkUp);
                }
            }
            else if (event instanceof EventKeyboardKeyRepeated kkRepeated)
            {
                if (this.focusedElement != null)
                {
                    if (!kkRepeated.consumed()) this.focusedElement.onKeyboardKeyRepeated(kkRepeated);
                }
            }
            else if (event instanceof EventKeyboardTyped kTyped)
            {
                if (this.focusedElement != null)
                {
                    if (!kTyped.consumed()) this.focusedElement.onKeyboardTyped(kTyped);
                }
            }
        }
    }
    
    public void draw()
    {
        for (int i = this.elements.size() - 1; i >= 0; i--)
        {
            this.elements.get(i).draw(0, 0, 0, 0);
        }
    }
    
    public boolean addElement(DebugElement window)
    {
        return this.elements.add(window);
    }
    
    public boolean removeElement(DebugElement window)
    {
        return this.elements.remove(window);
    }
}
