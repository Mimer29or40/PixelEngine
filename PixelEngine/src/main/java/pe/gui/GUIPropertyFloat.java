package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_float;
import static org.lwjgl.nuklear.Nuklear.nk_property_int;

public class GUIPropertyFloat extends GUIProperty
{
    protected final float[] val = new float[1];
    
    protected float min, max, step, inc;
    
    public GUIPropertyFloat(@NotNull Supplier<@NotNull String> name, float initial, float min, float max, float step, float inc)
    {
        super(name);
        
        this.val[0] = initial;
        
        this.min  = min;
        this.max  = max;
        this.step = step;
        this.inc  = inc;
    }
    
    public GUIPropertyFloat(@NotNull String name, float initial, float min, float max, float step, float inc)
    {
        super(name);
        
        this.val[0] = initial;
        
        this.min  = min;
        this.max  = max;
        this.step = step;
        this.inc  = inc;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        nk_property_float(ctx, '#' + this.name.get(), this.min, this.val, this.max, this.step, this.inc);
    }
}
