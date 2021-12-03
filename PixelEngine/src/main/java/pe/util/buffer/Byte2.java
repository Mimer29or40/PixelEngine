package pe.util.buffer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;

public class Byte2 extends Struct implements NativeResource
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
    
    static
    {
        Layout layout = __struct(
                __member(1),
                __member(1)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        X = layout.offsetof(0);
        Y = layout.offsetof(1);
    }
    
    protected static byte toByte(int value)
    {
        if (value < 0) value = 0;
        if (value > 255) value = 255;
        return (byte) value;
    }
    
    protected static int toInt(byte value)
    {
        return value & 0xFF;
    }
    
    /**
     * Unsafe version of {@link #x}.
     */
    public static int nx(long struct)
    {
        return toInt(UNSAFE.getByte(null, struct + Byte2.X));
    }
    
    /**
     * Unsafe version of {@link #x(int) buttons}.
     */
    public static void nx(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Byte2.X, toByte(value));
    }
    
    /**
     * Unsafe version of {@link #y}.
     */
    public static int ny(long struct)
    {
        return toInt(UNSAFE.getByte(null, struct + Byte2.Y));
    }
    
    /**
     * Unsafe version of {@link #y(int) buttons}.
     */
    public static void ny(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Byte2.Y, toByte(value));
    }
    
    // ---------- Creation ---------- //
    
    /**
     * Returns a new {@link Byte2} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Byte2 create()
    {
        ByteBuffer container = BufferUtils.createByteBuffer(Byte2.SIZEOF);
        return wrap(Byte2.class, MemoryUtil.memAddress(container), container);
    }
    
    /**
     * Returns a new {@link Byte2} instance for the specified memory address.
     */
    public static @NotNull Byte2 create(long address)
    {
        return wrap(Byte2.class, address);
    }
    
    /**
     * Returns a new {@link Byte2} instance for the specified memory address, but returns {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Byte2 createSafe(long address)
    {
        return address == MemoryUtil.NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@link Byte2} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Byte2 malloc()
    {
        return wrap(Byte2.class, MemoryUtil.nmemAllocChecked(Byte2.SIZEOF));
    }
    
    /**
     * Returns a new {@link Byte2} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Byte2 malloc(@NotNull MemoryStack stack)
    {
        return wrap(Byte2.class, stack.nmalloc(Byte2.ALIGNOF, Byte2.SIZEOF));
    }
    
    /**
     * Returns a new {@link Byte2} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Byte2 calloc()
    {
        return wrap(Byte2.class, MemoryUtil.nmemCallocChecked(1, Byte2.SIZEOF));
    }
    
    /**
     * Returns a new {@link Byte2} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Byte2 calloc(@NotNull MemoryStack stack)
    {
        return wrap(Byte2.class, stack.ncalloc(Byte2.ALIGNOF, 1, Byte2.SIZEOF));
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Byte2.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Byte2.SIZEOF);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory, but returns {@code null} if {@code address} is {@code MemoryUtil#NULL NULL}.
     */
    public static @Nullable Byte2.Buffer createSafe(long address, int capacity)
    {
        return address == MemoryUtil.NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Byte2.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining() / Byte2.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a Buffer
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Byte2.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, Byte2.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Byte2.ALIGNOF, capacity * Byte2.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, Byte2.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Byte2.Buffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Byte2.ALIGNOF, capacity, Byte2.SIZEOF), capacity);
    }
    
    public static @NotNull Byte2.Buffer realloc(@Nullable Byte2.Buffer ptr, int capacity)
    {
        ByteBuffer old    = MemoryUtil.memByteBufferSafe(MemoryUtil.memAddressSafe(ptr), capacity * Byte2.SIZEOF);
        ByteBuffer newPtr = MemoryUtil.memRealloc(old, capacity * Byte2.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(newPtr), capacity, newPtr);
    }
    
    // ---------- Instance ---------- //
    
    public Byte2(@NotNull ByteBuffer container)
    {
        super(MemoryUtil.memAddress(container), __checkContainer(container, Byte2.SIZEOF));
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Int4{" + "x=" + x() + ", y=" + y() + '}';
    }
    
    /**
     * Returns {@code sizeof(struct)}.
     */
    @Override
    public int sizeof()
    {
        return Byte2.SIZEOF;
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
    public @NotNull Byte2 x(int value)
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
    public @NotNull Byte2 y(int value)
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
    public @NotNull Byte2 set(int x, int y)
    {
        return x(x).y(y);
    }
    
    // ---------- Buffer ---------- //
    
    public static class Buffer extends StructBuffer<Byte2, Buffer>
    {
        private static final Byte2 ELEMENT_FACTORY = Byte2.create(-1L);
        
        protected Buffer(@NotNull ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, @Nullable ByteBuffer container, int mark, int position, int limit, int capacity)
        {
            super(address, container, mark, position, limit, capacity);
        }
        
        @Override
        protected @NotNull Byte2 getElementFactory()
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
