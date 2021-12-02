package pe.color;

public enum BlendFunc
{
    ZERO((cs, as, cd, ad) -> 0),
    ONE((cs, as, cd, ad) -> 255),
    
    SRC_COLOR((cs, as, cd, ad) -> cs),
    ONE_MINUS_SRC_COLOR((cs, as, cd, ad) -> 255 - cs),
    SRC_ALPHA((cs, as, cd, ad) -> as),
    ONE_MINUS_SRC_ALPHA((cs, as, cd, ad) -> 255 - as),
    
    DST_COLOR((cs, as, cd, ad) -> cd),
    ONE_MINUS_DST_COLOR((cs, as, cd, ad) -> 255 - cd),
    DST_ALPHA((cs, as, cd, ad) -> ad),
    ONE_MINUS_DST_ALPHA((cs, as, cd, ad) -> 255 - ad),
    ;
    
    private final IBlendFunction func;
    
    BlendFunc(IBlendFunction func)
    {
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
