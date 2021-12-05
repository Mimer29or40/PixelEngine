package pe.render;

public enum CullFace
{
    NONE, FRONT, BACK, FRONT_AND_BACK,
    ;
    
    public static final CullFace DEFAULT = BACK;
}
