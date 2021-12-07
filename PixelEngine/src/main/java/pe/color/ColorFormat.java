package pe.color;

public enum ColorFormat
{
    GRAY(Color_GRAY.SIZEOF, false),
    GRAY_ALPHA(Color_GRAY_ALPHA.SIZEOF, true),
    RGB(Color_RGB.SIZEOF, false),
    RGBA(Color_RGBA.SIZEOF, true),
    
    UNKNOWN(0, false),
    ;
    
    public static final ColorFormat DEFAULT = RGBA;
    
    private final int     sizeof;
    private final boolean alpha;
    
    ColorFormat(int sizeof, boolean alpha)
    {
        this.sizeof = sizeof;
        this.alpha  = alpha;
    }
    
    public int sizeof()
    {
        return sizeof;
    }
    
    public boolean alpha()
    {
        return this.alpha;
    }
}
