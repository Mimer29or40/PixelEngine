package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;
import pe.util.Property;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_double;

public class GUIPropertyDouble extends GUIProperty
{
    protected final double[]         internal;
    protected final Supplier<Double> supplier;
    
    public final Property<Double> value, min, max, step, inc;
    
    public GUIPropertyDouble(@NotNull Supplier<@NotNull String> name, double initial, double min, double max, double step, double inc)
    {
        super(name);
        
        this.internal = new double[] {initial};
        this.supplier = () -> this.internal[0];
        
        this.value = new Property<>("value", this.supplier);
        this.min   = new Property<>("min", min);
        this.max   = new Property<>("max", max);
        this.step  = new Property<>("step", step);
        this.inc   = new Property<>("inc", inc);
    }
    
    public GUIPropertyDouble(@NotNull String name, double initial, double min, double max, double step, double inc)
    {
        this(() -> name, initial, min, max, step, inc);
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        this.internal[0] = this.value.get();
        nk_property_double(ctx, '#' + this.name.get(), this.min.get(), this.internal, this.max.get(), this.step.get(), (float) (double) this.inc.get());
        this.value.set(this.supplier);
    }
}
