package pe.draw;

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
    private double x, y;
    private boolean hasCenter;
    
    private double rx, ry;
    private boolean hasSize;
    
    private double  thickness;
    private boolean hasThickness;
    
    private double startAngle, stopAngle;
    
    private double rotationOriginX, rotationOriginY;
    
    private double rotationAngle;
    
    private int segments;
    
    private int r, g, b, a;
    
    @Override
    public String toString()
    {
        return "DrawEllipse2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "radius=(" + this.rx + ", " + this.ry + ')' + ' ' +
               "thickness=" + this.thickness + ' ' +
               "angles=(" + this.startAngle + ", " + this.stopAngle + ')' + ' ' +
               "rotationOrigin=(" + this.rotationOriginX + ", " + this.rotationOriginY + ')' + ' ' +
               "rotationAngle=" + this.rotationAngle + ' ' +
               "segments=" + this.segments + ' ' +
               "color=(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasCenter    = false;
        this.hasSize      = false;
        this.hasThickness = false;
        
        this.startAngle = 0;
        this.stopAngle  = Math.PI2;
        
        this.rotationOriginX = 0.0;
        this.rotationOriginY = 0.0;
        
        this.rotationAngle = 0.0;
        
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
        drawEllipse(this.x, this.y, this.rx, this.ry, this.thickness, this.startAngle, this.stopAngle,
                    this.rotationOriginX, this.rotationOriginY, this.rotationAngle, this.segments,
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
        this.startAngle = start;
        return this;
    }
    
    @Override
    public DrawEllipse2D stopAngle(double stop)
    {
        this.stopAngle = stop;
        return this;
    }
    
    @Override
    public DrawEllipse2D rotationOrigin(double x, double y)
    {
        this.rotationOriginX = x;
        this.rotationOriginY = y;
        return this;
    }
    
    @Override
    public DrawEllipse2D rotationAngle(double angleRadians)
    {
        this.rotationAngle = angleRadians;
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
