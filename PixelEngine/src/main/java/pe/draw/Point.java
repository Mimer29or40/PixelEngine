package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Point<SELF>
{
    SELF point(double x, double y);
    
    default SELF point(@NotNull Vector2ic vec)
    {
        return point(vec.x(), vec.y());
    }
    
    default SELF point(@NotNull Vector2fc vec)
    {
        return point(vec.x(), vec.y());
    }
    
    default SELF point(@NotNull Vector2dc vec)
    {
        return point(vec.x(), vec.y());
    }
}
