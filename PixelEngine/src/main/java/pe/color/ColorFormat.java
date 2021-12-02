package pe.color;

public enum ColorFormat
{
    GRAY(Color_GRAY.SIZEOF),
    GRAY_ALPHA(Color_GRAY_ALPHA.SIZEOF),
    RGB(Color_RGB.SIZEOF),
    RGBA(Color_RGBA.SIZEOF),
    
    UNKNOWN(0),
    ;
    
    public static final ColorFormat DEFAULT = RGBA;
    
    private final int sizeOf;
    
    ColorFormat(int sizeOf)
    {
        this.sizeOf = sizeOf;
    }
    
    public int sizeOf()
    {
        return sizeOf;
    }
}
