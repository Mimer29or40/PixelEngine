package pe.draw;

import rutils.Logger;
import rutils.Math;

public class DrawEllipse2D extends Draw2D implements Point<DrawEllipse2D>,
                                                     Radius<DrawEllipse2D>,
                                                     Corners<DrawEllipse2D>,
                                                     Thickness<DrawEllipse2D>,
                                                     StartStop<DrawEllipse2D>,
                                                     Rotation<DrawEllipse2D>,
                                                     Segments<DrawEllipse2D>,
                                                     Color<DrawEllipse2D>
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
    
    @Override
    public DrawEllipse2D point(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    @Override
    public DrawEllipse2D radius(double x, double y)
    {
        this.rx      = x;
        this.ry      = y;
        this.hasSize = true;
        return this;
    }
    
    @Override
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
    
    @Override
    public DrawEllipse2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawEllipse2D startAngle(double start)
    {
        this.start = start;
        return this;
    }
    
    @Override
    public DrawEllipse2D stopAngle(double stop)
    {
        this.stop = stop;
        return this;
    }
    
    @Override
    public DrawEllipse2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public DrawEllipse2D rotationAngle(double angleRadians)
    {
        this.angle = angleRadians;
        return this;
    }
    
    @Override
    public DrawEllipse2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    @Override
    public DrawEllipse2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
