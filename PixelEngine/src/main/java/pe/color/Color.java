package pe.color;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4dc;
import org.joml.Vector4fc;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.*;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryUtil.memCopy;

@SuppressWarnings("OctalInteger")
public abstract class Color extends Struct implements NativeResource, Colorc
{
    // ---------- Static ---------- //
    
    public static final Colorc WHITE      = createColor(255, 255, 255, 255);
    public static final Colorc LIGHT_GRAY = createColor(191, 191, 191, 255);
    public static final Colorc GRAY       = createColor(127, 127, 127, 255);
    public static final Colorc DARK_GRAY  = createColor(063, 063, 063, 255);
    public static final Colorc BLACK      = createColor(000, 000, 000, 255);
    
    public static final Colorc LIGHT_GREY = LIGHT_GRAY;
    public static final Colorc GREY       = GRAY;
    public static final Colorc DARK_GREY  = DARK_GRAY;
    
    public static final Colorc LIGHTEST_RED = createColor(255, 191, 191, 255);
    public static final Colorc LIGHTER_RED  = createColor(255, 127, 127, 255);
    public static final Colorc LIGHT_RED    = createColor(255, 063, 063, 255);
    public static final Colorc RED          = createColor(255, 000, 000, 255);
    public static final Colorc DARK_RED     = createColor(191, 000, 000, 255);
    public static final Colorc DARKER_RED   = createColor(127, 000, 000, 255);
    public static final Colorc DARKEST_RED  = createColor(063, 000, 000, 255);
    
    public static final Colorc LIGHTEST_YELLOW = createColor(255, 255, 191, 255);
    public static final Colorc LIGHTER_YELLOW  = createColor(255, 255, 127, 255);
    public static final Colorc LIGHT_YELLOW    = createColor(255, 255, 063, 255);
    public static final Colorc YELLOW          = createColor(255, 255, 000, 255);
    public static final Colorc DARK_YELLOW     = createColor(191, 191, 000, 255);
    public static final Colorc DARKER_YELLOW   = createColor(127, 127, 000, 255);
    public static final Colorc DARKEST_YELLOW  = createColor(063, 063, 000, 255);
    
    public static final Colorc LIGHTEST_GREEN = createColor(191, 255, 191, 255);
    public static final Colorc LIGHTER_GREEN  = createColor(127, 255, 127, 255);
    public static final Colorc LIGHT_GREEN    = createColor(063, 255, 063, 255);
    public static final Colorc GREEN          = createColor(000, 255, 000, 255);
    public static final Colorc DARK_GREEN     = createColor(000, 191, 000, 255);
    public static final Colorc DARKER_GREEN   = createColor(000, 127, 000, 255);
    public static final Colorc DARKEST_GREEN  = createColor(000, 063, 000, 255);
    
    public static final Colorc LIGHTEST_CYAN = createColor(191, 255, 255, 255);
    public static final Colorc LIGHTER_CYAN  = createColor(127, 255, 255, 255);
    public static final Colorc LIGHT_CYAN    = createColor(063, 255, 255, 255);
    public static final Colorc CYAN          = createColor(000, 255, 255, 255);
    public static final Colorc DARK_CYAN     = createColor(000, 191, 191, 255);
    public static final Colorc DARKER_CYAN   = createColor(000, 127, 127, 255);
    public static final Colorc DARKEST_CYAN  = createColor(000, 063, 063, 255);
    
    public static final Colorc LIGHTEST_BLUE = createColor(191, 191, 255, 255);
    public static final Colorc LIGHTER_BLUE  = createColor(127, 127, 255, 255);
    public static final Colorc LIGHT_BLUE    = createColor(063, 063, 255, 255);
    public static final Colorc BLUE          = createColor(000, 000, 255, 255);
    public static final Colorc DARK_BLUE     = createColor(000, 000, 191, 255);
    public static final Colorc DARKER_BLUE   = createColor(000, 000, 127, 255);
    public static final Colorc DARKEST_BLUE  = createColor(000, 000, 063, 255);
    
    public static final Colorc LIGHTEST_MAGENTA = createColor(255, 191, 255, 255);
    public static final Colorc LIGHTER_MAGENTA  = createColor(255, 127, 255, 255);
    public static final Colorc LIGHT_MAGENTA    = createColor(255, 063, 255, 255);
    public static final Colorc MAGENTA          = createColor(255, 000, 255, 255);
    public static final Colorc DARK_MAGENTA     = createColor(191, 000, 191, 255);
    public static final Colorc DARKER_MAGENTA   = createColor(127, 000, 127, 255);
    public static final Colorc DARKEST_MAGENTA  = createColor(063, 000, 063, 255);
    
    public static final Colorc BLANK = createColor(000, 000, 000, 000);
    
    public static final Colorc RAY_WHITE       = createColor(245, 245, 245, 255);
    public static final Colorc BACKGROUND_GRAY = createColor(051, 051, 051, 255);
    
    // 0.299R + 0.587G + 0.114B
    private static final double R_TO_GRAY_F = 0.299;
    private static final double G_TO_GRAY_F = 0.587;
    private static final double B_TO_GRAY_F = 0.114;
    
    private static final int R_TO_GRAY = (int) Math.round(R_TO_GRAY_F * 255);
    private static final int G_TO_GRAY = (int) Math.round(G_TO_GRAY_F * 255);
    private static final int B_TO_GRAY = (int) Math.round(B_TO_GRAY_F * 255);
    
    public static int toGray(int r, int g, int b)
    {
        return ((r * Color.R_TO_GRAY + g * Color.G_TO_GRAY + b * Color.B_TO_GRAY) / 255) & 0xFF;
    }
    
    public static int toGray(double r, double g, double b)
    {
        return (int) ((r * Color.R_TO_GRAY_F + g * Color.G_TO_GRAY_F + b * Color.B_TO_GRAY_F) * 255);
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
    
    private static Colorc createColor(int r, int g, int b, int a)
    {
        return Color_RGBA.create().set(r, g, b, a);
    }
    
    // ---------- Creation ---------- //
    
    /**
     * Returns a new {@link Color} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Color create(@NotNull ColorFormat format)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.create();
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.create();
                    case RGB -> Color_RGB.create();
                    case RGBA -> Color_RGBA.create();
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance allocated with {@link BufferUtils}.
     */
    public static @NotNull Color create()
    {
        return create(ColorFormat.DEFAULT);
    }
    
    /**
     * Returns a new {@link Color} instance for the specified memory address.
     */
    public static @NotNull Color create(@NotNull ColorFormat format, long address)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.create(address);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.create(address);
                    case RGB -> Color_RGB.create(address);
                    case RGBA -> Color_RGBA.create(address);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance for the specified memory address.
     */
    public static @NotNull Color create(long address)
    {
        return create(ColorFormat.DEFAULT);
    }
    
    /**
     * Returns a new {@link Color} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color createSafe(@NotNull ColorFormat format, long address)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.createSafe(address);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.createSafe(address);
                    case RGB -> Color_RGB.createSafe(address);
                    case RGBA -> Color_RGBA.createSafe(address);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color createSafe(long address)
    {
        return createSafe(ColorFormat.DEFAULT, address);
    }
    
    /**
     * Returns a new {@link Color} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color malloc(@NotNull ColorFormat format)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.malloc();
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.malloc();
                    case RGB -> Color_RGB.malloc();
                    case RGBA -> Color_RGBA.malloc();
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color malloc()
    {
        return malloc(ColorFormat.DEFAULT);
    }
    
    /**
     * Returns a new {@link Color} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color malloc(@NotNull ColorFormat format, @NotNull MemoryStack stack)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.malloc(stack);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.malloc(stack);
                    case RGB -> Color_RGB.malloc(stack);
                    case RGBA -> Color_RGBA.malloc(stack);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color malloc(@NotNull MemoryStack stack)
    {
        return malloc(ColorFormat.DEFAULT, stack);
    }
    
    /**
     * Returns a new {@link Color} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color calloc(@NotNull ColorFormat format)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.calloc();
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.calloc();
                    case RGB -> Color_RGB.calloc();
                    case RGBA -> Color_RGBA.calloc();
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     */
    public static @NotNull Color calloc()
    {
        return calloc(ColorFormat.DEFAULT);
    }
    
    /**
     * Returns a new {@link Color} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color calloc(@NotNull ColorFormat format, @NotNull MemoryStack stack)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.calloc(stack);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.calloc(stack);
                    case RGB -> Color_RGB.calloc(stack);
                    case RGBA -> Color_RGBA.calloc(stack);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Color} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack the stack from which to allocate
     */
    public static @NotNull Color calloc(@NotNull MemoryStack stack)
    {
        return calloc(ColorFormat.DEFAULT, stack);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param format   the color format
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer create(@NotNull ColorFormat format, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.create(capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.create(capacity);
                    case RGB -> Color_RGB.create(capacity);
                    case RGBA -> Color_RGBA.create(capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link BufferUtils}.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer create(int capacity)
    {
        return create(ColorFormat.DEFAULT, capacity);
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param format   the color format
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer create(@NotNull ColorFormat format, long address, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.create(address, capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.create(address, capacity);
                    case RGB -> Color_RGB.create(address, capacity);
                    case RGBA -> Color_RGBA.create(address, capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Create a {@link Buffer} instance at the specified memory.
     *
     * @param address  the memory address
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer create(long address, int capacity)
    {
        return create(ColorFormat.DEFAULT, address, capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color.Buffer createSafe(@NotNull ColorFormat format, long address, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.createSafe(address, capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.createSafe(address, capacity);
                    case RGB -> Color_RGB.createSafe(address, capacity);
                    case RGBA -> Color_RGBA.createSafe(address, capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance for the specified memory address or {@code null} if {@code address} is {@link MemoryUtil#NULL NULL}.
     */
    public static @Nullable Color.Buffer createSafe(long address, int capacity)
    {
        return createSafe(ColorFormat.DEFAULT, address, capacity);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param format    the color format
     * @param container The data buffer to wrap.
     */
    public static @NotNull Color.Buffer wrap(@NotNull ColorFormat format, @NotNull ByteBuffer container)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.wrap(container);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.wrap(container);
                    case RGB -> Color_RGB.wrap(container);
                    case RGBA -> Color_RGBA.wrap(container);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @NotNull Color.Buffer wrap(@NotNull ByteBuffer container)
    {
        return wrap(ColorFormat.DEFAULT, container);
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param format    the color format
     * @param container The data buffer to wrap.
     */
    public static @Nullable Color.Buffer wrapSafe(@NotNull ColorFormat format, @Nullable ByteBuffer container)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.wrapSafe(container);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.wrapSafe(container);
                    case RGB -> Color_RGB.wrapSafe(container);
                    case RGBA -> Color_RGBA.wrapSafe(container);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Wraps a {@link ByteBuffer} instance in a {@link Buffer}
     *
     * @param container The data buffer to wrap.
     */
    public static @Nullable Color.Buffer wrapSafe(@Nullable ByteBuffer container)
    {
        return wrapSafe(ColorFormat.DEFAULT, container);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param format   the color format
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer malloc(@NotNull ColorFormat format, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.malloc(capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.malloc(capacity);
                    case RGB -> Color_RGB.malloc(capacity);
                    case RGBA -> Color_RGBA.malloc(capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memAlloc memAlloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer malloc(int capacity)
    {
        return malloc(ColorFormat.DEFAULT, capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param format   the color format
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer malloc(@NotNull ColorFormat format, int capacity, @NotNull MemoryStack stack)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.malloc(capacity, stack);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.malloc(capacity, stack);
                    case RGB -> Color_RGB.malloc(capacity, stack);
                    case RGBA -> Color_RGBA.malloc(capacity, stack);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack}.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer malloc(int capacity, @NotNull MemoryStack stack)
    {
        return malloc(ColorFormat.DEFAULT, capacity, stack);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param format   the color format
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer calloc(@NotNull ColorFormat format, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.calloc(capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.calloc(capacity);
                    case RGB -> Color_RGB.calloc(capacity);
                    case RGBA -> Color_RGBA.calloc(capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated with {@link MemoryUtil#memCalloc memCalloc}. The instance must be explicitly freed.
     *
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer calloc(int capacity)
    {
        return calloc(ColorFormat.DEFAULT, capacity);
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param format   the color format
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer calloc(@NotNull ColorFormat format, int capacity, @NotNull MemoryStack stack)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.calloc(capacity, stack);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.calloc(capacity, stack);
                    case RGB -> Color_RGB.calloc(capacity, stack);
                    case RGBA -> Color_RGBA.calloc(capacity, stack);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    /**
     * Returns a new {@link Buffer} instance allocated on the specified {@link MemoryStack} and initializes all its bits to zero.
     *
     * @param stack    the stack from which to allocate
     * @param capacity the buffer capacity
     */
    public static @NotNull Color.Buffer calloc(int capacity, @NotNull MemoryStack stack)
    {
        return calloc(ColorFormat.DEFAULT, capacity, stack);
    }
    
    public static @NotNull Color.Buffer realloc(@NotNull ColorFormat format, @Nullable Color.Buffer ptr, int capacity)
    {
        return switch (format)
                {
                    case GRAY -> Color_GRAY.realloc(ptr, capacity);
                    case GRAY_ALPHA -> Color_GRAY_ALPHA.realloc(ptr, capacity);
                    case RGB -> Color_RGB.realloc(ptr, capacity);
                    case RGBA -> Color_RGBA.realloc(ptr, capacity);
                    default -> throw new UnsupportedOperationException("Invalid ColorFormat: " + format);
                };
    }
    
    public static @NotNull Color.Buffer realloc(@Nullable Color.Buffer ptr, int capacity)
    {
        return realloc(ColorFormat.DEFAULT, ptr, capacity);
    }
    
    // ---------- Instance ---------- //
    
    public Color(long address, @Nullable ByteBuffer container)
    {
        super(address, container);
    }
    
    /**
     * Sets the values of this colors {@code red} channel.
     *
     * @param value red channel value [0-255]
     * @return this
     */
    public abstract @NotNull Color r(int value);
    
    /**
     * Sets the values of this colors {@code green} channel.
     *
     * @param value green channel value [0-255]
     * @return this
     */
    public abstract @NotNull Color g(int value);
    
    /**
     * Sets the values of this colors {@code blue} channel.
     *
     * @param value blue channel value [0-255]
     * @return this
     */
    public abstract @NotNull Color b(int value);
    
    /**
     * Sets the values of this colors {@code alpha} channel.
     *
     * @param value alpha channel value [0-255]
     * @return this
     */
    public abstract @NotNull Color a(int value);
    
    /**
     * Sets the values of this colors {@code red} channel.
     *
     * @param value red channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color rf(double value)
    {
        return r((int) (value * 255));
    }
    
    /**
     * Sets the values of this colors {@code green} channel.
     *
     * @param value green channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color gf(double value)
    {
        return g((int) (value * 255));
    }
    
    /**
     * Sets the values of this colors {@code blue} channel.
     *
     * @param value blue channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color bf(double value)
    {
        return b((int) (value * 255));
    }
    
    /**
     * Sets the values of this colors {@code alpha} channel.
     *
     * @param value alpha channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color af(double value)
    {
        return a((int) (value * 255));
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param gray red, green, and blue channels value [0-255]
     * @return this
     */
    public @NotNull Color set(int gray)
    {
        return set(gray, gray, gray, 255);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param gray  red, green, and blue channels value [0-255]
     * @param alpha alpha channel value [0-255]
     * @return this
     */
    public @NotNull Color set(int gray, int alpha)
    {
        return set(gray, gray, gray, alpha);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param r red channel value [0-255]
     * @param g green channel value [0-255]
     * @param b blue channel value [0-255]
     * @return this
     */
    public @NotNull Color set(int r, int g, int b)
    {
        return set(r, g, b, 255);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param r red channel value [0-255]
     * @param g green channel value [0-255]
     * @param b blue channel value [0-255]
     * @param a alpha channel value [0-255]
     * @return this
     */
    public @NotNull Color set(int r, int g, int b, int a)
    {
        return r(r).g(g).b(b).a(a);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param gray red, green, and blue channels value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color set(double gray)
    {
        return set(gray, gray, gray, 1.0);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param gray  red, green, and blue channels value [0.0F-1.0F]
     * @param alpha alpha channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color set(double gray, double alpha)
    {
        return set(gray, gray, gray, alpha);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param r red channel value [0.0F-1.0F]
     * @param g green channel value [0.0F-1.0F]
     * @param b blue channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color set(double r, double g, double b)
    {
        return set(r, g, b, 1.0);
    }
    
    /**
     * Sets the values of this color to provided data.
     *
     * @param r red channel value [0.0F-1.0F]
     * @param g green channel value [0.0F-1.0F]
     * @param b blue channel value [0.0F-1.0F]
     * @param a alpha channel value [0.0F-1.0F]
     * @return this
     */
    public @NotNull Color set(double r, double g, double b, double a)
    {
        return set((int) (r * 255), (int) (g * 255), (int) (b * 255), (int) (a * 255));
    }
    
    /**
     * Copies the specified color data to this color.
     *
     * @param color the source color
     * @return this
     */
    public @NotNull Color set(@NotNull Colorc color)
    {
        if (color instanceof Color c && color.getClass() == getClass())
        {
            memCopy(c.address, this.address, sizeof());
            return this;
        }
        return set(color.r(), color.g(), color.b(), color.a());
    }
    
    /**
     * Sets the values of this color from a 32-bit integer (argb)
     *
     * @param value The integer value
     * @return this
     */
    public @NotNull Color setFromInt(int value)
    {
        int r = (value >>> 16) & 0xFF;
        int g = (value >>> 8) & 0xFF;
        int b = value & 0xFF;
        int a = (value >>> 24) & 0xFF;
        
        return set(r, g, b, a);
    }
    
    /**
     * Sets this color from normalized values. [{@code 0.0 - 1.0}]
     *
     * @param normalized The normalized values.
     * @return this
     */
    public @NotNull Color setFromNormalized(@NotNull Vector4fc normalized)
    {
        return set(normalized.x(), normalized.y(), normalized.z(), normalized.w());
    }
    
    /**
     * Sets this color from normalized values. [{@code 0.0 - 1.0}]
     *
     * @param normalized The normalized values.
     * @return this
     */
    public @NotNull Color setFromNormalized(@NotNull Vector4dc normalized)
    {
        return set(normalized.x(), normalized.y(), normalized.z(), normalized.w());
    }
    
    /**
     * Set the values of this color from HSV values.
     *
     * @param hue        The hue value [{@code 0.0 - 360.0}]
     * @param saturation The saturation value [{@code 0.0 - 1.0}]
     * @param value      The value value [{@code 0.0 - 1.0}]
     * @return this
     * @see <a href="https://en.wikipedia.org/wiki/HSL_and_HSV#Alternative_HSV_conversion">Implementation Reference</a>
     */
    public @NotNull Color setFromHSB(double hue, double saturation, double value)
    {
        if (hue < 0) hue = 0;
        if (hue > 360) hue = 360;
        if (saturation < 0) saturation = 0;
        if (saturation > 1) saturation = 1;
        if (value < 0) value = 0;
        if (value > 1) value = 1;
        
        double k;
        
        // Red channel
        k = (5.0 + hue / 60.0) % 6.0;
        k = Math.min(4.0 - k, k);
        k = k > 1 ? 1 : k < 0 ? 0 : k;
        float r = (float) (value - value * saturation * k);
        
        // Green channel
        k = (3.0 + hue / 60.0) % 6.0;
        k = Math.min(4.0 - k, k);
        k = k > 1 ? 1 : k < 0 ? 0 : k;
        float g = (float) (value - value * saturation * k);
        
        // Blue channel
        k = (1.0 + hue / 60.0) % 6.0;
        k = Math.min(4.0 - k, k);
        k = k > 1 ? 1 : k < 0 ? 0 : k;
        float b = (float) (value - value * saturation * k);
        
        return set(r, g, b, af());
    }
    
    /**
     * Creates a new color with the same format and values as this color.
     *
     * @return The new color
     */
    public @NotNull Color copy()
    {
        return Color.create(format()).set(this);
    }
    
    /**
     * Tints this color.
     *
     * @param color The color to tint this color by.
     * @return {@code this}
     */
    public @NotNull Color tint(@NotNull Colorc color)
    {
        return tint(color, this);
    }
    
    /**
     * Converts this color to grayscale.
     *
     * @return {@code this}
     */
    public @NotNull Color grayscale()
    {
        return grayscale(this);
    }
    
    /**
     * Changes the brightness of this color by a specified amount.
     *
     * @param brightness The amount to change the brightness by [{@code -255 - +255}]
     * @return {@code this}
     */
    public @NotNull Color brightness(int brightness)
    {
        return brightness(brightness, this);
    }
    
    /**
     * Changes the brightness of this color by a specified amount.
     *
     * @param brightness The amount to change the brightness by [{@code -1.0 - +1.0}]
     * @return {@code this}
     */
    public @NotNull Color brightness(double brightness)
    {
        return brightness(brightness, this);
    }
    
    /**
     * Changes the contrast of this color by a specified amount.
     *
     * @param contrast The amount to change the contrast by [{@code -255 - +255}]
     * @return {@code this}
     */
    public @NotNull Color contrast(int contrast)
    {
        return contrast(contrast, this);
    }
    
    /**
     * Changes this contrast of the color by a specified amount.
     *
     * @param contrast The amount to change the contrast by [{@code -1.0 - +1.0}]
     * @return {@code this}
     */
    public @NotNull Color contrast(double contrast)
    {
        return contrast(contrast, this);
    }
    
    /**
     * Changes the gamma of this color by a specified amount.
     *
     * @param gamma The gamma value.
     * @return {@code this}
     */
    public @NotNull Color gamma(double gamma)
    {
        return gamma(gamma, this);
    }
    
    /**
     * Inverts this color.
     *
     * @return {@code this}
     */
    public @NotNull Color invert()
    {
        return invert(this);
    }
    
    /**
     * Makes this color brighter by a percentage and stores the result in
     * {@code out}.
     *
     * @param percentage the percentage to make the color brighter [{@code 0.0 - 1.0}]
     * @return this
     */
    public @NotNull Color brighter(double percentage)
    {
        return brighter(percentage, this);
    }
    
    /**
     * Makes this color darker by a percentage and stores the result in
     * {@code out}.
     *
     * @param percentage the percentage to make the color darker [{@code 0.0 - 1.0}]
     * @return this
     */
    public @NotNull Color darker(double percentage)
    {
        return darker(percentage, this);
    }
    
    /**
     * Linear interpolates this color with {@code other} by a specified amount.
     *
     * @param other  The other color
     * @param amount The amount to interpolate [{@code 0.0 - 1.0}]
     * @return this
     */
    public @NotNull Color interpolate(@NotNull Colorc other, double amount)
    {
        return interpolate(other, amount, this);
    }
    
    /**
     * Blends this color with the src color according to the {@link BlendMode}
     * and stores the result in {@code out}.
     *
     * @param src       The src color.
     * @param blendMode The blendMode mode.
     * @return this
     */
    public @NotNull Color blend(@NotNull Colorc src, @NotNull BlendMode blendMode)
    {
        return blend(src, blendMode, this);
    }
    
    /**
     * Blends this color with the src color using
     * {@link BlendMode#DEFAULT DEFAULT}.
     *
     * @param src The src color.
     * @return this
     */
    public @NotNull Color blend(@NotNull Colorc src)
    {
        return blend(src, this);
    }
    
    public @NotNull ByteBuffer toBuffer()
    {
        return MemoryUtil.memByteBuffer(address(), sizeof());
    }
    
    public abstract static class Buffer extends StructBuffer<Color, Buffer>
    {
        protected Buffer(ByteBuffer container, int remaining)
        {
            super(container, remaining);
        }
        
        protected Buffer(long address, int capacity)
        {
            super(address, null, -1, 0, capacity, capacity);
        }
        
        public ByteBuffer toBuffer()
        {
            return MemoryUtil.memByteBuffer(this);
        }
        
        public @NotNull ColorFormat format()
        {
            return getElementFactory().format();
        }
        
        @Override
        public @NotNull Color.Buffer get(@NotNull Color value)
        {
            if (getElementFactory().getClass() == value.getClass()) return super.get(value);
            value.set(get());
            return this;
        }
        
        @Override
        public @NotNull Color.Buffer put(@NotNull Color value)
        {
            if (getElementFactory().getClass() == value.getClass()) return super.put(value);
            get().set(value);
            return this;
        }
        
        @Override
        public @NotNull Color.Buffer get(int index, @NotNull Color value)
        {
            if (getElementFactory().getClass() == value.getClass()) return super.get(index, value);
            value.set(get(index));
            return this;
        }
        
        @Override
        public @NotNull Color.Buffer put(int index, @NotNull Color value)
        {
            if (getElementFactory().getClass() == value.getClass()) return super.put(index, value);
            get(index).set(value);
            return this;
        }
        
        @Override
        public @NotNull Color.Buffer put(Buffer src)
        {
            if (getClass() == src.getClass()) return super.put(src);
            
            src.mark();
            for (int i = 0, n = src.remaining(); i < n; i++) put(src.get());
            src.reset();
            
            return this;
        }
        
        public @NotNull Color.Buffer put(int index, int gray)
        {
            get(index).set(gray);
            return this;
        }
        
        public @NotNull Color.Buffer put(int index, int gray, int alpha)
        {
            get(index).set(gray, alpha);
            return this;
        }
        
        public @NotNull Color.Buffer put(int index, int r, int g, int b)
        {
            get(index).set(r, g, b);
            return this;
        }
        
        public @NotNull Color.Buffer put(int index, int r, int g, int b, int a)
        {
            get(index).set(r, g, b, a);
            return this;
        }
        
        public @NotNull Color.Buffer put(int index, Colorc color)
        {
            get(index).set(color);
            return this;
        }
        
        public @NotNull Color.Buffer put(Colorc value)
        {
            get().set(value);
            return this;
        }
        
        public @NotNull Color.Buffer fill(Colorc color)
        {
            forEach(c -> c.set(color));
            return this;
        }
        
        public @NotNull Color.Buffer copy(ColorFormat format)
        {
            Buffer copy = malloc(format, remaining());
            if (getClass() == copy.getClass())
            {
                long bytes = Integer.toUnsignedLong(remaining()) * sizeof();
                memCopy(address(), copy.address(), bytes);
                return copy;
            }
            return copy.put(this).clear();
        }
    }
}
