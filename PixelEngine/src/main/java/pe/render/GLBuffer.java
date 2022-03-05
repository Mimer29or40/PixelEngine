package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import rutils.Logger;
import rutils.MemUtil;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Objects;

public abstract class GLBuffer
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    protected static void setup()
    {
        GLBuffer.LOGGER.fine("Setup");
    }
    
    protected static void destroy()
    {
        GLBuffer.LOGGER.fine("Destroy");
        
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, 0);
    }
    
    public static void bind(@Nullable GLBuffer buffer)
    {
        if (buffer instanceof GLBufferArray buf) {bind(buf);}
        else if (buffer instanceof GLBufferElementArray buf) {bind(buf);}
        else if (buffer instanceof GLBufferUniform buf) bind(buf);
    }
    
    public static void bind(@Nullable GLBufferArray buffer)
    {
        GLBuffer.LOGGER.finest("Binding Array Buffer:", buffer);
        
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, buffer != null ? buffer.id : 0);
    }
    
    public static void bind(@Nullable GLBufferElementArray buffer)
    {
        GLBuffer.LOGGER.finest("Binding Element Array Buffer:", buffer);
        
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, buffer != null ? buffer.id : 0);
    }
    
    public static void bind(@Nullable GLBufferUniform buffer)
    {
        GLBuffer.LOGGER.finest("Binding Uniform Buffer:", buffer);
        
        GL33.glBindBuffer(GL33.GL_UNIFORM_BUFFER, buffer != null ? buffer.id : 0);
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected final int   type;
    protected final Usage usage;
    
    protected long size;
    
    protected ByteBuffer mapped;
    
    /**
     * Creates and binds a GLBuffer of a specified size
     *
     * @param type  The type of the buffer
     * @param size  The size in bytes
     * @param usage How the buffer will be used.
     */
    protected GLBuffer(int type, long size, Usage usage)
    {
        this.id = GL33.glGenBuffers();
        
        this.type  = type;
        this.usage = usage;
        
        this.size = size;
        
        bind(this);
        
        GL33.nglBufferData(this.type, this.size, MemoryUtil.NULL, usage.ref);
        
        GLBuffer.LOGGER.fine("Created", this);
    }
    
    /**
     * Creates and binds a GLBuffer of specified data
     *
     * @param type  The type of the buffer
     * @param data  The data
     * @param usage How the buffer will be used.
     */
    protected GLBuffer(int type, @NotNull Buffer data, @NotNull Usage usage)
    {
        this.id = GL33.glGenBuffers();
        
        this.type  = type;
        this.usage = usage;
        
        this.size = Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data));
        
        bind(this);
        
        GL33.nglBufferData(this.type, Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)), MemoryUtil.memAddress(data), usage.ref);
        
        GLBuffer.LOGGER.fine("Created", this);
    }
    
    /**
     * Creates and binds a GLBuffer of specified data
     *
     * @param type  The type of the buffer
     * @param data  The data
     * @param usage How the buffer will be used.
     */
    protected GLBuffer(int type, @NotNull CustomBuffer<?> data, @NotNull Usage usage)
    {
        this.id = GL33.glGenBuffers();
        
        this.type  = type;
        this.usage = usage;
        
        this.size = Integer.toUnsignedLong(data.remaining() * data.sizeof());
        
        bind(this);
        
        GL33.nglBufferData(this.type, Integer.toUnsignedLong(data.remaining() * data.sizeof()), MemoryUtil.memAddress(data), usage.ref);
        
        GLBuffer.LOGGER.fine("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLBuffer that = (GLBuffer) o;
        return this.id == that.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", size=" + this.size + '}';
    }
    
    //-----------------------
    // ----- Properties -----
    //-----------------------
    
    /**
     * @return The object id.
     */
    public int id()
    {
        return this.id;
    }
    
    /**
     * @return The size in bytes of the buffer.
     */
    public long size()
    {
        return this.size;
    }
    
    /**
     * @return The buffer usage.
     */
    public @NotNull Usage usage()
    {
        return this.usage;
    }
    
    /**
     * Deletes the contents of the buffer and free's its memory.
     */
    public void delete()
    {
        GLBuffer.LOGGER.fine("Deleting", this);
        
        GL33.glDeleteBuffers(this.id);
        
        this.id   = 0;
        this.size = 0;
    }
    
    //-----------------------------
    // ----- Buffer Functions -----
    //-----------------------------
    
    public @Nullable ByteBuffer map(@NotNull Access access)
    {
        GLBuffer.LOGGER.finer("Mapping %s as %s", this, access);
        
        bind(this);
        
        return this.mapped = GL33.glMapBuffer(this.type, access.gl(), this.size, this.mapped);
    }
    
    public void unmap()
    {
        GLBuffer.LOGGER.finer("Unmapping %s", this);
        
        bind(this);
        
        if (!GL33.glUnmapBuffer(this.type)) GLBuffer.LOGGER.warning("Could not unmap", this);
    }
    
    /**
     * Gets the data in the buffer.
     *
     * @param offset The offset into the buffer.
     * @param buffer The destination buffer.
     * @return The data in the buffer.
     */
    public GLBuffer get(long offset, Buffer buffer)
    {
        GLBuffer.LOGGER.finer("Getting Contents of", this);
        
        bind(this);
        
        GL33.nglGetBufferSubData(this.type, offset, Integer.toUnsignedLong(buffer.remaining() * MemUtil.elementSize(buffer)), MemoryUtil.memAddress(buffer));
        return this;
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param buffer The destination buffer.
     * @return The data in the buffer.
     */
    public GLBuffer get(Buffer buffer)
    {
        return get(0, buffer);
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param buffer The destination buffer.
     * @return The data in the buffer.
     */
    public GLBuffer get(long offset, CustomBuffer<?> buffer)
    {
        GLBuffer.LOGGER.finer("Getting Contents of", this);
        
        bind(this);
        
        GL33.nglGetBufferSubData(this.type, offset, Integer.toUnsignedLong(buffer.remaining() * buffer.sizeof()), MemoryUtil.memAddress(buffer));
        return this;
    }
    
    /**
     * Gets the data in the buffer.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param buffer The destination buffer.
     * @return The data in the buffer.
     */
    public GLBuffer get(CustomBuffer<?> buffer)
    {
        return get(0, buffer);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param data   The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(long offset, @NotNull Buffer data)
    {
        GLBuffer.LOGGER.finer("Setting Contents of", this);
        
        bind(this);
        
        GL33.nglBufferSubData(this.type, offset, Integer.toUnsignedLong(data.remaining() * MemUtil.elementSize(data)), MemoryUtil.memAddress(data));
        return this;
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(@NotNull Buffer data)
    {
        return set(0, data);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param offset The offset into the buffer.
     * @param data   The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(long offset, @NotNull CustomBuffer<?> data)
    {
        GLBuffer.LOGGER.finer("Setting Contents of", this);
        
        bind(this);
        
        GL33.nglBufferSubData(this.type, offset, Integer.toUnsignedLong(data.remaining() * data.sizeof()), MemoryUtil.memAddress(data));
        return this;
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(@NotNull CustomBuffer<?> data)
    {
        return set(0, data);
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(byte... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.bytes(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(short... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.shorts(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(int... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.ints(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(long... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.longs(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(float... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.floats(data));
        }
    }
    
    /**
     * Sets the contents of the buffer. If the data is larger than the buffer, then it will be resized.
     * <p>
     * Make sure to bind the buffer first.
     *
     * @param data The data.
     * @return This instance for call chaining.
     */
    public GLBuffer set(double... data)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            return set(0, stack.doubles(data));
        }
    }
    
    public enum Access
    {
        READ_ONLY(GL33.GL_READ_ONLY),
        WRITE_ONLY(GL33.GL_WRITE_ONLY),
        READ_WRITE(GL33.GL_READ_WRITE),
        ;
        
        private final int gl;
        
        Access(int gl)
        {
            this.gl = gl;
        }
        
        public int gl()
        {
            return this.gl;
        }
    }
}
