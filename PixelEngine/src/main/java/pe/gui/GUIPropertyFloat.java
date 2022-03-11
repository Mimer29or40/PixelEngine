package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;
import pe.util.Property;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.nk_property_float;

public class GUIPropertyFloat extends GUIProperty
{
    protected final float[]         internal;
    protected final Supplier<Float> supplier;
    
    public final Property<Float> value, min, max, step, inc;
    
    public GUIPropertyFloat(@NotNull Supplier<@NotNull String> name, float initial, float min, float max, float step, float inc)
    {
        super(name);
        
        this.internal = new float[] {initial};
        this.supplier = () -> this.internal[0];
        
        this.value = new Property<>("value", this.supplier);
        this.min   = new Property<>("min", min);
        this.max   = new Property<>("max", max);
        this.step  = new Property<>("step", step);
        this.inc   = new Property<>("inc", inc);
    }
    
    public GUIPropertyFloat(@NotNull String name, float initial, float min, float max, float step, float inc)
    {
        this(() -> name, initial, min, max, step, inc);
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        this.internal[0] = this.value.get();
        nk_property_float(ctx, '#' + this.name.get(), this.min.get(), this.internal, this.max.get(), this.step.get(), this.inc.get());
        this.value.set(this.supplier);
    }
}
