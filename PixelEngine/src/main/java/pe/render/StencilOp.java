package pe.render;

import org.lwjgl.opengl.GL33;

public enum StencilOp
{
    ZERO(GL33.GL_ZERO, (s, ref) -> 0),
    
    KEEP(GL33.GL_KEEP, (s, ref) -> s),
    REPLACE(GL33.GL_REPLACE, (s, ref) -> ref),
    
    INCR(GL33.GL_INCR, (s, ref) -> s == 0xFF ? s : s++),
    DECR(GL33.GL_DECR, (s, ref) -> s == 0x00 ? s : s--),
    INCR_WRAP(GL33.GL_INCR_WRAP, (s, ref) -> s == 0xFF ? 0x00 : s++),
    DECR_WRAP(GL33.GL_DECR_WRAP, (s, ref) -> s == 0x00 ? 0xFF : s--),
    
    INVERT(GL33.GL_INVERT, (s, ref) -> ~s),
    ;
    
    public final  int        ref;
    private final IStencilOp func;
    
    StencilOp(int ref, IStencilOp func)
    {
        this.ref  = ref;
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
