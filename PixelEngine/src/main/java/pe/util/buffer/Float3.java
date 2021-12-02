package pe.util.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.*;

public class Float3 extends Struct implements NativeResource
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
    
    static
    {
        Layout layout = __struct(
                __member(4),
                __member(4),
                __member(4)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
        Z = layout.offsetof(2);
    }
    
    protected Float3(long address, @Nullable ByteBuffer container)
    {
        super(address, container);
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Float3{" + "x=" + x() + ", y=" + y() + ", z=" + z() + '}';
    }
    
    /**
     * Returns {@code sizeof(struct)}.
     */
    @Override
    public int sizeof()
    {
        return Float3.SIZEOF;
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
     * Sets the x value
     *
     * @param value x value
     * @return this
     */
    public @NotNull Float3 x(double value)
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
    public @NotNull Float3 y(double value)
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
    public @NotNull Float3 z(double value)
    {
        nz(address(), value);
        return this;
    }
    
    /**
     * Sets the values
     *
     * @param x x value
     * @param y y value
     * @param z z value
     * @return this
     */
    public @NotNull Float3 set(double x, double y, double z)
    {
        return x(x).y(y).z(z);
    }
    
    // -----------------------------------
    
    /**
     * Unsafe version of {@link #x}.
     */
    public static float nx(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float3.X);
    }
    
    /**
     * Unsafe version of {@link #y}.
     */
    public static float ny(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float3.Y);
    }
    
    /**
     * Unsafe version of {@link #z}.
     */
    public static float nz(long struct)
    {
        return UNSAFE.getFloat(null, struct + Float3.Z);
    }
    
    /**
     * Unsafe version of {@link #x(double) buttons}.
     */
    public static void nx(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float3.X, (float) value);
    }
    
    /**
     * Unsafe version of {@link #y(double) buttons}.
     */
    public static void ny(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float3.Y, (float) value);
    }
    
    /**
     * Unsafe version of {@link #z(double) buttons}.
     */
    public static void nz(long struct, double value)
    {
        UNSAFE.putFloat(null, struct + Float3.Z, (float) value);
    }
    
    // --------------------------------------
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Float3 malloc()
    {
        return wrap(Float3.class, nmemAllocChecked(Float3.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Float3 calloc()
    {
        return wrap(Float3.class, nmemCallocChecked(1, Float3.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Float3 create()
    {
        ByteBuffer container = __create(1, Float3.SIZEOF);
        return wrap(Float3.class, memAddress(container), container);
    }
    
    /**
     * Returns a new {@code Float2} instance for the specified memory address.
     */
    public static @NotNull Float3 create(long address)
    {
        return wrap(Float3.class, address);
    }
    
    /**
     * Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Float3 createSafe(long address)
    {
        return address == NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack}.
     */
    public static @NotNull Float3 mallocStack()
    {
        return mallocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     */
    public static @NotNull Float3 callocStack()
    {
        return callocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Float3 mallocStack(MemoryStack stack)
    {
        return wrap(Float3.class, stack.nmalloc(Float3.ALIGNOF, Float3.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Float3 callocStack(MemoryStack stack)
    {
        return wrap(Float3.class, stack.ncalloc(Float3.ALIGNOF, 1, Float3.SIZEOF));
    }
    
    // -----------------------------------
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, nmemAllocChecked(__checkMalloc(capacity, Float3.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, nmemCallocChecked(capacity, Float3.SIZEOF), capacity);
    }
    
    public static @NotNull Float3.Buffer realloc(@Nullable Float3.Buffer ptr, int capacity)
    {
        ByteBuffer old    = MemoryUtil.memByteBufferSafe(MemoryUtil.memAddressSafe(ptr), capacity * Float3.SIZEOF);
        ByteBuffer newPtr = MemoryUtil.memRealloc(old, capacity * Float3.SIZEOF);
        return wrap(Buffer.class, memAddress(newPtr), capacity, newPtr);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Float3.SIZEOF);
        return wrap(Buffer.class, memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Float3.SIZEOF);
    }
    
    /**
     * Like {@link #create(long, int) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Float3.Buffer createSafe(long address, int capacity)
    {
        return address == NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Float3.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining() / Float3.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Float3.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Float3.Buffer wrap(@NotNull FloatBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining());
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Float3.Buffer wrapSafe(@Nullable FloatBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer mallocStack(int capacity)
    {
        return mallocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer callocStack(int capacity)
    {
        return callocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer mallocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Float3.ALIGNOF, capacity * Float3.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Float3.Buffer callocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Float3.ALIGNOF, capacity, Float3.SIZEOF), capacity);
    }
    
    // -----------------------------------
    
    public static class Buffer extends StructBuffer<Float3, Buffer>
    {
        private static final Float3 ELEMENT_FACTORY = Float3.create(-1L);
        
        protected Buffer(@NotNull ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity)
        {
            super(address, container, mark, position, limit, capacity);
        }
        
        @Override
        protected @NotNull Float3 getElementFactory()
        {
            return Buffer.ELEMENT_FACTORY;
        }
        
        @Override
        protected @NotNull Buffer self()
        {
            return this;
        }
        
        public @NotNull Buffer put(double x, double y, double z)
        {
            get().x(x).y(y).z(z);
            return this;
        }
        
        public @NotNull Buffer put(int i, double x, double y, double z)
        {
            get(i).x(x).y(y).z(z);
            return this;
        }
    }
}
