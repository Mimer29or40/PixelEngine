package pe.render;

import org.lwjgl.opengl.GL33;

public enum ScreenBuffer
{
    COLOR(GL33.GL_COLOR_BUFFER_BIT),
    DEPTH(GL33.GL_DEPTH_BUFFER_BIT),
    STENCIL(GL33.GL_STENCIL_BUFFER_BIT),
    ;
    
    public final int ref;
    
    ScreenBuffer(int ref)
    {
        this.ref = ref;
    }
}
