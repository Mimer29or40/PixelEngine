package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import pe.texture.Texture;
import rutils.Logger;

public class DrawTexture2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private Texture texture;
    
    private double srcX, srcY, srcW, srcH;
    
    private double dstX, dstY, dstW, dstH;
    private boolean hasDst;
    
    private double originX, originY;
    
    private double angle;
    
    private int r, g, b, a;
    
    @Override
    protected void reset()
    {
        this.texture = null;
        
        this.srcX = 0.0;
        this.srcY = 0.0;
        
        this.hasDst = false;
        
        this.originX = 0.0;
        this.originY = 0.0;
        
        this.angle = 0.0;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    protected void check()
    {
        if (this.texture == null) throw new IllegalStateException("Must provide texture");
        if (!this.hasDst) throw new IllegalStateException("Must provide dst");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawTexture2D.LOGGER.finest("Drawing texture=%s src=(%s, %s, %s, %s) dst=(%s, %s, %s, %s) origin=(%s, %s) rotation=%s tint=(%s, %s, %s, %s)",
                                    this.texture,
                                    this.srcX, this.srcY, this.srcW, this.srcH,
                                    this.dstX, this.dstY, this.dstW, this.dstH,
                                    this.originX, this.originY, this.angle,
                                    this.r, this.g, this.b, this.a);
        
        drawTexture(this.texture,
                    this.srcX, this.srcY, this.srcW, this.srcH,
                    this.dstX, this.dstY, this.dstW, this.dstH,
                    this.originX, this.originY, this.angle,
                    this.r, this.g, this.b, this.a);
    }
    
    public DrawTexture2D texture(@NotNull Texture texture)
    {
        this.texture = texture;
        this.srcW    = this.dstW = texture.width();
        this.srcH    = this.dstH = texture.height();
        return this;
    }
    
    public DrawTexture2D src(double x, double y, double width, double height)
    {
        this.srcX = x;
        this.srcY = y;
        this.srcW = width;
        this.srcH = height;
        return this;
    }
    
    public DrawTexture2D src(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D src(@NotNull Vector2fc pos, @NotNull Vector2fc size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D src(@NotNull Vector2dc pos, @NotNull Vector2dc size)
    {
        return src(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D src(double x, double y)
    {
        this.srcX = x;
        this.srcY = y;
        return this;
    }
    
    public DrawTexture2D src(@NotNull Vector2ic pos)
    {
        return src(pos.x(), pos.y());
    }
    
    public DrawTexture2D src(@NotNull Vector2fc pos)
    {
        return src(pos.x(), pos.y());
    }
    
    public DrawTexture2D src(@NotNull Vector2dc pos)
    {
        return src(pos.x(), pos.y());
    }
    
    public DrawTexture2D dst(double x, double y, double width, double height)
    {
        this.dstX   = x;
        this.dstY   = y;
        this.dstW   = width;
        this.dstH   = height;
        this.hasDst = true;
        return this;
    }
    
    public DrawTexture2D dst(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D dst(@NotNull Vector2fc pos, @NotNull Vector2fc size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D dst(@NotNull Vector2dc pos, @NotNull Vector2dc size)
    {
        return dst(pos.x(), pos.y(), size.x(), size.y());
    }
    
    public DrawTexture2D dst(double x, double y)
    {
        this.dstX   = x;
        this.dstY   = y;
        this.hasDst = true;
        return this;
    }
    
    public DrawTexture2D dst(@NotNull Vector2ic pos)
    {
        return dst(pos.x(), pos.y());
    }
    
    public DrawTexture2D dst(@NotNull Vector2fc pos)
    {
        return dst(pos.x(), pos.y());
    }
    
    public DrawTexture2D dst(@NotNull Vector2dc pos)
    {
        return dst(pos.x(), pos.y());
    }
    
    public DrawTexture2D origin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    public DrawTexture2D origin(@NotNull Vector2ic origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawTexture2D origin(@NotNull Vector2fc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawTexture2D origin(@NotNull Vector2dc origin)
    {
        return origin(origin.x(), origin.y());
    }
    
    public DrawTexture2D angle(double angle)
    {
        this.angle = angle;
        return this;
    }
    
    public DrawTexture2D tint(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawTexture2D tint(@NotNull Colorc color)
    {
        return tint(color.r(), color.g(), color.b(), color.a());
    }
}
