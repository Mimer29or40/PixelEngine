package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.event.*;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static pe.Engine.Delegator;

public final class Window
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Static Objects -------------------- //
    
    static long handle;
    
    // -------------------- Callback Objects -------------------- //
    
    static boolean close;
    static boolean _close;
    
    static final Vector2i pos  = new Vector2i();
    static final Vector2i _pos = new Vector2i();
    
    static final Vector2i size  = new Vector2i();
    static final Vector2i _size = new Vector2i();
    
    static final Vector2d scale  = new Vector2d();
    static final Vector2d _scale = new Vector2d();
    
    static final Vector2i fbSize  = new Vector2i();
    static final Vector2i _fbSize = new Vector2i();
    
    static String[] _dropped;
    
    // -------------------- Internal Objects -------------------- //
    
    private static final Vector2i deltaI = new Vector2i();
    private static final Vector2d deltaD = new Vector2d();
    
    static void setup()
    {
        Window.LOGGER.finer("Setup");
        
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        
        int width  = Engine.screenSize.x * Engine.pixelSize.x;
        int height = Engine.screenSize.y * Engine.pixelSize.y;
        
        Window.handle = glfwCreateWindow(width, height, "", MemoryUtil.NULL, MemoryUtil.NULL);
        if (Window.handle == MemoryUtil.NULL) throw new RuntimeException("Failed to create the GLFW window");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            
            FloatBuffer xf = stack.mallocFloat(1);
            FloatBuffer yf = stack.mallocFloat(1);
            
            glfwGetWindowPos(Window.handle, x, y);
            Window.pos.set(Window._pos.set(x.get(0), y.get(0)));
            
            glfwGetWindowSize(Window.handle, x, y);
            Window.size.set(Window._size.set(x.get(0), y.get(0)));
            
            glfwGetWindowContentScale(Window.handle, xf, yf);
            Window.scale.set(Window._scale.set(xf.get(0), yf.get(0)));
            
            glfwGetFramebufferSize(Window.handle, x, y);
            Window.fbSize.set(Window._fbSize.set(x.get(0), y.get(0)));
        }
        
        glfwSetWindowSizeLimits(Window.handle, Engine.screenSize.x, Engine.screenSize.y, GLFW_DONT_CARE, GLFW_DONT_CARE);
        
        glfwSetInputMode(Window.handle, GLFW_LOCK_KEY_MODS, Modifier.lockMods() ? GLFW_TRUE : GLFW_FALSE);
        
        glfwShowWindow(Window.handle);
        
        glfwSetWindowCloseCallback(Window.handle, Window::closeCallback);
        glfwSetWindowPosCallback(Window.handle, Window::posCallback);
        glfwSetWindowSizeCallback(Window.handle, Window::sizeCallback);
        glfwSetWindowContentScaleCallback(Window.handle, Window::contentScaleCallback);
        glfwSetFramebufferSizeCallback(Window.handle, Window::framebufferSizeCallback);
        glfwSetDropCallback(Window.handle, Window::dropCallback);
    }
    
    static void destroy()
    {
        Window.LOGGER.finer("Destroy");
        
        glfwFreeCallbacks(Window.handle);
        glfwDestroyWindow(Window.handle);
        
        org.lwjgl.opengl.GL.destroy();
    }
    
    private Window() {}
    
    // -------------------- Properties -------------------- //
    
    /**
     * Sets the window title, encoded as UTF-8, of the window.
     *
     * @param title The new title.
     */
    public static void title(CharSequence title)
    {
        Window.LOGGER.finest("Setting Title:", title);
        
        Delegator.runTask(() -> glfwSetWindowTitle(Window.handle, title));
    }
    
    /**
     * Sets the icon for the window.
     * <p>
     * This function sets the icon of the window. If passed an array of
     * candidate images, those of or closest to the sizes desired by the system
     * are selected. If no images are specified, the window reverts to its
     * default icon.
     * <p>
     * The pixels are 32-bit, little-endian, non-premultiplied RGBA, i.e. eight
     * bits per channel with the red channel first. They are arranged
     * canonically as packed sequential rows, starting from the top-left
     * corner.
     * <p>
     * The desired image sizes varies depending on platform and system
     * settings. The selected images will be rescaled as needed. Good sizes
     * include 16x16, 32x32 and 48x48.
     *
     * @param icons The new icons.
     */
    public static void icons(GLFWImage... icons)
    {
        // TODO - Make this not depend on GLFW
        Window.LOGGER.finest("Setting Icons:", icons);
        
        Delegator.runTask(() -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                int count = icons.length;
                
                GLFWImage.Buffer buffer = GLFWImage.malloc(count, stack);
                for (int i = 0; i < count; i++) buffer.put(i, icons[i]);
                
                glfwSetWindowIcon(Window.handle, buffer);
            }
        });
    }
    
    /**
     * @return Retrieves the current aspect ration of the window.
     */
    public static double aspectRatio()
    {
        return (double) Window.fbSize.x / (double) Window.fbSize.y;
    }
    
    /**
     * Sets the required aspect ratio of the content area of the window. If the
     * window is full screen, the aspect ratio only takes effect once it is
     * made windowed. If the window is not resizable, this function does
     * nothing.
     * <p>
     * The aspect ratio is as a numerator and a denominator and both values
     * must be greater than zero. For example, the common 16:9 aspect ratio is
     * as 16 and 9, respectively.
     * <p>
     * If the numerator and denominator is set to
     * {@link org.lwjgl.glfw.GLFW#GLFW_DONT_CARE DONT_CARE} then the aspect
     * ratio limit is disabled.
     * <p>
     * The aspect ratio is applied immediately to a windowed mode window and
     * may cause it to be resized.
     *
     * @param numer the numerator of the desired aspect ratio, or {@link org.lwjgl.glfw.GLFW#GLFW_DONT_CARE DONT_CARE}
     * @param denom the denominator of the desired aspect ratio, or {@link org.lwjgl.glfw.GLFW#GLFW_DONT_CARE DONT_CARE}
     */
    public static void aspectRatio(int numer, int denom)
    {
        Window.LOGGER.finest("Setting Aspect Ratio: %s/%s", numer, denom);
        
        Delegator.runTask(() -> glfwSetWindowAspectRatio(Window.handle, numer, denom));
    }
    
    /**
     * Restores the window if it was previously iconified (minimized) or
     * maximized. If the window is already restored, this function does
     * nothing.
     *
     * <p>If the window is a full screen window, the resolution
     * chosen for the window is restored on the selected monitor.</p>
     */
    public static void restore()
    {
        Delegator.runTask(() -> glfwRestoreWindow(Window.handle));
    }
    
    /**
     * @return Retrieves if the window is resizable <i>by the user</i>.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean resizable()
    {
        return Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_RESIZABLE) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window is resizable <i>by the user</i>.
     *
     * @param resizable if the window is resizable <i>by the user</i>.
     */
    public static void resizable(boolean resizable)
    {
        Window.LOGGER.finest("Setting Resizable Flag:", resizable);
        
        Delegator.runTask(() -> glfwSetWindowAttrib(Window.handle, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the window is visible. Window visibility can be controlled with {@link #show} and {@link #hide}.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean visible()
    {
        return Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_VISIBLE) == GLFW_TRUE);
    }
    
    /**
     * Makes the window visible if it was previously hidden. If the window is
     * already visible or is in full screen mode, this function does nothing.
     */
    public static void show()
    {
        Window.LOGGER.finest("Showing");
        
        Delegator.runTask(() -> glfwShowWindow(Window.handle));
    }
    
    /**
     * Hides the window, if it was previously visible. If the window is already
     * hidden or is in full screen mode, this function does nothing.
     */
    public static void hide()
    {
        Window.LOGGER.finest("Hiding");
        
        Delegator.runTask(() -> glfwHideWindow(Window.handle));
    }
    
    /**
     * @return Retrieves if the cursor is currently directly over the content area of the window, with no other windows between.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean hovered()
    {
        return Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_HOVERED) == GLFW_TRUE);
    }
    
    // -------------------- Callback Related Things -------------------- //
    
    /**
     * Retrieves the position, in screen coordinates, of the upper-left corner
     * of the content area of the window.
     *
     * @return The position of the upper-left corner of the content area
     */
    @NotNull
    public static Vector2ic pos()
    {
        return Window.pos;
    }
    
    /**
     * Retrieves the x-coordinate of the position, in screen coordinates, of
     * the upper-left corner of the content area of the window.
     *
     * @return The x-coordinate of the upper-left corner of the content area
     */
    public int x()
    {
        return Window.pos.x;
    }
    
    /**
     * Retrieves the y-coordinate of the position, in screen coordinates, of
     * the upper-left corner of the content area of the window.
     *
     * @return The y-coordinate of the upper-left corner of the content area
     */
    public int y()
    {
        return Window.pos.y;
    }
    
    /**
     * Sets the position, in screen coordinates, of the upper-left corner of
     * the content area of the windowed mode window. If the window is a full
     * screen window, this function does nothing.
     *
     * <p><b>Do not use this function</b> to move an already visible window
     * unless you have very good reasons for doing so, as it will confuse and
     * annoy the user.</p>
     *
     * <p>The window manager may put limits on what positions are allowed. GLFW
     * cannot and should not override these limits.</p>
     *
     * @param x The x-coordinate of the upper-left corner of the content area.
     * @param y The y-coordinate of the upper-left corner of the content area.
     */
    public static void pos(int x, int y)
    {
        Window.LOGGER.finest("Setting Position: (%s, %s)", x, y);
        
        Delegator.waitRunTask(() -> glfwSetWindowPos(Window.handle, x, y));
    }
    
    /**
     * Sets the position, in screen coordinates, of the upper-left corner of
     * the content area of the windowed mode window. If the window is a full
     * screen window, this function does nothing.
     *
     * <p><b>Do not use this function</b> to move an already visible window
     * unless you have very good reasons for doing so, as it will confuse and
     * annoy the user.</p>
     *
     * <p>The window manager may put limits on what positions are allowed. GLFW
     * cannot and should not override these limits.</p>
     *
     * @param pos The position of the upper-left corner of the content area.
     */
    public static void pos(@NotNull Vector2ic pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Sets the position, in screen coordinates, of the upper-left corner of
     * the content area of the windowed mode window. If the window is a full
     * screen window, this function does nothing.
     *
     * <p><b>Do not use this function</b> to move an already visible window
     * unless you have very good reasons for doing so, as it will confuse and
     * annoy the user.</p>
     *
     * <p>The window manager may put limits on what positions are allowed. GLFW
     * cannot and should not override these limits.</p>
     *
     * @param pos The position of the upper-left corner of the content area.
     */
    public static void pos(@NotNull Vector2dc pos)
    {
        pos((int) pos.x(), (int) pos.y());
    }
    
    /**
     * Retrieves the size, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The size, in screen coordinates, of the content area.
     */
    @NotNull
    public static Vector2ic size()
    {
        return Window.size;
    }
    
    /**
     * Retrieves the width, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The width, in screen coordinates, of the content area.
     */
    public static int width()
    {
        return Window.size.x;
    }
    
    /**
     * Retrieves the height, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The height, in screen coordinates, of the content area.
     */
    public static int height()
    {
        return Window.size.y;
    }
    
    /**
     * Sets the size, in pixels, of the content area of the window.
     * <p>
     * For full screen windows, this function updates the resolution of its
     * desired video mode and switches to the video mode closest to it, without
     * affecting the window's context. As the context is unaffected, the bit
     * depths of the framebuffer remain unchanged.
     * <p>
     * The window manager may put limits on what sizes are allowed. GLFW cannot
     * and should not override these limits.
     *
     * @param width  The desired width, in screen coordinates, of the window content area
     * @param height The desired height, in screen coordinates, of the window content area
     */
    public static void size(int width, int height)
    {
        Window.LOGGER.finest("Setting Size: (%s, %s)", width, height);
        
        Delegator.waitRunTask(() -> glfwSetWindowSize(Window.handle, width, height));
    }
    
    /**
     * Sets the size, in pixels, of the content area of the window.
     * <p>
     * For full screen windows, this function updates the resolution of its
     * desired video mode and switches to the video mode closest to it, without
     * affecting the window's context. As the context is unaffected, the bit
     * depths of the framebuffer remain unchanged.
     * <p>
     * The window manager may put limits on what sizes are allowed. GLFW
     * cannot and should not override these limits.
     *
     * @param size The desired size, in screen coordinates, of the window content area
     */
    public static void size(@NotNull Vector2ic size)
    {
        size(size.x(), size.y());
    }
    
    /**
     * Sets the size, in pixels, of the content area of the window.
     * <p>
     * For full screen windows, this function updates the resolution of its
     * desired video mode and switches to the video mode closest to it, without
     * affecting the window's context. As the context is unaffected, the bit
     * depths of the framebuffer remain unchanged.
     * <p>
     * The window manager may put limits on what sizes are allowed. GLFW
     * cannot and should not override these limits.
     *
     * @param size The desired size, in screen coordinates, of the window content area
     */
    public static void size(@NotNull Vector2dc size)
    {
        size((int) size.x(), (int) size.y());
    }
    
    /**
     * Retrieves the content scale for the window.
     * <p>
     * This function retrieves the content scale for the window. The content
     * scale is the ratio between the current DPI and the platform's default
     * DPI. This is especially important for text and any UI elements. If the
     * pixel dimensions of your UI scaled by this look appropriate on your
     * machine then it should appear at a reasonable size on other machines
     * regardless of their DPI and scaling settings. This relies on the system
     * DPI and scaling settings being somewhat correct.
     * <p>
     * On systems where each monitor can have its own content scale, the window
     * content scale will depend on which monitor the system considers the
     * window to be on.
     *
     * @return the content scale for the window.
     */
    @NotNull
    public static Vector2dc contentScale()
    {
        return Window.scale;
    }
    
    /**
     * Retrieves the horizontal content scale for the window.
     * <p>
     * This function retrieves the content scale for the window. The content
     * scale is the ratio between the current DPI and the platform's default
     * DPI. This is especially important for text and any UI elements. If the
     * pixel dimensions of your UI scaled by this look appropriate on your
     * machine then it should appear at a reasonable size on other machines
     * regardless of their DPI and scaling settings. This relies on the system
     * DPI and scaling settings being somewhat correct.
     * <p>
     * On systems where each monitor can have its own content scale, the window
     * content scale will depend on which monitor the system considers the
     * window to be on.
     *
     * @return the horizontal content scale for the window.
     */
    public static double contentScaleX()
    {
        return Window.scale.x;
    }
    
    /**
     * Retrieves the vertical content scale for the window.
     * <p>
     * This function retrieves the content scale for the window. The content
     * scale is the ratio between the current DPI and the platform's default
     * DPI. This is especially important for text and any UI elements. If the
     * pixel dimensions of your UI scaled by this look appropriate on your
     * machine then it should appear at a reasonable size on other machines
     * regardless of their DPI and scaling settings. This relies on the system
     * DPI and scaling settings being somewhat correct.
     * <p>
     * On systems where each monitor can have its own content scale, the window
     * content scale will depend on which monitor the system considers the
     * window to be on.
     *
     * @return the vertical content scale for the window.
     */
    public static double contentScaleY()
    {
        return Window.scale.y;
    }
    
    /**
     * Retrieves the size, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The size, in pixels, of the framebuffer
     */
    @NotNull
    public static Vector2ic framebufferSize()
    {
        return Window.fbSize;
    }
    
    /**
     * Retrieves the width, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The width, in pixels, of the framebuffer
     */
    public static int framebufferWidth()
    {
        return Window.fbSize.x;
    }
    
    /**
     * Retrieves the height, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The height, in pixels, of the framebuffer
     */
    public static int framebufferHeight()
    {
        return Window.fbSize.y;
    }
    
    // -------------------- GLFW Methods -------------------- //
    
    public static void close()
    {
        Window.LOGGER.finest("Closing");
        
        Window._close = true;
    }
    
    public static void makeCurrent()
    {
        Window.LOGGER.finest("Making Context Current");
        
        glfwMakeContextCurrent(Window.handle);
        org.lwjgl.opengl.GL.createCapabilities();
    }
    
    public static void unmakeCurrent()
    {
        Window.LOGGER.finest("Unmaking Context Current");
        
        org.lwjgl.opengl.GL.setCapabilities(null);
        glfwMakeContextCurrent(MemoryUtil.NULL);
    }
    
    public static void swap()
    {
        glfwSwapBuffers(Window.handle);
    }
    
    /**
     * Sets the system clipboard to the specified, UTF-8 encoded string.
     * <p>
     * The specified string is copied before this function returns.
     *
     * @param string a UTF-8 encoded string
     */
    public static void setClipboard(ByteBuffer string)
    {
        Delegator.runTask(() -> glfwSetClipboardString(Window.handle, string));
    }
    
    /**
     * Sets the system clipboard to the specified, UTF-8 encoded string.
     * <p>
     * The specified string is copied before this function returns.
     *
     * @param string a UTF-8 encoded string
     */
    public static void setClipboard(CharSequence string)
    {
        Delegator.runTask(() -> glfwSetClipboardString(Window.handle, string));
    }
    
    /**
     * Returns the contents of the system clipboard, if it contains or is
     * convertible to a UTF-8 encoded string. If the clipboard is empty or if
     * its contents cannot be converted, {@code null} is returned and a
     * {@link org.lwjgl.glfw.GLFW#GLFW_FORMAT_UNAVAILABLE FORMAT_UNAVAILABLE}
     * error is generated.
     *
     * @return the contents of the clipboard as a UTF-8 encoded string, or
     * {@code null} if an error occurred
     */
    @Nullable
    public static String getClipboard()
    {
        return Delegator.waitReturnTask(() -> glfwGetClipboardString(Window.handle));
    }
    
    /**
     * Returns the contents of the system clipboard, if it contains or is
     * convertible to a UTF-8 encoded string. If the clipboard is empty or if
     * its contents cannot be converted, {@link MemoryUtil#NULL NULL} is
     * returned and a
     * {@link org.lwjgl.glfw.GLFW#GLFW_FORMAT_UNAVAILABLE FORMAT_UNAVAILABLE}
     * error is generated.
     *
     * @return the contents of the clipboard as a UTF-8 encoded string, or
     * {@code null} if an error occurred
     */
    @SuppressWarnings("ConstantConditions")
    public static long getClipboardRaw()
    {
        return Delegator.waitReturnTask(() -> nglfwGetClipboardString(Window.handle));
    }
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time The system time in nanoseconds.
     */
    static void processEvents(long time)
    {
        if (Window.close != Window._close)
        {
            Window.close = Window._close;
            Engine.Events.post(EventWindowClosed.create(time));
            Engine.stop();
        }
        
        if (Window.pos.x != Window._pos.x || Window.pos.y != Window._pos.y)
        {
            Window._pos.sub(Window.pos, Window.deltaI);
            Window.pos.set(Window._pos);
            Engine.Events.post(EventWindowMoved.create(time, Window.pos, Window.deltaI));
        }
        
        if (Window.size.x != Window._size.x || Window.size.y != Window._size.y)
        {
            Window._size.sub(Window.size, Window.deltaI);
            Window.size.set(Window._size);
            Engine.Events.post(EventWindowResized.create(time, Window.size, Window.deltaI));
        }
        
        if (Double.compare(Window.scale.x, Window._scale.x) != 0 || Double.compare(Window.scale.y, Window._scale.y) != 0)
        {
            Window._scale.sub(Window.scale, Window.deltaD);
            Window.scale.set(Window._scale);
            Engine.Events.post(EventWindowContentScaleChanged.create(time, Window.scale, Window.deltaD));
        }
        
        if (Window.fbSize.x != Window._fbSize.x || Window.fbSize.y != Window._fbSize.y)
        {
            Window._fbSize.sub(Window.fbSize, Window.deltaI);
            Window.fbSize.set(Window._fbSize);
            Engine.Events.post(EventWindowFramebufferResized.create(time, Window.fbSize, Window.deltaI));
        }
        
        if (Window._dropped != null)
        {
            Path[] paths = new Path[Window._dropped.length];
            for (int i = 0; i < Window._dropped.length; i++) paths[i] = Paths.get(Window._dropped[i]);
            Window._dropped = null;
            Engine.Events.post(EventWindowDropped.create(time, paths));
        }
    }
    
    private static void closeCallback(long handle)
    {
        Window._close = true;
    }
    
    private static void posCallback(long handle, int x, int y)
    {
        Window._pos.set(x, y);
    }
    
    private static void sizeCallback(long handle, int width, int height)
    {
        Window._size.set(width, height);
    }
    
    private static void contentScaleCallback(long handle, float xScale, float yScale)
    {
        Window._scale.set(xScale, yScale);
    }
    
    private static void framebufferSizeCallback(long handle, int width, int height)
    {
        Window._fbSize.set(width, height);
    }
    
    private static void dropCallback(long handle, int count, long names)
    {
        Window._dropped = new String[count];
        PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
        for (int i = 0; i < count; i++) Window._dropped[i] = MemoryUtil.memUTF8(charPointers.get(i));
    }
}
