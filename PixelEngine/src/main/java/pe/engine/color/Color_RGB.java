package pe.engine.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;


public class Color_RGB extends Color
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
    public static final int RED;
    public static final int GREEN;
    public static final int BLUE;
    
    static
    {
        Layout layout = __struct(
                __member(1),
                __member(1),
                __member(1)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        RED   = layout.offsetof(0);
        GREEN = layout.offsetof(1);
        BLUE  = layout.offsetof(2);
    }
    
    /**
     * Unsafe version of {@link #r}.
     */
    public static int nr(long struct)
    {
        return Color.toInt(UNSAFE.getByte(null, struct + Color_RGB.RED));
    }
    
    /**
     * Unsafe version of {@link #r(int) buttons}.
     */
    public static void nr(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Color_RGB.RED, Color.toByte(value));
    }
    
    /**
     * Unsafe version of {@link #g}.
     */
    public static int ng(long struct)
    {
        return Color.toInt(UNSAFE.getByte(null, struct + Color_RGB.GREEN));
    }
    
    /**
     * Unsafe version of {@link #g(int) buttons}.
     */
    public static void ng(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Color_RGB.GREEN, Color.toByte(value));
    }
    
    /**
     * Unsafe version of {@link #b}.
     */
    public static int nb(long struct)
    {
        return Color.toInt(UNSAFE.getByte(null, struct + Color_RGB.BLUE));
    }
    
    /**
     * Unsafe version of {@link #b(int) buttons}.
     */
    public static void nb(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Color_RGB.BLUE, Color.toByte(value));
    }
    
    // ---------- Creation ---------- //
    
    /**
     * Returns a new {@link Color_RGB} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Color_RGB create()
    {
        ByteBuffer container = BufferUtils.createByteBuffer(Color_RGB.SIZEOF);
        return wrap(Color_RGB.class, MemoryUtil.memAddress(container), container);
    }
    
    /**
     * Returns a new {@link Color_RGB} instance for the specified memory address.
     */
    public static @NotNull Color_RGB create(long address)
    {
        return wrap(Color_RGB.class, address);
    }
    
    /**
     * Returns a new {@link Color_RGB} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color_RGB createSafe(long address)
    {
        return address == MemoryUtil.NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@link Color_RGB} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color_RGB malloc()
    {
        return wrap(Color_RGB.class, MemoryUtil.nmemAllocChecked(Color_RGB.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_RGB} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color_RGB malloc(@NotNull MemoryStack stack)
    {
        return wrap(Color_RGB.class, stack.nmalloc(Color_RGB.ALIGNOF, Color_RGB.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_RGB} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color_RGB calloc()
    {
        return wrap(Color_RGB.class, MemoryUtil.nmemCallocChecked(1, Color_RGB.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_RGB} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color_RGB calloc(@NotNull MemoryStack stack)
    {
        return wrap(Color_RGB.class, stack.ncalloc(Color_RGB.ALIGNOF, 1, Color_RGB.SIZEOF));
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Color_RGB.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Color_RGB.SIZEOF);
    }
    
    /**
     * Returns a new {@link Buffer} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color_RGB.Buffer createSafe(long address, int capacity)
    {
        return address == MemoryUtil.NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Color_RGB.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining() / Color_RGB.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Color_RGB.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, Color_RGB.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Color_RGB.ALIGNOF, capacity * Color_RGB.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, Color_RGB.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_RGB.Buffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Color_RGB.ALIGNOF, capacity, Color_RGB.SIZEOF), capacity);
    }
    
    public static @NotNull Color_RGB.Buffer realloc(@Nullable Color.Buffer ptr, int capacity)
    {
        ByteBuffer newPtr = MemoryUtil.memRealloc(ptr != null ? ptr.toBuffer() : null, ptr != null ? capacity * ptr.sizeof() : capacity);
        return wrap(Buffer.class, MemoryUtil.memAddress(newPtr), capacity, newPtr);
    }
    
    // ---------- Instance ---------- //
    
    public Color_RGB(@NotNull ByteBuffer container)
    {
        super(MemoryUtil.memAddress(container), __checkContainer(container, Color_RGB.SIZEOF));
    }
    
    @Override
    public int sizeof()
    {
        return Color_RGB.SIZEOF;
    }
    
    @Override
    public @NotNull ColorFormat format()
    {
        return ColorFormat.RGB;
    }
    
    @Override
    public int r()
    {
        return nr(address());
    }
    
    @Override
    public int g()
    {
        return ng(address());
    }
    
    @Override
    public int b()
    {
        return nb(address());
    }
    
    @Override
    public int a()
    {
        return 0xFF;
    }
    
    @Override
    public @NotNull Color r(int value)
    {
        nr(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color g(int value)
    {
        ng(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color b(int value)
    {
        nb(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color a(int value)
    {
        return this;
    }
    
    // ---------- Buffer ---------- //
    
    public static class Buffer extends Color.Buffer
    {
        private static final Color_RGB ELEMENT_FACTORY = Color_RGB.create(-1L);
        
        public Buffer(@NotNull ByteBuffer container)
        {
            super(container, container.remaining() / Color_RGB.SIZEOF);
        }
        
        public Buffer(long address, int capacity)
        {
            super(address, capacity);
        }
        
        @Override
        protected @NotNull Color getElementFactory()
        {
            return Buffer.ELEMENT_FACTORY;
        }
        
        @Override
        protected @NotNull Color.Buffer self()
        {
            return this;
        }
    }
}
