package pe.engine.render;

import org.jetbrains.annotations.NotNull;

public record GLAttribute(@NotNull GLType type, int count, boolean normalized)
{
    public GLAttribute(@NotNull GLType type, int count)
    {
        this(type, count, false);
    }
    
    public GLAttribute(@NotNull GLType type, boolean normalized)
    {
        this(type, 1, normalized);
    }
    
    public GLAttribute(@NotNull GLType type)
    {
        this(type, 1, false);
    }
    
    @Override
    public String toString()
    {
        return this.type + "x" + this.count;
    }
    
    public int size()
    {
        return this.type.bytes() * this.count;
    }
}
