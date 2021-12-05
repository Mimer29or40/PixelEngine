package pe.render;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;

import java.nio.Buffer;

public class GLBufferArray extends GLBuffer
{
    public GLBufferArray(long size, Usage usage)
    {
        super(GL33.GL_ARRAY_BUFFER, size, usage);
    }
    
    public GLBufferArray(@NotNull Buffer data, Usage usage)
    {
        super(GL33.GL_ARRAY_BUFFER, data, usage);
    }
    
    public GLBufferArray(@NotNull CustomBuffer<?> data, Usage usage)
    {
        super(GL33.GL_ARRAY_BUFFER, data, usage);
    }
}
