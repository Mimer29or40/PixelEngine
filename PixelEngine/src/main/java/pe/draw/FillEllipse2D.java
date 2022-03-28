package pe.draw;

import rutils.Math;

public class FillEllipse2D extends Draw2D implements Point<FillEllipse2D>,
                                                     Radius<FillEllipse2D>,
                                                     Corners<FillEllipse2D>,
                                                     StartStop<FillEllipse2D>,
                                                     Rotation<FillEllipse2D>,
                                                     Segments<FillEllipse2D>,
                                                     Color<FillEllipse2D>,
                                                     ColorRoundQuad<FillEllipse2D>
{
    private double x, y;
    private boolean hasCenter;
    
    private double rx, ry;
    private boolean hasSize;
    
    private double startAngle, stopAngle;
    
    private double rotationOriginX, rotationOriginY;
    
    private double rotationAngle;
    
    private int segments;
    
    private int innerStartR, innerStartG, innerStartB, innerStartA;
    private int innerStopR, innerStopG, innerStopB, innerStopA;
    private int outerStartR, outerStartG, outerStartB, outerStartA;
    private int outerStopR, outerStopG, outerStopB, outerStopA;
    
    @Override
    public String toString()
    {
        return "FillEllipse2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "radius=(" + this.rx + ", " + this.ry + ')' + ' ' +
               "angles=(" + this.startAngle + ", " + this.stopAngle + ')' + ' ' +
               "rotationOrigin=(" + this.rotationOriginX + ", " + this.rotationOriginY + ')' + ' ' +
               "rotationAngle=" + this.rotationAngle + ' ' +
               "segments=" + this.segments + ' ' +
               "innerStart=(" + this.innerStartR + ", " + this.innerStartG + ", " + this.innerStartB + ", " + this.innerStartA + ')' + ' ' +
               "innerStop=(" + this.innerStopR + ", " + this.innerStopG + ", " + this.innerStopB + ", " + this.innerStopA + ')' + ' ' +
               "outerStart=(" + this.outerStartR + ", " + this.outerStartG + ", " + this.outerStartB + ", " + this.outerStartA + ')' + ' ' +
               "outerStop=(" + this.outerStopR + ", " + this.outerStopG + ", " + this.outerStopB + ", " + this.outerStopA + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasCenter = false;
        this.hasSize   = false;
        
        this.startAngle = 0;
        this.stopAngle  = Math.PI2;
        
        this.rotationOriginX = 0.0;
        this.rotationOriginY = 0.0;
        
        this.rotationAngle = 0.0;
        
        this.segments = 0;
        
        this.innerStartR = this.innerStopR = this.outerStartR = this.outerStopR = 255;
        this.innerStartG = this.innerStopG = this.outerStartG = this.outerStopG = 255;
        this.innerStartB = this.innerStopB = this.outerStartB = this.outerStopB = 255;
        this.innerStartA = this.innerStopA = this.outerStartA = this.outerStopA = 255;
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
        fillEllipse(this.x, this.y,
                    this.rx, this.ry,
                    this.startAngle, this.stopAngle,
                    this.rotationOriginX, this.rotationOriginY, this.rotationAngle,
                    this.segments,
                    this.innerStartR, this.innerStartG, this.innerStartB, this.innerStartA,
                    this.innerStopR, this.innerStopG, this.innerStopB, this.innerStopA,
                    this.outerStartR, this.outerStartG, this.outerStartB, this.outerStartA,
                    this.outerStopR, this.outerStopG, this.outerStopB, this.outerStopA);
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
        this.startAngle = start;
        return this;
    }
    
    @Override
    public FillEllipse2D stopAngle(double stop)
    {
        this.stopAngle = stop;
        return this;
    }
    
    @Override
    public FillEllipse2D rotationOrigin(double x, double y)
    {
        this.rotationOriginX = x;
        this.rotationOriginY = y;
        return this;
    }
    
    @Override
    public FillEllipse2D rotationAngle(double angleRadians)
    {
        this.rotationAngle = angleRadians;
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
        this.innerStartR = this.innerStopR = this.outerStartR = this.outerStopR = r;
        this.innerStartG = this.innerStopG = this.outerStartG = this.outerStopG = g;
        this.innerStartB = this.innerStopB = this.outerStartB = this.outerStopB = b;
        this.innerStartA = this.innerStopA = this.outerStartA = this.outerStopA = a;
        return this;
    }
    
    @Override
    public FillEllipse2D innerStartColor(int r, int g, int b, int a)
    {
        this.innerStartR = r;
        this.innerStartG = g;
        this.innerStartB = b;
        this.innerStartA = a;
        return this;
    }
    
    @Override
    public FillEllipse2D innerStopColor(int r, int g, int b, int a)
    {
        this.innerStopR = r;
        this.innerStopG = g;
        this.innerStopB = b;
        this.innerStopA = a;
        return this;
    }
    
    @Override
    public FillEllipse2D outerStartColor(int r, int g, int b, int a)
    {
        this.outerStartR = r;
        this.outerStartG = g;
        this.outerStartB = b;
        this.outerStartA = a;
        return this;
    }
    
    @Override
    public FillEllipse2D outerStopColor(int r, int g, int b, int a)
    {
        this.outerStopR = r;
        this.outerStopG = g;
        this.outerStopB = b;
        this.outerStopA = a;
        return this;
    }
}
