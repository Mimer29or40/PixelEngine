package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;

interface Rotation<SELF>
{
    SELF rotationOrigin(double x, double y);
    
    default SELF rotationOrigin(@NotNull Vector2ic origin)
    {
        return rotationOrigin(origin.x(), origin.y());
    }
    
    default SELF rotationOrigin(@NotNull Vector2fc origin)
    {
        return rotationOrigin(origin.x(), origin.y());
    }
    
    default SELF rotationOrigin(@NotNull Vector2dc origin)
    {
        return rotationOrigin(origin.x(), origin.y());
    }
    
    SELF rotationAngle(double angleRadians);
    
    default SELF rotationAngleDegrees(double angleDegrees)
    {
        return rotationAngle(Math.toRadians(angleDegrees));
    }
}
