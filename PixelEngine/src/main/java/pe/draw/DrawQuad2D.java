package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;

import java.util.Arrays;

public class DrawQuad2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x0, y0;
    private boolean hasPoint0;
    
    private double x1, y1;
    private boolean hasPoint1;
    
    private double x2, y2;
    private boolean hasPoint2;
    
    private double x3, y3;
    private boolean hasPoint3;
    
    private double  thickness;
    private boolean hasThickness;
    
    private int r, g, b, a;
    
    private double[] points;
    
    @Override
    protected void reset()
    {
        this.hasPoint0 = false;
        this.hasPoint1 = false;
        this.hasPoint2 = false;
        this.hasPoint3 = false;
        
        this.hasThickness = false;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
        
        if (this.points == null) this.points = new double[14];
        Arrays.fill(this.points, 0.0);
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint0) throw new IllegalStateException("Must provide point0");
        if (!this.hasPoint1) throw new IllegalStateException("Must provide point1");
        if (!this.hasPoint2) throw new IllegalStateException("Must provide point2");
        if (!this.hasPoint3) throw new IllegalStateException("Must provide point2");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawQuad2D.LOGGER.finest("Drawing point0=(%s, %s) point1=(%s, %s) point2=(%s, %s) point3=(%s, %s) thickness=%s color=(%s, %s, %s, %s)",
                                 this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.x3, this.y3, this.thickness, this.r, this.g, this.b, this.a);
        
        if (this.thickness <= 0) return;
        
        this.points[0]  = this.x3;
        this.points[1]  = this.y3;
        this.points[2]  = this.x0;
        this.points[3]  = this.y0;
        this.points[4]  = this.x1;
        this.points[5]  = this.y1;
        this.points[6]  = this.x2;
        this.points[7]  = this.y2;
        this.points[8]  = this.x3;
        this.points[9]  = this.y3;
        this.points[10] = this.x0;
        this.points[11] = this.y0;
        this.points[12] = this.x1;
        this.points[13] = this.y1;
        
        drawLines(this.points, this.thickness,
                  this.r, this.g, this.b, this.a,
                  this.r, this.g, this.b, this.a);
    }
    
    
    public DrawQuad2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    public DrawQuad2D point0(@NotNull Vector2ic vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawQuad2D point0(@NotNull Vector2fc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawQuad2D point0(@NotNull Vector2dc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawQuad2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    public DrawQuad2D point1(@NotNull Vector2ic vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawQuad2D point1(@NotNull Vector2fc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawQuad2D point1(@NotNull Vector2dc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawQuad2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    public DrawQuad2D point2(@NotNull Vector2ic vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawQuad2D point2(@NotNull Vector2fc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawQuad2D point2(@NotNull Vector2dc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawQuad2D point3(double x, double y)
    {
        this.x3        = x;
        this.y3        = y;
        this.hasPoint3 = true;
        return this;
    }
    
    public DrawQuad2D point3(@NotNull Vector2ic vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawQuad2D point3(@NotNull Vector2fc vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawQuad2D point3(@NotNull Vector2dc vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawQuad2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    public DrawQuad2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawQuad2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
}
