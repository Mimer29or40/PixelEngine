package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import pe.texture.Texture;
import rutils.Logger;

public class DrawTextureWarped2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private Texture texture;
    
    private double x0, y0, x1, y1, x2, y2, x3, y3;
    private boolean hasPoint0, hasPoint1, hasPoint2, hasPoint3;
    
    private double u0, v0, u1, v1, u2, v2, u3, v3;
    
    private int r, g, b, a;
    
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
        DrawTextureWarped2D.LOGGER.finest("Drawing texture=%s p0=(%s, %s) p1=(%s, %s) p2=(%s, %s) p3=(%s, %s) uv0=(%s, %s) uv1=(%s, %s) uv2=(%s, %s) uv3=(%s, %s) tint=(%s, %s, %s, %s)",
                                          this.texture,
                                          this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.x3, this.y3,
                                          this.u0, this.v0, this.u1, this.v1, this.u2, this.v2, this.u3, this.v3,
                                          this.r, this.g, this.b, this.a);
        
        drawTexture(this.texture,
                    this.x0, this.y0, this.x1, this.y1, this.x2, this.y2, this.x3, this.y3,
                    this.u0, this.v0, this.u1, this.v1, this.u2, this.v2, this.u3, this.v3,
                    this.r, this.g, this.b, this.a);
    }
    
    public DrawTextureWarped2D texture(@NotNull Texture texture)
    {
        this.texture = texture;
        return this;
    }
    
    public DrawTextureWarped2D point0(double x, double y)
    {
        this.x0        = x;
        this.y0        = y;
        this.hasPoint0 = true;
        return this;
    }
    
    public DrawTextureWarped2D point0(@NotNull Vector2ic vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point0(@NotNull Vector2fc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point0(@NotNull Vector2dc vec)
    {
        return point0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point1(double x, double y)
    {
        this.x1        = x;
        this.y1        = y;
        this.hasPoint1 = true;
        return this;
    }
    
    public DrawTextureWarped2D point1(@NotNull Vector2ic vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point1(@NotNull Vector2fc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point1(@NotNull Vector2dc vec)
    {
        return point1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point2(double x, double y)
    {
        this.x2        = x;
        this.y2        = y;
        this.hasPoint2 = true;
        return this;
    }
    
    public DrawTextureWarped2D point2(@NotNull Vector2ic vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point2(@NotNull Vector2fc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point2(@NotNull Vector2dc vec)
    {
        return point2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point3(double x, double y)
    {
        this.x3        = x;
        this.y3        = y;
        this.hasPoint3 = true;
        return this;
    }
    
    public DrawTextureWarped2D point3(@NotNull Vector2ic vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point3(@NotNull Vector2fc vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D point3(@NotNull Vector2dc vec)
    {
        return point3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv0(double u, double v)
    {
        this.u0 = u;
        this.v0 = v;
        return this;
    }
    
    public DrawTextureWarped2D uv0(@NotNull Vector2ic vec)
    {
        return uv0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv0(@NotNull Vector2fc vec)
    {
        return uv0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv0(@NotNull Vector2dc vec)
    {
        return uv0(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv1(double u, double v)
    {
        this.u1 = u;
        this.v1 = v;
        return this;
    }
    
    public DrawTextureWarped2D uv1(@NotNull Vector2ic vec)
    {
        return uv1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv1(@NotNull Vector2fc vec)
    {
        return uv1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv1(@NotNull Vector2dc vec)
    {
        return uv1(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv2(double u, double v)
    {
        this.u2 = u;
        this.v2 = v;
        return this;
    }
    
    public DrawTextureWarped2D uv2(@NotNull Vector2ic vec)
    {
        return uv2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv2(@NotNull Vector2fc vec)
    {
        return uv2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv2(@NotNull Vector2dc vec)
    {
        return uv2(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv3(double u, double v)
    {
        this.u3 = u;
        this.v3 = v;
        return this;
    }
    
    public DrawTextureWarped2D uv3(@NotNull Vector2ic vec)
    {
        return uv3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv3(@NotNull Vector2fc vec)
    {
        return uv3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D uv3(@NotNull Vector2dc vec)
    {
        return uv3(vec.x(), vec.y());
    }
    
    public DrawTextureWarped2D tint(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawTextureWarped2D tint(@NotNull Colorc color)
    {
        return tint(color.r(), color.g(), color.b(), color.a());
    }
}
