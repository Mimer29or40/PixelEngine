package pe.engine.color;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector4d;
import pe.engine.render.BlendMode;

public interface Colorc
{
    /**
     * @return The size in bytes of the color object.
     */
    int sizeof();
    
    /**
     * @return The {@link ColorFormat} of the Color.
     */
    @NotNull ColorFormat format();
    
    /**
     * @return {@code int} value of the {@code red} channel. [0-255]
     */
    int r();
    
    /**
     * @return {@code int} value of the {@code green} channel. [0-255]
     */
    int g();
    
    /**
     * @return {@code int} value of the {@code blue} channel. [0-255]
     */
    int b();
    
    /**
     * @return {@code int} value of the {@code alpha} channel. [0-255]
     */
    int a();
    
    /**
     * @return {@code float} value of the {@code red} channel. [0.0F-1.0F]
     */
    default float rf()
    {
        return r() / 255F;
    }
    
    /**
     * @return {@code float} value of the {@code green} channel. [0.0F-1.0F]
     */
    default float gf()
    {
        return g() / 255F;
    }
    
    /**
     * @return {@code float} value of the {@code blue} channel. [0.0F-1.0F]
     */
    default float bf()
    {
        return b() / 255F;
    }
    
    /**
     * @return {@code float} value of the {@code alpha} channel. [0.0F-1.0F]
     */
    default float af()
    {
        return a() / 255F;
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(g, g, g, 255)</code>
     * and return whether all of them are equal.
     *
     * @param gray the r, g, and b component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(int gray)
    {
        return equals(gray, gray, gray, 255);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(g, g, g, a)</code>
     * and return whether all of them are equal.
     *
     * @param gray the r, g, and b component to compare to
     * @param a    the a component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(int gray, int a)
    {
        return equals(gray, gray, gray, a);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(r, g, b, 255)</code>
     * and return whether all of them are equal.
     *
     * @param r the r component to compare to
     * @param g the g component to compare to
     * @param b the b component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(int r, int g, int b)
    {
        return equals(r, g, b, 255);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(r, g, b, a)</code>
     * and return whether all of them are equal.
     *
     * @param r the r component to compare to
     * @param g the g component to compare to
     * @param b the b component to compare to
     * @param a the a component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(int r, int g, int b, int a)
    {
        return r() == r && g() == g && b() == b && a() == a;
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(g, g, g, 1.0)</code>
     * and return whether all of them are equal.
     *
     * @param gray the r, g, and b component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(double gray)
    {
        return equals(gray, gray, gray, 1.0);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(g, g, g, a)</code>
     * and return whether all of them are equal.
     *
     * @param gray the r, g, and b component to compare to
     * @param a    the a component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(double gray, double a)
    {
        return equals(gray, gray, gray, a);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(r, g, b, 1.0)</code>
     * and return whether all of them are equal.
     *
     * @param r the r component to compare to
     * @param g the g component to compare to
     * @param b the b component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(double r, double g, double b)
    {
        return equals(r, g, b, 1.0);
    }
    
    /**
     * Compare the color components of <code>this</code> color with the given <code>(r, g, b, a)</code>
     * and return whether all of them are equal.
     *
     * @param r the r component to compare to
     * @param g the g component to compare to
     * @param b the b component to compare to
     * @param a the a component to compare to
     * @return <code>true</code> if all the color components are equal
     */
    default boolean equals(double r, double g, double b, double a)
    {
        return Double.compare(rf(), r) == 0 && Double.compare(gf(), g) == 0 && Double.compare(bf(), b) == 0 && Double.compare(af(), a) == 0;
    }
    
    /**
     * Tints this color and stores the result in {@code out}
     *
     * @param color The color to tint this color by.
     * @param out   The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color tint(Colorc color, @NotNull Color out)
    {
        return out.set(r() * color.r() / 255,
                       g() * color.g() / 255,
                       b() * color.b() / 255,
                       a() * color.a() / 255);
    }
    
    /**
     * Converts this color to grayscale and stores the result in {@code out}.
     *
     * @param out The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color grayscale(@NotNull Color out)
    {
        return out.set(Color.toGray(r(), g(), b()), a());
    }
    
    /**
     * Changes the brightness of the color by a specified amount and stores it
     * in {@code out}.
     *
     * @param brightness The amount to change the brightness by [{@code -255 - +255}]
     * @param out        The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color brightness(int brightness, @NotNull Color out)
    {
        if (brightness < -255) brightness = -255;
        if (brightness > 255) brightness = 255;
        
        int r = r() + brightness;
        int g = g() + brightness;
        int b = b() + brightness;
        
        return out.set(r, g, b, a());
    }
    
    /**
     * Changes the brightness of the color by a specified amount and stores it
     * in {@code out}.
     *
     * @param brightness The amount to change the brightness by [{@code -1.0 - +1.0}]
     * @param out        The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color brightness(double brightness, @NotNull Color out)
    {
        if (brightness < -1.0) brightness = -1.0;
        if (brightness > 1.0) brightness = 1.0;
        
        double r = rf() + brightness;
        double g = gf() + brightness;
        double b = bf() + brightness;
        
        return out.set(r, g, b, af());
    }
    
    /**
     * Changes the contrast of the color by a specified amount and stores it in
     * {@code out}.
     *
     * @param contrast The amount to change the contrast by [{@code -255 - +255}]
     * @param out      The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color contrast(int contrast, @NotNull Color out)
    {
        if (contrast < -255) contrast = -255;
        if (contrast > 255) contrast = 255;
        
        double f = (259D * (contrast + 255D)) / (255D * (259D - contrast));
        
        int r = (int) (f * (r() - 128) + 128);
        int g = (int) (f * (g() - 128) + 128);
        int b = (int) (f * (b() - 128) + 128);
        
        return out.set(r, g, b, a());
    }
    
    /**
     * Changes the contrast of the color by a specified amount.
     *
     * @param contrast The amount to change the contrast by [{@code -1.0 - +1.0}]
     * @param out      The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color contrast(double contrast, @NotNull Color out)
    {
        if (contrast < -1.0) contrast = -1.0;
        if (contrast > 1.0) contrast = 1.0;
        
        double f = (contrast + 1.0) / (259.0 - (255.0 * contrast));
        
        double r = f * (rf() - 0.5) + 0.5;
        double g = f * (gf() - 0.5) + 0.5;
        double b = f * (bf() - 0.5) + 0.5;
        
        return out.set(r, g, b, af());
    }
    
    /**
     * Changes the gamma of the color by a specified amount and stores it in
     * {@code out}.
     *
     * @param gamma The gamma value.
     * @param out   The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color gamma(double gamma, @NotNull Color out)
    {
        gamma = 1 / gamma;
        
        double r = Math.pow(rf(), gamma);
        double g = Math.pow(gf(), gamma);
        double b = Math.pow(bf(), gamma);
        
        return out.set(r, g, b, af());
    }
    
    /**
     * Inverts the color and stores it in {@code out}.
     *
     * @param out The color to store the results.
     * @return {@code out}
     */
    default @NotNull Color invert(@NotNull Color out)
    {
        int r = 255 - r();
        int g = 255 - g();
        int b = 255 - b();
        
        return out.set(r, g, b, a());
    }
    
    /**
     * Makes this color brighter by a percentage and stores the result in
     * {@code out}.
     *
     * @param percentage the percentage to make the color brighter [{@code 0.0 - 1.0}]
     * @param out        The color to store the results.
     * @return out
     */
    default @NotNull Color brighter(double percentage, @NotNull Color out)
    {
        if (percentage < 0) return out.set(this);
        if (percentage > 1.0) percentage = 1.0;
        
        // percentage = 1 + percentage * (2 - percentage); // Quadratic
        percentage = (1 + percentage); // Linear
        
        int r = (int) (r() * percentage);
        int g = (int) (g() * percentage);
        int b = (int) (b() * percentage);
        
        return out.set(r, g, b, a());
    }
    
    /**
     * Makes this color darker by a percentage and stores the result in
     * {@code out}.
     *
     * @param percentage the percentage to make the color darker [{@code 0.0 - 1.0}]
     * @param out        The color to store the results.
     * @return out
     */
    default @NotNull Color darker(double percentage, @NotNull Color out)
    {
        if (percentage < 0) return out.set(this);
        if (percentage > 1.0) percentage = 1.0;
        
        // percentage = 1 + percentage * (0.5 * percentage - 1); // Quadratic
        percentage = 0.5 * (2 - percentage); // Linear
        
        int r = (int) (r() * percentage);
        int g = (int) (g() * percentage);
        int b = (int) (b() * percentage);
        
        return out.set(r, g, b, a());
    }
    
    /**
     * Linear interpolates this color with {@code src} by a specified amount and
     * stores the result in {@code out}.
     *
     * @param src    The src color
     * @param amount The amount to interpolate [{@code 0.0 - 1.0}]
     * @param out    The color to store the results.
     * @return out
     */
    default @NotNull Color interpolate(@NotNull Colorc src, double amount, @NotNull Color out)
    {
        if (amount <= 0) return out.set(this);
        if (amount >= 1) return out.set(src);
        
        int f        = (int) (amount * 255);
        int fInverse = 255 - f;
        
        return out.set((r() * fInverse + src.r() * f) / 255,
                       (g() * fInverse + src.g() * f) / 255,
                       (b() * fInverse + src.b() * f) / 255,
                       (a() * fInverse + src.a() * f) / 255);
    }
    
    /**
     * Blends this color with the src color according to the {@link BlendMode}
     * and stores the result in {@code out}.
     *
     * @param src       The src color.
     * @param blendMode The blendMode mode.
     * @param out       The color to store the results.
     * @return out
     */
    default @NotNull Color blend(@NotNull Colorc src, @NotNull BlendMode blendMode, @NotNull Color out)
    {
        if (blendMode == BlendMode.NONE) return out.set(src);
        return blendMode.blend(src, this, out);
    }
    
    /**
     * Blends this color with the src color using
     * {@link BlendMode#DEFAULT DEFAULT} and stores the result in {@code out}.
     *
     * @param src The src color.
     * @param out The color to store the results.
     * @return out
     */
    default @NotNull Color blend(@NotNull Colorc src, @NotNull Color out)
    {
        return BlendMode.DEFAULT.blend(src, this, out);
    }
    
    /**
     * @return The color as a 32-bit integer (argb)
     */
    default int toInt()
    {
        return (a() << 24) | (r() << 16) | (g() << 8) | b();
    }
    
    /**
     * @return The color formatted as a Vector4f (rgba) [{@code 0.0 - 1.0}]
     */
    default @NotNull Vector4d toNormalized()
    {
        return new Vector4d(rf(), gf(), bf(), af());
    }
    
    /**
     * Converts the color to HSV.
     *
     * <li>Hue: [{@code 0.0 - 360.0}]</li>
     * <li>Saturation: [{@code 0.0 - 1.0}]</li>
     * <li>Value: [{@code 0.0 - 1.0}]</li>
     *
     * @return The color formatted to HSV.
     */
    default @NotNull Vector3d toHSV()
    {
        Vector3d hsv = new Vector3d();
        
        float rf = rf();
        float gf = gf();
        float bf = bf();
        
        float min = Math.min(Math.min(rf, gf), bf);
        float max = Math.max(Math.max(rf, gf), bf);
        
        float delta = max - min;
        
        hsv.z = max; // Value
        
        if (max > 0F)
        {
            // NOTE: If max is 0, this divide would cause a crash
            hsv.y = delta / max; // Saturation
        }
        else
        {
            // NOTE: If max is 0, then r = g = b = 0, s = 0, h is undefined
            hsv.x = 0F; // Hue can be any value, defaults to 0
            hsv.y = 0F;
            return hsv;
        }
        
        // NOTE: Comparing float values could not work properly
        if (rf >= max)
        {
            hsv.x = (gf - bf) / delta; // Between yellow & magenta
        }
        else if (gf >= max)
        {
            hsv.x = 2F + (bf - rf) / delta; // Between cyan & yellow
        }
        else
        {
            hsv.x = 4F + (rf - gf) / delta; // Between magenta & cyan
        }
        
        hsv.x *= 60F; // Convert to degrees
        
        if (hsv.x < 0F) hsv.x += 360F;
        
        return hsv;
    }
}
