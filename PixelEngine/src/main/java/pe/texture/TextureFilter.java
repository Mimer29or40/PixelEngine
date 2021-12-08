package pe.texture;

import org.lwjgl.opengl.GL33;

public enum TextureFilter
{
    NEAREST(GL33.GL_NEAREST),
    LINEAR(GL33.GL_LINEAR),
    NEAREST_MIPMAP_NEAREST(GL33.GL_NEAREST_MIPMAP_NEAREST),
    LINEAR_MIPMAP_NEAREST(GL33.GL_LINEAR_MIPMAP_NEAREST),
    NEAREST_MIPMAP_LINEAR(GL33.GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_MIPMAP_LINEAR(GL33.GL_LINEAR_MIPMAP_LINEAR),
    ;
    
    public static final TextureFilter DEFAULT = NEAREST;
    
    public final int ref;
    
    TextureFilter(int ref)
    {
        this.ref = ref;
    }
}
