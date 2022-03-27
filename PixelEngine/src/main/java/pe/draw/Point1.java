package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Point1<SELF>
{
    SELF point1(double x, double y);
    
    default SELF point1(@NotNull Vector2ic vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    default SELF point1(@NotNull Vector2fc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    default SELF point1(@NotNull Vector2dc vec)
    {
        return point1(vec.x(), vec.y());
    }
}
