package pe.shape;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

public interface AABB2ic extends Shape2ic
{
    @NotNull Vector2ic size();
    
    default int width()
    {
        return size().x();
    }
    
    default int height()
    {
        return size().y();
    }
    
    @NotNull Vector2ic min();
    
    int minX();
    
    int minY();
    
    @NotNull Vector2ic max();
    
    int maxX();
    
    int maxY();
}
