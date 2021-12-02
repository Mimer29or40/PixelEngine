package pe.engine.color;

import org.jetbrains.annotations.NotNull;

public record BlendMode(@NotNull BlendEqn blendEqn, @NotNull BlendFunc srcFunc, @NotNull BlendFunc dstFunc)
{
    public static final BlendMode NONE           = new BlendMode(BlendEqn.ADD, BlendFunc.ZERO, BlendFunc.ONE);
    public static final BlendMode ALPHA          = new BlendMode(BlendEqn.ADD, BlendFunc.SRC_ALPHA, BlendFunc.ONE_MINUS_SRC_ALPHA);
    public static final BlendMode ADDITIVE       = new BlendMode(BlendEqn.ADD, BlendFunc.SRC_ALPHA, BlendFunc.ONE);
    public static final BlendMode MULTIPLICATIVE = new BlendMode(BlendEqn.ADD, BlendFunc.DST_COLOR, BlendFunc.ONE_MINUS_SRC_ALPHA);
    public static final BlendMode STENCIL        = new BlendMode(BlendEqn.ADD, BlendFunc.ZERO, BlendFunc.SRC_ALPHA);
    public static final BlendMode ADD_COLORS     = new BlendMode(BlendEqn.ADD, BlendFunc.ONE, BlendFunc.ONE);
    public static final BlendMode SUB_COLORS     = new BlendMode(BlendEqn.SUBTRACT, BlendFunc.ONE, BlendFunc.ONE);
    public static final BlendMode ILLUMINATE     = new BlendMode(BlendEqn.ADD, BlendFunc.ONE_MINUS_SRC_ALPHA, BlendFunc.SRC_ALPHA);
    
    public static final BlendMode DEFAULT = ALPHA;
    
    public @NotNull Color blend(Colorc src, Colorc dst, Color out)
    {
        int rSrc = src.r(), gSrc = src.g(), bSrc = src.b(), aSrc = src.a();
        int rDst = dst.r(), gDst = dst.g(), bDst = dst.b(), aDst = dst.a();
        
        int rsf = this.srcFunc.apply(rSrc, aSrc, rDst, aDst);
        int gsf = this.srcFunc.apply(gSrc, aSrc, gDst, aDst);
        int bsf = this.srcFunc.apply(bSrc, aSrc, bDst, aDst);
        int asf = this.srcFunc.apply(aSrc, aSrc, aDst, aDst);
        
        int rdf = this.dstFunc.apply(rSrc, aSrc, rDst, aDst);
        int gdf = this.dstFunc.apply(gSrc, aSrc, gDst, aDst);
        int bdf = this.dstFunc.apply(bSrc, aSrc, bDst, aDst);
        int adf = this.dstFunc.apply(aSrc, aSrc, aDst, aDst);
        
        return out.set(this.blendEqn.apply(rsf * rSrc, rdf * rDst) / 255,
                       this.blendEqn.apply(gsf * gSrc, gdf * gDst) / 255,
                       this.blendEqn.apply(bsf * bSrc, bdf * bDst) / 255,
                       this.blendEqn.apply(asf * aSrc, adf * aDst) / 255);
    }
}
