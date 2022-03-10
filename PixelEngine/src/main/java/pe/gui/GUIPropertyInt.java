package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_int;

public class GUIPropertyInt extends GUIProperty
{
    protected final int[] val = new int[1];
    
    protected int min, max, step, inc;
    
    public GUIPropertyInt(@NotNull Supplier<@NotNull String> name, int initial, int min, int max, int step, int inc)
    {
        super(name);
        
        this.val[0] = initial;
        
        this.min  = min;
        this.max  = max;
        this.step = step;
        this.inc  = inc;
    }
    
    public GUIPropertyInt(@NotNull String name, int initial, int min, int max, int step, int inc)
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
        nk_property_int(ctx, '#' + this.name.get(), this.min, this.val, this.max, this.step, this.inc);
    }
}
