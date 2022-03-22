package pe.guiOld;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class GUILayout extends GUIElement
{
    protected final Set<GUIElement> children = new LinkedHashSet<>();
    
    @NotNull
    public Set<GUIElement> children()
    {
        return Collections.unmodifiableSet(this.children);
    }
    
    public <T extends GUIElement> T add(@NotNull T child)
    {
        if (child.parent != null) throw new IllegalStateException("Child cannot have multiple parents");
        this.children.add(child);
        child.parent = this;
        return child;
    }
}
