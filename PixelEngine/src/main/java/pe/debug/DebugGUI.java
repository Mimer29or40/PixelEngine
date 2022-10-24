package pe.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.Engine;
import pe.event.*;
import pe.render.GLFramebuffer;
import rutils.Logger;

public class DebugGUI
{
    private static final Logger LOGGER = new Logger();
    
    private static final RootContainer  ROOT  = new RootContainer();
    private static final ModalContainer MODAL = new ModalContainer();
    
    private static Element hoveredElement = null;
    private static Element focusedElement = null;
    // private static DebugLabel  tooltip = null;  // TODO
    
    // private static String prevTooltip = "";  // TODO
    
    private static Element drag = null;
    
    public static void addChild(@NotNull Element child)
    {
        DebugGUI.ROOT.addChild(child);
    }
    
    public static void removeChild(@NotNull Element child)
    {
        DebugGUI.ROOT.removeChild(child);
    }
    
    public static void focus(@Nullable Element element)
    {
        if (DebugGUI.focusedElement != element)
        {
            if (DebugGUI.focusedElement != null) DebugGUI.focusedElement.focused = false;
            DebugGUI.focusedElement = element;
            if (DebugGUI.focusedElement != null) DebugGUI.focusedElement.focused = true;
        }
    }
    
    public static void handleEvents()
    {
        for (Event event : Engine.Events.get())
        {
            if (event instanceof EventMouseMoved mMoved)
            {
                // TODO - Need to transform mouse coordinates
                int x = (int) mMoved.x();
                int y = (int) mMoved.y();
                
                Element hoveredElement = DebugGUI.drag;
                if (hoveredElement == null) hoveredElement = DebugGUI.MODAL.getTopElementAt(x, y);
                if (hoveredElement == null) hoveredElement = DebugGUI.ROOT.getTopElementAt(x, y);
                
                if (DebugGUI.hoveredElement != hoveredElement)
                {
                    if (DebugGUI.hoveredElement != null) DebugGUI.hoveredElement.hovered = false;
                    DebugGUI.hoveredElement = hoveredElement;
                    LOGGER.fine(hoveredElement);
                    if (DebugGUI.hoveredElement != null) DebugGUI.hoveredElement.hovered = true;
                }
                
                Element element = DebugGUI.hoveredElement;
                while (element != null && !mMoved.consumed())
                {
                    element.onMouseMoved(mMoved);
                    element = element.getParent();
                }
            }
            else if (event instanceof EventMouseScrolled mScrolled)
            {
                Element element = DebugGUI.hoveredElement;
                while (element != null && !mScrolled.consumed())
                {
                    element.onMouseScrolled(mScrolled);
                    element = element.getParent();
                }
            }
            else if (event instanceof EventMouseButtonDown mbDown)
            {
                Element element = DebugGUI.hoveredElement;
                if (element != null) DebugGUI.ROOT.moveToTop(element.getRoot());
                while (element != null && !mbDown.consumed())
                {
                    element.onMouseButtonDown(mbDown);
                    if (mbDown.consumed()) DebugGUI.focus(element);
                    element = element.getParent();
                }
            }
            else if (event instanceof EventMouseButtonUp mbUp)
            {
                DebugGUI.drag = null;
                
                Element element = DebugGUI.hoveredElement;
                while (element != null && !mbUp.consumed())
                {
                    element.onMouseButtonUp(mbUp);
                    element = element.getParent();
                }
            }
            else if (event instanceof EventMouseButtonDragged mbDragged)
            {
                Element element = DebugGUI.hoveredElement;
                while (element != null && !mbDragged.consumed())
                {
                    element.onMouseButtonDragged(mbDragged);
                    if (mbDragged.consumed()) DebugGUI.drag = element;
                    element = element.getParent();
                }
            }
            else if (event instanceof EventKeyboardKeyDown kkDown)
            {
                Element window = DebugGUI.focusedElement;
                while (window != null)
                {
                    window.onKeyboardKeyDown(kkDown);
                    window = window.getParent();
                }
            }
            else if (event instanceof EventKeyboardKeyUp kkUp)
            {
                Element window = DebugGUI.focusedElement;
                while (window != null)
                {
                    window.onKeyboardKeyUp(kkUp);
                    window = window.getParent();
                }
            }
            else if (event instanceof EventKeyboardKeyRepeated kkRepeated)
            {
                Element window = DebugGUI.focusedElement;
                while (window != null)
                {
                    window.onKeyboardKeyHeld(kkRepeated);
                    window = window.getParent();
                }
            }
            else if (event instanceof EventKeyboardTyped kTyped)
            {
                Element window = DebugGUI.focusedElement;
                while (window != null)
                {
                    window.onKeyboardKeyRepeated(kTyped);
                    window = window.getParent();
                }
            }
        }
    }
    
    public static void draw()
    {
        // if (DebugGUI.top != null && !DebugGUI.top.getTooltipText().equals(""))
        // {
        //     String tooltip = DebugGUI.top.getTooltipText();
        //     DebugGUI.tooltip.setVisible(true);
        //     int posX = mouseX + 12 / pixelWidth();
        //     if (!DebugGUI.prevTooltip.equals(tooltip))
        //     {
        //         DebugGUI.tooltip.setText(tooltip);
        //
        //         int maxWidth = screenWidth() - posX - (DebugGUI.tooltip.getBorderSize() + DebugGUI.tooltip.getMarginSize()) * 2;
        //
        //         String linesString = join(clipTextWidth(tooltip, DebugGUI.tooltip.getScale(), maxWidth), "\n");
        //
        //         DebugGUI.tooltip.setForegroundSize(textWidth(linesString, DebugGUI.tooltip.getScale()), textHeight(linesString, DebugGUI.tooltip.getScale()));
        //     }
        //     DebugGUI.tooltip.setPosition(posX, mouseY);
        // }
        // else
        // {
        //     DebugGUI.tooltip.setVisible(false);
        //     DebugGUI.tooltip.setPosition(screenWidth() + 10, screenHeight() + 10);
        // }
        
        int fbWidth  = GLFramebuffer.currentWidth();
        int fbHeight = GLFramebuffer.currentHeight();
        
        DebugGUI.ROOT.layout(0, 0, fbWidth, fbHeight);
        DebugGUI.MODAL.layout(0, 0, fbWidth, fbHeight);
        
        DebugGUI.ROOT.draw();
        DebugGUI.MODAL.draw();
        
        // if (DebugGUI.top != null && DebugGUI.tooltip.isVisible())
        // {
        //     DebugGUI.tooltip.draw(elapsedTime);
        //
        //     if (DebugGUI.tooltip.isVisible())
        //     {
        //         PixelEngine.renderer().drawMode(DrawMode.NORMAL);
        //         PixelEngine.renderer().drawTarget(null);
        //         PixelEngine.renderer().sprite(DebugGUI.tooltip.getX(), DebugGUI.tooltip.getY(), DebugGUI.tooltip.getSprite(), 1);
        //     }
        // }
    }
    
    private static class RootContainer extends ElementContainer
    {
        public void addChild(@NotNull Element child)
        {
            this.children.add(child);
        }
        
        public void removeChild(@NotNull Element child)
        {
            this.children.remove(child);
        }
        
        public void moveToTop(@NotNull Element child)
        {
            this.children.remove(child);
            this.children.add(child);
        }
        
        @Override
        public @Nullable Element getTopElementAt(int x, int y)
        {
            for (int i = this.children.size() - 1; i >= 0; i--)
            {
                Element child      = this.children.get(i);
                Element topElement = child.getTopElementAt(x, y);
                if (topElement != null) return topElement;
            }
            return null;
        }
        
        @Override
        protected void layout(int contentX, int contentY, int contentW, int contentH)
        {
            for (Element child : this.children)
            {
                child.layout(contentX, contentY, contentW, contentH);
            }
        }
        
        @Override
        protected void draw()
        {
            for (Element child : this.children)
            {
                child.draw();
            }
        }
    }
    
    private static class ModalContainer extends RootContainer
    {
        @Override
        public @Nullable Element getTopElementAt(int x, int y)
        {
            if (!this.children.isEmpty())
            {
                return this.children.get(this.children.size() - 1).getTopElementAt(x, y);
            }
            return null;
        }
    }
}
