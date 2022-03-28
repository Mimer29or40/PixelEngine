package pe.draw;

public class FillRect2D extends Draw2D implements Point<FillRect2D>,
                                                  Size<FillRect2D>,
                                                  Corners<FillRect2D>,
                                                  CornerRadius<FillRect2D>,
                                                  Rotation<FillRect2D>,
                                                  Color<FillRect2D>,
                                                  ColorRect<FillRect2D>
{
    private double x, y;
    private boolean hasPoint;
    
    private double width, height;
    private boolean hasSize;
    
    private double cornerRadius;
    
    private double rotationOriginX, rotationOriginY;
    private double rotationAngle;
    
    private int topLeftR, topLeftG, topLeftB, topLeftA;
    private int topRightR, topRightG, topRightB, topRightA;
    private int bottomLeftR, bottomLeftG, bottomLeftB, bottomLeftA;
    private int bottomRightR, bottomRightG, bottomRightB, bottomRightA;
    
    @Override
    public String toString()
    {
        return "FillRect2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "size=(" + this.width + ", " + this.height + ')' + ' ' +
               "cornerRadius=" + this.cornerRadius + ' ' +
               "rotationOrigin=(" + this.rotationOriginX + ", " + this.rotationOriginY + ')' + ' ' +
               "rotationAngle=" + this.rotationAngle + ' ' +
               "topLeft=(" + this.topLeftR + ", " + this.topLeftG + ", " + this.topLeftB + ", " + this.topLeftA + ')' + ' ' +
               "topRight=(" + this.topRightR + ", " + this.topRightG + ", " + this.topRightB + ", " + this.topRightA + ')' + ' ' +
               "bottomLeft=(" + this.bottomLeftR + ", " + this.bottomLeftG + ", " + this.bottomLeftB + ", " + this.bottomLeftA + ')' + ' ' +
               "bottomRight=(" + this.bottomRightR + ", " + this.bottomRightG + ", " + this.bottomRightB + ", " + this.bottomRightA + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasPoint = false;
        this.hasSize  = false;
        
        this.cornerRadius = 0.0;
        
        this.rotationOriginX = 0.0;
        this.rotationOriginY = 0.0;
        
        this.rotationAngle = 0.0;
        
        this.topLeftR = this.topRightR = this.bottomLeftR = this.bottomRightR = 255;
        this.topLeftG = this.topRightG = this.bottomLeftG = this.bottomRightG = 255;
        this.topLeftB = this.topRightB = this.bottomLeftB = this.bottomRightB = 255;
        this.topLeftA = this.topRightA = this.bottomLeftA = this.bottomRightA = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint) throw new IllegalStateException("Must provide point");
        if (!this.hasSize) throw new IllegalStateException("Must provide radius");
    }
    
    @Override
    protected void drawImpl()
    {
        fillRect(this.x, this.y,
                 this.width, this.height,
                 this.cornerRadius,
                 this.rotationOriginX, this.rotationOriginY, this.rotationAngle,
                 this.topLeftR, this.topLeftG, this.topLeftB, this.topLeftA,
                 this.topRightR, this.topRightG, this.topRightB, this.topRightA,
                 this.bottomLeftR, this.bottomLeftG, this.bottomLeftB, this.bottomLeftA,
                 this.bottomRightR, this.bottomRightG, this.bottomRightB, this.bottomRightA);
    }
    
    @Override
    public FillRect2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    @Override
    public FillRect2D size(double x, double y)
    {
        this.width   = x;
        this.height  = y;
        this.hasSize = true;
        return this;
    }
    
    @Override
    public FillRect2D corners(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY)
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
    public FillRect2D cornerRadius(double cornerRadius)
    {
        this.cornerRadius = cornerRadius;
        return this;
    }
    
    @Override
    public FillRect2D rotationOrigin(double x, double y)
    {
        this.rotationOriginX = x;
        this.rotationOriginY = y;
        return this;
    }
    
    @Override
    public FillRect2D rotationAngle(double angleRadians)
    {
        this.rotationAngle = angleRadians;
        return this;
    }
    
    @Override
    public FillRect2D color(int r, int g, int b, int a)
    {
        this.topLeftR = this.topRightR = this.bottomLeftR = this.bottomRightR = r;
        this.topLeftG = this.topRightG = this.bottomLeftG = this.bottomRightG = g;
        this.topLeftB = this.topRightB = this.bottomLeftB = this.bottomRightB = b;
        this.topLeftA = this.topRightA = this.bottomLeftA = this.bottomRightA = a;
        return this;
    }
    
    @Override
    public FillRect2D colorTopLeft(int r, int g, int b, int a)
    {
        this.topLeftR = r;
        this.topLeftG = g;
        this.topLeftB = b;
        this.topLeftA = a;
        return this;
    }
    
    @Override
    public FillRect2D colorTopRight(int r, int g, int b, int a)
    {
        this.topRightR = r;
        this.topRightG = g;
        this.topRightB = b;
        this.topRightA = a;
        return this;
    }
    
    @Override
    public FillRect2D colorBottomLeft(int r, int g, int b, int a)
    {
        this.bottomLeftR = r;
        this.bottomLeftG = g;
        this.bottomLeftB = b;
        this.bottomLeftA = a;
        return this;
    }
    
    @Override
    public FillRect2D colorBottomRight(int r, int g, int b, int a)
    {
        this.bottomRightR = r;
        this.bottomRightG = g;
        this.bottomRightB = b;
        this.bottomRightA = a;
        return this;
    }
}
