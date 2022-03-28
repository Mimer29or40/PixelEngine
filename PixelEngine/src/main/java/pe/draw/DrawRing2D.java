package pe.draw;

import rutils.Math;

public class DrawRing2D extends Draw2D implements Point<DrawRing2D>,
                                                  RadiusInner<DrawRing2D>,
                                                  RadiusOuter<DrawRing2D>,
                                                  Thickness<DrawRing2D>,
                                                  StartStop<DrawRing2D>,
                                                  Rotation<DrawRing2D>,
                                                  Segments<DrawRing2D>,
                                                  Color<DrawRing2D>
{
    private double x, y;
    private boolean hasCenter;
    
    private double innerRX, innerRY;
    private boolean hasInner;
    
    private double outerRX, outerRY;
    private boolean hasOuter;
    
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
        return "DrawRing2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "radius0=(" + this.innerRX + ", " + this.innerRY + ')' + ' ' +
               "radius1=(" + this.outerRX + ", " + this.outerRY + ')' + ' ' +
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
        this.hasInner     = false;
        this.hasOuter     = false;
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
        if (!this.hasInner) throw new IllegalStateException("Must provide inner size");
        if (!this.hasOuter) throw new IllegalStateException("Must provide outer size");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        drawEllipse(this.x, this.y,
                    this.innerRX, this.innerRY,
                    this.thickness,
                    this.startAngle, this.stopAngle,
                    this.rotationOriginX, this.rotationOriginY, this.rotationAngle,
                    this.segments,
                    this.r, this.g, this.b, this.a);
        
        drawEllipse(this.x, this.y,
                    this.outerRX, this.outerRY,
                    this.thickness,
                    this.startAngle, this.stopAngle,
                    this.rotationOriginX, this.rotationOriginY, this.rotationAngle,
                    this.segments,
                    this.r, this.g, this.b, this.a);
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
    public DrawRing2D innerRadius(double x, double y)
    {
        this.innerRX  = x;
        this.innerRY  = y;
        this.hasInner = true;
        return this;
    }
    
    @Override
    public DrawRing2D outerRadius(double x, double y)
    {
        this.outerRX  = x;
        this.outerRY  = y;
        this.hasOuter = true;
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
        this.startAngle = start;
        return this;
    }
    
    @Override
    public DrawRing2D stopAngle(double stop)
    {
        this.stopAngle = stop;
        return this;
    }
    
    @Override
    public DrawRing2D rotationOrigin(double x, double y)
    {
        this.rotationOriginX = x;
        this.rotationOriginY = y;
        return this;
    }
    
    @Override
    public DrawRing2D rotationAngle(double angle)
    {
        this.rotationAngle = angle;
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
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
