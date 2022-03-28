package pe.draw;

public class FillTriangle2D extends Draw2D implements Point0<FillTriangle2D>,
                                                      Point1<FillTriangle2D>,
                                                      Point2<FillTriangle2D>,
                                                      Color<FillTriangle2D>,
                                                      Color0<FillTriangle2D>,
                                                      Color1<FillTriangle2D>,
                                                      Color2<FillTriangle2D>
{
    private double x0, y0;
    private boolean hasPoint0;
    
    private double x1, y1;
    private boolean hasPoint1;
    
    private double x2, y2;
    private boolean hasPoint2;
    
    private int r0, g0, b0, a0;
    private int r1, g1, b1, a1;
    private int r2, g2, b2, a2;
    
    @Override
    public String toString()
    {
        return "FillTriangle2D{" +
               "point0=(" + this.x0 + ", " + this.y0 + ')' + ' ' +
               "point1=(" + this.x1 + ", " + this.y1 + ')' + ' ' +
               "point2=(" + this.x2 + ", " + this.y2 + ')' + ' ' +
               "color0=(" + this.r0 + ", " + this.g0 + ", " + this.b0 + ", " + this.a0 + ')' + ' ' +
               "color1=(" + this.r1 + ", " + this.g1 + ", " + this.b1 + ", " + this.a1 + ')' + ' ' +
               "color2=(" + this.r2 + ", " + this.g2 + ", " + this.b2 + ", " + this.a2 + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasPoint0 = false;
        this.hasPoint1 = false;
        this.hasPoint2 = false;
        
        this.r0 = this.r1 = this.r2 = 255;
        this.g0 = this.g1 = this.g2 = 255;
        this.b0 = this.b1 = this.b2 = 255;
        this.a0 = this.a1 = this.a2 = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint0) throw new IllegalStateException("Must provide point0");
        if (!this.hasPoint1) throw new IllegalStateException("Must provide point1");
        if (!this.hasPoint2) throw new IllegalStateException("Must provide point2");
    }
    
    @Override
    protected void drawImpl()
    {
        fillTriangle(this.x0, this.y0,
                     this.x1, this.y1,
                     this.x2, this.y2,
                     this.r0, this.g0, this.b0, this.a0,
                     this.r1, this.g1, this.b1, this.a1,
                     this.r2, this.g2, this.b2, this.a2);
    }
    
    @Override
    public FillTriangle2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    @Override
    public FillTriangle2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    @Override
    public FillTriangle2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    @Override
    public FillTriangle2D color(int r, int g, int b, int a)
    {
        this.r0 = this.r1 = this.r2 = r;
        this.g0 = this.g1 = this.g2 = g;
        this.b0 = this.b1 = this.b2 = b;
        this.a0 = this.a1 = this.a2 = a;
        return this;
    }
    
    @Override
    public FillTriangle2D color0(int r, int g, int b, int a)
    {
        this.r0 = r;
        this.g0 = g;
        this.b0 = b;
        this.a0 = a;
        return this;
    }
    
    @Override
    public FillTriangle2D color1(int r, int g, int b, int a)
    {
        this.r1 = r;
        this.g1 = g;
        this.b1 = b;
        this.a1 = a;
        return this;
    }
    
    @Override
    public FillTriangle2D color2(int r, int g, int b, int a)
    {
        this.r2 = r;
        this.g2 = g;
        this.b2 = b;
        this.a2 = a;
        return this;
    }
}
