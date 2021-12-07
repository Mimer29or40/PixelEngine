package pe.render;

import org.lwjgl.opengl.GL33;

public enum Usage
{
    STREAM_DRAW(GL33.GL_STREAM_DRAW),
    STREAM_READ(GL33.GL_STREAM_READ),
    STREAM_COPY(GL33.GL_STREAM_COPY),
    STATIC_DRAW(GL33.GL_STATIC_DRAW),
    STATIC_READ(GL33.GL_STATIC_READ),
    STATIC_COPY(GL33.GL_STATIC_COPY),
    DYNAMIC_DRAW(GL33.GL_DYNAMIC_DRAW),
    DYNAMIC_READ(GL33.GL_DYNAMIC_READ),
    DYNAMIC_COPY(GL33.GL_DYNAMIC_COPY),
    ;
    
    public final int ref;
    
    Usage(int ref)
    {
        this.ref = ref;
    }
}
