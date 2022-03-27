package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Points<SELF>
{
    SELF points(double @NotNull ... points);
    
    default SELF points(@NotNull Vector2ic @NotNull ... vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2ic point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
    
    default SELF points(@NotNull Vector2fc @NotNull ... vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2fc point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
    
    default SELF points(@NotNull Vector2dc @NotNull ... vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2dc point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
}
