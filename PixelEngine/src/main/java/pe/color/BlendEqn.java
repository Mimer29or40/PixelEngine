package pe.color;

import rutils.Math;

public enum BlendEqn
{
    ADD(Math::sum),
    
    SUBTRACT(Math::sub),
    REVERSE_SUBTRACT(Math::revSub),
    
    MIN(Math::min),
    MAX(Math::max),
    ;
    
    private final IBlendEquation func;
    
    BlendEqn(IBlendEquation func)
    {
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
