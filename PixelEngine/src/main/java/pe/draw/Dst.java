package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Dst<SELF>
{
    SELF dst(double x, double y, double width, double height);
    
    default SELF dst(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF dst(@NotNull Vector2fc pos, @NotNull Vector2fc size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF dst(@NotNull Vector2dc pos, @NotNull Vector2dc size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF dst(double x, double y)
    {
        return dst(x, y, 0.0, 0.0);
    }
    
    default SELF dst(@NotNull Vector2ic pos)
    {
        return dst(pos.x(), pos.y());
    }
    
    default SELF dst(@NotNull Vector2fc pos)
    {
        return dst(pos.x(), pos.y());
    }
    
    default SELF dst(@NotNull Vector2dc pos)
    {
        return dst(pos.x(), pos.y());
    }
}
