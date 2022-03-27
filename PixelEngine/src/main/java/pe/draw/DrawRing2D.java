package pe.draw;

import rutils.Logger;
import rutils.Math;

public class DrawRing2D extends Draw2D implements Point<DrawRing2D>,
                                                  Radius0<DrawRing2D>,
                                                  Radius1<DrawRing2D>,
                                                  Thickness<DrawRing2D>,
                                                  StartStop<DrawRing2D>,
                                                  Rotation<DrawRing2D>,
                                                  Segments<DrawRing2D>,
                                                  Color<DrawRing2D>,
                                                  Color0<DrawRing2D>,
                                                  Color1<DrawRing2D>
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasCenter;
    
    private double rxi, ryi;
    private boolean hasSizeI;
    
    private double rxo, ryo;
    private boolean hasSizeO;
    
    private double  thickness;
    private boolean hasThickness;
    
    private double start, stop;
    
    private double originX, originY;
    
    private double angle;
    
    private int segments;
    
    private int ri, gi, bi, ai;
    private int ro, go, bo, ao;
    
    @Override
    protected void reset()
    {
        this.hasCenter    = false;
        this.hasSizeI     = false;
        this.hasSizeO     = false;
        this.hasThickness = false;
        
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
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawRing2D.LOGGER.finest("Drawing center=(%s, %s) inner=(%s, %s) outer=(%s, %s) thickness=%s angles=(%s, %s) origin=(%s, %s) rotation=%s segments=%s colorInner=(%s, %s, %s, %s) colorOuter=(%s, %s, %s, %s)",
                                 this.x, this.y, this.rxi, this.ryi, this.rxo, this.ryo, this.thickness, this.start, this.stop,
                                 this.originX, this.originY, this.angle, this.segments,
                                 this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
        
        drawEllipse(this.x, this.y, this.rxi, this.ryi, this.thickness, this.start, this.stop,
                    this.originX, this.originY, this.angle, this.segments,
                    this.ri, this.gi, this.bi, this.ai);
        
        drawEllipse(this.x, this.y, this.rxo, this.ryo, this.thickness, this.start, this.stop,
                    this.originX, this.originY, this.angle, this.segments,
                    this.ro, this.go, this.bo, this.ao);
    }
    
    @Override
    public DrawRing2D point(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    @Override
    public DrawRing2D radius0(double x, double y)
    {
        this.rxi      = x;
        this.ryi      = y;
        this.hasSizeI = true;
        return this;
    }
    
    @Override
    public DrawRing2D radius1(double x, double y)
    {
        this.rxo      = x;
        this.ryo      = y;
        this.hasSizeO = true;
        return this;
    }
    
    @Override
    public DrawRing2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawRing2D startAngle(double start)
    {
        this.start = start;
        return this;
    }
    
    @Override
    public DrawRing2D stopAngle(double stop)
    {
        this.stop = stop;
        return this;
    }
    
    @Override
    public DrawRing2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public DrawRing2D rotationAngle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    @Override
    public DrawRing2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    @Override
    public DrawRing2D color(int r, int g, int b, int a)
    {
        this.ri = this.ro = r;
        this.gi = this.go = g;
        this.bi = this.bo = b;
        this.ai = this.ao = a;
        return this;
    }
    
    @Override
    public DrawRing2D color0(int r, int g, int b, int a)
    {
        this.ri = r;
        this.gi = g;
        this.bi = b;
        this.ai = a;
        return this;
    }
    
    @Override
    public DrawRing2D color1(int r, int g, int b, int a)
    {
        this.ro = r;
        this.go = g;
        this.bo = b;
        this.ao = a;
        return this;
    }
}
