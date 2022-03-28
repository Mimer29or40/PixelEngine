package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface RadiusInner<SELF>
{
    SELF innerRadius(double x, double y);
    
    default SELF innerRadius(@NotNull Vector2ic vec)
    {
        return innerRadius(vec.x(), vec.y());
    }
    
    default SELF innerRadius(@NotNull Vector2fc vec)
    {
        return innerRadius(vec.x(), vec.y());
    }
    
    default SELF innerRadius(@NotNull Vector2dc vec)
    {
        return innerRadius(vec.x(), vec.y());
    }
    
    default SELF innerRadius(double radius)
    {
        return innerRadius(radius, radius);
    }
}
