package pe.util.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryUtil.*;

public class Int2 extends Struct implements NativeResource
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
    
    static
    {
        Layout layout = __struct(
                __member(4),
                __member(4)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
    }
    
    protected Int2(long address, @Nullable ByteBuffer container)
    {
        super(address, container);
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Int2{" + "x=" + x() + ", y=" + y() + '}';
    }
    
    /**
     * Returns {@code sizeof(struct)}.
     */
    @Override
    public int sizeof()
    {
        return Int2.SIZEOF;
    }
    
    /**
     * @return {@code int} value of {@code x}
     */
    public int x()
    {
        return nx(address());
    }
    
    /**
     * @return {@code int} value of {@code y}
     */
    public int y()
    {
        return ny(address());
    }
    
    /**
     * Sets the x value
     *
     * @param value x value
     * @return this
     */
    public @NotNull Int2 x(int value)
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
    public @NotNull Int2 y(int value)
    {
        ny(address(), value);
        return this;
    }
    
    /**
     * Sets the values
     *
     * @param x x value
     * @param y y value
     * @return this
     */
    public @NotNull Int2 set(int x, int y)
    {
        return x(x).y(y);
    }
    
    // -----------------------------------
    
    /**
     * Unsafe version of {@link #x}.
     */
    public static int nx(long struct)
    {
        return UNSAFE.getInt(null, struct + Int2.X);
    }
    
    /**
     * Unsafe version of {@link #y}.
     */
    public static int ny(long struct)
    {
        return UNSAFE.getInt(null, struct + Int2.Y);
    }
    
    /**
     * Unsafe version of {@link #x(int) buttons}.
     */
    public static void nx(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int2.X, value);
    }
    
    /**
     * Unsafe version of {@link #y(int) buttons}.
     */
    public static void ny(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int2.Y, value);
    }
    
    // --------------------------------------
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Int2 malloc()
    {
        return wrap(Int2.class, nmemAllocChecked(Int2.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Int2 calloc()
    {
        return wrap(Int2.class, nmemCallocChecked(1, Int2.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Int2 create()
    {
        ByteBuffer container = __create(1, Int2.SIZEOF);
        return wrap(Int2.class, memAddress(container), container);
    }
    
    /**
     * Returns a new {@code Float2} instance for the specified memory address.
     */
    public static @NotNull Int2 create(long address)
    {
        return wrap(Int2.class, address);
    }
    
    /**
     * Like {@link #create(long) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Int2 createSafe(long address)
    {
        return address == NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack}.
     */
    public static @NotNull Int2 mallocStack()
    {
        return mallocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     */
    public static @NotNull Int2 callocStack()
    {
        return callocStack(stackGet());
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Int2 mallocStack(MemoryStack stack)
    {
        return wrap(Int2.class, stack.nmalloc(Int2.ALIGNOF, Int2.SIZEOF));
    }
    
    /**
     * Returns a new {@code Float2} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Int2 callocStack(MemoryStack stack)
    {
        return wrap(Int2.class, stack.ncalloc(Int2.ALIGNOF, 1, Int2.SIZEOF));
    }
    
    // -----------------------------------
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, nmemAllocChecked(__checkMalloc(capacity, Int2.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, nmemCallocChecked(capacity, Int2.SIZEOF), capacity);
    }
    
    public static @NotNull Int2.Buffer realloc(@Nullable Int2.Buffer ptr, int capacity)
    {
        ByteBuffer old    = MemoryUtil.memByteBufferSafe(MemoryUtil.memAddressSafe(ptr), capacity * Int2.SIZEOF);
        ByteBuffer newPtr = MemoryUtil.memRealloc(old, capacity * Int2.SIZEOF);
        return wrap(Buffer.class, memAddress(newPtr), capacity, newPtr);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Int2.SIZEOF);
        return wrap(Buffer.class, memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Int2.SIZEOF);
    }
    
    /**
     * Like {@link #create(long, int) create}, but returns {@code null} if {@code address} is {@code NULL}.
     */
    public static @Nullable Int2.Buffer createSafe(long address, int capacity)
    {
        return address == NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Int2.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining() / Int2.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Int2.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Int2.Buffer wrap(@NotNull FloatBuffer container)
    {
        return wrap(Buffer.class, memAddress(container), container.remaining());
    }
    
    /**
     * Wraps a {@link FloatBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Int2.Buffer wrapSafe(@Nullable FloatBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer mallocStack(int capacity)
    {
        return mallocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the thread-local {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer callocStack(int capacity)
    {
        return callocStack(capacity, stackGet());
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer mallocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Int2.ALIGNOF, capacity * Int2.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Int2.Buffer callocStack(int capacity, MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Int2.ALIGNOF, capacity, Int2.SIZEOF), capacity);
    }
    
    // -----------------------------------
    
    public static class Buffer extends StructBuffer<Int2, Buffer>
    {
        private static final Int2 ELEMENT_FACTORY = Int2.create(-1L);
        
        protected Buffer(@NotNull ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity)
        {
            super(address, container, mark, position, limit, capacity);
        }
        
        @Override
        protected @NotNull Int2 getElementFactory()
        {
            return Buffer.ELEMENT_FACTORY;
        }
        
        @Override
        protected @NotNull Buffer self()
        {
            return this;
        }
        
        public @NotNull Buffer put(int x, int y)
        {
            get().x(x).y(y);
            return this;
        }
        
        public @NotNull Buffer put(int i, int x, int y)
        {
            get(i).x(x).y(y);
            return this;
        }
    }
}
