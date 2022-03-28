package pe.draw;

public class DrawRect2D extends Draw2D implements Point<DrawRect2D>,
                                                  Size<DrawRect2D>,
                                                  Corners<DrawRect2D>,
                                                  Thickness<DrawRect2D>,
                                                  CornerRadius<DrawRect2D>,
                                                  Rotation<DrawRect2D>,
                                                  Color<DrawRect2D>
{
    private double x, y;
    private boolean hasPoint;
    
    private double width, height;
    private boolean hasSize;
    
    private double  thickness;
    private boolean hasThickness;
    
    private double cornerRadius;
    
    private double originX, originY;
    
    private double angle;
    
    private int r, g, b, a;
    
    @Override
    protected void reset()
    {
        this.hasPoint     = false;
        this.hasSize      = false;
        this.hasThickness = false;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    public String toString()
    {
        return "DrawRect2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "size=(" + this.width + ", " + this.height + ')' + ' ' +
               "thickness=" + this.thickness + ' ' +
               "rotationOrigin=(" + this.originX + ", " + this.originY + ')' + ' ' +
               "rotationAngle=" + this.angle + ' ' +
               "color=(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ')' +
               '}';
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint) throw new IllegalStateException("Must provide point");
        if (!this.hasSize) throw new IllegalStateException("Must provide radius");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    protected void drawImpl()
    {
        drawRect(this.x, this.y, this.width, this.height, this.thickness, this.cornerRadius,
                 this.originX, this.originY, this.angle,
                 this.r, this.g, this.b, this.a);
    }
    
    @Override
    public DrawRect2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    @Override
    public DrawRect2D size(double width, double height)
    {
        this.width   = width;
        this.height  = height;
        this.hasSize = true;
        return this;
    }
    
    @Override
    public DrawRect2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
    {
        this.width  = bottomRightX - topLeftX;
        this.height = bottomRightY - topLeftY;
        this.x      = topLeftX + (this.width * 0.5);
        this.y      = topLeftY + (this.height * 0.5);
        
        this.hasPoint = true;
        this.hasSize  = true;
        return this;
    }
    
    @Override
    public DrawRect2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawRect2D cornerRadius(double cornerRadius)
    {
        this.cornerRadius = cornerRadius;
        return this;
    }
    
    @Override
    public DrawRect2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public DrawRect2D rotationAngle(double angleRadians)
    {
        this.angle = angleRadians;
        return this;
    }
    
    @Override
    public DrawRect2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
