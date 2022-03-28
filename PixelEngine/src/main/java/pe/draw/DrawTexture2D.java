package pe.draw;

import org.jetbrains.annotations.NotNull;

public class DrawTexture2D extends Draw2D implements Texture<DrawTexture2D>,
                                                     Src<DrawTexture2D>,
                                                     Dst<DrawTexture2D>,
                                                     Rotation<DrawTexture2D>,
                                                     Color<DrawTexture2D>
{
    private pe.texture.Texture texture;
    
    private double srcX, srcY, srcW, srcH;
    
    private double dstX, dstY, dstW, dstH;
    private boolean hasDst;
    
    private double originX, originY;
    
    private double angle;
    
    private int r, g, b, a;
    
    @Override
    public String toString()
    {
        return "DrawTexture2D{" +
               "texture=" + this.texture + ' ' +
               "src=(" + this.srcX + ", " + this.srcY + ", " + this.srcW + ", " + this.srcH + ')' + ' ' +
               "dst=(" + this.dstX + ", " + this.dstY + ", " + this.dstW + ", " + this.dstH + ')' + ' ' +
               "rotationOrigin=(" + this.originX + ", " + this.originY + ')' + ' ' +
               "rotationAngle=" + this.angle + ' ' +
               "color=(" + this.r + ", " + this.g + ", " + this.b + ", " + this.a + ')' +
               '}';
    }
    
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
        drawTexture(this.texture,
                    this.srcX, this.srcY, this.srcW, this.srcH,
                    this.dstX, this.dstY, this.dstW, this.dstH,
                    this.originX, this.originY, this.angle,
                    this.r, this.g, this.b, this.a);
    }
    
    @Override
    public DrawTexture2D texture(@NotNull pe.texture.Texture texture)
    {
        this.texture = texture;
        this.srcW    = this.dstW = texture.width();
        this.srcH    = this.dstH = texture.height();
        return this;
    }
    
    @Override
    public DrawTexture2D src(double x, double y, double width, double height)
    {
        this.srcX = x;
        this.srcY = y;
        this.srcW = width;
        this.srcH = height;
        return this;
    }
    
    @Override
    public DrawTexture2D dst(double x, double y, double width, double height)
    {
        this.dstX   = x;
        this.dstY   = y;
        this.dstW   = width;
        this.dstH   = height;
        this.hasDst = true;
        return this;
    }
    
    @Override
    public DrawTexture2D rotationOrigin(double x, double y)
    {
        this.originX = x;
        this.originY = y;
        return this;
    }
    
    @Override
    public DrawTexture2D rotationAngle(double angleRadians)
    {
        this.angle = angleRadians;
        return this;
    }
    
    @Override
    public DrawTexture2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
}
