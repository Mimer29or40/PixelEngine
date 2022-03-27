package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import rutils.Math;

interface Color3<SELF>
{
    SELF color3(int r, int g, int b, int a);
    
    default SELF color3(@NotNull Colorc color)
    {
        return color3(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF color3(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return color3(ri, gi, bi, ai);
    }
}
