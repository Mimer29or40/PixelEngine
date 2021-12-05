package pe.render;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;
import rutils.Logger;

import java.nio.Buffer;

public class GLBufferUniform extends GLBuffer
{
    private static final Logger LOGGER = new Logger();
    
    public GLBufferUniform(long size, Usage usage)
    {
        super(GL33.GL_UNIFORM_BUFFER, size, usage);
    }
    
    public GLBufferUniform(@NotNull Buffer data, Usage usage)
    {
        super(GL33.GL_UNIFORM_BUFFER, data, usage);
    }
    
    public GLBufferUniform(@NotNull CustomBuffer<?> data, Usage usage)
    {
        super(GL33.GL_UNIFORM_BUFFER, data, usage);
    }
    
    /**
     * Binds the buffer for reading/writing.
     * <p>
     * Make sure to bind the buffer.
     *
     * @return This instance for call chaining.
     */
    public GLBuffer base(int index)
    {
        GLBufferUniform.LOGGER.finest("%s: Binding to Base: %s", this, index);
        
        GLBuffer.bind(this);
        
        GL33.glBindBufferBase(this.type, index, this.id);
        
        return this;
    }
}
