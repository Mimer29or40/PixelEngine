package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
import pe.util.NotNullProperty;
import pe.util.Property;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUILabel extends GUIElement
{
    public final Property<String>    text;
    public final Property<Alignment> alignment;
    public final Property<Boolean>   wrap;
    
    protected Color color = null;
    
    public GUILabel(@NotNull Supplier<@NotNull String> text)
    {
        this.text      = new NotNullProperty<>("text", text);
        this.alignment = new NotNullProperty<>("alignment", Alignment.LEFT);
        this.wrap      = new NotNullProperty<>("wrap", false);
    }
    
    public GUILabel(@NotNull String text)
    {
        this(() -> text);
    }
    
    @Nullable
    public Colorc color()
    {
        return this.color;
    }
    
    @NotNull
    public GUILabel color(@Nullable Colorc color)
    {
        if (color != null)
        {
            if (this.color == null) this.color = Color_RGBA.malloc();
            this.color.set(color);
        }
        else
        {
            this.color.free();
            this.color = null;
        }
        return this;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        if (this.color != null)
        {
            NkColor color = NkColor.create(this.color.address());
            
            if (this.wrap.get())
            {
                nk_label_colored_wrap(ctx, this.text.get(), color);
            }
            else
            {
                nk_label_colored(ctx, this.text.get(), this.alignment.get().value, color);
            }
        }
        else
        {
            if (this.wrap.get())
            {
                nk_label_wrap(ctx, this.text.get());
            }
            else
            {
                nk_label(ctx, this.text.get(), this.alignment.get().value);
            }
        }
    }
}
