package pe.draw;

import org.jetbrains.annotations.NotNull;

public class DrawTextureWarped2D extends Draw2D implements Texture<DrawTextureWarped2D>,
                                                           Point0<DrawTextureWarped2D>,
                                                           Point1<DrawTextureWarped2D>,
                                                           Point2<DrawTextureWarped2D>,
                                                           Point3<DrawTextureWarped2D>,
                                                           UV0<DrawTextureWarped2D>,
                                                           UV1<DrawTextureWarped2D>,
                                                           UV2<DrawTextureWarped2D>,
                                                           UV3<DrawTextureWarped2D>,
                                                           Color<DrawTextureWarped2D>
{
    private pe.texture.Texture texture;
    
    private double x0, y0, x1, y1, x2, y2, x3, y3;
    private boolean hasPoint0, hasPoint1, hasPoint2, hasPoint3;
    
    private double u0, v0, u1, v1, u2, v2, u3, v3;
    
    private int r, g, b, a;
    
    @Override
    public String toString()
    {
        return "DrawTextureWarped2D{" +
               "texture=" + this.texture + ' ' +
               "point0=(" + this.x0 + ", " + this.y0 + ')' + ' ' +
               "point1=(" + this.x1 + ", " + this.y1 + ')' + ' ' +
               "point2=(" + this.x2 + ", " + this.y2 + ')' + ' ' +
               "point3=(" + this.x3 + ", " + this.y3 + ')' + ' ' +
               "uv0=(" + this.u0 + ", " + this.v0 + ')' + ' ' +
               "uv1=(" + this.u1 + ", " + this.v1 + ')' + ' ' +
               "uv2=(" + this.u2 + ", " + this.v2 + ')' + ' ' +
               "uv3=(" + this.u3 + ", " + this.v3 + ')' + ' ' +
               "color=(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ')' +
               '}';
    }
    
    @Override
    protected void reset()
    {
        this.texture = null;
        
        this.hasPoint0 = false;
        this.hasPoint1 = false;
        this.hasPoint2 = false;
        this.hasPoint3 = false;
        
        this.u0 = 0.0;
        this.v0 = 0.0;
        
        this.u1 = 0.0;
        this.v1 = 1.0;
        
        this.u2 = 1.0;
        this.v2 = 1.0;
        
        this.u3 = 1.0;
        this.v3 = 0.0;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    protected void check()
    {
        if (this.texture == null) throw new IllegalStateException("Must provide texture");
        if (!this.hasPoint0) throw new IllegalStateException("Must provide point0");
        if (!this.hasPoint1) throw new IllegalStateException("Must provide point1");
        if (!this.hasPoint2) throw new IllegalStateException("Must provide point2");
        if (!this.hasPoint3) throw new IllegalStateException("Must provide point3");
    }
    
    @Override
    protected void drawImpl()
    {
        drawTexture(this.texture,
                    this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.x3, this.y3,
                    this.u0, this.v0, this.u1, this.v1, this.u2, this.v2, this.u3, this.v3,
                    this.r, this.g, this.b, this.a);
    }
    
    @Override
    public DrawTextureWarped2D texture(@NotNull pe.texture.Texture texture)
    {
        this.texture = texture;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D point3(double x, double y)
    {
        this.x3        = x;
        this.y3        = y;
        this.hasPoint3 = true;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D uv0(double u, double v)
    {
        this.u0 = u;
        this.v0 = v;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D uv1(double u, double v)
    {
        this.u1 = u;
        this.v1 = v;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D uv2(double u, double v)
    {
        this.u2 = u;
        this.v2 = v;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D uv3(double u, double v)
    {
        this.u3 = u;
        this.v3 = v;
        return this;
    }
    
    @Override
    public DrawTextureWarped2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
