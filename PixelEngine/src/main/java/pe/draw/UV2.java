package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface UV2<SELF>
{
    SELF uv2(double x, double y);
    
    default SELF uv2(@NotNull Vector2ic vec)
    {
        return uv2(vec.x(), vec.y());
    }
    
    default SELF uv2(@NotNull Vector2fc vec)
    {
        return uv2(vec.x(), vec.y());
    }
    
    default SELF uv2(@NotNull Vector2dc vec)
    {
        return uv2(vec.x(), vec.y());
    }
}
