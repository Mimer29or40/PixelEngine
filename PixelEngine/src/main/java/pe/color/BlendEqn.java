package pe.color;

import org.lwjgl.opengl.GL33;
import rutils.Math;

public enum BlendEqn
{
    ADD(GL33.GL_FUNC_ADD, Math::sum),
    
    SUBTRACT(GL33.GL_FUNC_SUBTRACT, Math::sub),
    REVERSE_SUBTRACT(GL33.GL_FUNC_REVERSE_SUBTRACT, Math::revSub),
    
    MIN(GL33.GL_MIN, Math::min),
    MAX(GL33.GL_MAX, Math::max),
    ;
    
    public final  int            ref;
    private final IBlendEquation func;
    
    BlendEqn(int ref, IBlendEquation func)
    {
        this.ref  = ref;
        this.func = func;
    }
    
    public int apply(int s, int d)
    {
        return this.func.apply(s, d);
    }
    
    private interface IBlendEquation
    {
        int apply(int s, int d);
    }
}
