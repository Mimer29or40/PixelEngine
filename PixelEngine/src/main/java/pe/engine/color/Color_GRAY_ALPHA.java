package pe.engine.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;


public class Color_GRAY_ALPHA extends Color
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
    public static final int GRAY;
    public static final int ALPHA;
    
    static
    {
        Layout layout = __struct(
                __member(1),
                __member(1)
                                );
        
        SIZEOF  = layout.getSize();
        ALIGNOF = layout.getAlignment();
        
        GRAY  = layout.offsetof(0);
        ALPHA = layout.offsetof(1);
    }
    
    /**
     * Unsafe version of {@link #r}.
     */
    public static int ngray(long struct)
    {
        return Color.toInt(UNSAFE.getByte(null, struct + Color_GRAY_ALPHA.GRAY));
    }
    
    /**
     * Unsafe version of {@link #r(int) buttons}.
     */
    public static void ngray(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Color_GRAY_ALPHA.GRAY, Color.toByte(value));
    }
    
    /**
     * Unsafe version of {@link #a}.
     */
    public static int na(long struct)
    {
        return Color.toInt(UNSAFE.getByte(null, struct + Color_GRAY_ALPHA.ALPHA));
    }
    
    /**
     * Unsafe version of {@link #a(int) buttons}.
     */
    public static void na(long struct, int value)
    {
        UNSAFE.putByte(null, struct + Color_GRAY_ALPHA.ALPHA, Color.toByte(value));
    }
    
    // ---------- Creation ---------- //
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Color_GRAY_ALPHA create()
    {
        ByteBuffer container = BufferUtils.createByteBuffer(Color_GRAY_ALPHA.SIZEOF);
        return wrap(Color_GRAY_ALPHA.class, MemoryUtil.memAddress(container), container);
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance for the specified memory address.
     */
    public static @NotNull Color_GRAY_ALPHA create(long address)
    {
        return wrap(Color_GRAY_ALPHA.class, address);
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color_GRAY_ALPHA createSafe(long address)
    {
        return address == MemoryUtil.NULL ? null : create(address);
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color_GRAY_ALPHA malloc()
    {
        return wrap(Color_GRAY_ALPHA.class, MemoryUtil.nmemAllocChecked(Color_GRAY_ALPHA.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color_GRAY_ALPHA malloc(@NotNull MemoryStack stack)
    {
        return wrap(Color_GRAY_ALPHA.class, stack.nmalloc(Color_GRAY_ALPHA.ALIGNOF, Color_GRAY_ALPHA.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color_GRAY_ALPHA calloc()
    {
        return wrap(Color_GRAY_ALPHA.class, MemoryUtil.nmemCallocChecked(1, Color_GRAY_ALPHA.SIZEOF));
    }
    
    /**
     * Returns a new {@link Color_GRAY_ALPHA} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color_GRAY_ALPHA calloc(@NotNull MemoryStack stack)
    {
        return wrap(Color_GRAY_ALPHA.class, stack.ncalloc(Color_GRAY_ALPHA.ALIGNOF, 1, Color_GRAY_ALPHA.SIZEOF));
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer create(int capacity)
    {
        ByteBuffer container = __create(capacity, Color_GRAY_ALPHA.SIZEOF);
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining(), container);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer create(long address, int capacity)
    {
        return wrap(Buffer.class, address, capacity * Color_GRAY_ALPHA.SIZEOF);
    }
    
    /**
     * Returns a new {@link Buffer} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color_GRAY_ALPHA.Buffer createSafe(long address, int capacity)
    {
        return address == MemoryUtil.NULL ? null : create(address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(Buffer.class, MemoryUtil.memAddress(container), container.remaining() / Color_GRAY_ALPHA.SIZEOF);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Color_GRAY_ALPHA.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return container == null ? null : wrap(container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer malloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemAllocChecked(__checkMalloc(capacity, Color_GRAY_ALPHA.SIZEOF)), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.nmalloc(Color_GRAY_ALPHA.ALIGNOF, capacity * Color_GRAY_ALPHA.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer calloc(int capacity)
    {
        return wrap(Buffer.class, MemoryUtil.nmemCallocChecked(capacity, Color_GRAY_ALPHA.SIZEOF), capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color_GRAY_ALPHA.Buffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return wrap(Buffer.class, stack.ncalloc(Color_GRAY_ALPHA.ALIGNOF, capacity, Color_GRAY_ALPHA.SIZEOF), capacity);
    }
    
    public static @NotNull Color_GRAY_ALPHA.Buffer realloc(@Nullable Color.Buffer ptr, int capacity)
    {
        ByteBuffer newPtr = MemoryUtil.memRealloc(ptr != null ? ptr.toBuffer() : null, ptr != null ? capacity * ptr.sizeof() : capacity);
        return wrap(Buffer.class, MemoryUtil.memAddress(newPtr), capacity, newPtr);
    }
    
    // ---------- Instance ---------- //
    
    public Color_GRAY_ALPHA(@NotNull ByteBuffer container)
    {
        super(MemoryUtil.memAddress(container), __checkContainer(container, Color_GRAY_ALPHA.SIZEOF));
    }
    
    @Override
    public int sizeof()
    {
        return Color_GRAY_ALPHA.SIZEOF;
    }
    
    @Override
    public @NotNull ColorFormat format()
    {
        return ColorFormat.GRAY_ALPHA;
    }
    
    @Override
    public int r()
    {
        return ngray(address());
    }
    
    @Override
    public int g()
    {
        return ngray(address());
    }
    
    @Override
    public int b()
    {
        return ngray(address());
    }
    
    @Override
    public int a()
    {
        return na(address());
    }
    
    @Override
    public @NotNull Color r(int value)
    {
        ngray(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color g(int value)
    {
        ngray(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color b(int value)
    {
        ngray(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color a(int value)
    {
        na(address(), value);
        return this;
    }
    
    @Override
    public @NotNull Color set(int r, int g, int b, int a)
    {
        ngray(address(), Color.toGray(r, g, b));
        na(address(), a);
        return this;
    }
    
    @Override
    public @NotNull Color set(double r, double g, double b, double a)
    {
        ngray(address(), Color.toGray(r, g, b));
        na(address(), (int) (a * 255));
        return this;
    }
    
    @Override
    public boolean equals(int r, int g, int b, int a)
    {
        return r() == Color.toGray(r, g, b) && a() == a;
    }
    
    @Override
    public boolean equals(double r, double g, double b, double a)
    {
        return r() == Color.toGray(r, g, b) && Double.compare(af(), a) == 0;
    }
    
    // ---------- Buffer ---------- //
    
    public static class Buffer extends Color.Buffer
    {
        private static final Color_GRAY_ALPHA ELEMENT_FACTORY = Color_GRAY_ALPHA.create(-1L);
        
        public Buffer(@NotNull ByteBuffer container)
        {
            super(container, container.remaining() / Color_GRAY_ALPHA.SIZEOF);
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
