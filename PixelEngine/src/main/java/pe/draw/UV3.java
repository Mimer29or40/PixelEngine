package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface UV3<SELF>
{
    SELF uv3(double x, double y);
    
    default SELF uv3(@NotNull Vector2ic vec)
    {
        return uv3(vec.x(), vec.y());
    }
    
    default SELF uv3(@NotNull Vector2fc vec)
    {
        return uv3(vec.x(), vec.y());
    }
    
    default SELF uv3(@NotNull Vector2dc vec)
    {
        return uv3(vec.x(), vec.y());
    }
}
