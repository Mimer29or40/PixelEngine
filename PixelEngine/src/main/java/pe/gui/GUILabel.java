package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;

import java.util.function.Supplier;

import static org.lwjgl.nuklear.Nuklear.*;

public class GUILabel extends GUIElement
{
    protected Supplier<String> name;
    
    protected Color color = null;
    
    protected Alignment alignment = Alignment.LEFT;
    protected boolean   wrap      = false;
    
    public GUILabel(@NotNull Supplier<@NotNull String> name)
    {
        this.name = name;
    }
    
    public GUILabel(@NotNull String name)
    {
        this(() -> name);
    }
    
    @NotNull
    public String name()
    {
        return this.name.get();
    }
    
    @NotNull
    public GUILabel name(@NotNull Supplier<@NotNull String> name)
    {
        this.name = name;
        return this;
    }
    
    @NotNull
    public GUILabel name(@NotNull String name)
    {
        this.name = () -> name;
        return this;
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
    
    @NotNull
    public Alignment alignment()
    {
        return this.alignment;
    }
    
    @NotNull
    public GUILabel alignment(@NotNull Alignment alignment)
    {
        this.alignment = alignment;
        return this;
    }
    
    public boolean wrap()
    {
        return this.wrap;
    }
    
    @NotNull
    public GUILabel wrap(boolean wrap)
    {
        this.wrap = wrap;
        return this;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        if (this.color != null)
        {
            NkColor color = NkColor.create(this.color.address());
            
            if (this.wrap)
            {
                nk_label_colored_wrap(ctx, this.name.get(), color);
            }
            else
            {
                nk_label_colored(ctx, this.name.get(), this.alignment.value, color);
            }
        }
        else
        {
            if (this.wrap)
            {
                nk_label_wrap(ctx, this.name.get());
            }
            else
            {
                nk_label(ctx, this.name.get(), this.alignment.value);
            }
        }
    }
}
