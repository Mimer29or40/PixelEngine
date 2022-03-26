package pe.font;

import org.jetbrains.annotations.NotNull;
import rutils.Math;

@SuppressWarnings("unused")
public class TextFormat
{
    static final char MODIFIER  = '¶';
    static final char SEPARATOR = '-';
    
    static final        String RESET     = "0";
    public static final String RESET_ALL = MODIFIER + RESET + SEPARATOR + RESET + MODIFIER;
    
    static final        String WEIGHT             = "1";
    public static final String WEIGHT_THIN        = MODIFIER + WEIGHT + SEPARATOR + Weight.THIN.tag() + MODIFIER;
    public static final String WEIGHT_EXTRA_LIGHT = MODIFIER + WEIGHT + SEPARATOR + Weight.EXTRA_LIGHT.tag() + MODIFIER;
    public static final String WEIGHT_LIGHT       = MODIFIER + WEIGHT + SEPARATOR + Weight.LIGHT.tag() + MODIFIER;
    public static final String WEIGHT_REGULAR     = MODIFIER + WEIGHT + SEPARATOR + Weight.REGULAR.tag() + MODIFIER;
    public static final String WEIGHT_MEDIUM      = MODIFIER + WEIGHT + SEPARATOR + Weight.MEDIUM.tag() + MODIFIER;
    public static final String WEIGHT_SEMI_BOLD   = MODIFIER + WEIGHT + SEPARATOR + Weight.SEMI_BOLD.tag() + MODIFIER;
    public static final String WEIGHT_BOLD        = MODIFIER + WEIGHT + SEPARATOR + Weight.BOLD.tag() + MODIFIER;
    public static final String WEIGHT_EXTRA_BOLD  = MODIFIER + WEIGHT + SEPARATOR + Weight.EXTRA_BOLD.tag() + MODIFIER;
    public static final String WEIGHT_BLACK       = MODIFIER + WEIGHT + SEPARATOR + Weight.BLACK.tag() + MODIFIER;
    public static final String WEIGHT_RESET       = WEIGHT_REGULAR;
    
    static final        String ITALICS       = "2";
    public static final String ITALICS_OFF   = MODIFIER + ITALICS + SEPARATOR + false + MODIFIER;
    public static final String ITALICS_ON    = MODIFIER + ITALICS + SEPARATOR + true + MODIFIER;
    public static final String ITALICS_RESET = ITALICS_OFF;
    
    static final        String UNDERLINE       = "3";
    public static final String UNDERLINE_OFF   = MODIFIER + UNDERLINE + SEPARATOR + false + MODIFIER;
    public static final String UNDERLINE_ON    = MODIFIER + UNDERLINE + SEPARATOR + true + MODIFIER;
    public static final String UNDERLINE_RESET = UNDERLINE_OFF;
    
    static final        String STRIKE       = "4";
    public static final String STRIKE_OFF   = MODIFIER + STRIKE + SEPARATOR + false + MODIFIER;
    public static final String STRIKE_ON    = MODIFIER + STRIKE + SEPARATOR + true + MODIFIER;
    public static final String STRIKE_RESET = STRIKE_OFF;
    
    private static final String _WHITE      = "F9FFFF";
    private static final String _LIGHT_GRAY = "9C9D97";
    private static final String _GRAY       = "474F52";
    private static final String _BLACK      = "1D1C21";
    private static final String _RED        = "B02E26";
    private static final String _ORANGE     = "F9801D";
    private static final String _YELLOW     = "FFD83D";
    private static final String _GREEN      = "5D7C15";
    private static final String _BLUE       = "3C44A9";
    private static final String _PURPLE     = "8932B7";
    private static final String _BROWN      = "825432";
    
    private static final String _ALPHA_0   = "00";
    private static final String _ALPHA_25  = "3F";
    private static final String _ALPHA_50  = "7F";
    private static final String _ALPHA_75  = "BF";
    private static final String _ALPHA_100 = "FF";
    
    static final        String COLOR            = "5";
    public static final String COLOR_WHITE      = MODIFIER + COLOR + SEPARATOR + _WHITE + MODIFIER;
    public static final String COLOR_LIGHT_GRAY = MODIFIER + COLOR + SEPARATOR + _LIGHT_GRAY + MODIFIER;
    public static final String COLOR_GRAY       = MODIFIER + COLOR + SEPARATOR + _GRAY + MODIFIER;
    public static final String COLOR_BLACK      = MODIFIER + COLOR + SEPARATOR + _BLACK + MODIFIER;
    public static final String COLOR_RED        = MODIFIER + COLOR + SEPARATOR + _RED + MODIFIER;
    public static final String COLOR_ORANGE     = MODIFIER + COLOR + SEPARATOR + _ORANGE + MODIFIER;
    public static final String COLOR_YELLOW     = MODIFIER + COLOR + SEPARATOR + _YELLOW + MODIFIER;
    public static final String COLOR_GREEN      = MODIFIER + COLOR + SEPARATOR + _GREEN + MODIFIER;
    public static final String COLOR_BLUE       = MODIFIER + COLOR + SEPARATOR + _BLUE + MODIFIER;
    public static final String COLOR_PURPLE     = MODIFIER + COLOR + SEPARATOR + _PURPLE + MODIFIER;
    public static final String COLOR_BROWN      = MODIFIER + COLOR + SEPARATOR + _BROWN + MODIFIER;
    public static final String COLOR_RESET      = COLOR_WHITE;
    
    static final        String COLOR_ALPHA       = "6";
    public static final String COLOR_ALPHA_0     = MODIFIER + COLOR_ALPHA + SEPARATOR + _ALPHA_0 + MODIFIER;
    public static final String COLOR_ALPHA_25    = MODIFIER + COLOR_ALPHA + SEPARATOR + _ALPHA_25 + MODIFIER;
    public static final String COLOR_ALPHA_50    = MODIFIER + COLOR_ALPHA + SEPARATOR + _ALPHA_50 + MODIFIER;
    public static final String COLOR_ALPHA_75    = MODIFIER + COLOR_ALPHA + SEPARATOR + _ALPHA_75 + MODIFIER;
    public static final String COLOR_ALPHA_100   = MODIFIER + COLOR_ALPHA + SEPARATOR + _ALPHA_100 + MODIFIER;
    public static final String COLOR_ALPHA_RESET = COLOR_ALPHA_100;
    
    static final        String BACKGROUND            = "7";
    public static final String BACKGROUND_WHITE      = MODIFIER + BACKGROUND + SEPARATOR + _WHITE + MODIFIER;
    public static final String BACKGROUND_LIGHT_GRAY = MODIFIER + BACKGROUND + SEPARATOR + _LIGHT_GRAY + MODIFIER;
    public static final String BACKGROUND_GRAY       = MODIFIER + BACKGROUND + SEPARATOR + _GRAY + MODIFIER;
    public static final String BACKGROUND_BLACK      = MODIFIER + BACKGROUND + SEPARATOR + _BLACK + MODIFIER;
    public static final String BACKGROUND_RED        = MODIFIER + BACKGROUND + SEPARATOR + _RED + MODIFIER;
    public static final String BACKGROUND_ORANGE     = MODIFIER + BACKGROUND + SEPARATOR + _ORANGE + MODIFIER;
    public static final String BACKGROUND_YELLOW     = MODIFIER + BACKGROUND + SEPARATOR + _YELLOW + MODIFIER;
    public static final String BACKGROUND_GREEN      = MODIFIER + BACKGROUND + SEPARATOR + _GREEN + MODIFIER;
    public static final String BACKGROUND_BLUE       = MODIFIER + BACKGROUND + SEPARATOR + _BLUE + MODIFIER;
    public static final String BACKGROUND_PURPLE     = MODIFIER + BACKGROUND + SEPARATOR + _PURPLE + MODIFIER;
    public static final String BACKGROUND_BROWN      = MODIFIER + BACKGROUND + SEPARATOR + _BROWN + MODIFIER;
    public static final String BACKGROUND_RESET      = BACKGROUND_BLACK;
    
    static final        String BACKGROUND_ALPHA       = "8";
    public static final String BACKGROUND_ALPHA_0     = MODIFIER + BACKGROUND_ALPHA + SEPARATOR + _ALPHA_0 + MODIFIER;
    public static final String BACKGROUND_ALPHA_25    = MODIFIER + BACKGROUND_ALPHA + SEPARATOR + _ALPHA_25 + MODIFIER;
    public static final String BACKGROUND_ALPHA_50    = MODIFIER + BACKGROUND_ALPHA + SEPARATOR + _ALPHA_50 + MODIFIER;
    public static final String BACKGROUND_ALPHA_75    = MODIFIER + BACKGROUND_ALPHA + SEPARATOR + _ALPHA_75 + MODIFIER;
    public static final String BACKGROUND_ALPHA_100   = MODIFIER + BACKGROUND_ALPHA + SEPARATOR + _ALPHA_100 + MODIFIER;
    public static final String BACKGROUND_ALPHA_RESET = BACKGROUND_ALPHA_0;
    
    private static String color(String prefix, int r, int g, int b)
    {
        return String.format(MODIFIER + prefix + SEPARATOR + "%02X%02X%02X" + MODIFIER,
                             Math.clamp(r, 0, 255),
                             Math.clamp(g, 0, 255),
                             Math.clamp(b, 0, 255));
    }
    
    private static String alpha(String prefix, int a)
    {
        return String.format(MODIFIER + prefix + SEPARATOR + "%02X" + MODIFIER,
                             Math.clamp(a, 0, 255));
    }
    
    /**
     * Generates a string that will change the rendered text to the specified rgba value.
     *
     * @param r The r value of the color.
     * @param g The g value of the color.
     * @param b The b value of the color.
     * @param a The alpha value of the color.
     * @return The modifier string.
     */
    public static @NotNull String color(int r, int g, int b, int a)
    {
        return color(r, g, b) + alpha(a);
    }
    
    /**
     * Generates a string that will change the rendered text to the specified rgb value.
     *
     * @param r The r value of the color.
     * @param g The g value of the color.
     * @param b The b value of the color.
     * @return The modifier string.
     */
    public static @NotNull String color(int r, int g, int b)
    {
        return color(COLOR, r, g, b);
    }
    
    /**
     * Generates a string that will change the rendered text alpha value to the specified value.
     *
     * @param a The alpha value of the color.
     * @return The modifier string.
     */
    public static @NotNull String alpha(int a)
    {
        return alpha(COLOR_ALPHA, a);
    }
    
    /**
     * Generates a string that will change the background color to the specified rgba value.
     *
     * @param r The r value of the color.
     * @param g The g value of the color.
     * @param b The b value of the color.
     * @param a The alpha value of the color.
     * @return The modifier string.
     */
    public static @NotNull String background(int r, int g, int b, int a)
    {
        return background(r, g, b) + backgroundAlpha(a);
    }
    
    /**
     * Generates a string that will change the background color to the specified rgb value.
     *
     * @param r The r value of the color.
     * @param g The g value of the color.
     * @param b The b value of the color.
     * @return The modifier string.
     */
    public static @NotNull String background(int r, int g, int b)
    {
        return color(BACKGROUND, r, g, b);
    }
    
    /**
     * Generates a string that will change the background alpha value to the specified value.
     *
     * @param a The alpha value of the color.
     * @return The modifier string.
     */
    public static @NotNull String backgroundAlpha(int a)
    {
        return alpha(BACKGROUND_ALPHA, a);
    }
    
    /**
     * Strips the formatting codes from a string.
     *
     * @param string The string with formatting.
     * @return The stripped string.
     */
    public static @NotNull String stripFormatting(@NotNull CharSequence string)
    {
        StringBuilder builder = new StringBuilder();
        
        boolean inModifier = false;
        for (int i = 0, n = string.length(); i < n; i++)
        {
            char character = string.charAt(i);
            if (character == MODIFIER)
            {
                inModifier = !inModifier;
                continue;
            }
            if (inModifier) continue;
            builder.append(character);
        }
        
        return builder.toString();
    }
}
