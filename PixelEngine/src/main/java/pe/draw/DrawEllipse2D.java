package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;
import rutils.Math;

public class DrawEllipse2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasCenter;
    
    private double rx, ry;
    private boolean hasSize;
    
    private double  thickness;
    private boolean hasThickness;
    
    private double start, stop;
    
    private double originX, originY;
    
    private double angle;
    
    private int segments;
    
    private int r, g, b, a;
    
    @Override
    protected void reset()
    {
        this.hasCenter    = false;
        this.hasSize      = false;
        this.hasThickness = false;
        
        this.start = 0;
        this.stop  = Math.PI2;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.segments = 0;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasCenter) throw new IllegalStateException("Must provide center");
        if (!this.hasSize) throw new IllegalStateException("Must provide size");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawEllipse2D.LOGGER.finest("Drawing center=(%s, %s) size=(%s, %s) thickness=%s angles=(%s, %s) origin=(%s, %s) rotation=%s segments=%s color=(%s, %s, %s, %s)",
                                    this.x, this.y, this.rx, this.ry, this.thickness, this.start, this.stop,
                                    this.originX, this.originY, this.angle, this.segments,
                                    this.r, this.g, this.b, this.a);
        
        drawEllipse(this.x, this.y, this.rx, this.ry, this.thickness, this.start, this.stop,
                    this.originX, this.originY, this.angle, this.segments,
                    this.r, this.g, this.b, this.a);
    }
    
    public DrawEllipse2D center(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    public DrawEllipse2D center(@NotNull Vector2ic vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public DrawEllipse2D center(@NotNull Vector2fc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public DrawEllipse2D center(@NotNull Vector2dc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public DrawEllipse2D radius(double x, double y)
    {
        this.rx      = x;
        this.ry      = y;
        this.hasSize = true;
        return this;
    }
    
    public DrawEllipse2D radius(@NotNull Vector2ic vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public DrawEllipse2D radius(@NotNull Vector2fc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public DrawEllipse2D radius(@NotNull Vector2dc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public DrawEllipse2D radius(double radius)
    {
        this.rx      = radius;
        this.ry      = radius;
        this.hasSize = true;
        return this;
    }
    
    public DrawEllipse2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
    {
        this.rx = (bottomRightX - topLeftX) * 0.5;
        this.ry = (bottomRightY - topLeftY) * 0.5;
        this.x  = topLeftX + this.rx;
        this.y  = topLeftY + this.ry;
        
        this.hasCenter = true;
        this.hasSize   = true;
        return this;
    }
    
    public DrawEllipse2D corners(@NotNull Vector2ic topLeft, @NotNull Vector2ic bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawEllipse2D corners(@NotNull Vector2fc topLeft, @NotNull Vector2fc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawEllipse2D corners(@NotNull Vector2dc topLeft, @NotNull Vector2dc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public DrawEllipse2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    public DrawEllipse2D angles(double start, double stop)
    {
        this.start = start;
        this.stop  = stop;
        return this;
    }
    
    public DrawEllipse2D angles(@NotNull Vector2ic angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public DrawEllipse2D angles(@NotNull Vector2fc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public DrawEllipse2D angles(@NotNull Vector2dc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public DrawEllipse2D origin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    public DrawEllipse2D origin(@NotNull Vector2ic origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawEllipse2D origin(@NotNull Vector2fc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawEllipse2D origin(@NotNull Vector2dc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawEllipse2D angle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    public DrawEllipse2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    public DrawEllipse2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawEllipse2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
}
