package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Point2<SELF>
{
    SELF point2(double x, double y);
    
    default SELF point2(@NotNull Vector2ic vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    default SELF point2(@NotNull Vector2fc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    default SELF point2(@NotNull Vector2dc vec)
    {
        return point2(vec.x(), vec.y());
    }
}
