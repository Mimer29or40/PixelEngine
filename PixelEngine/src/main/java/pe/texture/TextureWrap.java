package pe.texture;

import org.lwjgl.opengl.GL33;

public enum TextureWrap
{
    CLAMP_TO_EDGE(GL33.GL_CLAMP_TO_EDGE),
    CLAMP_TO_BORDER(GL33.GL_CLAMP_TO_BORDER),
    MIRRORED_REPEAT(GL33.GL_MIRRORED_REPEAT),
    REPEAT(GL33.GL_REPEAT),
    // MIRROR_CLAMP_TO_EDGE(GL46.GL_MIRROR_CLAMP_TO_EDGE),
    ;
    
    public static final TextureWrap DEFAULT = REPEAT;
    
    public final int ref;
    
    TextureWrap(int ref)
    {
        this.ref = ref;
    }
}
