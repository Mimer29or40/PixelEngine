package pe.gui;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class GUILayout extends GUIElement
{
    protected final Set<GUIElement> children = new LinkedHashSet<>();
    
    @NotNull
    public Set<GUIElement> children()
    {
        return Collections.unmodifiableSet(this.children);
    }
    
    public void add(@NotNull GUIElement child)
    {
        if (child.parent != null) throw new IllegalStateException("Child cannot have multiple parents");
        this.children.add(child);
        child.parent = this;
    }
}
