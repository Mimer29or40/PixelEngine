package pe.engine.render;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;

import java.nio.IntBuffer;

public class GLBufferElementArray extends GLBuffer
{
    public GLBufferElementArray(int indexCount, Usage usage)
    {
        super(GL33.GL_ELEMENT_ARRAY_BUFFER, Integer.toUnsignedLong(indexCount * Integer.BYTES), usage);
    }
    
    public GLBufferElementArray(@NotNull IntBuffer data, Usage usage)
    {
        super(GL33.GL_ELEMENT_ARRAY_BUFFER, data, usage);
    }
}
