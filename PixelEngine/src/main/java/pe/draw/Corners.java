package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Corners<SELF>
{
    SELF corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY);
    
    default SELF corners(@NotNull Vector2ic topLeft, @NotNull Vector2ic bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    default SELF corners(@NotNull Vector2fc topLeft, @NotNull Vector2fc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    default SELF corners(@NotNull Vector2dc topLeft, @NotNull Vector2dc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    default SELF corners(double topLeftX, double topLeftY, @NotNull Vector2dc bottomRight)
    {
        return corners(topLeftX, topLeftY, bottomRight.x(), bottomRight.y());
    }
    
    default SELF corners(@NotNull Vector2dc topLeft, double bottomRightX, double bottomRightY)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRightX, bottomRightY);
    }
}
