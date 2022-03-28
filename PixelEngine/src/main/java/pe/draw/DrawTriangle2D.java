package pe.draw;

import java.util.Arrays;

public class DrawTriangle2D extends Draw2D implements Point0<DrawTriangle2D>,
                                                      Point1<DrawTriangle2D>,
                                                      Point2<DrawTriangle2D>,
                                                      Thickness<DrawTriangle2D>,
                                                      Color<DrawTriangle2D>
{
    private double x0, y0;
    private boolean hasPoint0;
    
    private double x1, y1;
    private boolean hasPoint1;
    
    private double x2, y2;
    private boolean hasPoint2;
    
    private double  thickness;
    private boolean hasThickness;
    
    private int r, g, b, a;
    
    private double[] points;
    
    @Override
    public String toString()
    {
        return "DrawTriangle2D{" +
               "point0=(" + this.x0 + ", " + this.y0 + ')' + ' ' +
               "point1=(" + this.x1 + ", " + this.y1 + ')' + ' ' +
               "point2=(" + this.x2 + ", " + this.y2 + ')' + ' ' +
               "thickness=" + this.thickness + ' ' +
               "color=(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasPoint0 = false;
        this.hasPoint1 = false;
        this.hasPoint2 = false;
        
        this.hasThickness = false;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
        
        if (this.points == null) this.points = new double[12];
        Arrays.fill(this.points, 0.0);
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint0) throw new IllegalStateException("Must provide point0");
        if (!this.hasPoint1) throw new IllegalStateException("Must provide point1");
        if (!this.hasPoint2) throw new IllegalStateException("Must provide point2");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        if (this.thickness <= 0) return;
        
        this.points[0]  = this.x2;
        this.points[1]  = this.y2;
        this.points[2]  = this.x0;
        this.points[3]  = this.y0;
        this.points[4]  = this.x1;
        this.points[5]  = this.y1;
        this.points[6]  = this.x2;
        this.points[7]  = this.y2;
        this.points[8]  = this.x0;
        this.points[9]  = this.y0;
        this.points[10] = this.x1;
        this.points[11] = this.y1;
        
        drawLines(this.points, this.thickness,
                  this.r, this.g, this.b, this.a,
                  this.r, this.g, this.b, this.a);
    }
    
    @Override
    public DrawTriangle2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    @Override
    public DrawTriangle2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    @Override
    public DrawTriangle2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    @Override
    public DrawTriangle2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawTriangle2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
