package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import rutils.Math;

public class FillRect2D extends Draw2D implements Point<FillRect2D>,
                                                  Size<FillRect2D>,
                                                  Corners<FillRect2D>,
                                                  Rotation<FillRect2D>,
                                                  Color<FillRect2D>,
                                                  Color0<FillRect2D>,
                                                  Color1<FillRect2D>,
                                                  Color2<FillRect2D>,
                                                  Color3<FillRect2D>
{
    private double x, y;
    private boolean hasPoint;
    
    private double width, height;
    private boolean hasSize;
    
    private double originX, originY;
    
    private double angle;
    
    private int r0, g0, b0, a0;
    private int r1, g1, b1, a1;
    private int r2, g2, b2, a2;
    private int r3, g3, b3, a3;
    
    @Override
    public String toString()
    {
        return "FillRect2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "size=(" + this.width + ", " + this.height + ')' + ' ' +
               "rotationOrigin=(" + this.originX + ", " + this.originY + ')' + ' ' +
               "rotationAngle=" + this.angle + ' ' +
               "color0=(" + this.r0 + ", " + this.g0 + ", " + this.b0 + ", " + this.a0 + ')' + ' ' +
               "color1=(" + this.r1 + ", " + this.g1 + ", " + this.b1 + ", " + this.a1 + ')' + ' ' +
               "color2=(" + this.r2 + ", " + this.g2 + ", " + this.b2 + ", " + this.a2 + ')' + ' ' +
               "color3=(" + this.r3 + ", " + this.g3 + ", " + this.b3 + ", " + this.a3 + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasPoint = false;
        this.hasSize  = false;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.r0 = this.r1 = this.r2 = this.r3 = 255;
        this.g0 = this.g1 = this.g2 = this.g3 = 255;
        this.b0 = this.b1 = this.b2 = this.b3 = 255;
        this.a0 = this.a1 = this.a2 = this.a3 = 255;
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
        // if (this.width <= 0.0 || this.height <= 0.0) return;
        
        double halfW = this.width * 0.5;
        double halfH = this.height * 0.5;
        
        double x0, y0, x1, y1, x2, y2, x3, y3;
        
        // Only calculate rotation if needed
        if (Math.equals(this.angle, 0.0, 1e-6))
        {
            x0 = this.x - halfW;
            y0 = this.y - halfH;
            
            x1 = this.x - halfW;
            y1 = this.y + halfH;
            
            x2 = this.x + halfW;
            y2 = this.y + halfH;
            
            x3 = this.x + halfW;
            y3 = this.y - halfH;
        }
        else
        {
            double s = Math.sin(this.angle);
            double c = Math.cos(this.angle);
            
            double minCX = (-halfW - this.originX) * c;
            double maxCX = (halfW - this.originX) * c;
            double minSX = (-halfW - this.originX) * s;
            double maxSX = (halfW - this.originX) * s;
            double minCY = (-halfH - this.originY) * c;
            double maxCY = (halfH - this.originY) * c;
            double minSY = (-halfH - this.originY) * s;
            double maxSY = (halfH - this.originY) * s;
            
            x0 = this.x + this.originX + minCX - minSY;
            y0 = this.y + this.originY + minSX + minCY;
            
            x1 = this.x + this.originX + minCX - maxSY;
            y1 = this.y + this.originY + minSX + maxCY;
            
            x2 = this.x + this.originX + maxCX - maxSY;
            y2 = this.y + this.originY + maxSX + maxCY;
            
            x3 = this.x + this.originX + maxCX - minSY;
            y3 = this.y + this.originY + maxSX + minCY;
        }
        
        fillQuad(x0, y0, x1, y1, x2, y2, x3, y3,
                 this.r0, this.g0, this.b0, this.a0,
                 this.r1, this.g1, this.b1, this.a1,
                 this.r2, this.g2, this.b2, this.a2,
                 this.r3, this.g3, this.b3, this.a3);
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
    public FillRect2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public FillRect2D rotationAngle(double angleRadians)
    {
        this.angle = angleRadians;
        return this;
    }
    
    @Override
    public FillRect2D color(int r, int g, int b, int a)
    {
        this.r0 = this.r1 = this.r2 = this.r3 = r;
        this.g0 = this.g1 = this.g2 = this.g3 = g;
        this.b0 = this.b1 = this.b2 = this.b3 = b;
        this.a0 = this.a1 = this.a2 = this.a3 = a;
        return this;
    }
    
    @Override
    public FillRect2D color0(int r, int g, int b, int a)
    {
        this.r0 = r;
        this.g0 = g;
        this.b0 = b;
        this.a0 = a;
        return this;
    }
    
    @Override
    public FillRect2D color1(int r, int g, int b, int a)
    {
        this.r1 = r;
        this.g1 = g;
        this.b1 = b;
        this.a1 = a;
        return this;
    }
    
    @Override
    public FillRect2D color2(int r, int g, int b, int a)
    {
        this.r2 = r;
        this.g2 = g;
        this.b2 = b;
        this.a2 = a;
        return this;
    }
    
    @Override
    public FillRect2D color3(int r, int g, int b, int a)
    {
        this.r3 = r;
        this.g3 = g;
        this.b3 = b;
        this.a3 = a;
        return this;
    }
    
    public FillRect2D gradientV(int rTop, int gTop, int bTop, int aTop, int rBottom, int gBottom, int bBottom, int aBottom)
    {
        this.r0 = this.r3 = rTop;
        this.g0 = this.g3 = gTop;
        this.b0 = this.b3 = bTop;
        this.a0 = this.a3 = aTop;
        this.r1 = this.r2 = rBottom;
        this.g1 = this.g2 = gBottom;
        this.b1 = this.b2 = bBottom;
        this.a1 = this.a2 = aBottom;
        return this;
    }
    
    public FillRect2D gradientV(@NotNull Colorc top, @NotNull Colorc bottom)
    {
        return gradientV(top.r(), top.g(), top.b(), top.a(), bottom.r(), bottom.g(), bottom.b(), bottom.a());
    }
    
    public FillRect2D gradientH(int rLeft, int gLeft, int bLeft, int aLeft, int rRight, int gRight, int bRight, int aRight)
    {
        this.r0 = this.r1 = rLeft;
        this.g0 = this.g1 = gLeft;
        this.b0 = this.b1 = bLeft;
        this.a0 = this.a1 = aLeft;
        this.r2 = this.r3 = rRight;
        this.g2 = this.g3 = gRight;
        this.b2 = this.b3 = bRight;
        this.a2 = this.a3 = aRight;
        return this;
    }
    
    public FillRect2D gradientH(@NotNull Colorc left, @NotNull Colorc right)
    {
        return gradientH(left.r(), left.g(), left.b(), left.a(), right.r(), right.g(), right.b(), right.a());
    }
}
