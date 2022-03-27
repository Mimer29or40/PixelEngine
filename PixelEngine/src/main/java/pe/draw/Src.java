package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Src<SELF>
{
    SELF src(double x, double y, double width, double height);
    
    default SELF src(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF src(@NotNull Vector2fc pos, @NotNull Vector2fc size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF src(@NotNull Vector2dc pos, @NotNull Vector2dc size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    default SELF src(double x, double y)
    {
        return src(x, y, 0.0, 0.0);
    }
    
    default SELF src(@NotNull Vector2ic pos)
    {
        return src(pos.x(), pos.y());
    }
    
    default SELF src(@NotNull Vector2fc pos)
    {
        return src(pos.x(), pos.y());
    }
    
    default SELF src(@NotNull Vector2dc pos)
    {
        return src(pos.x(), pos.y());
    }
}
