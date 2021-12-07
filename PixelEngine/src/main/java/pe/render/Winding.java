package pe.render;

import org.lwjgl.opengl.GL33;

public enum Winding
{
    CCW(GL33.GL_CCW),
    CW(GL33.GL_CW),
    ;
    
    public static final Winding DEFAULT = CCW;
    
    public final int ref;
    
    Winding(int ref)
    {
        this.ref = ref;
    }
}
