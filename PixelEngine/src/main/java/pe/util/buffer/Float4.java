package pe.util.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.*;

public class Float4 extends Struct implements NativeResource
{
    /**
     * The struct size in bytes.
     */
    public static final int SIZEOF;
    
    /**
     * The struct alignment in bytes.
     */
    public static final int ALIGNOF;
    
    /**
     * The struct member offsets.
     */
    public static final int X;
    public static final int Y;
    public static final int Z;
    public static final int W;
    
    static
    {
        Layout layout = __struct(
                __member(4),
                __member(4),
                __member(4),
                __member(4)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        Z = layout.offsetof(2);
        W = layout.offsetof(3);
    }
    
    protected Float4(long address, @Nullable ByteBuffer container)
    {
        super(address, container);
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Float4{" + "x=" + x() + ", y=" + y() + ", z=" + z() + ", w=" + w() + '}';
    }
    
    /**
     * Returns {@code sizeof(struct)}.
     */
    @Override
    public int sizeof()
    {
        return Float4.SIZEOF;
    }
    
    /**
     * @return {@code float} value of {@code x}
     */
    public float x()
    {
        return nx(address());
    }
    
    /**
     * @return {@code float} value of {@code y}
     */
    public float y()
    {
        return ny(address());
    }
    
    /**
     * @return {@code float} value of {@code z}
     */
    public float z()
    {
        return nz(address());
    }
    
    /**
     * @return {@code float} value of {@code w}
     */
    public float w()
    {
        return nw(address());
    }
    
    /**
     * Sets the x value
     *
     * @param value x value
     * @return this
     */
    public @NotNull Float4 x(double value)
    {
        nx(address(), value);
        return this;
    }
    
    /**
     * Sets the y value
     *
     * @param value y value
     * @return this
     */
    public @NotNull Float4 y(double value)
    {
        ny(address(), value);
        return this;
    }
    
    /**
     * Sets the z value
     *
     * @param value z value
     * @return this
     */
    public @NotNull Float4 z(double value)
    {
        nz(address(), value);
        return this;
    }
    
    /**
     * Sets the w value
     *
     * @param value w value
     * @return this
     */
    public @NotNull Float4 w(double value)
    {
        nw(address(), value);
        return this;
    }
    
    /**
     * Sets the values
     *
     * @param x x value
     * @param y y value
     * @param z z value
     * @param w w value
     * @return this
     */
    public @NotNull Float4 set(double x, double y, double z, double w)
    {
        return x(x).y(y).z(z).w(w);
    }
    
    // -----------------------------------
    
    /**
     * Unsafe version of {@link #x}.
     */
    public static float nx(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float4.X);
    }
    
    /**
     * Unsafe version of {@link #y}.
     */
    public static float ny(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float4.Y);
    }
    
    /**
     * Unsafe version of {@link #z}.
     */
    public static float nz(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float4.Z);
    }
    
    /**
     * Unsafe version of {@link #w}.
     */
    public static float nw(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float4.W);
    }
    
    /**
     * Unsafe version of {@link #x(double) buttons}.
     */
    public static void nx(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float4.X, (float) value);
    }
    
    /**
     * Unsafe version of {@link #y(double) buttons}.
     */
    public static void ny(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float4.Y, (float) value);
    }
    
    /**
     * Unsafe version of {@link #z(double) buttons}.
     */
    public static void nz(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float4.Z, (float) value);
    }
    
    /**
     * Unsafe version of {@link #w(double) buttons}.
     */
    public static void nw(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float4.W, (float) value);
    }
    
    // --------------------------------------
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Float4 malloc()
    {
        return wrap(Float4.class, nmemAllocChecked(Float4.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Float4 calloc()
    {
        return wrap(Float4.class, nmemCallocChecked(1, Float4.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Float4 create()
    {
        ByteBuffer container = __create(1, Float4.SIZEOF);
        return wrap(Float4.class, memAddress(container), container);
    }
    
    /**
     * Returns a new {@code Float2} instance for the specified memory address.
     */
    public static @NotNull Float4 create(long address)
    {
        return wrap(Float4.class, address);
    }
    
    /**
     * Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Float4 createSafe(long address)
    {
        return address == NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack}.
     */
    public static @NotNull Float4 mallocStack()
    {
        return mallocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     */
    public static @NotNull Float4 callocStack()
    {
        return callocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Float4 mallocStack(MemoryStack stack)
    {
        return wrap(Float4.class, stack.nmalloc(Float4.ALIGNOF, Float4.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Float4 callocStack(MemoryStack stack)
    {
        return wrap(Float4.class, stack.ncalloc(Float4.ALIGNOF, 1, Float4.SIZEOF));
    }
    
    // -----------------------------------
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, nmemAllocChecked(__checkMalloc(capacity, Float4.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, nmemCallocChecked(capacity, Float4.SIZEOF), capacity);
    }
    
    public static @NotNull Float4.Buffer realloc(@Nullable Float4.Buffer ptr, int capacity)
    {
        ByteBuffer old    = MemoryUtil.memByteBufferSafe(MemoryUtil.memAddressSafe(ptr), capacity * Float4.SIZEOF);
        ByteBuffer newPtr = MemoryUtil.memRealloc(old, capacity * Float4.SIZEOF);
        return wrap(Buffer.class, memAddress(newPtr), capacity, newPtr);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Float4.SIZEOF);
        return wrap(Buffer.class, memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Float4.SIZEOF);
    }
    
    /**
     * Like {@link #create(long, int) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Float4.Buffer createSafe(long address, int capacity)
    {
        return address == NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Float4.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining() / Float4.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Float4.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Float4.Buffer wrap(@NotNull FloatBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining());
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Float4.Buffer wrapSafe(@Nullable FloatBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer mallocStack(int capacity)
    {
        return mallocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer callocStack(int capacity)
    {
        return callocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer mallocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Float4.ALIGNOF, capacity * Float4.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Float4.Buffer callocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Float4.ALIGNOF, capacity, Float4.SIZEOF), capacity);
    }
    
    // -----------------------------------
    
    public static class Buffer extends StructBuffer<Float4, Buffer>
    {
        private static final Float4 ELEMENT_FACTORY = Float4.create(-1L);
        
        protected Buffer(@NotNull ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity)
        {
            super(address, container, mark, position, limit, capacity);
        }
        
        @Override
        protected @NotNull Float4 getElementFactory()
        {
            return Buffer.ELEMENT_FACTORY;
        }
        
        @Override
        protected @NotNull Buffer self()
        {
            return this;
        }
        
        public @NotNull Buffer put(double x, double y, double z, double w)
        {
            get().x(x).y(y).z(z).w(w);
            return this;
        }
        
        public @NotNull Buffer put(int i, double x, double y, double z, double w)
        {
            get(i).x(x).y(y).z(z).w(w);
            return this;
        }
    }
}
