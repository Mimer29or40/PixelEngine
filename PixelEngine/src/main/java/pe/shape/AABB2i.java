package pe.shape;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

public class AABB2i implements AABB2ic
{
    public Vector2i pos;
    public Vector2i size;
    
    protected final Vector2i min = new Vector2i();
    protected final Vector2i max = new Vector2i();
    
    public AABB2i()
    {
        this.pos  = new Vector2i();
        this.size = new Vector2i();
    }
    
    public AABB2i(int x, int y, int width, int height)
    {
        this.pos  = new Vector2i(x, y);
        this.size = new Vector2i(width, height);
    }
    
    public AABB2i(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        this.pos  = new Vector2i(pos);
        this.size = new Vector2i(size);
    }
    
    public AABB2i(@NotNull Vector2i pos, @NotNull Vector2i size)
    {
        this.pos  = pos;
        this.size = size;
    }
    
    public AABB2i(@NotNull AABB2ic other)
    {
        this.pos  = new Vector2i(other.pos());
        this.size = new Vector2i(other.size());
    }
    
    @Override
    public @NotNull Vector2ic pos()
    {
        return this.pos;
    }
    
    @Override
    public @NotNull Vector2ic size()
    {
        return this.size;
    }
    
    @Override
    public @NotNull Vector2ic min()
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
    public @NotNull Vector2ic max()
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
    public @NotNull AABB2ic aabb()
    {
        return this;
    }
    
    public AABB2i set(int x, int y, int width, int height)
    {
        this.pos  = new Vector2i(x, y);
        this.size = new Vector2i(width, height);
        return this;
    }
    
    public AABB2i set(@NotNull Vector2ic pos, @NotNull Vector2ic size)
    {
        this.pos  = new Vector2i(pos);
        this.size = new Vector2i(size);
        return this;
    }
    
    public AABB2i set(@NotNull Vector2i pos, @NotNull Vector2i size)
    {
        this.pos  = pos;
        this.size = size;
        return this;
    }
    
    public AABB2i set(@NotNull AABB2ic other)
    {
        this.pos  = new Vector2i(other.pos());
        this.size = new Vector2i(other.size());
        return this;
    }
    
    @Override
    public boolean intersects(int x, int y)
    {
        return minX() <= x && x <= maxX() && minY() <= y && y <= maxY();
    }
    
    @Override
    public boolean intersects(@NotNull AABB2ic aabb)
    {
        return !(maxX() < aabb.minX() ||
                 minX() > aabb.maxX() ||
                 maxY() < aabb.minY() ||
                 minY() > aabb.maxY());
    }
    
    @Override
    public boolean contains(int x, int y)
    {
        return minX() <= x && x <= maxX() && minY() <= y && y <= maxY();
    }
    
    @Override
    public boolean contains(@NotNull AABB2ic aabb)
    {
        return minX() <= aabb.minX() &&
               aabb.maxX() <= maxX() &&
               minY() <= aabb.minY() &&
               aabb.maxY() <= maxY();
    }
}
