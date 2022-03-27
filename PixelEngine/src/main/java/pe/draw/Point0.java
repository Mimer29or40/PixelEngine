package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Point0<SELF>
{
    SELF point0(double x, double y);
    
    default SELF point0(@NotNull Vector2ic vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    default SELF point0(@NotNull Vector2fc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    default SELF point0(@NotNull Vector2dc vec)
    {
        return point0(vec.x(), vec.y());
    }
}
