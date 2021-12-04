package pe.engine.render;

public enum StencilOp
{
    ZERO((s, ref) -> 0),
    
    KEEP((s, ref) -> s),
    REPLACE((s, ref) -> ref),
    
    INCR((s, ref) -> s == 0xFF ? s : s++),
    DECR((s, ref) -> s == 0x00 ? s : s--),
    INCR_WRAP((s, ref) -> s == 0xFF ? 0x00 : s++),
    DECR_WRAP((s, ref) -> s == 0x00 ? 0xFF : s--),
    
    INVERT((s, ref) -> ~s),
    ;
    
    private final IStencilOp func;
    
    StencilOp(IStencilOp func)
    {
        this.func = func;
    }
    
    public int test(int s, int ref)
    {
        return this.func.test(s, ref);
    }
    
    private interface IStencilOp
    {
        int test(int s, int ref);
    }
}
