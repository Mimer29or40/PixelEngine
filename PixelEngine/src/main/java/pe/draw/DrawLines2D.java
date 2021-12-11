package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import rutils.Logger;

public class DrawLines2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private double[] points;
    
    private double  thickness;
    private boolean hasThickness;
    
    private int r0, g0, b0, a0;
    private int r1, g1, b1, a1;
    
    @Override
    public void reset()
    {
        this.points = null;
        
        this.hasThickness = false;
        
        this.r0 = this.r1 = 255;
        this.g0 = this.g1 = 255;
        this.b0 = this.b1 = 255;
        this.a0 = this.a1 = 255;
    }
    
    @Override
    protected void check()
    {
        if (this.points == null) throw new IllegalStateException("Must provide points");
        if (this.points.length >> 1 == 0) throw new IllegalStateException("Invalid points array");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    public void drawImpl()
    {
        DrawLines2D.LOGGER.finest("Drawing points=%s thickness=%s color0=(%s, %s, %s, %s) color1=(%s, %s, %s, %s)",
                                  this.points, this.thickness, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1);
        
        if (this.thickness <= 0) return;
        
        int pointsCount = this.points.length >> 1;
        
        if (pointsCount == 1)
        {
            drawPoint(this.points[0], this.points[1], this.thickness, this.r0, this.g0, this.b0, this.a0);
            return;
        }
        if (pointsCount == 2)
        {
            drawLine(this.points[0], this.points[1], this.points[2], this.points[3], this.thickness, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1);
            return;
        }
        
        double[] points = new double[this.points.length + 4];
        points[0] = this.points[0]; // First coordinate get repeated.
        points[1] = this.points[1]; // First coordinate get repeated.
        System.arraycopy(this.points, 0, points, 2, this.points.length);
        points[points.length - 2] = this.points[this.points.length - 2]; // Last coordinate get repeated.
        points[points.length - 1] = this.points[this.points.length - 1]; // Last coordinate get repeated.
        
        drawLines(points, this.thickness, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1);
    }
    
    public DrawLines2D points(double[] points)
    {
        this.points = points;
        return this;
    }
    
    public DrawLines2D points(@NotNull Vector2ic @NotNull [] vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2ic point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
    
    public DrawLines2D points(@NotNull Vector2fc @NotNull [] vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2fc point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
    
    public DrawLines2D points(@NotNull Vector2dc @NotNull [] vec)
    {
        double[] points = new double[vec.length << 1];
        
        int index = 0;
        for (Vector2dc point : vec)
        {
            points[index++] = point.x();
            points[index++] = point.y();
        }
        
        return points(points);
    }
    
    public DrawLines2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    public DrawLines2D color(int r, int g, int b, int a)
    {
        this.r0 = this.r1 = r;
        this.g0 = this.g1 = g;
        this.b0 = this.b1 = b;
        this.a0 = this.a1 = a;
        return this;
    }
    
    public DrawLines2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
    
    public DrawLines2D color0(int r, int g, int b, int a)
    {
        this.r0 = r;
        this.g0 = g;
        this.b0 = b;
        this.a0 = a;
        return this;
    }
    
    public DrawLines2D color0(@NotNull Colorc color)
    {
        return color0(color.r(), color.g(), color.b(), color.a());
    }
    
    public DrawLines2D color1(int r, int g, int b, int a)
    {
        this.r1 = r;
        this.g1 = g;
        this.b1 = b;
        this.a1 = a;
        return this;
    }
    
    public DrawLines2D color1(@NotNull Colorc color)
    {
        return color1(color.r(), color.g(), color.b(), color.a());
    }
}
