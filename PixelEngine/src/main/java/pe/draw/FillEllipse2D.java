package pe.draw;

import rutils.Logger;
import rutils.Math;

public class FillEllipse2D extends Draw2D implements Point<FillEllipse2D>,
                                                     Radius<FillEllipse2D>,
                                                     Corners<FillEllipse2D>,
                                                     StartStop<FillEllipse2D>,
                                                     Rotation<FillEllipse2D>,
                                                     Segments<FillEllipse2D>,
                                                     Color<FillEllipse2D>,
                                                     Color0<FillEllipse2D>,
                                                     Color1<FillEllipse2D>
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasCenter;
    
    private double rx, ry;
    private boolean hasSize;
    
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
        this.hasSize   = false;
        
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
        if (!this.hasSize) throw new IllegalStateException("Must provide size");
    }
    
    @Override
    protected void drawImpl()
    {
        FillEllipse2D.LOGGER.finest("Drawing center=(%s, %s) size=(%s, %s) angles=(%s, %s) origin=(%s, %s) rotation=%s segments=%s colorInner=(%s, %s, %s, %s) colorOuter=(%s, %s, %s, %s)",
                                    this.x, this.y, this.rx, this.ry, this.start, this.stop,
                                    this.originX, this.originY, this.angle, this.segments,
                                    this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
        
        fillEllipse(this.x, this.y, this.rx, this.ry, this.start, this.stop,
                    this.originX, this.originY, this.angle, this.segments,
                    this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
    }
    
    @Override
    public FillEllipse2D point(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    @Override
    public FillEllipse2D radius(double x, double y)
    {
        this.rx      = x;
        this.ry      = y;
        this.hasSize = true;
        return this;
    }
    
    @Override
    public FillEllipse2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
    {
        this.rx = (bottomRightX - topLeftX) * 0.5;
        this.ry = (bottomRightY - topLeftY) * 0.5;
        this.x  = topLeftX + this.rx;
        this.y  = topLeftY + this.ry;
        
        this.hasCenter = true;
        this.hasSize   = true;
        return this;
    }
    
    @Override
    public FillEllipse2D startAngle(double start)
    {
        this.start = start;
        return this;
    }
    
    @Override
    public FillEllipse2D stopAngle(double stop)
    {
        this.stop = stop;
        return this;
    }
    
    @Override
    public FillEllipse2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public FillEllipse2D rotationAngle(double angleRadians)
    {
        this.angle = angleRadians;
        return this;
    }
    
    @Override
    public FillEllipse2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    @Override
    public FillEllipse2D color(int r, int g, int b, int a)
    {
        this.ri = this.ro = r;
        this.gi = this.go = g;
        this.bi = this.bo = b;
        this.ai = this.ao = a;
        return this;
    }
    
    @Override
    public FillEllipse2D color0(int r, int g, int b, int a)
    {
        this.ri = r;
        this.gi = g;
        this.bi = b;
        this.ai = a;
        return this;
    }
    
    @Override
    public FillEllipse2D color1(int r, int g, int b, int a)
    {
        this.ro = r;
        this.go = g;
        this.bo = b;
        this.ao = a;
        return this;
    }
}
