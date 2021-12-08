package pe.color;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;

public enum ColorFormat
{
    GRAY(Color_GRAY.SIZEOF, false, GL33.GL_RED, GL33.GL_R8),
    GRAY_ALPHA(Color_GRAY_ALPHA.SIZEOF, true, GL33.GL_RG, GL33.GL_RG8),
    RGB(Color_RGB.SIZEOF, false, GL33.GL_RGB, GL33.GL_RGB8),
    RGBA(Color_RGBA.SIZEOF, true, GL33.GL_RGBA, GL33.GL_RGBA8),
    
    UNKNOWN(0, false, -1, -1),
    ;
    
    public static final ColorFormat DEFAULT = RGBA;
    
    public static @NotNull ColorFormat get(int channels)
    {
        return switch (channels)
                {
                    case 1 -> ColorFormat.GRAY;
                    case 2 -> ColorFormat.GRAY_ALPHA;
                    case 3 -> ColorFormat.RGB;
                    default -> ColorFormat.RGBA;
                };
    }
    
    public final int     sizeof;
    public final boolean alpha;
    public final int     format;
    public final int     internalFormat;
    
    ColorFormat(int sizeof, boolean alpha, int format, int internalFormat)
    {
        this.sizeof         = sizeof;
        this.alpha          = alpha;
        this.format         = format;
        this.internalFormat = internalFormat;
    }
}
