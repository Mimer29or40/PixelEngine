package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;
import rutils.Math;

public class FillRing2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasCenter;
    
    private double rxi, ryi;
    private boolean hasSizeI;
    
    private double rxo, ryo;
    private boolean hasSizeO;
    
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
        this.hasSizeI  = false;
        this.hasSizeO  = false;
        
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
        if (!this.hasSizeI) throw new IllegalStateException("Must provide inner size");
        if (!this.hasSizeO) throw new IllegalStateException("Must provide outer size");
    }
    
    @Override
    protected void drawImpl()
    {
        FillRing2D.LOGGER.finest("Drawing center=(%s, %s) inner=(%s, %s) outer=(%s, %s) angles=(%s, %s) origin=(%s, %s) rotation=%s segments=%s colorInner=(%s, %s, %s, %s) colorOuter=(%s, %s, %s, %s)",
                                 this.x, this.y, this.rxi, this.ryi, this.rxo, this.ryo, this.start, this.stop,
                                 this.originX, this.originY, this.angle, this.segments,
                                 this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
        
        fillRing(this.x, this.y, this.rxi, this.ryi, this.rxo, this.ryo, this.start, this.stop,
                 this.originX, this.originY, this.angle, this.segments,
                 this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
    }
    
    public FillRing2D center(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    public FillRing2D center(@NotNull Vector2ic vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillRing2D center(@NotNull Vector2fc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillRing2D center(@NotNull Vector2dc vec)
    {
        return center(vec.x(), vec.y());
    }
    
    public FillRing2D radiusInner(double x, double y)
    {
        this.rxi      = x;
        this.ryi      = y;
        this.hasSizeI = true;
        return this;
    }
    
    public FillRing2D radiusInner(@NotNull Vector2ic vec)
    {
        return radiusInner(vec.x(), vec.y());
    }
    
    public FillRing2D radiusInner(@NotNull Vector2fc vec)
    {
        return radiusInner(vec.x(), vec.y());
    }
    
    public FillRing2D radiusInner(@NotNull Vector2dc vec)
    {
        return radiusInner(vec.x(), vec.y());
    }
    
    public FillRing2D radiusInner(double radius)
    {
        this.rxi      = radius;
        this.ryi      = radius;
        this.hasSizeI = true;
        return this;
    }
    
    public FillRing2D radiusOuter(double x, double y)
    {
        this.rxo      = x;
        this.ryo      = y;
        this.hasSizeO = true;
        return this;
    }
    
    public FillRing2D radiusOuter(@NotNull Vector2ic vec)
    {
        return radiusOuter(vec.x(), vec.y());
    }
    
    public FillRing2D radiusOuter(@NotNull Vector2fc vec)
    {
        return radiusOuter(vec.x(), vec.y());
    }
    
    public FillRing2D radiusOuter(@NotNull Vector2dc vec)
    {
        return radiusOuter(vec.x(), vec.y());
    }
    
    public FillRing2D radiusOuter(double radius)
    {
        this.rxo      = radius;
        this.ryo      = radius;
        this.hasSizeO = true;
        return this;
    }
    
    public FillRing2D angles(double start, double stop)
    {
        this.start = start;
        this.stop  = stop;
        return this;
    }
    
    public FillRing2D angles(@NotNull Vector2ic angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillRing2D angles(@NotNull Vector2fc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillRing2D angles(@NotNull Vector2dc angles)
    {
        return angles(angles.x(), angles.y());
    }
    
    public FillRing2D origin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    public FillRing2D origin(@NotNull Vector2ic origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillRing2D origin(@NotNull Vector2fc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillRing2D origin(@NotNull Vector2dc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public FillRing2D angle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    public FillRing2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    public FillRing2D color(int r, int g, int b, int a)
    {
        this.ri = this.ro = r;
        this.gi = this.go = g;
        this.bi = this.bo = b;
        this.ai = this.ao = a;
        return this;
    }
    
    public FillRing2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillRing2D colorInner(int r, int g, int b, int a)
    {
        this.ri = r;
        this.gi = g;
        this.bi = b;
        this.ai = a;
        return this;
    }
    
    public FillRing2D colorInner(@NotNull Colorc color)
    {
        return colorInner(color.r(), color.g(), color.b(), color.a());
    }
    
    public FillRing2D colorOuter(int r, int g, int b, int a)
    {
        this.ro = r;
        this.go = g;
        this.bo = b;
        this.ao = a;
        return this;
    }
    
    public FillRing2D colorOuter(@NotNull Colorc color)
    {
        return colorOuter(color.r(), color.g(), color.b(), color.a());
    }
}
