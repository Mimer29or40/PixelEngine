package pe.shape;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class AABBi implements AABBic
{
    public final Vector2i pos;
    public final Vector2i size;
    
    protected final Vector2i min = new Vector2i();
    protected final Vector2i max = new Vector2i();
    
    public AABBi()
    {
        this.pos  = new Vector2i();
        this.size = new Vector2i();
    }
    
    public AABBi(int x, int y, int width, int height)
    {
        this.pos  = new Vector2i(x, y);
        this.size = new Vector2i(width, height);
    }
    
    public AABBi(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        this.pos = new Vector2i(pos);
        this.size = new Vector2i(size);
    }
    
    public AABBi(@NotNull Vector2i pos, @NotNull Vector2i size)
    {
        this.pos = pos;
        this.size = size;
    }
    
    public AABBi(@NotNull AABBic other)
    {
        this.pos = new Vector2i(other.pos());
        this.size = new Vector2i(other.size());
    }
    
    @Override
    public Vector2ic pos()
    {
        return this.pos;
    }
    
    @Override
    public int x()
    {
        return this.pos.x;
    }
    
    @Override
    public int y()
    {
        return this.pos.y;
    }
    
    @Override
    public Vector2ic size()
    {
        return this.size;
    }
    
    @Override
    public int width()
    {
        return this.size.x;
    }
    
    @Override
    public int height()
    {
        return this.size.y;
    }
    
    @Override
    public Vector2ic min()
    {
        return this.min.set(minX(), minY());
    }
    
    @Override
    public int minX()
    {
        return Math.min(x(), x() + width() - 1);
    }
    
    @Override
    public int minY()
    {
        return Math.min(y(), y() + height() - 1);
    }
    
    @Override
    public Vector2ic max()
    {
        return this.max.set(maxX(), maxY());
    }
    
    @Override
    public int maxX()
    {
        return Math.max(x(), x() + width() - 1);
    }
    
    @Override
    public int maxY()
    {
        return Math.max(y(), y() + height() - 1);
    }
    
    @Override
    public AABBic aabb()
    {
        return this;
    }
}
