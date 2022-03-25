package pe;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import pe.draw.*;
import pe.render.GL;

public final class Draw
{
    private static final DrawPoint2D         DRAW_POINT_2D          = new DrawPoint2D();
    private static final DrawLine2D          DRAW_LINE_2D           = new DrawLine2D();
    private static final DrawLines2D         DRAW_LINES_2D          = new DrawLines2D();
    private static final DrawBezier2D        DRAW_BEZIER_2D         = new DrawBezier2D();
    private static final DrawTriangle2D      DRAW_TRIANGLE_2D       = new DrawTriangle2D();
    private static final FillTriangle2D      FILL_TRIANGLE_2D       = new FillTriangle2D();
    private static final DrawQuad2D          DRAW_QUAD_2D           = new DrawQuad2D();
    private static final FillQuad2D          FILL_QUAD_2D           = new FillQuad2D();
    private static final DrawRect2D          DRAW_RECT_2D           = new DrawRect2D();
    private static final FillRect2D          FILL_RECT_2D           = new FillRect2D();
    private static final DrawEllipse2D       DRAW_ELLIPSE_2D        = new DrawEllipse2D();
    private static final FillEllipse2D       FILL_ELLIPSE_2D        = new FillEllipse2D();
    private static final DrawRing2D          DRAW_RING_2D           = new DrawRing2D();
    private static final FillRing2D          FILL_RING_2D           = new FillRing2D();
    private static final DrawTexture2D       DRAW_TEXTURE_2D        = new DrawTexture2D();
    private static final DrawTextureWarped2D DRAW_TEXTURE_WARPED_2D = new DrawTextureWarped2D();
    private static final DrawText2D          DRAW_TEXT_2D           = new DrawText2D();
    
    public static void clearBackground(@NotNull Colorc color)
    {
        GL.clearColor(color.rf(), color.gf(), color.bf(), color.af());
        GL.clearScreenBuffers();
    }
    
    public static DrawPoint2D point2D()
    {
        return Draw.DRAW_POINT_2D;
    }
    
    public static DrawLine2D line2D()
    {
        return Draw.DRAW_LINE_2D;
    }
    
    public static DrawLines2D lines2D()
    {
        return Draw.DRAW_LINES_2D;
    }
    
    public static DrawBezier2D bezier2D()
    {
        return Draw.DRAW_BEZIER_2D;
    }
    
    public static DrawTriangle2D drawTriangle2D()
    {
        return Draw.DRAW_TRIANGLE_2D;
    }
    
    public static FillTriangle2D fillTriangle2D()
    {
        return Draw.FILL_TRIANGLE_2D;
    }
    
    public static DrawQuad2D drawQuad2D()
    {
        return Draw.DRAW_QUAD_2D;
    }
    
    public static FillQuad2D fillQuad2D()
    {
        return Draw.FILL_QUAD_2D;
    }
    
    public static DrawRect2D drawRect2D()
    {
        return Draw.DRAW_RECT_2D;
    }
    
    public static FillRect2D fillRect2D()
    {
        return Draw.FILL_RECT_2D;
    }
    
    public static DrawEllipse2D drawEllipse2D()
    {
        return Draw.DRAW_ELLIPSE_2D;
    }
    
    public static FillEllipse2D fillEllipse2D()
    {
        return Draw.FILL_ELLIPSE_2D;
    }
    
    public static DrawRing2D drawRing2D()
    {
        return Draw.DRAW_RING_2D;
    }
    
    public static FillRing2D fillRing2D()
    {
        return Draw.FILL_RING_2D;
    }
    
    public static DrawTexture2D drawTexture2D()
    {
        return Draw.DRAW_TEXTURE_2D;
    }
    
    public static DrawTextureWarped2D drawTextureWarped2D()
    {
        return Draw.DRAW_TEXTURE_WARPED_2D;
    }
    
    public static DrawText2D drawText2D()
    {
        return Draw.DRAW_TEXT_2D;
    }
}
