package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import rutils.Math;

interface ColorRoundQuad<SELF extends ColorRoundQuad<SELF>>
{
    SELF innerStartColor(int r, int g, int b, int a);
    
    default SELF innerStartColor(@NotNull Colorc color)
    {
        return innerStartColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF innerStartColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return innerStartColor(ri, gi, bi, ai);
    }
    
    SELF innerStopColor(int r, int g, int b, int a);
    
    default SELF innerStopColor(@NotNull Colorc color)
    {
        return innerStopColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF innerStopColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return innerStopColor(ri, gi, bi, ai);
    }
    
    SELF outerStartColor(int r, int g, int b, int a);
    
    default SELF outerStartColor(@NotNull Colorc color)
    {
        return outerStartColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF outerStartColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return outerStartColor(ri, gi, bi, ai);
    }
    
    SELF outerStopColor(int r, int g, int b, int a);
    
    default SELF outerStopColor(@NotNull Colorc color)
    {
        return outerStopColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF outerStopColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return outerStopColor(ri, gi, bi, ai);
    }
    
    default SELF startColor(int r, int g, int b, int a)
    {
        return innerStartColor(r, g, b, a).outerStartColor(r, g, b, a);
    }
    
    default SELF startColor(@NotNull Colorc color)
    {
        return startColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF startColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return startColor(ri, gi, bi, ai);
    }
    
    default SELF stopColor(int r, int g, int b, int a)
    {
        return innerStopColor(r, g, b, a).outerStopColor(r, g, b, a);
    }
    
    default SELF stopColor(@NotNull Colorc color)
    {
        return stopColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF stopColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return stopColor(ri, gi, bi, ai);
    }
    
    default SELF innerColor(int r, int g, int b, int a)
    {
        return innerStartColor(r, g, b, a).innerStopColor(r, g, b, a);
    }
    
    default SELF innerColor(@NotNull Colorc color)
    {
        return innerColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF innerColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return innerColor(ri, gi, bi, ai);
    }
    
    default SELF outerColor(int r, int g, int b, int a)
    {
        return outerStartColor(r, g, b, a).outerStopColor(r, g, b, a);
    }
    
    default SELF outerColor(@NotNull Colorc color)
    {
        return outerColor(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF outerColor(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return outerColor(ri, gi, bi, ai);
    }
}
