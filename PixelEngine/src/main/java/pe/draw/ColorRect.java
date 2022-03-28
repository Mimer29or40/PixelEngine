package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import rutils.Math;

interface ColorRect<SELF extends ColorRect<SELF>>
{
    SELF colorTopLeft(int r, int g, int b, int a);
    
    default SELF colorTopLeft(@NotNull Colorc color)
    {
        return colorTopLeft(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorTopLeft(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorTopLeft(ri, gi, bi, ai);
    }
    
    SELF colorTopRight(int r, int g, int b, int a);
    
    default SELF colorTopRight(@NotNull Colorc color)
    {
        return colorTopRight(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorTopRight(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorTopRight(ri, gi, bi, ai);
    }
    
    SELF colorBottomLeft(int r, int g, int b, int a);
    
    default SELF colorBottomLeft(@NotNull Colorc color)
    {
        return colorBottomLeft(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorBottomLeft(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorBottomLeft(ri, gi, bi, ai);
    }
    
    SELF colorBottomRight(int r, int g, int b, int a);
    
    default SELF colorBottomRight(@NotNull Colorc color)
    {
        return colorBottomRight(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorBottomRight(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorBottomRight(ri, gi, bi, ai);
    }
    
    default SELF colorTop(int r, int g, int b, int a)
    {
        return colorTopLeft(r, g, b, a).colorTopRight(r, g, b, a);
    }
    
    default SELF colorTop(@NotNull Colorc color)
    {
        return colorTop(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorTop(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorTop(ri, gi, bi, ai);
    }
    
    default SELF colorBottom(int r, int g, int b, int a)
    {
        return colorBottomLeft(r, g, b, a).colorBottomRight(r, g, b, a);
    }
    
    default SELF colorBottom(@NotNull Colorc color)
    {
        return colorBottom(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorBottom(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorBottom(ri, gi, bi, ai);
    }
    
    default SELF colorLeft(int r, int g, int b, int a)
    {
        return colorTopLeft(r, g, b, a).colorBottomLeft(r, g, b, a);
    }
    
    default SELF colorLeft(@NotNull Colorc color)
    {
        return colorLeft(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorLeft(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorLeft(ri, gi, bi, ai);
    }
    
    default SELF colorRight(int r, int g, int b, int a)
    {
        return colorTopRight(r, g, b, a).colorBottomRight(r, g, b, a);
    }
    
    default SELF colorRight(@NotNull Colorc color)
    {
        return colorRight(color.r(), color.g(), color.b(), color.a());
    }
    
    default SELF colorRight(double r, double g, double b, double a)
    {
        int ri = Math.clamp((int) (r * 255), 0, 255);
        int gi = Math.clamp((int) (g * 255), 0, 255);
        int bi = Math.clamp((int) (b * 255), 0, 255);
        int ai = Math.clamp((int) (a * 255), 0, 255);
        return colorRight(ri, gi, bi, ai);
    }
}
