package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_double;

public class GUIPropertyDouble extends GUIProperty
{
    protected final double[] val = new double[1];
    
    protected double min, max, step, inc;
    
    public GUIPropertyDouble(@NotNull Supplier<@NotNull String> name, double initial, double min, double max, double step, double inc)
    {
        super(name);
        
        this.val[0] = initial;
        
        this.min  = min;
        this.max  = max;
        this.step = step;
        this.inc  = inc;
    }
    
    public GUIPropertyDouble(@NotNull String name, double initial, double min, double max, double step, double inc)
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
        nk_property_double(ctx, '#' + this.name.get(), this.min, this.val, this.max, this.step, (float) this.inc);
    }
}
