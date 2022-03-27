package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Radius1<SELF>
{
    SELF radius1(double x, double y);
    
    default SELF radius1(@NotNull Vector2ic vec)
    {
        return radius1(vec.x(), vec.y());
    }
    
    default SELF radius1(@NotNull Vector2fc vec)
    {
        return radius1(vec.x(), vec.y());
    }
    
    default SELF radius1(@NotNull Vector2dc vec)
    {
        return radius1(vec.x(), vec.y());
    }
    
    default SELF radius1(double radius)
    {
        return radius1(radius, radius);
    }
}
