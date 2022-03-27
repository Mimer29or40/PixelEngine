package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Radius0<SELF>
{
    SELF radius0(double x, double y);
    
    default SELF radius0(@NotNull Vector2ic vec)
    {
        return radius0(vec.x(), vec.y());
    }
    
    default SELF radius0(@NotNull Vector2fc vec)
    {
        return radius0(vec.x(), vec.y());
    }
    
    default SELF radius0(@NotNull Vector2dc vec)
    {
        return radius0(vec.x(), vec.y());
    }
    
    default SELF radius0(double radius)
    {
        return radius0(radius, radius);
    }
}
