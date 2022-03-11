package pe.gui;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class GUIContainer extends GUIElement
{
    protected final Set<GUILayout> children = new LinkedHashSet<>();
    
    @NotNull
    public Set<GUILayout> children()
    {
        return Collections.unmodifiableSet(this.children);
    }
    
    public <T extends GUILayout> T add(@NotNull T child)
    {
        if (child.parent != null) throw new IllegalStateException("Child cannot have multiple parents");
        this.children.add(child);
        child.parent = this;
        return child;
    }
}
