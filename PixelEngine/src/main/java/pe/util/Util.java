package pe.util;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Layer;
import pe.Window;
import pe.render.GLFramebuffer;
import rutils.Logger;

@SuppressWarnings("unused")
public class Util
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Conversion Objects -------------------- //
    
    private static final Vector2d windowToLayer       = new Vector2d();
    private static final Vector2d layerToWindow       = new Vector2d();
    private static final Vector2d framebufferToLayer  = new Vector2d();
    private static final Vector2d layerToFramebuffer  = new Vector2d();
    
    // -------------------- Conversion Methods -------------------- //
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the window point
     * @param y     The y coordinate of the window point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc windowToLayer(@NotNull Layer.Index index, double x, double y, @NotNull Vector2d out)
    {
        Layer layer = Layer.get(index);
        if (layer == null)
        {
            Util.LOGGER.warning("Layer.%s does not exist", index);
            return out.set(x, y);
        }
        out.x = (x - layer.bounds().x()) / (double) layer.bounds().width() * (double) layer.width();
        out.y = (y - layer.bounds().y()) / (double) layer.bounds().height() * (double) layer.height();
        return out;
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the window point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc windowToLayer(@NotNull Layer.Index index, @NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return windowToLayer(index, pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the window point
     * @param y     The y coordinate of the window point
     * @return The results.
     */
    @NotNull
    public static Vector2dc windowToLayer(@NotNull Layer.Index index, double x, double y)
    {
        return windowToLayer(index, x, y, Util.windowToLayer);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the window point
     * @return The results.
     */
    @NotNull
    public static Vector2dc windowToLayer(@NotNull Layer.Index index, @NotNull Vector2dc pos)
    {
        return windowToLayer(index, pos.x(), pos.y(), Util.windowToLayer);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the window.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the layer point
     * @param y     The y coordinate of the layer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc layerToWindow(@NotNull Layer.Index index, double x, double y, @NotNull Vector2d out)
    {
        Layer layer = Layer.get(index);
        if (layer == null)
        {
            Util.LOGGER.warning("Layer.%s does not exist", index);
            return out.set(x, y);
        }
        out.x = (x * (double) layer.bounds().width() / (double) layer.width()) + layer.bounds().x();
        out.y = (y * (double) layer.bounds().height() / (double) layer.height()) + layer.bounds().y();
        return out;
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the window.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the layer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc layerToWindow(@NotNull Layer.Index index, @NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return layerToWindow(index, pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the window.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the layer point
     * @param y     The y coordinate of the layer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc layerToWindow(@NotNull Layer.Index index, double x, double y)
    {
        return layerToWindow(index, x, y, Util.layerToWindow);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the window.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the layer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc layerToWindow(@NotNull Layer.Index index, @NotNull Vector2dc pos)
    {
        return layerToWindow(index, pos.x(), pos.y(), Util.layerToWindow);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative
     * to the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the framebuffer point
     * @param y     The y coordinate of the framebuffer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc framebufferToLayer(@NotNull Layer.Index index, double x, double y, @NotNull Vector2d out)
    {
        Layer layer = Layer.get(index);
        if (layer == null)
        {
            Util.LOGGER.warning("Layer.%s does not exist", index);
            return out.set(x, y);
        }
    
        // Framebuffer to Window
        x = x * Window.width() / GLFramebuffer.currentWidth();
        y = y * Window.height() / GLFramebuffer.currentHeight();
    
        // Window to Layer
        out.x = (x - layer.bounds().x()) / (double) layer.bounds().width() * (double) layer.width();
        out.y = (y - layer.bounds().y()) / (double) layer.bounds().height() * (double) layer.height();
        return out;
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative
     * to the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the framebuffer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc framebufferToLayer(@NotNull Layer.Index index, @NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return framebufferToLayer(index, pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative
     * to the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the framebuffer point
     * @param y     The y coordinate of the framebuffer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc framebufferToLayer(@NotNull Layer.Index index, double x, double y)
    {
        return framebufferToLayer(index, x, y, Util.framebufferToLayer);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative
     * to the indicated layer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the framebuffer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc framebufferToLayer(@NotNull Layer.Index index, @NotNull Vector2dc pos)
    {
        return framebufferToLayer(index, pos.x(), pos.y(), Util.framebufferToLayer);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the framebuffer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the layer point
     * @param y     The y coordinate of the layer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc layerToFramebuffer(@NotNull Layer.Index index, double x, double y, @NotNull Vector2d out)
    {
        Layer layer = Layer.get(index);
        if (layer == null)
        {
            Util.LOGGER.warning("Layer.%s does not exist", index);
            return out.set(x, y);
        }
        
        // Layer to Window
        x = (x * (double) layer.bounds().width() / (double) layer.width()) + layer.bounds().x();
        y = (y * (double) layer.bounds().height() / (double) layer.height()) + layer.bounds().y();
    
        // Window to Framebuffer
        out.x = x * GLFramebuffer.currentWidth() / Window.width();
        out.y = y * GLFramebuffer.currentHeight() / Window.height();
        return out;
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the framebuffer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the layer point
     * @param out   The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public static Vector2dc layerToFramebuffer(@NotNull Layer.Index index, @NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return layerToFramebuffer(index, pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the framebuffer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param x     The x coordinate of the layer point
     * @param y     The y coordinate of the layer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc layerToFramebuffer(@NotNull Layer.Index index, double x, double y)
    {
        return layerToFramebuffer(index, x, y, Util.layerToFramebuffer);
    }
    
    /**
     * Converts a point relative to the indicated layer origin to a point
     * relative to the framebuffer.
     * <p>
     * If the layer does not exist, then a warning is generated and no
     * transformation is performed.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param index The layer index.
     * @param pos   The coordinate of the layer point
     * @return The results.
     */
    @NotNull
    public static Vector2dc layerToFramebuffer(@NotNull Layer.Index index, @NotNull Vector2dc pos)
    {
        return layerToFramebuffer(index, pos.x(), pos.y(), Util.layerToFramebuffer);
    }
    
}
