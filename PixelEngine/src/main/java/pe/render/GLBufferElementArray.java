package pe.render;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;

import java.nio.Buffer;

public class GLBufferElementArray extends GLBuffer
{
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected final GLType indexType;
    
    public GLBufferElementArray(int indexCount, @NotNull Usage usage, @NotNull GLType indexType)
    {
        super(GL33.GL_ELEMENT_ARRAY_BUFFER, Integer.toUnsignedLong(indexCount * indexType.bytes), usage);
        
        this.indexType = indexType;
    }
    
    public GLBufferElementArray(int indexCount, @NotNull Usage usage)
    {
        this(indexCount, usage, GLType.UNSIGNED_INT);
    }
    
    public GLBufferElementArray(@NotNull Buffer data, @NotNull Usage usage, @NotNull GLType indexType)
    {
        super(GL33.GL_ELEMENT_ARRAY_BUFFER, data, usage);
        
        this.indexType = indexType;
    }
    
    public GLBufferElementArray(@NotNull Buffer data, @NotNull Usage usage)
    {
        this(data, usage, GLType.UNSIGNED_INT);
    }
    
    public GLBufferElementArray(@NotNull CustomBuffer<?> data, @NotNull Usage usage, @NotNull GLType indexType)
    {
        super(GL33.GL_ELEMENT_ARRAY_BUFFER, data, usage);
        
        this.indexType = indexType;
    }
    
    public GLBufferElementArray(@NotNull CustomBuffer<?> data, @NotNull Usage usage)
    {
        this(data, usage, GLType.UNSIGNED_INT);
    }
    
    //-----------------------
    // ----- Properties -----
    //-----------------------
    
    /**
     * @return The data type of the index buffer.
     */
    public GLType indexType()
    {
        return this.indexType;
    }
}
