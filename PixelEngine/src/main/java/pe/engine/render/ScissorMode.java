package pe.engine.render;

public record ScissorMode(int left, int bottom, int width, int height)
{
    public static final ScissorMode NONE = new ScissorMode(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    public static final ScissorMode DEFAULT = NONE;
    
    public boolean test(int x, int y)
    {
        return this.left <= x && x < this.left + this.width &&
               this.bottom <= y && y < this.bottom + this.height;
    }
}
