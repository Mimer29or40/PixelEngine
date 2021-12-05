package pe.render;

import org.jetbrains.annotations.NotNull;

public record StencilMode(@NotNull StencilFunc func, int ref, int mask, @NotNull StencilOp sFail, @NotNull StencilOp dpFail, @NotNull StencilOp dpPass)
{
    public static final StencilMode NONE = new StencilMode(StencilFunc.ALWAYS, 1, 0xFF, StencilOp.KEEP, StencilOp.KEEP, StencilOp.KEEP);
    // sfail, dpfail, dppass
    
    public static final StencilMode DEFAULT = NONE;
    
    public boolean test(int stencil)
    {
        return this.func.test(this.ref & this.mask, stencil & this.mask);
    }
}
