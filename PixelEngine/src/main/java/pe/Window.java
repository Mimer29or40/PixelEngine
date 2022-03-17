package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.event.*;
import pe.shape.AABBi;
import pe.shape.AABBic;
import pe.texture.Image;
import rutils.Logger;
import rutils.group.Pair;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class Window
{
    private static final Logger LOGGER = new Logger();
    
    public static final int DONT_CARE = GLFW_DONT_CARE;
    
    static final Map<Long, Window> windows = new LinkedHashMap<>();
    static       Window            primary = null;
    static       Window            current = null;
    
    static void setup(int width, int height, double pixelWidth, double pixelHeight)
    {
        Window.LOGGER.fine("Setup");
        
        Builder builder = new Builder();
        
        builder.name("main");
        builder.size((int) (width * pixelWidth), (int) (height * pixelHeight));
        builder.minSize(width, height);
        builder.vsync(false);
        builder.title("Engine - " + Engine.instance.name);
        builder.resizable(true);
        builder.visible(true);
        builder.decorated(true);
        builder.focused(true);
        builder.floating(false);
        builder.maximized(false);
        builder.centerCursor(false);
        builder.transparentFramebuffer(false);
        builder.focusOnShow(false);
        
        Window.primary = builder.build();
        Window.makeCurrent(Window.primary);
    }
    
    static void destroy()
    {
        Window.LOGGER.fine("Destroy");
        
        Window.makeCurrent(null);
        
        Window.windows.values().forEach(Window::releaseCallbacks);
    }
    
    static void events(long time)
    {
        long[] handlesToRemove = Window.windows.values()
                                               .stream()
                                               .filter(w -> !w.isOpen())
                                               .mapToLong(w -> w.handle)
                                               .toArray();
        
        Window.windows.values().forEach(w -> w.processEvents(time));
        
        for (long handle : handlesToRemove)
        {
            Window removed = Window.windows.remove(handle);
            removed.releaseCallbacks();
            if (removed == Window.primary) Engine.stop();
        }
    }
    
    public static void makeCurrent(@Nullable Window window)
    {
        if (Window.current != window)
        {
            Window.current = window;
            if (window != null)
            {
                Window.LOGGER.finer("Binding Context for", window);
                
                org.lwjgl.opengl.GL.setCapabilities(window.capabilities);
                glfwMakeContextCurrent(window.handle);
            }
            else
            {
                Window.LOGGER.finer("Unbinding Context");
                
                glfwMakeContextCurrent(MemoryUtil.NULL);
            }
        }
    }
    
    @UnmodifiableView
    @NotNull
    public static Collection<@NotNull Window> get()
    {
        return Collections.unmodifiableCollection(Window.windows.values());
    }
    
    @NotNull
    public static Window get(int index)
    {
        if (index < 0) throw new RuntimeException("Window Index cannot be < 0");
        for (Window window : Window.windows.values()) if (index-- <= 0) return window;
        return Window.primary;
    }
    
    @NotNull
    public static Window primary()
    {
        return Window.primary;
    }
    
    private static void windowFocusCallback(long handle, boolean focused)
    {
        Window window = Window.windows.get(handle);
        window.focusedChanges = focused;
    }
    
    private static void windowIconifyCallback(long handle, boolean iconified)
    {
        Window window = Window.windows.get(handle);
        window.iconifiedChanges = iconified;
    }
    
    private static void windowMaximizeCallback(long handle, boolean maximized)
    {
        Window window = Window.windows.get(handle);
        window.maximizedChanges = maximized;
    }
    
    private static void windowCloseCallback(long handle)
    {
        Window window = Window.windows.get(handle);
        window.shouldClose = true;
    }
    
    private static void windowRefreshCallback(long handle)
    {
        Window window = Window.windows.get(handle);
        window.shouldRefresh = true;
    }
    
    private static void windowPosCallback(long handle, int x, int y)
    {
        Window window = Window.windows.get(handle);
        window.posChanges.set(x, y);
    }
    
    private static void windowSizeCallback(long handle, int width, int height)
    {
        Window window = Window.windows.get(handle);
        window.sizeChanges.set(width, height);
    }
    
    private static void windowContentScaleCallback(long handle, float xScale, float yScale)
    {
        Window window = Window.windows.get(handle);
        window.scaleChanges.set(xScale, yScale);
    }
    
    private static void windowFramebufferSizeCallback(long handle, int width, int height)
    {
        Window window = Window.windows.get(handle);
        window.fbSizeChanges.set(width, height);
    }
    
    private static void windowDropCallback(long handle, int count, long names)
    {
        Window window = Window.windows.get(handle);
        window.dropped = new String[count];
        PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
        for (int i = 0; i < count; i++) window.dropped[i] = MemoryUtil.memUTF8(charPointers.get(i));
    }
    
    private static void mouseEnteredCallback(long handle, boolean entered)
    {
        Mouse.window         = Window.windows.get(handle);
        Mouse.enteredChanges = entered;
    }
    
    private static void mousePosCallback(long handle, double x, double y)
    {
        Mouse.window = Window.windows.get(handle);
        Mouse.posChanges.set(x, y);
    }
    
    private static void mouseScrollCallback(long handle, double dx, double dy)
    {
        Mouse.window = Window.windows.get(handle);
        Mouse.scrollChanges.add(dx, dy);
    }
    
    private static void mouseButtonCallback(long handle, int button, int action, int mods)
    {
        Mouse.window = Window.windows.get(handle);
        Mouse.buttonStateChanges.offer(new Pair<>(Mouse.Button.valueOf(button), action));
        Modifier.activeMods = mods;
    }
    
    private static void keyboardCharCallback(long handle, int codePoint)
    {
        Keyboard.window = Window.windows.get(handle);
        Keyboard.charChanges.offer(codePoint);
    }
    
    private static void keyboardKeyCallback(long handle, int key, int scancode, int action, int mods)
    {
        Keyboard.window = Window.windows.get(handle);
        Keyboard.keyStateChanges.offer(new Pair<>(Keyboard.Key.get(key, scancode), action));
        Modifier.activeMods = mods;
    }
    
    // -------------------- Instance -------------------- //
    
    final String name;
    final long   handle;
    
    Monitor monitor;
    
    boolean windowed;
    
    boolean open;
    
    int refreshRate;
    
    final Vector2i minSize;
    final Vector2i maxSize;
    
    final AABBi bounds;
    
    final Matrix4d viewMatrix;
    
    final GLCapabilities capabilities;
    
    // -------------------- State Objects -------------------- //
    
    boolean vsync;
    boolean vsyncChanges;
    
    boolean focused;
    boolean focusedChanges;
    
    boolean iconified;
    boolean iconifiedChanges;
    
    boolean maximized;
    boolean maximizedChanges;
    
    final Vector2i pos;
    final Vector2i posChanges;
    
    final Vector2i size;
    final Vector2i sizeChanges;
    
    final Vector2d scale;
    final Vector2d scaleChanges;
    
    final Vector2i fbSize;
    final Vector2i fbSizeChanges;
    
    boolean shouldClose;
    boolean shouldRefresh;
    
    String[] dropped;
    
    // -------------------- Utility Objects -------------------- //
    
    private final Vector2i deltaI              = new Vector2i();
    private final Vector2d deltaD              = new Vector2d();
    private final Vector2d windowToScreen      = new Vector2d();
    private final Vector2d screenToWindow      = new Vector2d();
    private final Vector2d windowToFramebuffer = new Vector2d();
    private final Vector2d framebufferToWindow = new Vector2d();
    
    private Window(final @NotNull Builder builder)
    {
        Boolean visible = builder.visible;
        if (builder.position != null) builder.visible(false);
        builder.applyHints();
        
        this.monitor = builder.monitor != null ? builder.monitor : Monitor.primary();
        
        this.windowed = builder.windowed;
        
        this.name = builder.name;
        String title   = builder.title != null ? builder.title : this.name != null ? this.name : "Window";
        long   monitor = this.windowed ? MemoryUtil.NULL : this.monitor.handle;
        long   window  = Window.primary != null ? Window.primary.handle : MemoryUtil.NULL;
        
        glfwMakeContextCurrent(MemoryUtil.NULL);
        this.handle = glfwCreateWindow(builder.size.x(), builder.size.y(), title, monitor, window);
        if (this.handle == MemoryUtil.NULL) throw new RuntimeException("Failed to create the window");
        
        Window.LOGGER.fine("Created", this);
        
        this.open = true;
        
        this.refreshRate = builder.refreshRate;
        
        this.vsync        = builder.vsync;
        this.vsyncChanges = this.vsync;
        
        this.focused        = glfwGetWindowAttrib(this.handle, GLFW_FOCUSED) == GLFW_TRUE;
        this.focusedChanges = this.focused;
        
        this.iconified        = glfwGetWindowAttrib(this.handle, GLFW_ICONIFIED) == GLFW_TRUE;
        this.iconifiedChanges = this.iconified;
        
        this.maximized        = glfwGetWindowAttrib(this.handle, GLFW_MAXIMIZED) == GLFW_TRUE;
        this.maximizedChanges = this.maximized;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            
            FloatBuffer xf = stack.mallocFloat(1);
            FloatBuffer yf = stack.mallocFloat(1);
            
            this.pos        = new Vector2i();
            this.posChanges = new Vector2i();
            if (builder.position != null)
            {
                this.pos.set(builder.position);
                this.posChanges.set(this.pos);
                glfwSetWindowPos(this.handle, this.pos.x(), this.pos.y());
                if (visible != null && visible) glfwShowWindow(this.handle);
            }
            else
            {
                glfwGetWindowPos(this.handle, x, y);
                this.pos.set(x.get(0), y.get(0));
                this.posChanges.set(this.pos);
            }
            
            glfwGetWindowSize(this.handle, x, y);
            this.size        = new Vector2i(x.get(0), y.get(0));
            this.sizeChanges = new Vector2i(this.size);
            
            glfwGetWindowContentScale(this.handle, xf, yf);
            this.scale        = new Vector2d(xf.get(0), yf.get(0));
            this.scaleChanges = new Vector2d(this.scale);
            
            glfwGetFramebufferSize(this.handle, x, y);
            this.fbSize        = new Vector2i(x.get(0), y.get(0));
            this.fbSizeChanges = new Vector2i(this.fbSize);
        }
        
        this.shouldClose = false;
        
        this.shouldRefresh = true;
        
        this.dropped = null;
        
        this.minSize = new Vector2i(builder.minSize);
        this.maxSize = new Vector2i(builder.maxSize);
        
        glfwSetWindowSizeLimits(this.handle, this.minSize.x, this.minSize.y, this.maxSize.x, this.maxSize.y);
        
        this.bounds = new AABBi(this.pos, this.size);
        
        this.viewMatrix = new Matrix4d().setOrtho(0, this.fbSize.x, this.fbSize.y, 0, -1F, 1F);
        
        Window.current = this;
        glfwMakeContextCurrent(this.handle);
        this.capabilities = org.lwjgl.opengl.GL.createCapabilities();
        
        Modifier.lockKeyMods(this, builder.lockKeyMods);
        
        glfwSetWindowCloseCallback(this.handle, Window::windowCloseCallback);
        glfwSetWindowFocusCallback(this.handle, Window::windowFocusCallback);
        glfwSetWindowIconifyCallback(this.handle, Window::windowIconifyCallback);
        glfwSetWindowMaximizeCallback(this.handle, Window::windowMaximizeCallback);
        glfwSetWindowPosCallback(this.handle, Window::windowPosCallback);
        glfwSetWindowSizeCallback(this.handle, Window::windowSizeCallback);
        glfwSetWindowContentScaleCallback(this.handle, Window::windowContentScaleCallback);
        glfwSetFramebufferSizeCallback(this.handle, Window::windowFramebufferSizeCallback);
        glfwSetWindowRefreshCallback(this.handle, Window::windowRefreshCallback);
        glfwSetDropCallback(this.handle, Window::windowDropCallback);
        
        glfwSetCursorEnterCallback(this.handle, Window::mouseEnteredCallback);
        glfwSetCursorPosCallback(this.handle, Window::mousePosCallback);
        glfwSetScrollCallback(this.handle, Window::mouseScrollCallback);
        glfwSetMouseButtonCallback(this.handle, Window::mouseButtonCallback);
        
        glfwSetCharCallback(this.handle, Window::keyboardCharCallback);
        glfwSetKeyCallback(this.handle, Window::keyboardKeyCallback);
        
        Window.windows.put(this.handle, this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Window window = (Window) o;
        return this.handle == window.handle;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.handle);
    }
    
    @Override
    public String toString()
    {
        return "Window{name='" + (this.name != null ? this.name : this.handle) + "'}";
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * Sets the window title, encoded as UTF-8, of the window.
     *
     * @param title The new title.
     */
    public void title(CharSequence title)
    {
        Window.LOGGER.finest("Setting Title for %s: \"%s\"", this, title);
        
        Engine.Delegator.runTask(() -> glfwSetWindowTitle(this.handle, title));
    }
    
    /**
     * @return The current monitor that the window is in.
     */
    public Monitor monitor()
    {
        return this.monitor;
    }
    
    /**
     * @return Retrieves if the window is in windowed mode.
     */
    public boolean windowed()
    {
        return this.windowed;
    }
    
    /**
     * Sets the window to into windowed mode or fullscreen mode.
     * <p>
     * If windowed is set to {@code true}, then the window will be set to
     * fullscreen in the current monitor that it is in.
     * <p>
     * If windowed is set to {@code false}, then the window will be set to
     * windowed in the current monitor and placed in the center of it and the
     * window will be resized to the previously set size.
     * <p>
     * If you only wish to update the resolution of a full screen window or the
     * size of a windowed mode window, see {@link #size(Vector2ic)}.
     * <p>
     * When a window transitions from full screen to windowed mode, this
     * function restores any previous window settings such as whether it is
     * decorated, floating, resizable, has size or aspect ratio limits, etc.
     *
     * @param windowed The new windowed mode state.
     */
    public void windowed(boolean windowed)
    {
        Engine.Delegator.runTask(() -> {
            this.windowed = windowed;
            long monitor = this.windowed ? MemoryUtil.NULL : this.monitor.handle;
            
            int x = ((this.monitor.primaryVideoMode.width - this.size.x) >> 1) + this.monitor.x();
            int y = ((this.monitor.primaryVideoMode.height - this.size.y) >> 1) + this.monitor.y();
            
            glfwSetWindowMonitor(this.handle, monitor, x, y, this.size.x, this.size.y, this.refreshRate);
        });
    }
    
    /**
     * @return If the window is open.
     */
    public boolean isOpen()
    {
        return this.open;
    }
    
    /**
     * Requests that the window close.
     */
    public void close()
    {
        Window.LOGGER.finest("Closing", this);
        
        this.shouldClose = true;
    }
    
    /**
     * @return Retrieves the refresh rate of the window, or {@link org.lwjgl.glfw.GLFW#GLFW_DONT_CARE DONT_CARE}
     */
    public int refreshRate()
    {
        return this.refreshRate;
    }
    
    /**
     * Sets the refresh rate of the window.
     *
     * @param refreshRate The new refresh rate.
     */
    public void refreshRate(int refreshRate)
    {
        Engine.Delegator.runTask(() -> {
            this.refreshRate = refreshRate;
            
            long monitor = this.windowed ? MemoryUtil.NULL : this.monitor.handle;
            glfwSetWindowMonitor(this.handle, monitor, this.pos.x, this.pos.y, this.size.x, this.size.y, this.refreshRate);
        });
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
    public void icons(Image... icons)
    {
        Window.LOGGER.finest("Setting Icons in %s: %s", this, icons);
        
        Engine.Delegator.runTask(() -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                int count = icons.length;
                
                GLFWImage.Buffer buffer = GLFWImage.malloc(count, stack);
                for (int i = 0; i < count; i++)
                {
                    int width  = icons[i].width();
                    int height = icons[i].height();
                    int sizeof = icons[i].format().sizeof;
                    
                    GLFWImage icon = buffer.get(i);
                    icon.width(width);
                    icon.height(height);
                    
                    Color.Buffer data = icons[i].data();
                    if (data == null)
                    {
                        icon.pixels(width * height * sizeof);
                    }
                    else
                    {
                        icon.pixels(data.toBuffer());
                    }
                }
                
                glfwSetWindowIcon(this.handle, buffer);
            }
        });
    }
    
    /**
     * @return Retrieves the current aspect ratio of the window.
     */
    public double aspectRatio()
    {
        return (double) this.fbSize.x / (double) this.fbSize.y;
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
     * {@link Window#DONT_CARE} then the aspect
     * ratio limit is disabled.
     * <p>
     * The aspect ratio is applied immediately to a windowed mode window and
     * may cause it to be resized.
     *
     * @param numer the numerator of the desired aspect ratio, or {@link Window#DONT_CARE}
     * @param denom the denominator of the desired aspect ratio, or {@link Window#DONT_CARE}
     */
    public void aspectRatio(int numer, int denom)
    {
        Window.LOGGER.finest("Setting Aspect Ratio for %s: %s/%s", this, numer, denom);
        
        Engine.Delegator.runTask(() -> glfwSetWindowAspectRatio(this.handle, numer, denom));
    }
    
    /**
     * Restores the window if it was previously iconified (minimized) or
     * maximized. If the window is already restored, this function does
     * nothing.
     * <p>
     * If the window is a full screen window, the resolution chosen for the
     * window is restored on the selected monitor.
     */
    public void restore()
    {
        Window.LOGGER.finest("Restoring", this);
        
        Engine.Delegator.runTask(() -> glfwRestoreWindow(this.handle));
    }
    
    /**
     * @return Retrieves if the window is resizable <i>by the user</i>.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean resizable()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_RESIZABLE) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window is resizable <i>by the user</i>.
     *
     * @param resizable if the window is resizable <i>by the user</i>.
     */
    public void resizable(boolean resizable)
    {
        Window.LOGGER.finest("Setting Resizable Flag for %s: %s", this, resizable);
        
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(this.handle, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the window is visible. Window visibility can be controlled with {@link #show} and {@link #hide}.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean visible()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_VISIBLE) == GLFW_TRUE);
    }
    
    /**
     * Makes the window visible if it was previously hidden. If the window is
     * already visible or is in full screen mode, this function does nothing.
     */
    public void show()
    {
        Window.LOGGER.finest("Showing", this);
        
        Engine.Delegator.runTask(() -> glfwShowWindow(this.handle));
    }
    
    /**
     * Hides the window, if it was previously visible. If the window is already
     * hidden or is in full screen mode, this function does nothing.
     */
    public void hide()
    {
        Window.LOGGER.finest("Hiding", this);
        
        Engine.Delegator.runTask(() -> glfwHideWindow(this.handle));
    }
    
    /**
     * @return Retrieves if the window has decorations such as a border, a close widget, etc.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean decorated()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_DECORATED) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window has decorations such as a border, a close
     * widget, etc.
     *
     * @param decorated if the window has decorations.
     */
    public void decorated(boolean decorated)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(this.handle, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the window is floating, also called topmost or always-on-top.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean floating()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_FLOATING) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window is floating, also called topmost or
     * always-on-top.
     *
     * @param floating if the window is floating.
     */
    public void floating(boolean floating)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(this.handle, GLFW_FLOATING, floating ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the cursor is currently directly over the content area of the window, with no other windows between.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean hovered()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_HOVERED) == GLFW_TRUE);
    }
    
    /**
     * @return Retrieves if input focuses on calling show window.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean focusOnShow()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(this.handle, GLFW_FOCUS_ON_SHOW) == GLFW_TRUE);
    }
    
    /**
     * Indicates if input focuses on calling show window.
     *
     * @param focusOnShow if input focuses on calling show window.
     */
    public void focusOnShow(boolean focusOnShow)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(this.handle, GLFW_FOCUS_ON_SHOW, focusOnShow ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * Retrieves the minimum size, in screen coordinates, of the content area
     * of the window. If you wish to retrieve the size of the framebuffer of
     * the window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The minimum size, in screen coordinates, of the content area.
     */
    public Vector2ic minSize()
    {
        return this.minSize;
    }
    
    /**
     * Retrieves the maximum size, in screen coordinates, of the content area
     * of the window. If you wish to retrieve the size of the framebuffer of
     * the window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The maximum size, in screen coordinates, of the content area.
     */
    public Vector2ic maxSize()
    {
        return this.maxSize;
    }
    
    /**
     * Sets the size limits of the content area of the window. If the window is
     * full screen, the size limits only take effect if once it is made
     * windowed. If the window is not resizable, this function does nothing.
     * <p>
     * The size limits are applied immediately to a windowed mode window and
     * may cause it to be resized.
     * <p>
     * The maximum dimensions must be greater than or equal to the minimum
     * dimensions and all must be greater than or equal to zero.
     *
     * @param minWidth  the minimum width, in screen coordinates, of the content area, or {@link #DONT_CARE}
     * @param minHeight the minimum height, in screen coordinates, of the content area, or {@link #DONT_CARE}
     * @param maxWidth  the maximum width, in screen coordinates, of the content area, or {@link #DONT_CARE}
     * @param maxHeight the maximum height, in screen coordinates, of the content area, or {@link #DONT_CARE}
     */
    public void sizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight)
    {
        this.minSize.set(minWidth, minHeight);
        this.maxSize.set(maxWidth, maxHeight);
        
        Engine.Delegator.runTask(() -> glfwSetWindowSizeLimits(this.handle, minWidth, minHeight, maxWidth, maxHeight));
    }
    
    /**
     * Sets the size limits of the content area of the window. If the window is
     * full screen, the size limits only take effect if once it is made
     * windowed. If the window is not resizable, this function does nothing.
     * <p>
     * The size limits are applied immediately to a windowed mode window and
     * may cause it to be resized.
     * <p>
     * The maximum dimensions must be greater than or equal to the minimum
     * dimensions and all must be greater than or equal to zero.
     *
     * @param min the minimum size, in screen coordinates, of the content area, or {@link #DONT_CARE}
     * @param max the maximum size, in screen coordinates, of the content area, or {@link #DONT_CARE}
     */
    public void sizeLimits(@NotNull Vector2ic min, @NotNull Vector2ic max)
    {
        sizeLimits(min.x(), min.y(), max.x(), max.y());
    }
    
    /**
     * @return A axis-aligned bounding box of the window's content-area.
     */
    public AABBic bounds()
    {
        return this.bounds;
    }
    
    /**
     * @return A read-only framebuffer view transformation matrix for this window.
     */
    public Matrix4dc viewMatrix()
    {
        return this.viewMatrix;
    }
    
    /**
     * Retrieves the size, in screen coordinates, of each edge of the frame of
     * the window. This size includes the title bar, if the window has one. The
     * size of the frame may vary depending on the
     * <a target="_blank" href="http://www.glfw.org/docs/latest/window.html#window-hints_wnd">window-related hints</a>
     * used to create it.
     * <p>
     * Because this function retrieves the size of each window frame edge and
     * not the offset along a particular coordinate axis, the retrieved values
     * will always be zero or positive.
     *
     * @return An {@link Integer} array with the edge sizes: {@code {left, top, right, bottom}}
     */
    @SuppressWarnings("ConstantConditions")
    public int @NotNull [] getFrameSize()
    {
        return Engine.Delegator.waitReturnTask(() -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer left   = stack.callocInt(1);
                IntBuffer top    = stack.callocInt(1);
                IntBuffer right  = stack.callocInt(1);
                IntBuffer bottom = stack.callocInt(1);
                
                glfwGetWindowFrameSize(this.handle, left, top, right, bottom);
                
                return new int[] {left.get(), top.get(), right.get(), bottom.get()};
            }
        });
    }
    
    /**
     * Sets the system clipboard to the specified, UTF-8 encoded string.
     * <p>
     * The specified string is copied before this function returns.
     *
     * @param string a UTF-8 encoded string
     */
    public void setClipboard(ByteBuffer string)
    {
        Engine.Delegator.runTask(() -> glfwSetClipboardString(this.handle, string));
    }
    
    /**
     * Sets the system clipboard to the specified, UTF-8 encoded string.
     * <p>
     * The specified string is copied before this function returns.
     *
     * @param string a UTF-8 encoded string
     */
    public void setClipboard(CharSequence string)
    {
        Engine.Delegator.runTask(() -> glfwSetClipboardString(this.handle, string));
    }
    
    /**
     * Returns the contents of the system clipboard, if it contains or is
     * convertible to a UTF-8 encoded string. If the clipboard is empty or if
     * its contents cannot be converted, {@code null} is returned and a
     * {@code FORMAT_UNAVAILABLE} error is generated.
     *
     * @return the contents of the clipboard as a UTF-8 encoded string, or
     * {@code null} if an error occurred
     */
    @Nullable
    public String getClipboard()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetClipboardString(this.handle));
    }
    
    /**
     * Returns the contents of the system clipboard, if it contains or is
     * convertible to a UTF-8 encoded string. If the clipboard is empty or if
     * its contents cannot be converted, {@link MemoryUtil#NULL NULL} is
     * returned and a {@code FORMAT_UNAVAILABLE} error is generated.
     *
     * @return the contents of the clipboard as a UTF-8 encoded string, or
     * {@code null} if an error occurred
     */
    @SuppressWarnings("ConstantConditions")
    public long getClipboardRaw()
    {
        return Engine.Delegator.waitReturnTask(() -> nglfwGetClipboardString(this.handle));
    }
    
    // -------------------- State Properties -------------------- //
    
    /**
     * @return Retrieves the vsync status for the current OpenGL or OpenGL ES context
     */
    public boolean vsync()
    {
        return this.vsync;
    }
    
    /**
     * Sets the vsync status for the current OpenGL or OpenGL ES context, i.e.
     * the number of screen updates to wait from the time
     * {@link org.lwjgl.glfw.GLFW#glfwSwapBuffers SwapBuffers} was called
     * before swapping the buffers and returning.
     *
     * @param vsync the new vsync status
     */
    public void vsync(boolean vsync)
    {
        this.vsyncChanges = vsync;
    }
    
    /**
     * Retrieves if the window has input focus.
     *
     * @return if the window has input focus
     */
    public boolean focused()
    {
        return this.focused;
    }
    
    /**
     * Brings the window to front and sets input focus. The window should
     * already be visible and not iconified.
     * <p>
     * By default, both windowed and full screen mode windows are focused when
     * initially created. Set the {@link Builder#focused(Boolean)} FOCUSED}
     * flag to disable this behavior.
     * <p>
     * Also by default, windowed mode windows are focused when shown with
     * {@link #show}. Set the {@link Builder#focusOnShow(boolean)} window hint
     * to disable this behavior.
     * <p>
     * <b>Do not use this function</b> to steal focus from other applications
     * unless you are certain that is what the user wants. Focus stealing can
     * be extremely disruptive.
     * <p>
     * For a less disruptive way of getting the user's attention, see
     * {@link #requestFocus}.
     */
    public void focus()
    {
        Engine.Delegator.runTask(() -> glfwFocusWindow(this.handle));
    }
    
    /**
     * Requests user attention to the window.
     * <p>
     * This function requests user attention to the window. On platforms where
     * this is not supported, attention is requested to the application as a
     * whole.
     * <p>
     * Once the user has given attention, usually by focusing the window or
     * application, the system will end the request automatically.
     */
    public void requestFocus()
    {
        Engine.Delegator.runTask(() -> glfwRequestWindowAttention(this.handle));
    }
    
    /**
     * @return Retrieves whether the window is iconified, whether by the user or with {@link #iconify}.
     */
    public boolean iconified()
    {
        return this.iconified;
    }
    
    /**
     * Iconifies (minimizes) the window if it was previously restored. If the
     * window is already iconified, this function does nothing.
     * <p>
     * If the window is a full screen window, the original monitor resolution
     * is restored until the window is restored.
     */
    public void iconify()
    {
        Engine.Delegator.runTask(() -> glfwIconifyWindow(this.handle));
    }
    
    /**
     * @return Retrieves whether the window is maximized, whether by the user or {@link #maximize}.
     */
    public boolean maximized()
    {
        return this.maximized;
    }
    
    /**
     * Maximizes the window if it was previously not maximized. If the window
     * is already maximized, this function does nothing.
     * <p>
     * If the window is a full screen window, this function does nothing.
     */
    public void maximize()
    {
        Engine.Delegator.runTask(() -> glfwMaximizeWindow(this.handle));
    }
    
    /**
     * Retrieves the position, in screen coordinates, of the upper-left corner
     * of the content area of the window.
     *
     * @return The position of the upper-left corner of the content area
     */
    public Vector2ic pos()
    {
        return this.pos;
    }
    
    /**
     * Retrieves the x-coordinate of the position, in screen coordinates, of
     * the upper-left corner of the content area of the window.
     *
     * @return The x-coordinate of the upper-left corner of the content area
     */
    public int x()
    {
        return this.pos.x;
    }
    
    /**
     * Retrieves the y-coordinate of the position, in screen coordinates, of
     * the upper-left corner of the content area of the window.
     *
     * @return The y-coordinate of the upper-left corner of the content area
     */
    public int y()
    {
        return this.pos.y;
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
    public void pos(int x, int y)
    {
        Engine.Delegator.waitRunTask(() -> glfwSetWindowPos(this.handle, x, y));
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
    public void pos(@NotNull Vector2ic pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Retrieves the size, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The size, in screen coordinates, of the content area.
     */
    public Vector2ic size()
    {
        return this.size;
    }
    
    /**
     * Retrieves the width, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The width, in screen coordinates, of the content area.
     */
    public int width()
    {
        return this.size.x;
    }
    
    /**
     * Retrieves the height, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The height, in screen coordinates, of the content area.
     */
    public int height()
    {
        return this.size.y;
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
    public void size(int width, int height)
    {
        Engine.Delegator.waitRunTask(() -> glfwSetWindowSize(this.handle, width, height));
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
    public void size(@NotNull Vector2ic size)
    {
        size(size.x(), size.y());
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
    public Vector2dc contentScale()
    {
        return this.scale;
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
    public double contentScaleX()
    {
        return this.scale.x;
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
    public double contentScaleY()
    {
        return this.scale.y;
    }
    
    /**
     * Retrieves the size, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The size, in pixels, of the framebuffer
     */
    public Vector2ic framebufferSize()
    {
        return this.fbSize;
    }
    
    /**
     * Retrieves the width, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The width, in pixels, of the framebuffer
     */
    public int framebufferWidth()
    {
        return this.fbSize.x;
    }
    
    /**
     * Retrieves the height, in pixels, of the framebuffer of the specified
     * window. If you wish to retrieve the size of the window in screen
     * coordinates, see {@link #size}.
     *
     * @return The height, in pixels, of the framebuffer
     */
    public int framebufferHeight()
    {
        return this.fbSize.y;
    }
    
    // -------------------- Utility Methods -------------------- //
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the screen origin.
     *
     * @param x   The x coordinate of the window point
     * @param y   The y coordinate of the window point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToScreen(double x, double y, @NotNull Vector2d out)
    {
        out.x = x - x();
        out.y = y - y();
        return out;
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the screen origin.
     *
     * @param pos The window point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToScreen(@NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return windowToScreen(pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the screen origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param x The x coordinate of the window point
     * @param y The y coordinate of the window point
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToScreen(double x, double y)
    {
        return windowToScreen(x, y, this.windowToScreen);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the screen origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param pos The window point
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToScreen(@NotNull Vector2dc pos)
    {
        return windowToScreen(pos.x(), pos.y(), this.windowToScreen);
    }
    
    /**
     * Converts a point relative to the screen origin to a point relative to
     * the window origin.
     *
     * @param x   The x coordinate of the screen point
     * @param y   The y coordinate of the screen point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc screenToWindow(double x, double y, @NotNull Vector2d out)
    {
        out.x = x + x();
        out.y = y + y();
        return out;
    }
    
    /**
     * Converts a point relative to the screen origin to a point relative to
     * the window origin.
     *
     * @param pos The screen point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc screenToWindow(@NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return screenToWindow(pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the screen origin to a point relative to
     * the window origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param x The x coordinate of the screen point
     * @param y The y coordinate of the screen point
     * @return The results
     */
    @NotNull
    public Vector2dc screenToWindow(double x, double y)
    {
        return screenToWindow(x, y, this.screenToWindow);
    }
    
    /**
     * Converts a point relative to the screen origin to a point relative to
     * the window origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param pos The screen point
     * @return The results
     */
    @NotNull
    public Vector2dc screenToWindow(@NotNull Vector2dc pos)
    {
        return screenToWindow(pos.x(), pos.y(), this.screenToWindow);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the framebuffer origin.
     *
     * @param x   The x coordinate of the window point
     * @param y   The y coordinate of the window point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToFramebuffer(double x, double y, @NotNull Vector2d out)
    {
        out.x = (x * framebufferWidth() / width());
        out.y = (y * framebufferHeight() / height());
        return out;
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the framebuffer origin.
     *
     * @param pos The window point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc windowToFramebuffer(@NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return windowToFramebuffer(pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the framebuffer origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param x The x coordinate of the window point
     * @param y The y coordinate of the window point
     * @return The results.
     */
    @NotNull
    public Vector2dc windowToFramebuffer(double x, double y)
    {
        return windowToFramebuffer(x, y, this.windowToFramebuffer);
    }
    
    /**
     * Converts a point relative to the window origin to a point relative to
     * the framebuffer origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param pos The window point
     * @return The results.
     */
    @NotNull
    public Vector2dc windowToFramebuffer(@NotNull Vector2dc pos)
    {
        return windowToFramebuffer(pos.x(), pos.y(), this.windowToFramebuffer);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative to
     * the window origin.
     *
     * @param x   The x coordinate of the framebuffer point
     * @param y   The y coordinate of the framebuffer point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc framebufferToWindow(double x, double y, @NotNull Vector2d out)
    {
        out.x = (x * width() / framebufferWidth());
        out.y = (y * height() / framebufferHeight());
        return out;
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative to
     * the window origin.
     *
     * @param pos The framebuffer point
     * @param out The vector to store the results
     * @return The results stored in {@code out}.
     */
    @NotNull
    public Vector2dc framebufferToWindow(@NotNull Vector2dc pos, @NotNull Vector2d out)
    {
        return framebufferToWindow(pos.x(), pos.y(), out);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative to
     * the window origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param x The x coordinate of the framebuffer point
     * @param y The y coordinate of the framebuffer point
     * @return The results.
     */
    @NotNull
    public Vector2dc framebufferToWindow(double x, double y)
    {
        return framebufferToWindow(x, y, this.framebufferToWindow);
    }
    
    /**
     * Converts a point relative to the framebuffer origin to a point relative to
     * the window origin.
     * <p>
     * <b>Note:</b> The results are only valid until the next time this is
     * called.
     *
     * @param pos The framebuffer point
     * @return The results.
     */
    @NotNull
    public Vector2dc framebufferToWindow(@NotNull Vector2dc pos)
    {
        return framebufferToWindow(pos.x(), pos.y(), this.framebufferToWindow);
    }
    
    // -------------------- Updating -------------------- //
    
    public void swap()
    {
        glfwSwapBuffers(this.handle);
    }
    
    // -------------------- State Updating -------------------- //
    
    void processEvents(long time)
    {
        boolean updateMonitor = false;
        
        if (this.vsync != this.vsyncChanges)
        {
            this.vsync = this.vsyncChanges;
            glfwSwapInterval(this.vsync ? 1 : 0);
            Engine.Events.post(EventWindowVsyncChanged.create(time, this, this.vsync));
        }
        
        if (this.focused != this.focusedChanges)
        {
            this.focused = this.focusedChanges;
            Engine.Events.post(EventWindowFocused.create(time, this, this.focused));
        }
        
        if (this.iconified != this.iconifiedChanges)
        {
            this.iconified = this.iconifiedChanges;
            Engine.Events.post(EventWindowIconified.create(time, this, this.iconified));
        }
        
        if (this.maximized != this.maximizedChanges)
        {
            this.maximized = this.maximizedChanges;
            Engine.Events.post(EventWindowMaximized.create(time, this, this.maximized));
        }
        
        if (this.pos.x != this.posChanges.x || this.pos.y != this.posChanges.y)
        {
            this.posChanges.sub(this.pos, this.deltaI);
            this.pos.set(this.posChanges);
            Engine.Events.post(EventWindowMoved.create(time, this, this.pos, this.deltaI));
            
            updateMonitor = true;
        }
        
        if (this.size.x != this.sizeChanges.x || this.size.y != this.sizeChanges.y)
        {
            this.sizeChanges.sub(this.size, this.deltaI);
            this.size.set(this.sizeChanges);
            Engine.Events.post(EventWindowResized.create(time, this, this.size, this.deltaI));
            
            updateMonitor = true;
        }
        
        if (Double.compare(this.scale.x, this.scaleChanges.x) != 0 || Double.compare(this.scale.y, this.scaleChanges.y) != 0)
        {
            this.scaleChanges.sub(this.scale, this.deltaD);
            this.scale.set(this.scaleChanges);
            Engine.Events.post(EventWindowContentScaleChanged.create(time, this, this.scale, this.deltaD));
        }
        
        if (this.fbSize.x != this.fbSizeChanges.x || this.fbSize.y != this.fbSizeChanges.y)
        {
            this.fbSizeChanges.sub(this.fbSize, this.deltaI);
            this.fbSize.set(this.fbSizeChanges);
            Engine.Events.post(EventWindowFramebufferResized.create(time, this, this.fbSize, this.deltaI));
            
            this.viewMatrix.setOrtho(0, this.fbSize.x, this.fbSize.y, 0, -1F, 1F);
        }
        
        if (this.shouldClose)
        {
            this.open = false;
            Engine.Events.post(EventWindowClosed.create(time, this));
        }
        
        if (this.shouldRefresh)
        {
            this.shouldRefresh = false;
            Engine.Events.post(EventWindowRefreshed.create(time, this));
        }
        
        if (this.dropped != null)
        {
            Path[] paths = new Path[this.dropped.length];
            for (int i = 0; i < this.dropped.length; i++) paths[i] = Paths.get(this.dropped[i]);
            this.dropped = null;
            Engine.Events.post(EventWindowDropped.create(time, this, paths));
        }
        
        if (updateMonitor)
        {
            Monitor prevMonitor = this.monitor;
            
            int overlap, maxOverlap = 0;
            for (Monitor monitor : Monitor.monitors.values())
            {
                if ((overlap = monitor.windowOverlap(this)) > maxOverlap)
                {
                    maxOverlap   = overlap;
                    this.monitor = monitor;
                }
            }
            
            if (this.monitor != prevMonitor)
            {
                Engine.Events.post(EventWindowMonitorChanged.create(time, this, prevMonitor, this.monitor));
            }
        }
    }
    
    void releaseCallbacks()
    {
        Window.LOGGER.finer("Destroying", this);
        
        Engine.Delegator.waitRunTask(() -> {
            glfwFreeCallbacks(this.handle);
            glfwDestroyWindow(this.handle);
        });
    }
    
    // -------------------- Utility Classes -------------------- //
    
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static final class Builder
    {
        private String name = null;
        
        private Monitor monitor = null;
        
        private Vector2ic position = null;
        private Vector2ic size     = new Vector2i(800, 600);
        private Vector2ic minSize  = new Vector2i(GLFW_DONT_CARE);
        private Vector2ic maxSize  = new Vector2i(GLFW_DONT_CARE);
        
        private boolean windowed = true;
        private boolean vsync    = false;
        
        private String title = null;
        
        private Boolean resizable              = null;
        private Boolean visible                = null;
        private Boolean decorated              = null;
        private Boolean focused                = null;
        private Boolean autoIconify            = null;
        private Boolean floating               = null;
        private Boolean maximized              = null;
        private Boolean centerCursor           = null;
        private Boolean transparentFramebuffer = null;
        private Boolean focusOnShow            = null;
        private Boolean mousePassthrough       = null;
        private Boolean scaleToMonitor         = null;
        
        private Integer redBits        = null;
        private Integer greenBits      = null;
        private Integer blueBits       = null;
        private Integer alphaBits      = null;
        private Integer depthBits      = null;
        private Integer stencilBits    = null;
        private Integer accumRedBits   = null;
        private Integer accumGreenBits = null;
        private Integer accumBlueBits  = null;
        private Integer accumAlphaBits = null;
        private Integer auxBuffers     = null;
        private Boolean stereo         = null;
        private Integer refreshRate    = null;
        private Integer samples        = null;
        private Boolean srgbCapable    = null;
        private Boolean doublebuffer   = null;
        
        private ClientAPI       clientApi       = null;
        private CreationAPI     creationApi     = null;
        private Integer         versionMajor    = null;
        private Integer         versionMinor    = null;
        private Boolean         forwardCompat   = null;
        private Boolean         debugContext    = null;
        private OpenGLProfile   profile         = null;
        private Robustness      robustness      = null;
        private ReleaseBehavior releaseBehavior = null;
        private Boolean         noError         = null;
        
        private Boolean win32KeyboardMenu = null;
        
        private Boolean cocoaRetinaFramebuffer = null;
        private String  cocoaFrameName         = null;
        private Boolean cocoaGraphicsSwitching = null;
        
        private String x11ClassName    = null;
        private String x11InstanceName = null;
        
        private Boolean lockKeyMods = null;
        
        /**
         * @return A new window with the properties provided by the builder.
         */
        public @NotNull Window build()
        {
            return Objects.requireNonNull(Engine.Delegator.waitReturnTask(() -> new Window(this)));
        }
        
        /**
         * Sets the name of the window. This is currently only used to name the
         * thread.
         *
         * @param name The name of the window.
         * @return This instance for call chaining.
         */
        public Builder name(String name)
        {
            this.name = name;
            return this;
        }
        
        /**
         * Sets the initial monitor that the window will be placed in, or the
         * primary monitor if {@code null} supplied.
         *
         * @param monitor The initial monitor that the window will be placed in.
         * @return This instance for call chaining.
         */
        public Builder monitor(Monitor monitor)
        {
            this.monitor = monitor;
            return this;
        }
        
        /**
         * Sets the initial position, in screen coordinates, of the upper-left
         * corner of the content area of the windowed mode window.
         *
         * @param pos The initial position of the window.
         * @return This instance for call chaining.
         */
        public Builder position(Vector2ic pos)
        {
            this.position = pos;
            return this;
        }
        
        /**
         * Sets the initial position, in screen coordinates, of the upper-left
         * corner of the content area of the windowed mode window.
         *
         * @param x The initial x coordinate of the window.
         * @param y The initial y coordinate of the window.
         * @return This instance for call chaining.
         */
        public Builder position(int x, int y)
        {
            this.position = new Vector2i(x, y);
            return this;
        }
        
        /**
         * This function sets the initial size, in screen coordinates, of the
         * content area of the window.
         * <p>
         * For full screen windows, this function updates the resolution of its
         * desired video mode and switches to the video mode closest to it,
         * without affecting the window's context. As the context is
         * unaffected, the bit depths of the framebuffer remain unchanged.
         *
         * @param size The initial size of the window.
         * @return This instance for call chaining.
         */
        public Builder size(Vector2ic size)
        {
            this.size = size;
            return this;
        }
        
        /**
         * This function sets the initial size, in screen coordinates, of the
         * content area of the window.
         * <p>
         * For full screen windows, this function updates the resolution of its
         * desired video mode and switches to the video mode closest to it,
         * without affecting the window's context. As the context is
         * unaffected, the bit depths of the framebuffer remain unchanged.
         *
         * @param width  The initial width of the window.
         * @param height The initial height of the window.
         * @return This instance for call chaining.
         */
        public Builder size(int width, int height)
        {
            this.size = new Vector2i(width, height);
            return this;
        }
        
        /**
         * This function sets the minimum size of the content area of the
         * window. If the window is full screen, the size limits only take
         * effect once it is made windowed. If the window is not resizable,
         * this function does nothing.
         * <p>
         * The size limits are applied immediately to a windowed mode window and
         * may cause it to be resized.
         * <p>
         * The maximum dimensions must be greater than or equal to the minimum
         * dimensions and all must be greater than or equal to zero.
         *
         * @param size The minimum size, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @return This instance for call chaining.
         */
        public Builder minSize(Vector2ic size)
        {
            this.minSize = size;
            return this;
        }
        
        /**
         * This function sets the minimum size of the content area of the
         * window. If the window is full screen, the size limits only take
         * effect once it is made windowed. If the window is not resizable,
         * this function does nothing.
         * <p>
         * The size limits are applied immediately to a windowed mode window and
         * may cause it to be resized.
         * <p>
         * The maximum dimensions must be greater than or equal to the minimum
         * dimensions and all must be greater than or equal to zero.
         *
         * @param width  The minimum width, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @param height The minimum height, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @return This instance for call chaining.
         */
        public Builder minSize(int width, int height)
        {
            this.minSize = new Vector2i(width, height);
            return this;
        }
        
        /**
         * This function sets the maximum size of the content area of the
         * window. If the window is full screen, the size limits only take
         * effect once it is made windowed. If the window is not resizable,
         * this function does nothing.
         * <p>
         * The size limits are applied immediately to a windowed mode window and
         * may cause it to be resized.
         * <p>
         * The maximum dimensions must be greater than or equal to the minimum
         * dimensions and all must be greater than or equal to zero.
         *
         * @param size The maximum size, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @return This instance for call chaining.
         */
        public Builder maxSize(Vector2ic size)
        {
            this.maxSize = size;
            return this;
        }
        
        /**
         * This function sets the maximum size of the content area of the
         * window. If the window is full screen, the size limits only take
         * effect once it is made windowed. If the window is not resizable,
         * this function does nothing.
         * <p>
         * The size limits are applied immediately to a windowed mode window and
         * may cause it to be resized.
         * <p>
         * The maximum dimensions must be greater than or equal to the minimum
         * dimensions and all must be greater than or equal to zero.
         *
         * @param width  The maximum width, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @param height The maximum height, in screen coordinates, of the content area, or {@link Window#DONT_CARE}.
         * @return This instance for call chaining.
         */
        public Builder maxSize(int width, int height)
        {
            this.maxSize = new Vector2i(width, height);
            return this;
        }
        
        /**
         * This function sets if the window will be windowed or not
         *
         * @param windowed if the window will be windowed.
         * @return This instance for call chaining.
         */
        public Builder windowed(boolean windowed)
        {
            this.windowed = windowed;
            return this;
        }
        
        /**
         * This function sets if the window's frame rate should be locked to its monitor's refresh rate.
         *
         * @param vsync if the window will be locked the its monitor's refresh rate..
         * @return This instance for call chaining.
         */
        public Builder vsync(boolean vsync)
        {
            this.vsync = vsync;
            return this;
        }
        
        /**
         * This function sets the initial title of the window.
         *
         * @param title the initial title of the window.
         * @return This instance for call chaining.
         */
        public Builder title(String title)
        {
            this.title = title;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will be resizable by the
         * user. The window will still be resizable using the glfwSetWindowSize
         * function. Possible values are {@code true}, {@code false} and
         * {@code null} for system default. This hint is ignored for full
         * screen and undecorated windows.
         *
         * @param resizable if the windowed mode window will be resizable by the user. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder resizable(Boolean resizable)
        {
            this.resizable = resizable;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will be initially
         * visible. Possible values are {@code true}, {@code false} and
         * {@code null} for system default. This hint is ignored for full
         * screen and undecorated windows.
         *
         * @param visible if the windowed mode window will be initially visible. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder visible(Boolean visible)
        {
            this.visible = visible;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will have window
         * decorations such as a border, a close widget, etc. An undecorated
         * window will not be resizable by the user but will still allow the
         * user to generate close events on some platforms. Possible values are
         * {@code true}, {@code false} and {@code null} for system default.
         * This hint is ignored for full screen and undecorated windows.
         *
         * @param decorated if the windowed mode window will have window decorations. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder decorated(Boolean decorated)
        {
            this.decorated = decorated;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will be given input focus
         * when created. Possible values are {@code true}, {@code false} and
         * {@code null} for system default.
         * This hint is ignored for full screen and initially hidden windows.
         *
         * @param focused if the windowed mode window will be given input focus when created. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder focused(Boolean focused)
        {
            this.focused = focused;
            return this;
        }
        
        /**
         * Specifies whether the full screen window will automatically iconify
         * and restore the previous video mode on input focus loss. Possible
         * values are {@code true}, {@code false} and {@code null} for system
         * default. This hint is ignored for windowed mode windows.
         *
         * @param autoIconify if the windowed mode window will automatically iconify. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder autoIconify(Boolean autoIconify)
        {
            this.autoIconify = autoIconify;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will be floating above
         * other regular windows, also called topmost or always-on-top. This is
         * intended primarily for debugging purposes and cannot be used to
         * implement proper full screen windows. Possible values are
         * {@code true}, {@code false} and {@code null} for system default.
         * This hint is ignored for full screen windows.
         *
         * @param floating if the windowed mode window will be floating above other regular windows. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder floating(Boolean floating)
        {
            this.floating = floating;
            return this;
        }
        
        /**
         * Specifies whether the windowed mode window will be maximized when
         * created. Possible values are {@code true}, {@code false} and
         * {@code null} for system default. This hint is ignored for full
         * screen windows.
         *
         * @param maximized if the windowed mode window will be maximized when created. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder maximized(Boolean maximized)
        {
            this.maximized = maximized;
            return this;
        }
        
        /**
         * Specifies whether the cursor should be centered over newly created
         * full screen windows. Possible values are {@code true}, {@code false}
         * and {@code null} for system default. This hint is ignored for
         * windowed mode windows.
         *
         * @param centerCursor if the cursor should be centered over newly created full screen windows. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder centerCursor(Boolean centerCursor)
        {
            this.centerCursor = centerCursor;
            return this;
        }
        
        /**
         * Specifies whether the window framebuffer will be transparent. If
         * enabled and supported by the system, the window framebuffer alpha
         * channel will be used to combine the framebuffer with the background.
         * This does not affect window decorations. Possible values are
         * {@code true}, {@code false} and {@code null} for system default.
         *
         * <ul><b>Windows:</b> GLFW sets a color key for the window to work
         * around repainting issues with a transparent framebuffer. The chosen
         * color value is RGB 255,0,255 (magenta). This will make pixels with
         * that exact color fully transparent regardless of their alpha values.
         * If this is a problem, make these pixels any other color before
         * buffer swap.</ul>
         *
         * @param transparentFramebuffer if the window framebuffer will be transparent. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder transparentFramebuffer(Boolean transparentFramebuffer)
        {
            this.transparentFramebuffer = transparentFramebuffer;
            return this;
        }
        
        /**
         * Specifies whether the window will be given input focus when
         * glfwShowWindow is called. Possible values are {@code true},
         * {@code false} and {@code null} for system default.
         *
         * @param focusOnShow if the window will be given input focus when glfwShowWindow is called. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder focusOnShow(Boolean focusOnShow)
        {
            this.focusOnShow = focusOnShow;
            return this;
        }
        
        /**
         * Specifies whether the window is transparent to mouse input, letting
         * any mouse events pass through to whatever window is behind it. This
         * is only supported for undecorated windows. Decorated windows with
         * this enabled will behave differently between platforms. Possible
         * values are {@code true}, {@code false} and {@code null} for system
         * default.
         *
         * @param mousePassthrough if the window is transparent to mouse input. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder mousePassthrough(boolean mousePassthrough)
        {
            this.mousePassthrough = mousePassthrough;
            return this;
        }
        
        /**
         * Specifies whether the window content area should be resized based on
         * the monitor content scale of any monitor it is placed on. This
         * includes the initial placement when the window is created. Possible
         * values are {@code true}, {@code false} and {@code null} for system
         * default.
         * <p>
         * This hint only has an effect on platforms where screen coordinates
         * and pixels always map 1:1 such as Windows and X11. On platforms like
         * macOS the resolution of the framebuffer is changed independently of
         * the window size.
         *
         * @param scaleToMonitor if the window content area should be resized based on the monitor content scale. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder scaleToMonitor(Boolean scaleToMonitor)
        {
            this.scaleToMonitor = scaleToMonitor;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the red component of the default
         * framebuffer. A value of {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param redBits the desired bit depth of the red component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder redBits(Integer redBits)
        {
            this.redBits = redBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the green component of the
         * default framebuffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param greenBits the desired bit depth of the green component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder greenBits(Integer greenBits)
        {
            this.greenBits = greenBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the blue component of the default
         * framebuffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param blueBits the desired bit depth of the blue component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder blueBits(Integer blueBits)
        {
            this.blueBits = blueBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the alpha component of the
         * default framebuffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param alphaBits the desired bit depth of the alpha component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder alphaBits(Integer alphaBits)
        {
            this.alphaBits = alphaBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the depth component of the
         * default framebuffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param depthBits the desired bit depth of the depth component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder depthBits(Integer depthBits)
        {
            this.depthBits = depthBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the stencil component of the
         * default framebuffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param stencilBits the desired bit depth of the stencil component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder stencilBits(Integer stencilBits)
        {
            this.stencilBits = stencilBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the red components of the
         * accumulation buffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         * <ul>Accumulation buffers are a legacy OpenGL feature and should not
         * be used in new code.</ul>
         *
         * @param accumRedBits the desired bit depth of the red component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder accumRedBits(Integer accumRedBits)
        {
            this.accumRedBits = accumRedBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the green components of the
         * accumulation buffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         * <ul>Accumulation buffers are a legacy OpenGL feature and should not
         * be used in new code.</ul>
         *
         * @param accumGreenBits the desired bit depth of the green component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder accumGreenBits(Integer accumGreenBits)
        {
            this.accumGreenBits = accumGreenBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the blue components of the
         * accumulation buffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         * <ul>Accumulation buffers are a legacy OpenGL feature and should not
         * be used in new code.</ul>
         *
         * @param accumBlueBits the desired bit depth of the blue component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder accumBlueBits(Integer accumBlueBits)
        {
            this.accumBlueBits = accumBlueBits;
            return this;
        }
        
        /**
         * Specifies the desired bit depth of the alpha components of the
         * accumulation buffer. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         * <ul>Accumulation buffers are a legacy OpenGL feature and should not
         * be used in new code.</ul>
         *
         * @param accumAlphaBits the desired bit depth of the alpha component. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder accumAlphaBits(Integer accumAlphaBits)
        {
            this.accumAlphaBits = accumAlphaBits;
            return this;
        }
        
        /**
         * Specifies the desired number of auxiliary buffers. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * <ul>Auxiliary buffers are a legacy OpenGL feature and should not be
         * used in new code.</ul>
         *
         * @param auxBuffers the desired number of auxiliary buffers. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder auxBuffers(Integer auxBuffers)
        {
            this.auxBuffers = auxBuffers;
            return this;
        }
        
        /**
         * Specifies whether to use OpenGL stereoscopic rendering. Possible
         * values are {@code true}, {@code false} and {@code null} for system
         * default. This is a hard constraint.
         *
         * @param stereo if to use OpenGL stereoscopic rendering. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder stereo(Boolean stereo)
        {
            this.stereo = stereo;
            return this;
        }
        
        /**
         * Specifies the desired number of samples to use for multisampling.
         * Zero disables multisampling. A value of
         * {@link Window#DONT_CARE} means the
         * application has no preference.
         *
         * @param samples the desired number of samples. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder samples(Integer samples)
        {
            this.samples = samples;
            return this;
        }
        
        /**
         * Specifies whether the framebuffer should be sRGB capable. Possible
         * values are {@code true}, {@code false} and {@code null} for system
         * default. This is a hard constraint.
         *
         * <ul><b>OpenGL:</b> If enabled and supported by the system, the
         * {@code GL_FRAMEBUFFER_SRGB} enable will control sRGB rendering. By
         * default, sRGB rendering will be disabled.
         * <p>
         * <b>OpenGL ES:</b> If enabled and supported by the system, the
         * context will always have sRGB rendering enabled.</ul>
         *
         * @param srgbCapable if the framebuffer should be sRGB capable. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder srgbCapable(Boolean srgbCapable)
        {
            this.srgbCapable = srgbCapable;
            return this;
        }
        
        /**
         * Specifies whether the framebuffer should be double buffered. You
         * nearly always want to use double buffering. Possible values are
         * {@code true}, {@code false} and {@code null} for system default.
         * This is a hard constraint.
         *
         * @param doublebuffer if the framebuffer should be double buffered. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder doublebuffer(Boolean doublebuffer)
        {
            this.doublebuffer = doublebuffer;
            return this;
        }
        
        /**
         * Specifies the desired refresh rate for full screen windows. A value
         * of {@link Window#DONT_CARE} means the
         * highest available refresh rate will be used. This hint is ignored
         * for windowed mode windows.
         *
         * @param refreshRate the desired refresh rate for full screen windows. In the range:<br>0 to {@link Integer#MAX_VALUE} or {@link Window#DONT_CARE}
         * @return This instance for call chaining.
         */
        public Builder refreshRate(Integer refreshRate)
        {
            this.refreshRate = refreshRate;
            return this;
        }
        
        /**
         * Specifies which client API to create the context for. Possible
         * values are {@link ClientAPI#NONE NONE},
         * {@link ClientAPI#OPENGL OPENGL} and
         * {@link ClientAPI#OPENGL_ES OPENGL_ES}. This is a hard constraint.
         *
         * @param clientApi the client API to create the context for. One of:<br>{@link ClientAPI#NONE NONE}, {@link ClientAPI#OPENGL OPENGL}, {@link ClientAPI#OPENGL_ES OPENGL_ES}
         * @return This instance for call chaining.
         */
        public Builder clientApi(ClientAPI clientApi)
        {
            this.clientApi = clientApi;
            return this;
        }
        
        /**
         * Specifies which context creation API to use to create the context.
         * Possible values are {@link CreationAPI#NATIVE NATIVE},
         * {@link CreationAPI#EGL EGL} and {@link CreationAPI#OSMESA OSMESA}.
         * This is a hard constraint. If no client API is requested, this hint
         * is ignored.
         *
         * <ul><b>macOS:</b> The EGL API is not available on this platform and
         * requests to use it will fail.
         * <p>
         * <b>Wayland:</b> The EGL API is the native context creation API,
         * so this hint will have no effect.
         * <p>
         * <b>OSMesa:</b> As its name implies, an OpenGL context created
         * with OSMesa does not update the window contents when its buffers are
         * swapped. Use OpenGL functions or the OSMesa native access functions
         * glfwGetOSMesaColorBuffer and glfwGetOSMesaDepthBuffer to retrieve
         * the framebuffer contents.</ul>
         *
         * @param contextCreationApi the context creation API to use to create the context. One of:<br>{@link CreationAPI#NATIVE NATIVE}, {@link CreationAPI#EGL EGL}, {@link CreationAPI#OSMESA OSMESA}
         * @return This instance for call chaining.
         */
        public Builder contextCreationApi(CreationAPI contextCreationApi)
        {
            this.creationApi = contextCreationApi;
            return this;
        }
        
        /**
         * Specify the client API major version that the created context must
         * be compatible with. The exact behavior of these hints depend on the
         * requested client API.
         *
         * <ul><b>OpenGL:</b> These hints are not hard constraints, but
         * creation will fail if the OpenGL version of the created context is
         * less than the one requested. It is therefore perfectly safe to use
         * the default of version 1.0 for legacy code and you will still get
         * backwards-compatible contexts of version 3.0 and above when
         * available.
         * <p>
         * While there is no way to ask the driver for a context of the
         * highest supported version, GLFW will attempt to provide this when
         * you ask for a version 1.0 context, which is the default for these
         * hints.
         * <p>
         * <b>OpenGL ES:</b> These hints are not hard constraints, but
         * creation will fail if the OpenGL ES version of the created context
         * is less than the one requested. Additionally, OpenGL ES 1.x cannot
         * be returned if 2.0 or later was requested, and vice versa. This is
         * because OpenGL ES 3.x is backward compatible with 2.0, but OpenGL ES
         * 2.0 is not backward compatible with 1.x.</ul>
         *
         * @param contextVersionMajor the client API major version that the created context must be compatible with.
         * @return This instance for call chaining.
         */
        public Builder contextVersionMajor(Integer contextVersionMajor)
        {
            this.versionMajor = contextVersionMajor;
            return this;
        }
        
        /**
         * Specify the client API minor version that the created context must
         * be compatible with. The exact behavior of these hints depend on the
         * requested client API.
         *
         * <ul><b>OpenGL:</b> These hints are not hard constraints, but
         * creation will fail if the OpenGL version of the created context is
         * less than the one requested. It is therefore perfectly safe to use
         * the default of version 1.0 for legacy code and you will still get
         * backwards-compatible contexts of version 3.0 and above when
         * available.
         * <p>
         * While there is no way to ask the driver for a context of the
         * highest supported version, GLFW will attempt to provide this when
         * you ask for a version 1.0 context, which is the default for these
         * hints.
         * <p>
         * <b>OpenGL ES:</b> These hints are not hard constraints, but
         * creation will fail if the OpenGL ES version of the created context
         * is less than the one requested. Additionally, OpenGL ES 1.x cannot
         * be returned if 2.0 or later was requested, and vice versa. This is
         * because OpenGL ES 3.x is backward compatible with 2.0, but OpenGL ES
         * 2.0 is not backward compatible with 1.x.</ul>
         *
         * @param contextVersionMinor the client API minor version that the created context must be compatible with.
         * @return This instance for call chaining.
         */
        public Builder contextVersionMinor(Integer contextVersionMinor)
        {
            this.versionMinor = contextVersionMinor;
            return this;
        }
        
        /**
         * Specifies whether the OpenGL context should be forward-compatible
         * i.e. one where all functionality deprecated in the requested version
         * of OpenGL is removed. This must only be used if the requested OpenGL
         * version is 3.0 or above. If OpenGL ES is requested, this hint is
         * ignored. Possible values are {@code true}, {@code false} and
         * {@code null} for system default.
         *
         * <ul>Forward-compatibility is described in detail in the
         * <a href="https://www.opengl.org/registry/">OpenGL Reference Manual.
         * </a></ul>
         *
         * @param openglForwardCompat if the OpenGL context should be forward-compatible. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder openglForwardCompat(Boolean openglForwardCompat)
        {
            this.forwardCompat = openglForwardCompat;
            return this;
        }
        
        /**
         * Specifies whether the context should be created in debug mode,
         * which may provide additional error and diagnostic reporting
         * functionality. Possible values are {@code true}, {@code false} and
         * {@code null} for system default.
         *
         * <ul>Debug contexts for OpenGL and OpenGL ES are described in detail
         * by the <a href="https://www.opengl.org/registry/specs/KHR/context_flush_control.txt">GL_KHR_debug
         * extension.</a></ul>
         *
         * @param openglDebugContext if the context should be created in debug mode. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder openglDebugContext(Boolean openglDebugContext)
        {
            this.debugContext = openglDebugContext;
            return this;
        }
        
        /**
         * Specifies which OpenGL profile to create the context for. Possible
         * values are {@link OpenGLProfile#ANY ANY},
         * {@link OpenGLProfile#CORE CORE} and
         * {@link OpenGLProfile#COMPAT COMPAT} to not request a specific
         * profile. If requesting an OpenGL version below 3.2,
         * {@link OpenGLProfile#ANY ANY} must be used. If OpenGL ES is
         * requested, this hint is ignored.
         *
         * <ul>Forward-compatibility is described in detail in the
         * <a href="https://www.opengl.org/registry/">OpenGL Reference Manual.
         * </a></ul>
         *
         * @param openglProfile the OpenGL profile to create the context for. One of:<br>{@link OpenGLProfile#ANY ANY}, {@link OpenGLProfile#CORE CORE}, {@link OpenGLProfile#COMPAT COMPAT}
         * @return This instance for call chaining.
         */
        public Builder openglProfile(OpenGLProfile openglProfile)
        {
            this.profile = openglProfile;
            return this;
        }
        
        /**
         * Specifies the robustness strategy to be used by the context. This
         * can be one of {@link Robustness#NONE NONE},
         * {@link Robustness#NO_RESET NO_RESET} or
         * {@link Robustness#LOSE_ON_RESET LOSE_ON_RESET} to not request a
         * robustness strategy.
         *
         * @param contextRobustness the robustness strategy to be used by the context. One of:<br>{@link Robustness#NONE NONE}, {@link Robustness#NO_RESET NO_RESET}, {@link Robustness#LOSE_ON_RESET LOSE_ON_RESET}
         * @return This instance for call chaining.
         */
        public Builder contextRobustness(Robustness contextRobustness)
        {
            this.robustness = contextRobustness;
            return this;
        }
        
        /**
         * Specifies the release behavior to be used by the context. Possible
         * values are:
         * <ul>
         *     <li>{@link ReleaseBehavior#ANY ANY}: The default behavior of the context creation API</li>
         *     <li>{@link ReleaseBehavior#FLUSH FLUSH}: The pipeline will be flushed whenever the context is released from being the current one</li>
         *     <li>{@link ReleaseBehavior#NONE NONE}: The pipeline will not be flushed on released</li>
         * </ul>
         *
         * <ul>Context release behaviors are described in detail by the
         * <a href="https://www.opengl.org/registry/specs/KHR/no_error.txt">GL_KHR_context_flush_control</a>
         * extension.</ul>
         *
         * @param contextReleaseBehavior the release behavior to be used by the context. One of:<br>{@link ReleaseBehavior#ANY ANY}, {@link ReleaseBehavior#FLUSH FLUSH}, {@link ReleaseBehavior#NONE NONE}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder contextReleaseBehavior(ReleaseBehavior contextReleaseBehavior)
        {
            this.releaseBehavior = contextReleaseBehavior;
            return this;
        }
        
        /**
         * Specifies whether errors should be generated by the context.
         * Possible values are {@code true}, {@code false} and {@code null} for
         * system default. If enabled, situations that would have generated
         * errors instead cause undefined behavior.
         *
         * <ul>The no error mode for OpenGL and OpenGL ES is described in
         * detail by the <a href="https://www.opengl.org/registry/specs/KHR/no_error.txt">GL_KHR_no_error</a>
         * extension</ul>
         *
         * @param contextNoError if the context should be created in debug mode. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder contextNoError(Boolean contextNoError)
        {
            this.noError = contextNoError;
            return this;
        }
        
        /**
         * Specifies whether to allow access to the window menu via the
         * {@code Alt+Space} and {@code Alt-and-then-Space} keyboard shortcuts.
         * This is ignored on other platforms. Possible values are {@code true},
         * {@code false}, and {@code null} fpr system default.
         *
         * @param win32KeyboardMenu if to use full resolution framebuffers on Retina displays. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder win32KeyboardMenu(Boolean win32KeyboardMenu)
        {
            this.win32KeyboardMenu = win32KeyboardMenu;
            return this;
        }
        
        /**
         * Specifies whether to use full resolution framebuffers on Retina
         * displays. Possible values are {@code true}, {@code false} or
         * {@code null} for system default.
         *
         * @param cocoaRetinaFramebuffer if to use full resolution framebuffers on Retina displays. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder cocoaRetinaFramebuffer(Boolean cocoaRetinaFramebuffer)
        {
            this.cocoaRetinaFramebuffer = cocoaRetinaFramebuffer;
            return this;
        }
        
        /**
         * Specifies whether to use Automatic Graphics Switching, i.e. to allow
         * the system to choose the integrated GPU for the OpenGL context and
         * move it between GPUs if necessary or whether to force it to always
         * run on the discrete GPU. This only affects systems with both
         * integrated and discrete GPUs. Possible values are {@code true},
         * {@code false} and {@code null} for system default. This is ignored
         * on other platforms.
         *
         * @param cocoaGraphicsSwitching if to use Automatic Graphics Switching. One of:<br>{@code true}, {@code false}, {@code null}
         * @return This instance for call chaining.
         */
        public Builder cocoaGraphicsSwitching(Boolean cocoaGraphicsSwitching)
        {
            this.cocoaGraphicsSwitching = cocoaGraphicsSwitching;
            return this;
        }
        
        /**
         * Specifies the UTF-8 encoded name to use for autosaving the window
         * frame, or if empty disables frame autosaving for the window. This is
         * ignored on other platforms.
         *
         * @param cocoaFrameName the UTF-8 encoded name to use for autosaving the window frame
         * @return This instance for call chaining.
         */
        public Builder cocoaFrameName(String cocoaFrameName)
        {
            this.cocoaFrameName = cocoaFrameName;
            return this;
        }
        
        /**
         * Specifies the desired ASCII encoded class name of the ICCCM {@code WM_CLASS} window property.
         *
         * @param x11ClassName the desired ASCII encoded class name
         * @return This instance for call chaining.
         */
        public Builder x11ClassName(String x11ClassName)
        {
            this.x11ClassName = x11ClassName;
            return this;
        }
        
        /**
         * Specifies the desired ASCII encoded instance name of the ICCCM {@code WM_CLASS} window property.
         *
         * @param x11InstanceName the desired ASCII encoded instance name
         * @return This instance for call chaining.
         */
        public Builder x11InstanceName(String x11InstanceName)
        {
            this.x11InstanceName = x11InstanceName;
            return this;
        }
        
        /**
         * Specifies the lock mods flag.
         *
         * @param lockKeyMods the desired lock mods flag
         * @return This instance for call chaining.
         */
        public Builder lockKeyMods(boolean lockKeyMods)
        {
            this.lockKeyMods = lockKeyMods;
            return this;
        }
        
        private void applyHints()
        {
            glfwDefaultWindowHints();
            
            this.resizable              = applyBoolean(GLFW_RESIZABLE, this.resizable, true);
            this.visible                = applyBoolean(GLFW_VISIBLE, this.visible, true);
            this.decorated              = applyBoolean(GLFW_DECORATED, this.decorated, true);
            this.focused                = applyBoolean(GLFW_FOCUSED, this.focused, true);
            this.autoIconify            = applyBoolean(GLFW_AUTO_ICONIFY, this.autoIconify, true);
            this.floating               = applyBoolean(GLFW_FLOATING, this.floating, false);
            this.maximized              = applyBoolean(GLFW_MAXIMIZED, this.maximized, false);
            this.centerCursor           = applyBoolean(GLFW_CENTER_CURSOR, this.centerCursor, true);
            this.transparentFramebuffer = applyBoolean(GLFW_TRANSPARENT_FRAMEBUFFER, this.transparentFramebuffer, false);
            this.focusOnShow            = applyBoolean(GLFW_FOCUS_ON_SHOW, this.focusOnShow, true);
            this.mousePassthrough       = applyBoolean(GLFW_MOUSE_PASSTHROUGH, this.mousePassthrough, false);
            this.scaleToMonitor         = applyBoolean(GLFW_SCALE_TO_MONITOR, this.scaleToMonitor, false);
            
            this.redBits        = applyInteger(GLFW_RED_BITS, this.redBits, GLFW_DONT_CARE);
            this.greenBits      = applyInteger(GLFW_GREEN_BITS, this.greenBits, GLFW_DONT_CARE);
            this.blueBits       = applyInteger(GLFW_BLUE_BITS, this.blueBits, GLFW_DONT_CARE);
            this.alphaBits      = applyInteger(GLFW_ALPHA_BITS, this.alphaBits, GLFW_DONT_CARE);
            this.depthBits      = applyInteger(GLFW_DEPTH_BITS, this.depthBits, GLFW_DONT_CARE);
            this.stencilBits    = applyInteger(GLFW_STENCIL_BITS, this.stencilBits, GLFW_DONT_CARE);
            this.accumRedBits   = applyInteger(GLFW_ACCUM_RED_BITS, this.accumRedBits, GLFW_DONT_CARE);
            this.accumGreenBits = applyInteger(GLFW_ACCUM_GREEN_BITS, this.accumGreenBits, GLFW_DONT_CARE);
            this.accumBlueBits  = applyInteger(GLFW_ACCUM_BLUE_BITS, this.accumBlueBits, GLFW_DONT_CARE);
            this.accumAlphaBits = applyInteger(GLFW_ACCUM_ALPHA_BITS, this.accumAlphaBits, GLFW_DONT_CARE);
            this.auxBuffers     = applyInteger(GLFW_AUX_BUFFERS, this.auxBuffers, GLFW_DONT_CARE);
            this.samples        = applyInteger(GLFW_SAMPLES, this.samples, GLFW_DONT_CARE);
            this.refreshRate    = applyInteger(GLFW_REFRESH_RATE, this.refreshRate, GLFW_DONT_CARE);
            this.stereo         = applyBoolean(GLFW_STEREO, this.stereo, false);
            this.srgbCapable    = applyBoolean(GLFW_SRGB_CAPABLE, this.srgbCapable, false);
            this.doublebuffer   = applyBoolean(GLFW_DOUBLEBUFFER, this.doublebuffer, true);
            
            this.clientApi       = applyRef(GLFW_CLIENT_API, this.clientApi, ClientAPI.OPENGL);
            this.creationApi     = applyRef(GLFW_CONTEXT_CREATION_API, this.creationApi, CreationAPI.NATIVE);
            this.versionMajor    = applyInteger(GLFW_CONTEXT_VERSION_MAJOR, this.versionMajor, 1);
            this.versionMinor    = applyInteger(GLFW_CONTEXT_VERSION_MINOR, this.versionMinor, 0);
            this.forwardCompat   = applyBoolean(GLFW_OPENGL_FORWARD_COMPAT, this.forwardCompat, false);
            this.debugContext    = applyBoolean(GLFW_OPENGL_DEBUG_CONTEXT, this.debugContext, false);
            this.profile         = applyRef(GLFW_OPENGL_PROFILE, this.profile, OpenGLProfile.ANY);
            this.robustness      = applyRef(GLFW_CONTEXT_ROBUSTNESS, this.robustness, Robustness.NONE);
            this.releaseBehavior = applyRef(GLFW_CONTEXT_RELEASE_BEHAVIOR, this.releaseBehavior, ReleaseBehavior.ANY);
            this.noError         = applyBoolean(GLFW_CONTEXT_NO_ERROR, this.noError, false);
            
            this.win32KeyboardMenu = applyBoolean(GLFW_WIN32_KEYBOARD_MENU, this.win32KeyboardMenu, false);
            
            this.cocoaRetinaFramebuffer = applyBoolean(GLFW_COCOA_RETINA_FRAMEBUFFER, this.cocoaRetinaFramebuffer, true);
            this.cocoaGraphicsSwitching = applyBoolean(GLFW_COCOA_GRAPHICS_SWITCHING, this.cocoaGraphicsSwitching, true);
            applyString(GLFW_COCOA_FRAME_NAME, this.cocoaFrameName);
            
            applyString(GLFW_X11_CLASS_NAME, this.x11ClassName);
            applyString(GLFW_X11_INSTANCE_NAME, this.x11InstanceName);
            
            if (this.lockKeyMods == null) this.lockKeyMods = false;
        }
        
        private int applyInteger(int glfw, Integer value, int defaultValue)
        {
            if (value == null) return defaultValue;
            glfwWindowHint(glfw, value);
            return value;
        }
        
        private boolean applyBoolean(int glfw, Boolean value, boolean defaultValue)
        {
            if (value == null) return defaultValue;
            glfwWindowHint(glfw, value ? GLFW_TRUE : GLFW_FALSE);
            return value;
        }
        
        private <T extends Ref> T applyRef(int glfw, T value, T defaultValue)
        {
            if (value == null) return defaultValue;
            glfwWindowHint(glfw, value.ref());
            return value;
        }
        
        private void applyString(int glfw, String value)
        {
            if (value != null) glfwWindowHintString(glfw, value);
        }
    }
    
    private interface Ref
    {
        int ref();
    }
    
    public enum ClientAPI implements Ref
    {
        NONE(GLFW_NO_API),
        OPENGL(GLFW_OPENGL_API),
        OPENGL_ES(GLFW_OPENGL_ES_API),
        ;
        
        private final int ref;
        
        ClientAPI(int ref) {this.ref = ref;}
        
        @Override
        public int ref()
        {
            return this.ref;
        }
    }
    
    public enum CreationAPI implements Ref
    {
        NATIVE(GLFW_NATIVE_CONTEXT_API),
        EGL(GLFW_EGL_CONTEXT_API),
        OSMESA(GLFW_OSMESA_CONTEXT_API),
        ;
        
        private final int ref;
        
        CreationAPI(int ref) {this.ref = ref;}
        
        @Override
        public int ref()
        {
            return this.ref;
        }
    }
    
    public enum OpenGLProfile implements Ref
    {
        ANY(GLFW_OPENGL_ANY_PROFILE),
        CORE(GLFW_OPENGL_CORE_PROFILE),
        COMPAT(GLFW_OPENGL_COMPAT_PROFILE),
        ;
        
        private final int ref;
        
        OpenGLProfile(int ref) {this.ref = ref;}
        
        @Override
        public int ref()
        {
            return this.ref;
        }
    }
    
    public enum Robustness implements Ref
    {
        NONE(GLFW_NO_ROBUSTNESS),
        NO_RESET(GLFW_NO_RESET_NOTIFICATION),
        LOSE_ON_RESET(GLFW_LOSE_CONTEXT_ON_RESET),
        ;
        
        private final int ref;
        
        Robustness(int ref) {this.ref = ref;}
        
        @Override
        public int ref()
        {
            return this.ref;
        }
    }
    
    public enum ReleaseBehavior implements Ref
    {
        ANY(GLFW_ANY_RELEASE_BEHAVIOR),
        FLUSH(GLFW_RELEASE_BEHAVIOR_FLUSH),
        NONE(GLFW_RELEASE_BEHAVIOR_NONE),
        ;
        
        private final int ref;
        
        ReleaseBehavior(int ref) {this.ref = ref;}
        
        @Override
        public int ref()
        {
            return this.ref;
        }
    }
}
