package pe.util.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;

public class Int4 extends Struct implements NativeResource
{
    // ---------- Static ---------- //
    
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
    
    /**
     * Unsafe version of {@link #x}.
     */
    public static int nx(long struct)
    {
        return UNSAFE.getInt(null, struct + Int4.X);
    }
    
    /**
     * Unsafe version of {@link #x(int) buttons}.
     */
    public static void nx(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int4.X, value);
    }
    
    /**
     * Unsafe version of {@link #y}.
     */
    public static int ny(long struct)
    {
        return UNSAFE.getInt(null, struct + Int4.Y);
    }
    
    /**
     * Unsafe version of {@link #y(int) buttons}.
     */
    public static void ny(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int4.Y, value);
    }
    
    /**
     * Unsafe version of {@link #z}.
     */
    public static int nz(long struct)
    {
        return UNSAFE.getInt(null, struct + Int4.Z);
    }
    
    /**
     * Unsafe version of {@link #z(int) buttons}.
     */
    public static void nz(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int4.Z, value);
    }
    
    /**
     * Unsafe version of {@link #w}.
     */
    public static int nw(long struct)
    {
        return UNSAFE.getInt(null, struct + Int4.W);
    }
    
    /**
     * Unsafe version of {@link #w(int) buttons}.
     */
    public static void nw(long struct, int value)
    {
        UNSAFE.putInt(null, struct + Int4.W, value);
    }
    
    // ---------- Creation ---------- //
    
    /**
     * Returns a new {@link Int4} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Int4 create()
    {
        ByteBuffer container = BufferUtils.createByteBuffer(Int4.SIZEOF);
        return wrap(Int4.class, MemoryUtil.memAddress(container), container);
    }
    
    /**
     * Returns a new {@link Int4} instance for the specified memory address.
     */
    public static @NotNull Int4 create(long address)
    {
        return wrap(Int4.class, address);
    }
    
    /**
     * Returns a new {@link Int4} instance for the specified memory address, but returns {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Int4 createSafe(long address)
    {
        return address == MemoryUtil.NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@link Int4} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Int4 malloc()
    {
        return wrap(Int4.class, MemoryUtil.nmemAllocChecked(Int4.SIZEOF));
    }
    
    /**
     * Returns a new {@link Int4} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Int4 malloc(@NotNull MemoryStack stack)
    {
        return wrap(Int4.class, stack.nmalloc(Int4.ALIGNOF, Int4.SIZEOF));
    }
    
    /**
     * Returns a new {@link Int4} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Int4 calloc()
    {
        return wrap(Int4.class, MemoryUtil.nmemCallocChecked(1, Int4.SIZEOF));
    }
    
    /**
     * Returns a new {@link Int4} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Int4 calloc(@NotNull MemoryStack stack)
    {
        return wrap(Int4.class, stack.ncalloc(Int4.ALIGNOF, 1, Int4.SIZEOF));
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Int4.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Int4.SIZEOF);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory, but returns {@code null} if {@code address} is {@code MemoryUtil#NULL NULL}.
     */
    public static @Nullable Int4.Buffer createSafe(long address, int capacity)
    {
        return address == MemoryUtil.NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Int4.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining() / Int4.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Int4.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, Int4.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Int4.ALIGNOF, capacity * Int4.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, Int4.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Int4.Buffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Int4.ALIGNOF, capacity, Int4.SIZEOF), capacity);
    }
    
    public static @NotNull Int4.Buffer realloc(@Nullable Int4.Buffer ptr, int capacity)
    {
        ByteBuffer old    = MemoryUtil.memByteBufferSafe(MemoryUtil.memAddressSafe(ptr), capacity * Int4.SIZEOF);
        ByteBuffer newPtr = MemoryUtil.memRealloc(old, capacity * Int4.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(newPtr), capacity, newPtr);
    }
    
    // ---------- Instance ---------- //
    
    public Int4(@NotNull ByteBuffer container)
    {
        super(MemoryUtil.memAddress(container), __checkContainer(container, Int4.SIZEOF));
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Int4{" + "x=" + x() + ", y=" + y() + ", z=" + z() + ", w=" + w() + '}';
    }
    
    /**
     * Returns {@code sizeof(struct)}.
     */
    @Override
    public int sizeof()
    {
        return Int4.SIZEOF;
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
     * @return {@code int} value of {@code z}
     */
    public int z()
    {
        return nz(address());
    }
    
    /**
     * @return {@code int} value of {@code w}
     */
    public int w()
    {
        return nw(address());
    }
    
    /**
     * Sets the x value
     *
     * @param value x value
     * @return this
     */
    public @NotNull Int4 x(int value)
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
    public @NotNull Int4 y(int value)
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
    public @NotNull Int4 z(int value)
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
    public @NotNull Int4 w(int value)
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
    public @NotNull Int4 set(int x, int y, int z, int w)
    {
        return x(x).y(y).z(z).w(w);
    }
    
    // ---------- Buffer ---------- //
    
    public static class Buffer extends StructBuffer<Int4, Buffer>
    {
        private static final Int4 ELEMENT_FACTORY = Int4.create(-1L);
        
        protected Buffer(@NotNull ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity)
        {
            super(address, container, mark, position, limit, capacity);
        }
        
        @Override
        protected @NotNull Int4 getElementFactory()
        {
            return Buffer.ELEMENT_FACTORY;
        }
        
        @Override
        protected @NotNull Buffer self()
        {
            return this;
        }
        
        public @NotNull Buffer put(int x, int y, int z, int w)
        {
            get().x(x).y(y).z(z).w(w);
            return this;
        }
        
        public @NotNull Buffer put(int i, int x, int y, int z, int w)
        {
            get(i).x(x).y(y).z(z).w(w);
            return this;
        }
    }
}
