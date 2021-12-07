package pe.color;

import org.lwjgl.opengl.GL33;

public enum BlendFunc
{
    ZERO(GL33.GL_ZERO, (cs, as, cd, ad) -> 0),
    ONE(GL33.GL_ONE, (cs, as, cd, ad) -> 255),
    
    SRC_COLOR(GL33.GL_SRC_COLOR, (cs, as, cd, ad) -> cs),
    ONE_MINUS_SRC_COLOR(GL33.GL_ONE_MINUS_SRC_COLOR, (cs, as, cd, ad) -> 255 - cs),
    SRC_ALPHA(GL33.GL_SRC_ALPHA, (cs, as, cd, ad) -> as),
    ONE_MINUS_SRC_ALPHA(GL33.GL_ONE_MINUS_SRC_ALPHA, (cs, as, cd, ad) -> 255 - as),
    
    DST_COLOR(GL33.GL_DST_COLOR, (cs, as, cd, ad) -> cd),
    ONE_MINUS_DST_COLOR(GL33.GL_ONE_MINUS_DST_COLOR, (cs, as, cd, ad) -> 255 - cd),
    DST_ALPHA(GL33.GL_DST_ALPHA, (cs, as, cd, ad) -> ad),
    ONE_MINUS_DST_ALPHA(GL33.GL_ONE_MINUS_DST_ALPHA, (cs, as, cd, ad) -> 255 - ad),
    ;
    
    public final  int            ref;
    private final IBlendFunction func;
    
    BlendFunc(int ref, IBlendFunction func)
    {
        this.ref  = ref;
        this.func = func;
    }
    
    public int apply(int cs, int as, int cd, int ad)
    {
        return this.func.apply(cs, as, cd, ad);
    }
    
    private interface IBlendFunction
    {
        int apply(int cs, int as, int cd, int ad);
    }
}
