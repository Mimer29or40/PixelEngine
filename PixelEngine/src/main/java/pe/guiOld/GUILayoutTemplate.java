package pe.guiOld;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;
import rutils.group.Pair;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.nuklear.Nuklear.*;

/**
 * Current layout is divided into n same sized growing columns
 */
public class GUILayoutTemplate extends GUILayout
{
    protected double height;
    
    protected final List<Pair<LayoutMode, Float>> columns = new ArrayList<>();
    
    public GUILayoutTemplate(double height)
    {
        this.height = height;
    }
    
    public double height()
    {
        return this.height;
    }
    
    @NotNull
    public GUILayoutTemplate height(double height)
    {
        this.height = height;
        return this;
    }
    
    @NotNull
    public GUILayoutTemplate clearColumns()
    {
        this.columns.clear();
        return this;
    }
    
    @NotNull
    public GUILayoutTemplate addColumn(@NotNull LayoutMode mode, double value)
    {
        this.columns.add(new Pair<>(mode, (float) value));
        return this;
    }
    
    public int columnCount()
    {
        return this.columns.size();
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        nk_layout_row_template_begin(ctx, (float) this.height);
        for (Pair<LayoutMode, Float> column : this.columns)
        {
            switch (column.a)
            {
                case DYNAMIC -> nk_layout_row_template_push_dynamic(ctx);
                case STATIC -> nk_layout_row_template_push_variable(ctx, column.b);
                case VARIABLE -> nk_layout_row_template_push_static(ctx, column.b);
            }
        }
        nk_layout_row_template_end(ctx);
        
        for (GUIElement child : this.children) child.layout(ctx);
    }
}
