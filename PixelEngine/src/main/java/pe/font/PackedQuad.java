package pe.font;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.stb.STBTTAlignedQuad;

public final class PackedQuad
{
    public final double x0, y0, x1, y1, u0, v0, u1, v1;
    
    PackedQuad(@NotNull STBTTAlignedQuad q, double ascent)
    {
        this.x0 = q.x0();
        this.y0 = q.y0() + ascent;
        this.x1 = q.x1();
        this.y1 = q.y1() + ascent;
        this.u0 = q.s0();
        this.v0 = q.t0();
        this.u1 = q.s1();
        this.v1 = q.t1();
    }
}
