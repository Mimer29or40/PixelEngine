package pe.color;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;

public enum ColorFormat
{
    GRAY(GL33.GL_RED, Color_GRAY.SIZEOF, false),
    GRAY_ALPHA(GL33.GL_RG, Color_GRAY_ALPHA.SIZEOF, true),
    RGB(GL33.GL_RGB, Color_RGB.SIZEOF, false),
    RGBA(GL33.GL_RGBA, Color_RGBA.SIZEOF, true),
    
    UNKNOWN(-1, 0, false),
    ;
    
    public static final ColorFormat DEFAULT = RGBA;
    
    public static @NotNull ColorFormat get(int channels)
    {
        return switch (channels)
                {
                    case 0 -> ColorFormat.GRAY;
                    case 1 -> ColorFormat.GRAY_ALPHA;
                    case 2 -> ColorFormat.RGB;
                    default -> ColorFormat.RGBA;
                };
    }
    
    public final int     ref;
    public final int     sizeof;
    public final boolean alpha;
    
    ColorFormat(int ref, int sizeof, boolean alpha)
    {
        this.ref    = ref;
        this.sizeof = sizeof;
        this.alpha  = alpha;
    }
}
