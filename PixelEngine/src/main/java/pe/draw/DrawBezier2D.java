package pe.draw;

import org.jetbrains.annotations.NotNull;
import rutils.Logger;
import rutils.Math;

public class DrawBezier2D extends Draw2D implements Points<DrawBezier2D>,
                                                    Thickness<DrawBezier2D>,
                                                    Color<DrawBezier2D>,
                                                    Color0<DrawBezier2D>,
                                                    Color1<DrawBezier2D>
{
    private static final Logger LOGGER = new Logger();
    
    private static final int LINE_DIVISIONS = 24; // Bezier line divisions
    
    private static long binomial(int n, int k)
    {
        if (k > n - k) k = n - k;
        long b = 1;
        for (int i = 1, m = n; i <= k; i++, m--) b = (b * m) / i;
        return b;
    }
    
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
        if (this.points.length >> 1 < 3) throw new IllegalStateException("Invalid points array");
        if (!this.hasThickness) throw new IllegalStateException("Must provide thickness");
    }
    
    @Override
    public void drawImpl()
    {
        DrawBezier2D.LOGGER.finest("Drawing points=%s point1=(%s, %s) thickness=%s color0=(%s, %s, %s, %s) color1=(%s, %s, %s, %s)",
                                   this.points, this.thickness, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1);
        
        if (this.thickness <= 0.0) return;
        
        int count = points.length >> 1;
        int order = count - 1;
        
        int      idx    = 0;
        double[] points = new double[(DrawBezier2D.LINE_DIVISIONS + 1 + 2) << 1];
        points[idx++] = this.points[0]; // First coordinate get repeated.
        points[idx++] = this.points[1]; // First coordinate get repeated.
        for (int i = 0; i <= DrawBezier2D.LINE_DIVISIONS; i++)
        {
            double t    = (double) i / (double) DrawBezier2D.LINE_DIVISIONS;
            double tInv = 1.0 - t;
            
            // sum i=0-n binome-coeff(n, i) * tInv^(n-i) * t^i * pi
            double x = 0;
            double y = 0;
            for (int j = 0; j <= order; j++)
            {
                double coeff = binomial(order, j) * Math.pow(tInv, order - j) * Math.pow(t, j);
                x += coeff * this.points[(j << 1)];
                y += coeff * this.points[(j << 1) + 1];
            }
            points[idx++] = x;
            points[idx++] = y;
        }
        points[points.length - 2] = this.points[this.points.length - 2]; // Last coordinate get repeated.
        points[points.length - 1] = this.points[this.points.length - 1]; // Last coordinate get repeated.
        
        drawLines(points, this.thickness, this.r0, this.g0, this.b0, this.a0, this.r1, this.g1, this.b1, this.a1);
    }
    
    @Override
    public DrawBezier2D points(double @NotNull ... points)
    {
        this.points = points;
        return this;
    }
    
    @Override
    public DrawBezier2D thickness(double thickness)
    {
        this.thickness    = thickness;
        this.hasThickness = true;
        return this;
    }
    
    @Override
    public DrawBezier2D color(int r, int g, int b, int a)
    {
        this.r0 = this.r1 = r;
        this.g0 = this.g1 = g;
        this.b0 = this.b1 = b;
        this.a0 = this.a1 = a;
        return this;
    }
    
    @Override
    public DrawBezier2D color0(int r, int g, int b, int a)
    {
        this.r0 = r;
        this.g0 = g;
        this.b0 = b;
        this.a0 = a;
        return this;
    }
    
    @Override
    public DrawBezier2D color1(int r, int g, int b, int a)
    {
        this.r1 = r;
        this.g1 = g;
        this.b1 = b;
        this.a1 = a;
        return this;
    }
}
