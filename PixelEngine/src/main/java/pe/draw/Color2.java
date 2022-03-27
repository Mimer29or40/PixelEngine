package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Math;

interface Color2<SELF>
{
    SELF color2(int r, int g, int b, int a);
    
    default SELF color2(@NotNull Colorc color)
    {
        return color2(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF color2(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return color2(ri, gi, bi, ai);
    }
}
