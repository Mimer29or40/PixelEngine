package pe.debug;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.Debug;
import pe.color.Colorc;

public abstract class Window
{
    public final String name;
    
    protected int headerHeight;
    protected int width, height;
    
    public Window(@NotNull String name)
    {
        this.name = name;
    }
    
    protected void draw()
    {
        this.width  = pe.Window.get().framebufferWidth();
        this.height = pe.Window.get().framebufferHeight() - this.headerHeight;
        drawImpl();
    }
    
    protected abstract void drawImpl();
    
    /**
     * Draws a colored quad to the screen.
     *
     * @param x      The x coordinate of the top left point if the quad.
     * @param y      The y coordinate of the top left point if the quad.
     * @param width  The width of the quad.
     * @param height The height of the quad.
     * @param color  The color of the quad.
     */
    protected void drawQuad(int x, int y, int width, int height, @NotNull Colorc color)
    {
        Debug.drawQuad(x, y + this.headerHeight, width, height, color);
    }
    
    /**
     * Draws Debug text to the screen.
     *
     * @param x     The x coordinate of the top left point if the text.
     * @param y     The y coordinate of the top left point if the text.
     * @param text  The text to render.
     * @param color The color of the text.
     */
    protected void drawText(int x, int y, String text, @NotNull Colorc color)
    {
        Debug.drawText(x, y + this.headerHeight, text, color);
    }
    
    /**
     * Draws Debug text to the screen with a background.
     *
     * @param x         The x coordinate of the top left point if the text.
     * @param y         The y coordinate of the top left point if the text.
     * @param text      The text to render.
     * @param color     The color of the text.
     * @param backColor The color of the background.
     */
    protected void drawTextWithBackground(int x, int y, @NotNull String text, @NotNull Colorc color, @Nullable Colorc backColor)
    {
        Debug.drawTextWithBackground(x, y + this.headerHeight, text, color, backColor);
    }
}
