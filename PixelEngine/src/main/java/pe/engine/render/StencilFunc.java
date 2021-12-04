package pe.engine.render;

public enum StencilFunc
{
    NEVER((r, s) -> false),
    ALWAYS((r, s) -> true),
    
    EQUAL((r, s) -> r == s),
    NOT_EQUAL((r, s) -> r != s),
    
    LESS((r, s) -> r < s),
    L_EQUAL((r, s) -> r <= s),
    G_EQUAL((r, s) -> r >= s),
    GREATER((r, s) -> r > s),
    ;
    
    private final IStencilFunc func;
    
    StencilFunc(IStencilFunc func)
    {
        this.func = func;
    }
    
    public boolean test(int r, int s)
    {
        return this.func.test(r, s);
    }
    
    private interface IStencilFunc
    {
        boolean test(int r, int s);
    }
}
