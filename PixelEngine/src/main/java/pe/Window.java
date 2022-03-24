package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.event.*;
import pe.shape.AABB2i;
import pe.shape.AABB2ic;
import pe.texture.Image;
import rutils.Logger;
import rutils.group.Pair;

import java.lang.Math;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;

public class Window
{
    private static final Logger LOGGER = new Logger();
    
    public static final int DONT_CARE = GLFW_DONT_CARE;
    
    static Window primary = null;
    
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
        
        builder.contextVersionMajor(3);
        builder.contextVersionMinor(3);
        
        Boolean visible = builder.visible;
        if (builder.position != null) builder.visible(false);
        builder.applyHints();
        
        Window.monitor = builder.monitor != null ? builder.monitor : Monitor.primary();
        
        Window.windowed = builder.windowed;
        
        Window.name  = builder.name;
        Window.title = builder.title != null ? builder.title : Window.name != null ? Window.name : "Window";
        long monitor = Window.windowed ? MemoryUtil.NULL : Window.monitor.handle;
        long window  = MemoryUtil.NULL;
        // long   window  = Window.primary != null ? Window.primary.handle : MemoryUtil.NULL;
        
        Window.handle = glfwCreateWindow(builder.size.x(), builder.size.y(), Window.title, monitor, window);
        if (Window.handle == MemoryUtil.NULL) throw new RuntimeException("Failed to create the window");
        
        Window.LOGGER.fine("Created Window");
        
        Window.open = true;
        
        Window.refreshRate = builder.refreshRate;
        
        Window.vsync        = builder.vsync;
        Window.vsyncChanges = Window.vsync;
        
        Window.focused        = glfwGetWindowAttrib(Window.handle, GLFW_FOCUSED) == GLFW_TRUE;
        Window.focusedChanges = Window.focused;
        
        Window.iconified        = glfwGetWindowAttrib(Window.handle, GLFW_ICONIFIED) == GLFW_TRUE;
        Window.iconifiedChanges = Window.iconified;
        
        Window.maximized        = glfwGetWindowAttrib(Window.handle, GLFW_MAXIMIZED) == GLFW_TRUE;
        Window.maximizedChanges = Window.maximized;
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            
            FloatBuffer xf = stack.mallocFloat(1);
            FloatBuffer yf = stack.mallocFloat(1);
            
            if (builder.position != null)
            {
                Window.pos.set(builder.position);
                Window.posChanges.set(Window.pos);
                glfwSetWindowPos(Window.handle, Window.pos.x(), Window.pos.y());
                if (visible != null && visible) glfwShowWindow(Window.handle);
            }
            else
            {
                glfwGetWindowPos(Window.handle, x, y);
                Window.pos.set(x.get(0), y.get(0));
                Window.posChanges.set(Window.pos);
            }
            
            glfwGetWindowSize(Window.handle, x, y);
            Window.size.set(x.get(0), y.get(0));
            Window.sizeChanges.set(Window.size);
            
            glfwGetWindowContentScale(Window.handle, xf, yf);
            Window.scale.set(xf.get(0), yf.get(0));
            Window.scaleChanges.set(Window.scale);
            
            glfwGetFramebufferSize(Window.handle, x, y);
            Window.fbSize.set(x.get(0), y.get(0));
            Window.fbSizeChanges.set(Window.fbSize);
        }
        
        Window.shouldClose = false;
        
        Window.shouldRefresh = true;
        
        Window.dropped = null;
        
        Window.minSize.set(builder.minSize);
        Window.maxSize.set(builder.maxSize);
        
        glfwSetWindowSizeLimits(Window.handle, Window.minSize.x, Window.minSize.y, Window.maxSize.x, Window.maxSize.y);
        
        Window.bounds.set(Window.pos, Window.size);
        
        Window.viewMatrix.setOrtho(0, Window.fbSize.x, Window.fbSize.y, 0, -1F, 1F);
        
        Modifier.lockKeyMods(builder.lockKeyMods);
        
        glfwSetWindowCloseCallback(Window.handle, Window::windowCloseCallback);
        glfwSetWindowFocusCallback(Window.handle, Window::windowFocusCallback);
        glfwSetWindowIconifyCallback(Window.handle, Window::windowIconifyCallback);
        glfwSetWindowMaximizeCallback(Window.handle, Window::windowMaximizeCallback);
        glfwSetWindowPosCallback(Window.handle, Window::windowPosCallback);
        glfwSetWindowSizeCallback(Window.handle, Window::windowSizeCallback);
        glfwSetWindowContentScaleCallback(Window.handle, Window::windowContentScaleCallback);
        glfwSetFramebufferSizeCallback(Window.handle, Window::windowFramebufferSizeCallback);
        glfwSetWindowRefreshCallback(Window.handle, Window::windowRefreshCallback);
        glfwSetDropCallback(Window.handle, Window::windowDropCallback);
        
        glfwSetCursorEnterCallback(Window.handle, Window::mouseEnteredCallback);
        glfwSetCursorPosCallback(Window.handle, Window::mousePosCallback);
        glfwSetScrollCallback(Window.handle, Window::mouseScrollCallback);
        glfwSetMouseButtonCallback(Window.handle, Window::mouseButtonCallback);
        
        glfwSetCharCallback(Window.handle, Window::keyboardCharCallback);
        glfwSetKeyCallback(Window.handle, Window::keyboardKeyCallback);
        
        Window.makeCurrent();
        
        Window.primary = new Window();
    }
    
    static void destroy()
    {
        Window.LOGGER.fine("Destroy");
        
        unbindContext();
        
        Engine.Delegator.waitRunTask(() -> {
            glfwFreeCallbacks(Window.handle);
            glfwDestroyWindow(Window.handle);
        });
        
        Window.primary = null;
    }
    
    static void events(long time)
    {
        boolean updateMonitor = false;
        
        if (Window.vsync != Window.vsyncChanges)
        {
            Window.vsync = Window.vsyncChanges;
            glfwSwapInterval(Window.vsync ? 1 : 0);
            Engine.Events.post(EventWindowVsyncChanged.create(time, Window.primary, Window.vsync));
        }
        
        if (Window.focused != Window.focusedChanges)
        {
            Window.focused = Window.focusedChanges;
            Engine.Events.post(EventWindowFocused.create(time, Window.primary, Window.focused));
        }
        
        if (Window.iconified != Window.iconifiedChanges)
        {
            Window.iconified = Window.iconifiedChanges;
            Engine.Events.post(EventWindowIconified.create(time, Window.primary, Window.iconified));
        }
        
        if (Window.maximized != Window.maximizedChanges)
        {
            Window.maximized = Window.maximizedChanges;
            Engine.Events.post(EventWindowMaximized.create(time, Window.primary, Window.maximized));
        }
        
        if (Window.pos.x != Window.posChanges.x || Window.pos.y != Window.posChanges.y)
        {
            Window.posChanges.sub(Window.pos, Window.deltaI);
            Window.pos.set(Window.posChanges);
            Engine.Events.post(EventWindowMoved.create(time, Window.primary, Window.pos, Window.deltaI));
            
            updateMonitor = true;
        }
        
        if (Window.size.x != Window.sizeChanges.x || Window.size.y != Window.sizeChanges.y)
        {
            Window.sizeChanges.sub(Window.size, Window.deltaI);
            Window.size.set(Window.sizeChanges);
            Engine.Events.post(EventWindowResized.create(time, Window.primary, Window.size, Window.deltaI));
            
            updateMonitor = true;
        }
        
        if (Double.compare(Window.scale.x, Window.scaleChanges.x) != 0 || Double.compare(Window.scale.y, Window.scaleChanges.y) != 0)
        {
            Window.scaleChanges.sub(Window.scale, Window.deltaD);
            Window.scale.set(Window.scaleChanges);
            Engine.Events.post(EventWindowContentScaleChanged.create(time, Window.primary, Window.scale, Window.deltaD));
        }
        
        if (Window.fbSize.x != Window.fbSizeChanges.x || Window.fbSize.y != Window.fbSizeChanges.y)
        {
            Window.fbSizeChanges.sub(Window.fbSize, Window.deltaI);
            Window.fbSize.set(Window.fbSizeChanges);
            Engine.Events.post(EventWindowFramebufferResized.create(time, Window.primary, Window.fbSize, Window.deltaI));
            
            Window.viewMatrix.setOrtho(0, Window.fbSize.x, Window.fbSize.y, 0, -1F, 1F);
        }
        
        if (Window.shouldClose)
        {
            Window.open = false;
            Engine.Events.post(EventWindowClosed.create(time, Window.primary));
            Engine.stop();
        }
        
        if (Window.shouldRefresh)
        {
            Window.shouldRefresh = false;
            Engine.Events.post(EventWindowRefreshed.create(time, Window.primary));
        }
        
        if (Window.dropped != null)
        {
            Path[] paths = new Path[Window.dropped.length];
            for (int i = 0; i < Window.dropped.length; i++) paths[i] = Paths.get(Window.dropped[i]);
            Window.dropped = null;
            Engine.Events.post(EventWindowDropped.create(time, Window.primary, paths));
        }
        
        if (updateMonitor)
        {
            Monitor prevMonitor = Window.monitor;
            
            int overlap, maxOverlap = 0;
            for (Monitor monitor : Monitor.monitors.values())
            {
                if ((overlap = Window.overlapArea(monitor)) > maxOverlap)
                {
                    maxOverlap     = overlap;
                    Window.monitor = monitor;
                }
            }
            
            if (Window.monitor != prevMonitor)
            {
                Engine.Events.post(EventWindowMonitorChanged.create(time, Window.primary, prevMonitor, Window.monitor));
            }
        }
    }
    
    public static void makeCurrent()
    {
        long thread = Thread.currentThread().getId();
        
        Window.LOGGER.fine("Making Window Context Current in Thread=%s", thread);
        
        glfwMakeContextCurrent(Window.handle);
        org.lwjgl.opengl.GL.createCapabilities(); // TODO - This should be in GL
    }
    
    static void unbindContext()
    {
        long thread = Thread.currentThread().getId();
        
        Window.LOGGER.fine("Removing Window Context in Thread=%s", thread);
        
        org.lwjgl.opengl.GL.setCapabilities(null); // TODO - This should be in GL
        glfwMakeContextCurrent(MemoryUtil.NULL);
    }
    
    private static void windowFocusCallback(long handle, boolean focused)
    {
        Window.focusedChanges = focused;
    }
    
    private static void windowIconifyCallback(long handle, boolean iconified)
    {
        Window.iconifiedChanges = iconified;
    }
    
    private static void windowMaximizeCallback(long handle, boolean maximized)
    {
        Window.maximizedChanges = maximized;
    }
    
    private static void windowCloseCallback(long handle)
    {
        Window.shouldClose = true;
    }
    
    private static void windowRefreshCallback(long handle)
    {
        Window.shouldRefresh = true;
    }
    
    private static void windowPosCallback(long handle, int x, int y)
    {
        Window.posChanges.set(x, y);
    }
    
    private static void windowSizeCallback(long handle, int width, int height)
    {
        Window.sizeChanges.set(width, height);
    }
    
    private static void windowContentScaleCallback(long handle, float xScale, float yScale)
    {
        Window.scaleChanges.set(xScale, yScale);
    }
    
    private static void windowFramebufferSizeCallback(long handle, int width, int height)
    {
        Window.fbSizeChanges.set(width, height);
    }
    
    private static void windowDropCallback(long handle, int count, long names)
    {
        Window.dropped = new String[count];
        PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
        for (int i = 0; i < count; i++) Window.dropped[i] = MemoryUtil.memUTF8(charPointers.get(i));
    }
    
    private static void mouseEnteredCallback(long handle, boolean entered)
    {
        Mouse.window         = Window.primary;
        Mouse.enteredChanges = entered;
    }
    
    private static void mousePosCallback(long handle, double x, double y)
    {
        Mouse.window = Window.primary;
        Mouse.posChanges.set(x, y);
    }
    
    private static void mouseScrollCallback(long handle, double dx, double dy)
    {
        Mouse.window = Window.primary;
        Mouse.scrollChanges.add(dx, dy);
    }
    
    private static void mouseButtonCallback(long handle, int button, int action, int mods)
    {
        Mouse.window = Window.primary;
        Mouse.buttonStateChanges.offer(new Pair<>(Mouse.Button.valueOf(button), action));
        Modifier.activeMods = mods;
    }
    
    private static void keyboardCharCallback(long handle, int codePoint)
    {
        Keyboard.window = Window.primary;
        Keyboard.charChanges.offer(codePoint);
    }
    
    private static void keyboardKeyCallback(long handle, int key, int scancode, int action, int mods)
    {
        Keyboard.window = Window.primary;
        Keyboard.keyStateChanges.offer(new Pair<>(Keyboard.Key.get(key, scancode), action));
        Modifier.activeMods = mods;
    }
    
    // -------------------- Properties -------------------- //
    
    static String name;
    static String title;
    static long   handle;
    
    static Monitor monitor;
    
    static boolean windowed;
    
    static boolean open;
    
    static int refreshRate;
    
    static final Vector2i minSize = new Vector2i();
    static final Vector2i maxSize = new Vector2i();
    
    static final AABB2i bounds = new AABB2i();
    
    static final Matrix4d viewMatrix = new Matrix4d();
    
    // -------------------- State Objects -------------------- //
    
    static boolean vsync;
    static boolean vsyncChanges;
    
    static boolean focused;
    static boolean focusedChanges;
    
    static boolean iconified;
    static boolean iconifiedChanges;
    
    static boolean maximized;
    static boolean maximizedChanges;
    
    static final Vector2i pos        = new Vector2i();
    static final Vector2i posChanges = new Vector2i();
    
    static final Vector2i size        = new Vector2i();
    static final Vector2i sizeChanges = new Vector2i();
    
    static final Vector2d scale        = new Vector2d();
    static final Vector2d scaleChanges = new Vector2d();
    
    static final Vector2i fbSize        = new Vector2i();
    static final Vector2i fbSizeChanges = new Vector2i();
    
    static boolean shouldClose;
    static boolean shouldRefresh;
    
    static String[] dropped;
    
    // -------------------- Utility Objects -------------------- //
    
    private static final Vector2i deltaI = new Vector2i();
    private static final Vector2d deltaD = new Vector2d();
    
    // -------------------- Instance -------------------- //
    
    private Window() {}
    
    @Override
    public boolean equals(Object o)
    {
        return this == o;
    }
    
    @Override
    public String toString()
    {
        return "Window{name='WindowMain'}";
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * @return The current title of the window.
     */
    @NotNull
    public static String title()
    {
        return Window.title;
    }
    
    /**
     * Sets the window title, encoded as UTF-8, of the window.
     *
     * @param title The new title.
     */
    public static void title(CharSequence title)
    {
        Window.LOGGER.finest("Setting Title for %s: \"%s\"", Window.primary, title);
        
        Window.title = title.toString();
        
        Engine.Delegator.runTask(() -> glfwSetWindowTitle(Window.handle, title));
    }
    
    /**
     * @return The current monitor that the window is in.
     */
    public static Monitor monitor()
    {
        return Window.monitor;
    }
    
    public static int overlapArea(@NotNull Monitor monitor)
    {
        Monitor.VideoMode current = Objects.requireNonNull(monitor.videoMode());
        
        int mx = monitor.x();
        int my = monitor.y();
        int mw = current.width;
        int mh = current.height;
        
        int wx = x();
        int wy = y();
        int ww = width();
        int wh = height();
        
        return Math.max(0, Math.min(wx + ww, mx + mw) - Math.max(wx, mx)) *
               Math.max(0, Math.min(wy + wh, my + mh) - Math.max(wy, my));
    }
    
    /**
     * @return Retrieves if the window is in windowed mode.
     */
    public static boolean windowed()
    {
        return Window.windowed;
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
    public static void windowed(boolean windowed)
    {
        Engine.Delegator.runTask(() -> {
            Window.windowed = windowed;
            long monitor = Window.windowed ? MemoryUtil.NULL : Window.monitor.handle;
            
            int x = ((Window.monitor.primaryVideoMode.width - Window.size.x) >> 1) + Window.monitor.x();
            int y = ((Window.monitor.primaryVideoMode.height - Window.size.y) >> 1) + Window.monitor.y();
            
            glfwSetWindowMonitor(Window.handle, monitor, x, y, Window.size.x, Window.size.y, Window.refreshRate);
        });
    }
    
    /**
     * @return If the window is open.
     */
    public static boolean isOpen()
    {
        return Window.open;
    }
    
    /**
     * Requests that the window close.
     */
    public static void close()
    {
        Window.LOGGER.finest("Closing", Window.primary);
        
        Window.shouldClose = true;
    }
    
    /**
     * @return Retrieves the refresh rate of the window, or {@link Window#DONT_CARE}
     */
    public static int refreshRate()
    {
        return Window.refreshRate;
    }
    
    /**
     * Sets the refresh rate of the window.
     *
     * @param refreshRate The new refresh rate.
     */
    public static void refreshRate(int refreshRate)
    {
        Engine.Delegator.runTask(() -> {
            Window.refreshRate = refreshRate;
            
            long monitor = Window.windowed ? MemoryUtil.NULL : Window.monitor.handle;
            glfwSetWindowMonitor(Window.handle, monitor, Window.pos.x, Window.pos.y, Window.size.x, Window.size.y, Window.refreshRate);
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
    public static void icons(Image... icons)
    {
        Window.LOGGER.finest("Setting Icons in %s: %s", Window.primary, icons);
        
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
                
                glfwSetWindowIcon(Window.handle, buffer);
            }
        });
    }
    
    /**
     * @return Retrieves the current aspect ratio of the window.
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
     * If the numerator and denominator is set to {@link Window#DONT_CARE} then
     * the aspect ratio limit is disabled.
     * <p>
     * The aspect ratio is applied immediately to a windowed mode window and
     * may cause it to be resized.
     *
     * @param numer the numerator of the desired aspect ratio, or {@link Window#DONT_CARE}
     * @param denom the denominator of the desired aspect ratio, or {@link Window#DONT_CARE}
     */
    public static void aspectRatio(int numer, int denom)
    {
        Window.LOGGER.finest("Setting Aspect Ratio for %s: %s/%s", Window.primary, numer, denom);
        
        Engine.Delegator.runTask(() -> glfwSetWindowAspectRatio(Window.handle, numer, denom));
    }
    
    /**
     * Restores the window if it was previously iconified (minimized) or
     * maximized. If the window is already restored, this function does
     * nothing.
     * <p>
     * If the window is a full screen window, the resolution chosen for the
     * window is restored on the selected monitor.
     */
    public static void restore()
    {
        Window.LOGGER.finest("Restoring", Window.primary);
        
        Engine.Delegator.runTask(() -> glfwRestoreWindow(Window.handle));
    }
    
    /**
     * @return Retrieves if the window is resizable <i>by the user</i>.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean resizable()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_RESIZABLE) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window is resizable <i>by the user</i>.
     *
     * @param resizable if the window is resizable <i>by the user</i>.
     */
    public static void resizable(boolean resizable)
    {
        Window.LOGGER.finest("Setting Resizable Flag for %s: %s", Window.primary, resizable);
        
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(Window.handle, GLFW_RESIZABLE, resizable ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the window is visible. Window visibility can be controlled with {@link #show} and {@link #hide}.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean visible()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_VISIBLE) == GLFW_TRUE);
    }
    
    /**
     * Makes the window visible if it was previously hidden. If the window is
     * already visible or is in full screen mode, this function does nothing.
     */
    public static void show()
    {
        Window.LOGGER.finest("Showing", Window.primary);
        
        Engine.Delegator.runTask(() -> glfwShowWindow(Window.handle));
    }
    
    /**
     * Hides the window, if it was previously visible. If the window is already
     * hidden or is in full screen mode, this function does nothing.
     */
    public static void hide()
    {
        Window.LOGGER.finest("Hiding", Window.primary);
        
        Engine.Delegator.runTask(() -> glfwHideWindow(Window.handle));
    }
    
    /**
     * @return Retrieves if the window has decorations such as a border, a close widget, etc.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean decorated()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_DECORATED) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window has decorations such as a border, a close
     * widget, etc.
     *
     * @param decorated if the window has decorations.
     */
    public static void decorated(boolean decorated)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(Window.handle, GLFW_DECORATED, decorated ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the window is floating, also called topmost or always-on-top.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean floating()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_FLOATING) == GLFW_TRUE);
    }
    
    /**
     * Indicates whether the window is floating, also called topmost or
     * always-on-top.
     *
     * @param floating if the window is floating.
     */
    public static void floating(boolean floating)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(Window.handle, GLFW_FLOATING, floating ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves if the cursor is currently directly over the content area of the window, with no other windows between.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean hovered()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_HOVERED) == GLFW_TRUE);
    }
    
    /**
     * @return Retrieves if input focuses on calling show window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean focusOnShow()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetWindowAttrib(Window.handle, GLFW_FOCUS_ON_SHOW) == GLFW_TRUE);
    }
    
    /**
     * Indicates if input focuses on calling show window.
     *
     * @param focusOnShow if input focuses on calling show window.
     */
    public static void focusOnShow(boolean focusOnShow)
    {
        Engine.Delegator.runTask(() -> glfwSetWindowAttrib(Window.handle, GLFW_FOCUS_ON_SHOW, focusOnShow ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * Retrieves the minimum size, in screen coordinates, of the content area
     * of the window. If you wish to retrieve the size of the framebuffer of
     * the window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The minimum size, in screen coordinates, of the content area.
     */
    public static Vector2ic minSize()
    {
        return Window.minSize;
    }
    
    /**
     * Retrieves the maximum size, in screen coordinates, of the content area
     * of the window. If you wish to retrieve the size of the framebuffer of
     * the window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The maximum size, in screen coordinates, of the content area.
     */
    public static Vector2ic maxSize()
    {
        return Window.maxSize;
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
     * @param minWidth  the minimum width, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     * @param minHeight the minimum height, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     * @param maxWidth  the maximum width, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     * @param maxHeight the maximum height, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     */
    public static void sizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight)
    {
        Window.minSize.set(minWidth, minHeight);
        Window.maxSize.set(maxWidth, maxHeight);
        
        Engine.Delegator.runTask(() -> glfwSetWindowSizeLimits(Window.handle, minWidth, minHeight, maxWidth, maxHeight));
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
     * @param min the minimum size, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     * @param max the maximum size, in screen coordinates, of the content area, or {@link Window#DONT_CARE}
     */
    public static void sizeLimits(@NotNull Vector2ic min, @NotNull Vector2ic max)
    {
        sizeLimits(min.x(), min.y(), max.x(), max.y());
    }
    
    /**
     * @return An axis-aligned bounding box of the window's content-area.
     */
    public static AABB2ic bounds()
    {
        return Window.bounds;
    }
    
    /**
     * @return A read-only framebuffer view transformation matrix for this window.
     */
    public static Matrix4dc viewMatrix()
    {
        return Window.viewMatrix;
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
    public static int @NotNull [] getFrameSize()
    {
        return Engine.Delegator.waitReturnTask(() -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer left   = stack.callocInt(1);
                IntBuffer top    = stack.callocInt(1);
                IntBuffer right  = stack.callocInt(1);
                IntBuffer bottom = stack.callocInt(1);
                
                glfwGetWindowFrameSize(Window.handle, left, top, right, bottom);
                
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
    public static void setClipboard(ByteBuffer string)
    {
        Engine.Delegator.runTask(() -> glfwSetClipboardString(Window.handle, string));
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
        Engine.Delegator.runTask(() -> glfwSetClipboardString(Window.handle, string));
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
    public static String getClipboard()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetClipboardString(Window.handle));
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
    public static long getClipboardRaw()
    {
        return Engine.Delegator.waitReturnTask(() -> nglfwGetClipboardString(Window.handle));
    }
    
    // -------------------- State Properties -------------------- //
    
    /**
     * @return Retrieves the vsync status for the current OpenGL or OpenGL ES context
     */
    public static boolean vsync()
    {
        return Window.vsync;
    }
    
    /**
     * Sets the vsync status for the current OpenGL or OpenGL ES context, i.e.
     * the number of screen updates to wait from the time
     * {@link org.lwjgl.glfw.GLFW#glfwSwapBuffers SwapBuffers} was called
     * before swapping the buffers and returning.
     *
     * @param vsync the new vsync status
     */
    public static void vsync(boolean vsync)
    {
        Window.vsyncChanges = vsync;
    }
    
    /**
     * Retrieves if the window has input focus.
     *
     * @return if the window has input focus
     */
    public static boolean focused()
    {
        return Window.focused;
    }
    
    /**
     * Brings the window to front and sets input focus. The window should
     * already be visible and not iconified.
     * <p>
     * By default, both windowed and full screen mode windows are focused when
     * initially created. Set the {@link Builder#focused(Boolean) focused}
     * flag to disable this behavior.
     * <p>
     * Also by default, windowed mode windows are focused when shown with
     * {@link #show}. Set the {@link Builder#focusOnShow(Boolean) focusOnShow}
     * window hint to disable this behavior.
     * <p>
     * <b>Do not use this function</b> to steal focus from other applications
     * unless you are certain that is what the user wants. Focus stealing can
     * be extremely disruptive.
     * <p>
     * For a less disruptive way of getting the user's attention, see
     * {@link #requestFocus}.
     */
    public static void focus()
    {
        Engine.Delegator.runTask(() -> glfwFocusWindow(Window.handle));
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
    public static void requestFocus()
    {
        Engine.Delegator.runTask(() -> glfwRequestWindowAttention(Window.handle));
    }
    
    /**
     * @return Retrieves whether the window is iconified, whether by the user or with {@link #iconify}.
     */
    public static boolean iconified()
    {
        return Window.iconified;
    }
    
    /**
     * Iconifies (minimizes) the window if it was previously restored. If the
     * window is already iconified, this function does nothing.
     * <p>
     * If the window is a full screen window, the original monitor resolution
     * is restored until the window is restored.
     */
    public static void iconify()
    {
        Engine.Delegator.runTask(() -> glfwIconifyWindow(Window.handle));
    }
    
    /**
     * @return Retrieves whether the window is maximized, whether by the user or {@link #maximize}.
     */
    public static boolean maximized()
    {
        return Window.maximized;
    }
    
    /**
     * Maximizes the window if it was previously not maximized. If the window
     * is already maximized, this function does nothing.
     * <p>
     * If the window is a full screen window, this function does nothing.
     */
    public static void maximize()
    {
        Engine.Delegator.runTask(() -> glfwMaximizeWindow(Window.handle));
    }
    
    /**
     * Retrieves the position, in screen coordinates, of the upper-left corner
     * of the content area of the window.
     *
     * @return The position of the upper-left corner of the content area
     */
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
    public static int x()
    {
        return Window.pos.x;
    }
    
    /**
     * Retrieves the y-coordinate of the position, in screen coordinates, of
     * the upper-left corner of the content area of the window.
     *
     * @return The y-coordinate of the upper-left corner of the content area
     */
    public static int y()
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
        Engine.Delegator.waitRunTask(() -> glfwSetWindowPos(Window.handle, x, y));
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
     * Retrieves the size, in screen coordinates, of the content area of the
     * window. If you wish to retrieve the size of the framebuffer of the
     * window in pixels, see {@link #framebufferSize framebufferSize}.
     *
     * @return The size, in screen coordinates, of the content area.
     */
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
        Engine.Delegator.waitRunTask(() -> glfwSetWindowSize(Window.handle, width, height));
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
    
    // -------------------- Updating -------------------- //
    
    public static void swap()
    {
        glfwSwapBuffers(Window.handle);
    }
    
    // -------------------- Utility Classes -------------------- //
    
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public static final class Builder
    {
        private String name = null;
        
        private Monitor monitor = null;
        
        private Vector2ic position = null;
        private Vector2ic size     = new Vector2i(800, 600);
        private Vector2ic minSize  = new Vector2i(DONT_CARE);
        private Vector2ic maxSize  = new Vector2i(DONT_CARE);
        
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
            // unbindContext();
            throw new RuntimeException("Not Implemented Yet");
            // return Objects.requireNonNull(Engine.Delegator.waitReturnTask(() -> new Window(this)));
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
         * @param vsync if the window will be locked to its monitor's refresh rate.
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
         * Zero disables multisampling. A value of {@link Window#DONT_CARE}
         * means the application has no preference.
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
         * of {@link Window#DONT_CARE} means the highest available refresh rate
         * will be used. This hint is ignored for windowed mode windows.
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
            
            this.redBits        = applyInteger(GLFW_RED_BITS, this.redBits, DONT_CARE);
            this.greenBits      = applyInteger(GLFW_GREEN_BITS, this.greenBits, DONT_CARE);
            this.blueBits       = applyInteger(GLFW_BLUE_BITS, this.blueBits, DONT_CARE);
            this.alphaBits      = applyInteger(GLFW_ALPHA_BITS, this.alphaBits, DONT_CARE);
            this.depthBits      = applyInteger(GLFW_DEPTH_BITS, this.depthBits, DONT_CARE);
            this.stencilBits    = applyInteger(GLFW_STENCIL_BITS, this.stencilBits, DONT_CARE);
            this.accumRedBits   = applyInteger(GLFW_ACCUM_RED_BITS, this.accumRedBits, DONT_CARE);
            this.accumGreenBits = applyInteger(GLFW_ACCUM_GREEN_BITS, this.accumGreenBits, DONT_CARE);
            this.accumBlueBits  = applyInteger(GLFW_ACCUM_BLUE_BITS, this.accumBlueBits, DONT_CARE);
            this.accumAlphaBits = applyInteger(GLFW_ACCUM_ALPHA_BITS, this.accumAlphaBits, DONT_CARE);
            this.auxBuffers     = applyInteger(GLFW_AUX_BUFFERS, this.auxBuffers, DONT_CARE);
            this.samples        = applyInteger(GLFW_SAMPLES, this.samples, DONT_CARE);
            this.refreshRate    = applyInteger(GLFW_REFRESH_RATE, this.refreshRate, DONT_CARE);
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
