package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;
import rutils.Math;

import java.util.Arrays;

public class DrawRect2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasPoint;
    
    private double width, height;
    private boolean hasSize;
    
    private double  thickness;
    private boolean hasThickness;
    
    private double originX, originY;
    
    private double angle;
    
    private int r, g, b, a;
    
    private double[] points;
    
    @Override
    protected void reset()
    {
        this.hasPoint     = false;
        this.hasSize      = false;
        this.hasThickness = false;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
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
        if (!this.hasPoint) throw new IllegalStateException("Must provide point");
        if (!this.hasSize) throw new IllegalStateException("Must provide radius");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawRect2D.LOGGER.finest("Drawing center=(%s, %s) size=(%s, %s) thickness=%s origin=(%s, %s) rotation=%s color0=(%s, %s, %s, %s) color1=(%s, %s, %s, %s) color2=(%s, %s, %s, %s) color3=(%s, %s, %s, %s)",
                                 this.x, this.y, this.width, this.height, this.thickness,
                                 this.originX, this.originY, this.angle,
                                 this.r, this.g, this.b, this.a);
        
        // if (this.width <= 0.0 || this.height <= 0.0) return;
        
        double halfW = this.width * 0.5;
        double halfH = this.height * 0.5;
        
        double x0, y0, x1, y1, x2, y2, x3, y3;
        
        // Only calculate rotation if needed
        if (Math.equals(this.angle, 0.0, 1e-6))
        {
            x0 = this.x - halfW;
            y0 = this.y - halfH;
            
            x1 = this.x - halfW;
            y1 = this.y + halfH;
            
            x2 = this.x + halfW;
            y2 = this.y + halfH;
            
            x3 = this.x + halfW;
            y3 = this.y - halfH;
        }
        else
        {
            double s = Math.sin(this.angle);
            double c = Math.cos(this.angle);
            
            double minCX = (-halfW - this.originX) * c;
            double maxCX = (halfW - this.originX) * c;
            double minSX = (-halfW - this.originX) * s;
            double maxSX = (halfW - this.originX) * s;
            double minCY = (-halfH - this.originY) * c;
            double maxCY = (halfH - this.originY) * c;
            double minSY = (-halfH - this.originY) * s;
            double maxSY = (halfH - this.originY) * s;
            
            x0 = this.x + this.originX + minCX - minSY;
            y0 = this.y + this.originY + minSX + minCY;
            
            x1 = this.x + this.originX + minCX - maxSY;
            y1 = this.y + this.originY + minSX + maxCY;
            
            x2 = this.x + this.originX + maxCX - maxSY;
            y2 = this.y + this.originY + maxSX + maxCY;
            
            x3 = this.x + this.originX + maxCX - minSY;
            y3 = this.y + this.originY + maxSX + minCY;
        }
        
        this.points[0]  = x3;
        this.points[1]  = y3;
        this.points[2]  = x0;
        this.points[3]  = y0;
        this.points[4]  = x1;
        this.points[5]  = y1;
        this.points[6]  = x2;
        this.points[7]  = y2;
        this.points[8]  = x3;
        this.points[9]  = y3;
        this.points[10] = x0;
        this.points[11] = y0;
        this.points[12] = x1;
        this.points[13] = y1;
        
        drawLines(this.points, this.thickness,
                  this.r, this.g, this.b, this.a,
                  this.r, this.g, this.b, this.a);
    }
    
    public DrawRect2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    public DrawRect2D point(@NotNull Vector2ic vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawRect2D point(@NotNull Vector2fc vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawRect2D point(@NotNull Vector2dc vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawRect2D size(double x, double y)
    {
        this.width   = x;
        this.height  = y;
        this.hasSize = true;
        return this;
    }
    
    public DrawRect2D size(@NotNull Vector2ic vec)
    {
        return size(vec.x(), vec.y());
    }
    
    public DrawRect2D size(@NotNull Vector2fc vec)
    {
        return size(vec.x(), vec.y());
    }
    
    public DrawRect2D size(@NotNull Vector2dc vec)
    {
        return size(vec.x(), vec.y());
    }
    
    public DrawRect2D size(double radius)
    {
        this.width   = radius;
        this.height  = radius;
        this.hasSize = true;
        return this;
    }
    
    public DrawRect2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
    {
        this.width  = bottomRightX - topLeftX;
        this.height = bottomRightY - topLeftY;
        this.x      = topLeftX;
        this.y      = topLeftY;
        
        this.hasPoint = true;
        this.hasSize  = true;
        return this;
    }
    
    public DrawRect2D corners(@NotNull Vector2ic topLeft, @NotNull Vector2ic bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawRect2D corners(@NotNull Vector2fc topLeft, @NotNull Vector2fc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawRect2D corners(@NotNull Vector2dc topLeft, @NotNull Vector2dc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawRect2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    public DrawRect2D origin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    public DrawRect2D origin(@NotNull Vector2ic origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawRect2D origin(@NotNull Vector2fc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawRect2D origin(@NotNull Vector2dc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawRect2D angle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    public DrawRect2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawRect2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
}