package pe.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.Debug2;
import pe.render.ScissorMode;
import rutils.Math;

import java.util.LinkedList;
import java.util.List;

public abstract class ElementContainer extends Element
{
    public static final int ELEMENT_SPACING_SIZE = 2;
    
    protected final List<Element> children = new LinkedList<>();
    
    public void addChild(@NotNull Element child)
    {
        this.children.add(child);
        child.parent = this;
    }
    
    public void removeChild(@NotNull Element child)
    {
        this.children.remove(child);
        child.parent = null;
    }
    
    public boolean isDescendant(@NotNull Element query)
    {
        for (Element child : this.children)
        {
            if (child == query || (child instanceof ElementContainer container && container.isDescendant(query)))
            {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public @Nullable Element getTopElementAt(int x, int y)
    {
        if (this.rect.contains(x, y))
        {
            for (int i = this.children.size() - 1; i >= 0; i--)
            {
                Element child      = this.children.get(i);
                Element topElement = child.getTopElementAt(x, y);
                if (topElement != null) return topElement;
            }
            return this;
        }
        return null;
    }
    
    @Override
    protected void layout(int contentX, int contentY, int contentW, int contentH)
    {
        int childWidth = 0, childHeight = -ElementContainer.ELEMENT_SPACING_SIZE;
        this.rect.pos.set(contentX, contentY);
        for (Element child : this.children)
        {
            child.layout(contentX, contentY, contentW, contentH);
            
            childWidth = Math.max(childWidth, child.rect.width());
            
            int inc = child.rect.height() + ElementContainer.ELEMENT_SPACING_SIZE;
            
            childHeight += inc;
            contentY += inc;
        }
        this.rect.size.set(childWidth, childHeight);
    }
    
    @Override
    protected void draw()
    {
        Debug2.scissor(this.rect.x(), this.rect.y(), this.rect.width(), this.rect.height());
        for (Element child : this.children)
        {
            child.draw();
        }
        Debug2.scissor(ScissorMode.NONE);
    }
}
