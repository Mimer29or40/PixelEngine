package pe.render;

import org.lwjgl.opengl.GL33;

public enum CullFace
{
    NONE(-1),
    FRONT(GL33.GL_FRONT),
    BACK(GL33.GL_BACK),
    FRONT_AND_BACK(GL33.GL_FRONT_AND_BACK),
    ;
    
    public static final CullFace DEFAULT = BACK;
    
    public final int ref;
    
    CullFace(int ref)
    {
        this.ref = ref;
    }
}
