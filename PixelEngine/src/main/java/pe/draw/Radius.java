package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Radius<SELF>
{
    SELF radius(double x, double y);
    
    default SELF radius(@NotNull Vector2ic vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    default SELF radius(@NotNull Vector2fc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    default SELF radius(@NotNull Vector2dc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    default SELF radius(double radius)
    {
        return radius(radius, radius);
    }
}
