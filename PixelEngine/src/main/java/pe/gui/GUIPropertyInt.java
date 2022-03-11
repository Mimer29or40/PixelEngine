package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;
import pe.util.Property;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_int;

public class GUIPropertyInt extends GUIProperty
{
    protected final int[]             internal;
    protected final Supplier<Integer> supplier;
    
    public final Property<Integer> value, min, max, step, inc;
    
    public GUIPropertyInt(@NotNull Supplier<@NotNull String> name, int initial, int min, int max, int step, int inc)
    {
        super(name);
        
        this.internal = new int[] {initial};
        this.supplier = () -> this.internal[0];
        
        this.value = new Property<>("value", this.supplier);
        this.min   = new Property<>("min", min);
        this.max   = new Property<>("max", max);
        this.step  = new Property<>("step", step);
        this.inc   = new Property<>("inc", inc);
    }
    
    public GUIPropertyInt(@NotNull String name, int initial, int min, int max, int step, int inc)
    {
        this(() -> name, initial, min, max, step, inc);
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        this.internal[0] = this.value.get();
        nk_property_int(ctx, '#' + this.name.get(), this.min.get(), this.internal, this.max.get(), this.step.get(), this.inc.get());
        this.value.set(this.supplier);
    }
}
