package pe.draw;

import rutils.Logger;

public class DrawPoint2D extends Draw2D implements Point<DrawPoint2D>,
                                                   Thickness<DrawPoint2D>,
                                                   Color<DrawPoint2D>
{
    private static final Logger LOGGER = new Logger();
    
    private double x, y;
    private boolean hasPoint;
    
    private double  thickness;
    private boolean hasThickness;
    
    private int r, g, b, a;
    
    @Override
    public void reset()
    {
        this.hasPoint = false;
        
        this.hasThickness = false;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint) throw new IllegalStateException("Must provide point0");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawPoint2D.LOGGER.finest("Drawing point=(%s, %s) thickness=%s color=(%s, %s, %s, %s)",
                                  this.x, this.y, this.thickness, this.r, this.g, this.b, this.a);
        
        if (this.thickness <= 0) return;
        
        drawPoint(this.x, this.y, this.thickness, this.r, this.g, this.b, this.a);
    }
    
    @Override
    public DrawPoint2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    @Override
    public DrawPoint2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawPoint2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
