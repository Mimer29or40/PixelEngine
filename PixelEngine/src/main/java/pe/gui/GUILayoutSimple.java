package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.nuklear.Nuklear.nk_layout_row;

/**
 * Current layout is divided into n same sized growing columns
 */
public class GUILayoutSimple extends GUILayout
{
    protected double     height;
    protected LayoutMode mode;
    protected float[]    columns;
    
    public GUILayoutSimple(double height, @NotNull LayoutMode mode, double... columns)
    {
        this.height  = height;
        this.mode    = mode;
        this.columns = new float[columns.length];
        for (int i = 0, n = columns.length; i < n; i++) this.columns[i] = (float) columns[i];
    }
    
    public double height()
    {
        return this.height;
    }
    
    @NotNull
    public GUILayoutSimple height(double height)
    {
        this.height = height;
        return this;
    }
    
    @NotNull
    public LayoutMode mode()
    {
        return this.mode;
    }
    
    @NotNull
    public GUILayoutSimple mode(@NotNull LayoutMode mode)
    {
        this.mode = mode;
        return this;
    }
    
    public int columnCount()
    {
        return this.columns.length;
    }
    
    public double[] columns()
    {
        double[] copy = new double[this.columns.length];
        for (int i = 0, n = copy.length; i < n; i++) copy[i] = this.columns[i];
        return copy;
    }
    
    @NotNull
    public GUILayoutSimple columns(double @NotNull ... columns)
    {
        this.columns = new float[columns.length];
        for (int i = 0, n = columns.length; i < n; i++) this.columns[i] = (float) columns[i];
        return this;
    }
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            nk_layout_row(ctx, this.mode.value, (float) this.height, stack.floats(this.columns));
            for (GUIElement child : this.children) child.layout(ctx);
        }
    }
}
