package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Size<SELF>
{
    SELF size(double width, double height);
    
    default SELF size(@NotNull Vector2ic vec)
    {
        return size(vec.x(), vec.y());
    }
    
    default SELF size(@NotNull Vector2fc vec)
    {
        return size(vec.x(), vec.y());
    }
    
    default SELF size(@NotNull Vector2dc vec)
    {
        return size(vec.x(), vec.y());
    }
    
    default SELF size(double size)
    {
        return size(size, size);
    }
}
