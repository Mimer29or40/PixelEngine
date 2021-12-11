package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;
import rutils.Math;

public class FillEllipse2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasCenter;
    
    private double rx, ry;
    private boolean hasRadius;
    
    private double start, stop;
    
    private double originX, originY;
    
    private double angle;
    
    private int segments;
    
    private int ri, gi, bi, ai;
    private int ro, go, bo, ao;
    
    @Override
    protected void reset()
    {
        this.hasCenter = false;
        this.hasRadius = false;
        
        this.start = 0;
        this.stop  = Math.PI2;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.segments = 0;
        
        this.ri = this.ro = 255;
        this.gi = this.go = 255;
        this.bi = this.bo = 255;
        this.ai = this.ao = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasCenter) throw new IllegalStateException("Must provide center");
        if (!this.hasRadius) throw new IllegalStateException("Must provide radius");
    }
    
    @Override
    protected void drawImpl()
    {
        FillEllipse2D.LOGGER.finest("Drawing center=(%s, %s) radius=(%s, %s) angles=(%s, %s) origin=(%s, %s) rotation=%s segments=%s colorInner=(%s, %s, %s, %s) colorOuter=(%s, %s, %s, %s)",
                                    this.x, this.y, this.rx, this.ry, this.start, this.stop,
                                    this.originX, this.originY, this.angle, this.segments,
                                    this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
        
        fillEllipse(this.x, this.y, this.rx, this.ry, this.start, this.stop,
                    this.originX, this.originY, this.angle, this.segments,
                    this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
    }
    
    public FillEllipse2D center(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    public FillEllipse2D center(@NotNull Vector2ic vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillEllipse2D center(@NotNull Vector2fc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillEllipse2D center(@NotNull Vector2dc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillEllipse2D radius(double x, double y)
    {
        this.rx        = x;
        this.ry        = y;
        this.hasRadius = true;
        return this;
    }
    
    public FillEllipse2D radius(@NotNull Vector2ic vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public FillEllipse2D radius(@NotNull Vector2fc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public FillEllipse2D radius(@NotNull Vector2dc vec)
    {
        return radius(vec.x(), vec.y());
    }
    
    public FillEllipse2D radius(double radius)
    {
        this.rx        = radius;
        this.ry        = radius;
        this.hasRadius = true;
        return this;
    }
    
    public FillEllipse2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
    {
        this.rx = (bottomRightX - topLeftX) * 0.5;
        this.ry = (bottomRightY - topLeftY) * 0.5;
        this.x  = topLeftX + this.rx;
        this.y  = topLeftY + this.ry;
        
        this.hasCenter = true;
        this.hasRadius = true;
        return this;
    }
    
    public FillEllipse2D corners(@NotNull Vector2ic topLeft, @NotNull Vector2ic bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public FillEllipse2D corners(@NotNull Vector2fc topLeft, @NotNull Vector2fc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public FillEllipse2D corners(@NotNull Vector2dc topLeft, @NotNull Vector2dc bottomRight)
    {
        return corners(topLeft.x(), topLeft.y(), bottomRight.x(), bottomRight.y());
    }
    
    public FillEllipse2D angles(double start, double stop)
    {
        this.start = start;
        this.stop  = stop;
        return this;
    }
    
    public FillEllipse2D angles(@NotNull Vector2ic angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillEllipse2D angles(@NotNull Vector2fc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillEllipse2D angles(@NotNull Vector2dc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillEllipse2D origin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    public FillEllipse2D origin(@NotNull Vector2ic origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillEllipse2D origin(@NotNull Vector2fc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillEllipse2D origin(@NotNull Vector2dc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillEllipse2D angle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    public FillEllipse2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    public FillEllipse2D color(int r, int g, int b, int a)
    {
        this.ri = this.ro = r;
        this.gi = this.go = g;
        this.bi = this.bo = b;
        this.ai = this.ao = a;
        return this;
    }
    
    public FillEllipse2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillEllipse2D colorInner(int r, int g, int b, int a)
    {
        this.ri = r;
        this.gi = g;
        this.bi = b;
        this.ai = a;
        return this;
    }
    
    public FillEllipse2D colorInner(@NotNull Colorc color)
    {
        return colorInner(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillEllipse2D colorOuter(int r, int g, int b, int a)
    {
        this.ro = r;
        this.go = g;
        this.bo = b;
        this.ao = a;
        return this;
    }
    
    public FillEllipse2D colorOuter(@NotNull Colorc color)
    {
        return colorOuter(color.r(), color.g(), color.b(), color.a());
    }
}
