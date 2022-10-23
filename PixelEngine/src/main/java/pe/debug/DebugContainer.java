package pe.debug;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class DebugContainer extends DebugElement
{
    protected final List<DebugElement> children = new ArrayList<>();
    
    public @Nullable DebugElement getHoveredElement(int x, int y)
    {
        DebugElement hoveredChild = null;
        for (DebugElement element : this.children)
        {
            DebugElement hoveredElement = element.getHoveredElement(x, y);
            if (hoveredElement != null) hoveredChild = hoveredElement;
        }
        if (hoveredChild != null)
        {
            this.hovered = false;
            return hoveredChild;
        }
        return super.getHoveredElement(x, y);
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
