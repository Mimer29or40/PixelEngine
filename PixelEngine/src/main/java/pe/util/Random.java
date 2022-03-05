package pe.util;

import pe.color.Color;
import pe.color.ColorFormat;

public class Random extends rutils.joml.Random
{
    public Random()
    {
        super();
    }
    
    public Random(long seed)
    {
        super(seed);
    }
    
    /**
     * Randomizes a {@code Color}'s values.
     *
     * @param lower The lower value in the range
     * @param upper The upper value in the range
     * @param alpha If alpha should be randomized too.
     * @param out   The color instance to set the random values to.
     * @return out
     */
    public Color nextColor(int lower, int upper, boolean alpha, Color out)
    {
        out.r(nextInt(lower, upper));
        out.g(nextInt(lower, upper));
        out.b(nextInt(lower, upper));
        if (alpha) out.a(nextInt(lower, upper));
        return out;
    }
    
    /**
     * Randomizes a {@code Color}'s values, except for its alpha value.
     *
     * @param lower The lower value in the range
     * @param upper The upper value in the range
     * @param out   The color instance to set the random values to.
     * @return out
     */
    public Color nextColor(int lower, int upper, Color out)
    {
        return nextColor(lower, upper, false, out);
    }
    
    /**
     * Creates a new random {@code Color}.
     *
     * @param lower The lower value in the range
     * @param upper The upper value in the range
     * @param alpha If alpha should be randomized too.
     * @return A new random {@code Color} instance.
     */
    public Color nextColor(int lower, int upper, boolean alpha)
    {
        return nextColor(lower, upper, alpha, Color.create(ColorFormat.RGBA));
    }
    
    /**
     * Creates a new random {@code Color}.
     *
     * @param lower The lower value in the range
     * @param upper The upper value in the range
     * @return A new random {@code Color} instance.
     */
    public Color nextColor(int lower, int upper)
    {
        return nextColor(lower, upper, false, Color.create(ColorFormat.RGBA));
    }
    
    /**
     * Randomizes a {@code Color}'s values, except for its alpha value, from [{@code 0} - {@code upper}.
     *
     * @param upper The upper value in the range
     * @param out   The color instance to set the random values to.
     * @return out
     */
    public Color nextColor(int upper, Color out)
    {
        return nextColor(0, upper, out);
    }
    
    /**
     * Creates a new random {@code Color} from [{@code 0} - {@code upper}.
     *
     * @param upper The upper value in the range
     * @param alpha If alpha should be randomized too.
     * @return A new random {@code Color} instance.
     */
    public Color nextColor(int upper, boolean alpha)
    {
        return nextColor(0, upper, alpha, Color.create(ColorFormat.RGBA));
    }
    
    /**
     * Creates a new random {@code Color} from [{@code 0} - {@code upper}.
     *
     * @param upper The upper value in the range
     * @return A new random {@code Color} instance.
     */
    public Color nextColor(int upper)
    {
        return nextColor(0, upper, Color.create(ColorFormat.RGBA));
    }
    
    /**
     * Randomizes a {@code Color}'s values, except for its alpha value, from [{@code 0} - {@code 255}.
     *
     * @param out The color instance to set the random values to.
     * @return out
     */
    public Color nextColor(Color out)
    {
        return nextColor(0, 255, out);
    }
    
    /**
     * Creates a new random {@code Color} from [{@code 0} - {@code 255}.
     *
     * @param alpha If alpha should be randomized too.
     * @return A new random {@code Color} instance.
     */
    public Color nextColor(boolean alpha)
    {
        return nextColor(0, 255, alpha, Color.create(ColorFormat.RGBA));
    }
    
    /**
     * Creates a new random {@code Color} from [{@code 0} - {@code 255}.
     *
     * @return A new random {@code Color} instance.
     */
    public Color nextColor()
    {
        return nextColor(0, 255, false, Color.create(ColorFormat.RGBA));
    }
}
