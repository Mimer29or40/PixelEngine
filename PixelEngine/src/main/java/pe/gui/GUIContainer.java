package pe.gui;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class GUIContainer extends GUIElement
{
    protected final Set<GUILayout> children = new LinkedHashSet<>();
    
    @NotNull
    public Set<GUILayout> children()
    {
        return Collections.unmodifiableSet(this.children);
    }
    
    public void add(@NotNull GUILayout child)
    {
        if (child.parent != null) throw new IllegalStateException("Child cannot have multiple parents");
        this.children.add(child);
        child.parent = this;
    }
    
    public boolean contains(@NotNull GUILayout query)
    {
        return this.children.contains(query);
    }
}
