package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Point3<SELF>
{
    SELF point3(double x, double y);
    
    default SELF point3(@NotNull Vector2ic vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    default SELF point3(@NotNull Vector2fc vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    default SELF point3(@NotNull Vector2dc vec)
    {
        return point3(vec.x(), vec.y());
    }
}
