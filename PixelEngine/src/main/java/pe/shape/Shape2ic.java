package pe.shape;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

public interface Shape2ic
{
    @NotNull Vector2ic pos();
    
    default int x()
    {
        return pos().x();
    }
    
    default int y()
    {
        return pos().y();
    }
    
    @NotNull AABB2ic aabb();
    
    boolean intersects(int x, int y);
    
    default boolean intersects(@NotNull Vector2ic pos)
    {
        return intersects(pos.x(), pos.y());
    }
    
    boolean intersects(@NotNull AABB2ic aabb);
    
    boolean contains(int x, int y);
    
    default boolean contains(@NotNull Vector2ic pos)
    {
        return contains(pos.x(), pos.y());
    }
    
    boolean contains(@NotNull AABB2ic aabb);
}
