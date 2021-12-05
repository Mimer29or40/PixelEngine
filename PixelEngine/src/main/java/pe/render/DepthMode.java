package pe.render;

public enum DepthMode
{
    NONE((s, d) -> false),
    
    NEVER((s, d) -> false),
    ALWAYS((s, d) -> true),
    
    EQUAL((s, d) -> Double.compare(s, d) == 0),
    NOT_EQUAL((s, d) -> Double.compare(s, d) != 0),
    
    LESS((s, d) -> Double.compare(s, d) < 0),
    L_EQUAL((s, d) -> Double.compare(s, d) <= 0),
    G_EQUAL((s, d) -> Double.compare(s, d) >= 0),
    GREATER((s, d) -> Double.compare(s, d) > 0),
    ;
    
    public static final DepthMode DEFAULT = LESS;
    
    private final IDepthFunc func;
    
    DepthMode(IDepthFunc func)
    {
        this.func = func;
    }
    
    public boolean test(double s, double d)
    {
        return this.func.test(s, d);
    }
    
    private interface IDepthFunc
    {
        boolean test(double s, double d);
    }
}
