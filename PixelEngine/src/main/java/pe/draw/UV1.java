package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface UV1<SELF>
{
    SELF uv1(double x, double y);
    
    default SELF uv1(@NotNull Vector2ic vec)
    {
        return uv1(vec.x(), vec.y());
    }
    
    default SELF uv1(@NotNull Vector2fc vec)
    {
        return uv1(vec.x(), vec.y());
    }
    
    default SELF uv1(@NotNull Vector2dc vec)
    {
        return uv1(vec.x(), vec.y());
    }
}
