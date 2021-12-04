package pe.engine.render;

public enum GLType
{
    BYTE(true, Byte.BYTES),
    UNSIGNED_BYTE(false, Byte.BYTES),
    
    SHORT(true, Short.BYTES),
    UNSIGNED_SHORT(false, Short.BYTES),
    
    INT(true, Integer.BYTES),
    UNSIGNED_INT(false, Integer.BYTES),
    
    // UNSIGNED_INT_2_10_10_10_REV(false, 4),
    // INT_2_10_10_10_REV(true, 4),
    
    FLOAT(true, Float.BYTES),
    DOUBLE(true, Double.BYTES),
    ;
    
    private final boolean signed;
    private final int     bytes;
    
    GLType(boolean signed, int bytes)
    {
        this.signed = signed;
        this.bytes  = bytes;
    }
    
    public boolean signed()
    {
        return signed;
    }
    
    public int bytes()
    {
        return bytes;
    }
}
