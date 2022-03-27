package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface UV0<SELF>
{
    SELF uv0(double x, double y);
    
    default SELF uv0(@NotNull Vector2ic vec)
    {
        return uv0(vec.x(), vec.y());
    }
    
    default SELF uv0(@NotNull Vector2fc vec)
    {
        return uv0(vec.x(), vec.y());
    }
    
    default SELF uv0(@NotNull Vector2dc vec)
    {
        return uv0(vec.x(), vec.y());
    }
}
