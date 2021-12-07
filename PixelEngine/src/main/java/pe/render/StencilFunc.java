package pe.render;

import org.lwjgl.opengl.GL33;

public enum StencilFunc
{
    NEVER(GL33.GL_NEVER, (r, s) -> false),
    ALWAYS(GL33.GL_ALWAYS, (r, s) -> true),
    
    EQUAL(GL33.GL_EQUAL, (r, s) -> r == s),
    NOT_EQUAL(GL33.GL_NOTEQUAL, (r, s) -> r != s),
    
    LESS(GL33.GL_LESS, (r, s) -> r < s),
    L_EQUAL(GL33.GL_LEQUAL, (r, s) -> r <= s),
    G_EQUAL(GL33.GL_GEQUAL, (r, s) -> r >= s),
    GREATER(GL33.GL_GREATER, (r, s) -> r > s),
    ;
    
    public final  int          ref;
    private final IStencilFunc func;
    
    StencilFunc(int ref, IStencilFunc func)
    {
        this.ref  = ref;
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
