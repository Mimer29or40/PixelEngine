package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import rutils.Math;

public class FillRing2D extends Draw2D implements Point<FillRing2D>,
                                                  Radius0<FillRing2D>,
                                                  Radius1<FillRing2D>,
                                                  StartStop<FillRing2D>,
                                                  Rotation<FillRing2D>,
                                                  Segments<FillRing2D>,
                                                  Color<FillRing2D>,
                                                  Color0<FillRing2D>,
                                                  Color1<FillRing2D>
{
    private double x, y;
    private boolean hasCenter;
    
    private double rxi, ryi;
    private boolean hasSizeI;
    
    private double rxo, ryo;
    private boolean hasSizeO;
    
    private double start, stop;
    
    private double originX, originY;
    
    private double angle;
    
    private int segments;
    
    private int ri, gi, bi, ai;
    private int ro, go, bo, ao;
    
    @Override
    public String toString()
    {
        return "FillRing2D{" +
               "center=(" + this.x + ", " + this.y + ')' + ' ' +
               "radius0=(" + this.rxi + ", " + this.ryi + ')' + ' ' +
               "radius1=(" + this.rxo + ", " + this.ryo + ')' + ' ' +
               "angles=(" + this.start + ", " + this.stop + ')' + ' ' +
               "rotationOrigin=(" + this.originX + ", " + this.originY + ')' + ' ' +
               "rotationAngle=" + this.angle + ' ' +
               "segments=" + this.segments + ' ' +
               "color0=(" + this.ri + ", " + this.gi + ", " + this.bi + ", " + this.ai + ')' + ' ' +
               "color1=(" + this.ro + ", " + this.go + ", " + this.bo + ", " + this.ao + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.hasCenter = false;
        this.hasSizeI  = false;
        this.hasSizeO  = false;
        
        this.start = 0;
        this.stop  = Math.PI2;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.segments = 0;
        
        this.ri = this.ro = 255;
        this.gi = this.go = 255;
        this.bi = this.bo = 255;
        this.ai = this.ao = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasCenter) throw new IllegalStateException("Must provide center");
        if (!this.hasSizeI) throw new IllegalStateException("Must provide inner size");
        if (!this.hasSizeO) throw new IllegalStateException("Must provide outer size");
    }
    
    @Override
    protected void drawImpl()
    {
        fillRing(this.x, this.y, this.rxi, this.ryi, this.rxo, this.ryo, this.start, this.stop,
                 this.originX, this.originY, this.angle, this.segments,
                 this.ri, this.gi, this.bi, this.ai, this.ro, this.go, this.bo, this.ao);
    }
    
    public FillRing2D point(double x, double y)
    {
        this.x         = x;
        this.y         = y;
        this.hasCenter = true;
        return this;
    }
    
    @Override
    public FillRing2D radius0(double x, double y)
    {
        this.rxi      = x;
        this.ryi      = y;
        this.hasSizeI = true;
        return this;
    }
    
    @Override
    public FillRing2D radius1(double x, double y)
    {
        this.rxo      = x;
        this.ryo      = y;
        this.hasSizeO = true;
        return this;
    }
    
    @Override
    public FillRing2D startAngle(double start)
    {
        this.start = start;
        return this;
    }
    
    @Override
    public FillRing2D stopAngle(double stop)
    {
        this.stop = stop;
        return this;
    }
    
    @Override
    public FillRing2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public FillRing2D rotationAngle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    @Override
    public FillRing2D segments(int segments)
    {
        this.segments = segments;
        return this;
    }
    
    @Override
    public FillRing2D color(int r, int g, int b, int a)
    {
        this.ri = this.ro = r;
        this.gi = this.go = g;
        this.bi = this.bo = b;
        this.ai = this.ao = a;
        return this;
    }
    
    @Override
    public FillRing2D color0(int r, int g, int b, int a)
    {
        this.ri = r;
        this.gi = g;
        this.bi = b;
        this.ai = a;
        return this;
    }
    
    public FillRing2D color0(@NotNull Colorc color)
    {
        return color0(color.r(), color.g(), color.b(), color.a());
    }
    
    @Override
    public FillRing2D color1(int r, int g, int b, int a)
    {
        this.ro = r;
        this.go = g;
        this.bo = b;
        this.ao = a;
        return this;
    }
}
