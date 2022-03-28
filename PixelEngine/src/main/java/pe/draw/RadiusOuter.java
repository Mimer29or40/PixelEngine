package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface RadiusOuter<SELF>
{
    SELF outerRadius(double x, double y);
    
    default SELF outerRadius(@NotNull Vector2ic vec)
    {
        return outerRadius(vec.x(), vec.y());
    }
    
    default SELF outerRadius(@NotNull Vector2fc vec)
    {
        return outerRadius(vec.x(), vec.y());
    }
    
    default SELF outerRadius(@NotNull Vector2dc vec)
    {
        return outerRadius(vec.x(), vec.y());
    }
    
    default SELF outerRadius(double radius)
    {
        return outerRadius(radius, radius);
    }
}
