package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;

public class FillTriangle2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x0, y0;
    private boolean hasPoint0;
    
    private double x1, y1;
    private boolean hasPoint1;
    
    private double x2, y2;
    private boolean hasPoint2;
    
    private int r0, g0, b0, a0;
    private int r1, g1, b1, a1;
    private int r2, g2, b2, a2;
    
    @Override
    protected void reset()
    {
        this.hasPoint0 = false;
        this.hasPoint1 = false;
        this.hasPoint2 = false;
        
        this.r0 = this.r1 = this.r2 = 255;
        this.g0 = this.g1 = this.g2 = 255;
        this.b0 = this.b1 = this.b2 = 255;
        this.a0 = this.a1 = this.a2 = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint0) throw new IllegalStateException("Must provide point0");
        if (!this.hasPoint1) throw new IllegalStateException("Must provide point1");
        if (!this.hasPoint2) throw new IllegalStateException("Must provide point2");
    }
    
    @Override
    protected void drawImpl()
    {
        FillTriangle2D.LOGGER.finest("Drawing point0=(%s, %s) point1=(%s, %s) point2=(%s, %s) color0=(%s, %s, %s, %s) color1=(%s, %s, %s, %s) color2=(%s, %s, %s, %s)",
                                     this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1, this.r2, this.g2, this.b2, this.a2);
        
        fillTriangle(this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1, this.r2, this.g2, this.b2, this.a2);
    }
    
    public FillTriangle2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    public FillTriangle2D point0(@NotNull Vector2ic vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public FillTriangle2D point0(@NotNull Vector2fc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public FillTriangle2D point0(@NotNull Vector2dc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public FillTriangle2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    public FillTriangle2D point1(@NotNull Vector2ic vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public FillTriangle2D point1(@NotNull Vector2fc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public FillTriangle2D point1(@NotNull Vector2dc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public FillTriangle2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    public FillTriangle2D point2(@NotNull Vector2ic vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public FillTriangle2D point2(@NotNull Vector2fc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public FillTriangle2D point2(@NotNull Vector2dc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public FillTriangle2D color(int r, int g, int b, int a)
    {
        this.r0 = this.r1 = this.r2 = r;
        this.g0 = this.g1 = this.g2 = g;
        this.b0 = this.b1 = this.b2 = b;
        this.a0 = this.a1 = this.a2 = a;
        return this;
    }
    
    public FillTriangle2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillTriangle2D color0(int r, int g, int b, int a)
    {
        this.r0 = r;
        this.g0 = g;
        this.b0 = b;
        this.a0 = a;
        return this;
    }
    
    public FillTriangle2D color0(@NotNull Colorc color)
    {
        return color0(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillTriangle2D color1(int r, int g, int b, int a)
    {
        this.r1 = r;
        this.g1 = g;
        this.b1 = b;
        this.a1 = a;
        return this;
    }
    
    public FillTriangle2D color1(@NotNull Colorc color)
    {
        return color1(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillTriangle2D color2(int r, int g, int b, int a)
    {
        this.r2 = r;
        this.g2 = g;
        this.b2 = b;
        this.a2 = a;
        return this;
    }
    
    public FillTriangle2D color2(@NotNull Colorc color)
    {
        return color2(color.r(), color.g(), color.b(), color.a());
    }
}
