package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWGammaRamp;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import pe.event.EventMonitorConnected;
import pe.event.EventMonitorDisconnected;
import rutils.Logger;
import rutils.MemUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;

public class Monitor
{
    private static final Logger LOGGER = new Logger();
    
    static final Map<Long, Monitor> monitors = new LinkedHashMap<>();
    static       Monitor            primary  = null;
    
    static void setup()
    {
        Monitor.LOGGER.fine("Setup");
        
        loadMonitors();
        
        glfwSetMonitorCallback(Monitor::monitorCallback);
    }
    
    static void destroy()
    {
        Monitor.LOGGER.fine("Destroy");
        
        Callback callback = glfwSetMonitorCallback(null);
        if (callback != null) callback.free();
    }
    
    @UnmodifiableView
    @NotNull
    public static Collection<@NotNull Monitor> get()
    {
        return Collections.unmodifiableCollection(Monitor.monitors.values());
    }
    
    @NotNull
    public static Monitor get(int index)
    {
        if (index < 0) throw new RuntimeException("Monitor Index cannot be < 0");
        for (Monitor monitor : Monitor.monitors.values()) if (index-- <= 0) return monitor;
        return Monitor.primary;
    }
    
    @NotNull
    public static Monitor primary()
    {
        return Monitor.primary;
    }
    
    private static void loadMonitors()
    {
        Monitor.monitors.clear();
        PointerBuffer m = Objects.requireNonNull(glfwGetMonitors(), "No monitors found.");
        for (long i = 0, n = m.remaining(), h; i < n; i++) Monitor.monitors.put(h = m.get(), new Monitor(h, (int) i));
        Monitor.primary = Monitor.monitors.get(glfwGetPrimaryMonitor());
    }
    
    private static void monitorCallback(long handle, int event)
    {
        switch (event)
        {
            case GLFW_CONNECTED -> {
                loadMonitors();
                Monitor monitor = Monitor.monitors.get(handle);
                Engine.Events.post(EventMonitorConnected.create(Time.getNS(), monitor));
            }
            case GLFW_DISCONNECTED -> {
                Monitor monitor = Monitor.monitors.get(handle);
                loadMonitors();
                Engine.Events.post(EventMonitorDisconnected.create(Time.getNS(), monitor));
            }
        }
    }
    
    // -------------------- Instance -------------------- //
    
    final long   handle;
    final int    index;
    final String name;
    
    final List<VideoMode> videoModes = new ArrayList<>();
    final VideoMode       primaryVideoMode;
    
    final Vector2i actualSize = new Vector2i();
    
    final Vector2f scale = new Vector2f();
    
    final Vector2i pos = new Vector2i();
    
    final Vector2i workAreaPos  = new Vector2i();
    final Vector2i workAreaSize = new Vector2i();
    
    Monitor(long handle, int index)
    {
        this.handle = handle;
        this.index  = index;
        this.name   = glfwGetMonitorName(this.handle);
        
        GLFWVidMode.Buffer modes = glfwGetVideoModes(this.handle);
        if (modes != null) while (modes.hasRemaining()) this.videoModes.add(VideoMode.get(modes.get()));
        this.primaryVideoMode = videoMode();
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            
            FloatBuffer sx = stack.mallocFloat(1);
            FloatBuffer sy = stack.mallocFloat(1);
            
            glfwGetMonitorPhysicalSize(this.handle, w, h);
            this.actualSize.set(w.get(0), h.get(0));
            
            glfwGetMonitorContentScale(this.handle, sx, sy);
            this.scale.set(sx.get(0), sy.get(0));
            
            glfwGetMonitorPos(this.handle, x, y);
            this.pos.set(x.get(0), y.get(0));
            
            glfwGetMonitorWorkarea(this.handle, x, y, w, h);
            this.workAreaPos.set(x.get(0), y.get(0));
            this.workAreaSize.set(w.get(0), h.get(0));
        }
        
        glfwSetGamma(this.handle, 1.0F);
        
        Monitor.LOGGER.finer("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Monitor monitor = (Monitor) o;
        return this.handle == monitor.handle;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.handle);
    }
    
    @Override
    public String toString()
    {
        return "Monitor{" + "name='" + this.name + '\'' + ", index=" + this.index + '}';
    }
    
    /**
     * @return The monitor index.
     */
    public int index()
    {
        return this.index;
    }
    
    /**
     * The human-readable, UTF-8 encoded name of a monitor.
     * <p>
     * Monitor names are not guaranteed to be unique. Two monitors of the same model and make may have the same name
     *
     * @return The human-readable, UTF-8 encoded name of a monitor.
     */
    public String name()
    {
        return this.name;
    }
    
    /**
     * @return The primary video mode of the monitor, i.e. the video mode that the monitor was in when GLFW was initialized..
     */
    public VideoMode primaryVideoMode()
    {
        return this.primaryVideoMode;
    }
    
    /**
     * @return The current video mode the monitor is in.
     */
    public VideoMode videoMode()
    {
        GLFWVidMode vidMode = glfwGetVideoMode(this.handle);
        if (vidMode != null) return VideoMode.get(vidMode);
        if (this.videoModes.size() > 0) return this.videoModes.get(0);
        return null;
    }
    
    /**
     * @return Every video mode associated with this monitor.
     */
    public List<VideoMode> videoModes()
    {
        return Collections.unmodifiableList(this.videoModes);
    }
    
    /**
     * @return The width of the video mode in screen coordinates, not pixels.
     */
    public int width()
    {
        return videoMode().width;
    }
    
    /**
     * @return The height of the video mode in screen coordinates, not pixels.
     */
    public int height()
    {
        return videoMode().height;
    }
    
    /**
     * @return The red bit depth of the current video mode.
     */
    public int redBits()
    {
        return videoMode().redBits;
    }
    
    /**
     * @return The green bit depth of the current video mode.
     */
    public int greenBits()
    {
        return videoMode().greenBits;
    }
    
    /**
     * @return The blue bit depth of the current video mode.
     */
    public int blueBits()
    {
        return videoMode().blueBits;
    }
    
    /**
     * @return The refresh rate, in Hz, of the current video mode.
     */
    public int refreshRate()
    {
        return videoMode().refreshRate;
    }
    
    /**
     * The physical size of a monitor in millimetres, or an estimation of it.
     * This has no relation to its current resolution, i.e. the width and
     * height of its current video mode.
     * <p>
     * While this can be used to calculate the raw DPI of a monitor, this is
     * often not useful. Instead, use the monitor content scale and window
     * content scale to scale your content.
     *
     * @return The physical size of a monitor in millimetres
     */
    public Vector2ic actualSize()
    {
        return this.actualSize;
    }
    
    /**
     * The physical width of a monitor in millimetres, or an estimation of it.
     * This has no relation to its current resolution, i.e. the width of its
     * current video mode.
     * <p>
     * While this can be used to calculate the raw DPI of a monitor, this is
     * often not useful. Instead, use the monitor content scale and window
     * content scale to scale your content.
     *
     * @return The physical width of a monitor in millimetres
     */
    public int actualWidth()
    {
        return this.actualSize.x;
    }
    
    /**
     * The physical height of a monitor in millimetres, or an estimation of it.
     * This has no relation to its current resolution, i.e. the height of its
     * current video mode.
     * <p>
     * While this can be used to calculate the raw DPI of a monitor, this is
     * often not useful. Instead, use the monitor content scale and window
     * content scale to scale your content.
     *
     * @return The physical height of a monitor in millimetres
     */
    public int actualHeight()
    {
        return this.actualSize.y;
    }
    
    /**
     * The content scale for a monitor. The content scale is the ratio between
     * the current DPI and the platform's default DPI. This is especially
     * important for text and any UI elements. If the pixel dimensions of your
     * UI scaled by this look appropriate on your machine then it should appear
     * at a reasonable size on other machines regardless of their DPI and
     * scaling settings. This relies on the system DPI and scaling settings
     * being somewhat correct.
     * <p>
     * The content scale may depend on both the monitor resolution and pixel
     * density and on user settings. It may be very different from the raw DPI
     * calculated from the physical size and current resolution.
     *
     * @return The content scale for a monitor.
     */
    public Vector2fc contentScale()
    {
        return this.scale;
    }
    
    /**
     * The horizontal content scale for a monitor. The content scale is the
     * ratio between the current DPI and the platform's default DPI. This is
     * especially important for text and any UI elements. If the pixel
     * dimensions of your UI scaled by this look appropriate on your machine
     * then it should appear at a reasonable size on other machines regardless
     * of their DPI and scaling settings. This relies on the system DPI and
     * scaling settings being somewhat correct.
     * <p>
     * The content scale may depend on both the monitor resolution and pixel
     * density and on user settings. It may be very different from the raw DPI
     * calculated from the physical size and current resolution.
     *
     * @return The horizontal content scale for a monitor.
     */
    public float contentScaleX()
    {
        return this.scale.x;
    }
    
    /**
     * The vertical content scale for a monitor. The content scale is the ratio
     * between the current DPI and the platform's default DPI. This is
     * especially important for text and any UI elements. If the pixel
     * dimensions of your UI scaled by this look appropriate on your machine
     * then it should appear at a reasonable size on other machines regardless
     * of their DPI and scaling settings. This relies on the system DPI and
     * scaling settings being somewhat correct.
     * <p>
     * The content scale may depend on both the monitor resolution and pixel
     * density and on user settings. It may be very different from the raw DPI
     * calculated from the physical size and current resolution.
     *
     * @return The horizontal content scale for a monitor.
     */
    public float contentScaleY()
    {
        return this.scale.y;
    }
    
    /**
     * @return The position of the monitor on the virtual desktop, in screen coordinates.
     */
    public Vector2ic pos()
    {
        return this.pos;
    }
    
    /**
     * @return The x position of the monitor on the virtual desktop, in screen coordinates.
     */
    public int x()
    {
        return this.pos.x;
    }
    
    /**
     * @return The y position of the monitor on the virtual desktop, in screen coordinates.
     */
    public int y()
    {
        return this.pos.y;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The position of the work area.
     */
    public Vector2ic workAreaPos()
    {
        return this.workAreaPos;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The x position of the work area.
     */
    public int workAreaX()
    {
        return this.workAreaPos.x;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The y position of the work area.
     */
    public int workAreaY()
    {
        return this.workAreaPos.y;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The size of the work area.
     */
    public Vector2ic workAreaSize()
    {
        return this.workAreaSize;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The width of the work area.
     */
    public int workAreaWidth()
    {
        return this.workAreaSize.x;
    }
    
    /**
     * The area of a monitor not occupied by global task bars or menu bars is the work area. This is specified in screen coordinates.
     *
     * @return The height of the work area.
     */
    public int workAreaHeight()
    {
        return this.workAreaSize.y;
    }
    
    /**
     * Returns the current gamma ramp for the monitor.
     * <p>
     * The returned structure and its arrays are allocated and freed by GLFW.
     * You should not free them yourself. They are valid until the specified
     * monitor is disconnected, this function is called again for that monitor
     * or the library is terminated.
     *
     * @return the current gamma ramp, or {@code null} if an error occurred
     */
    public @Nullable GammaRamp gammaRamp()
    {
        GLFWGammaRamp ramp = Engine.Delegator.waitReturnTask(() -> glfwGetGammaRamp(this.handle));
        return ramp != null ? new GammaRamp(ramp) : null;
    }
    
    /**
     * Sets the current gamma ramp for the monitor.
     *
     * <p>This function sets the current gamma ramp for the specified monitor.
     * The original gamma ramp for that monitor is saved by GLFW the first
     * time this function is called and is restored by {@link Engine#stop()}.
     * <p>
     * The software controlled gamma ramp is applied <em>in addition</em> to
     * the hardware gamma correction, which today is usually an approximation
     * of sRGB gamma. This means that setting a perfectly linear ramp, or gamma
     * 1.0, will produce the default (usually sRGB-like) behavior.
     * <p>
     * For gamma correct rendering with OpenGL or OpenGL ES, see the
     * {@link org.lwjgl.glfw.GLFW#GLFW_SRGB_CAPABLE SRGB_CAPABLE} hint.
     *
     * @param gammaRamp the gamma ramp to use
     */
    public void gammaRamp(@NotNull GammaRamp gammaRamp)
    {
        Engine.Delegator.waitRunTask(() -> {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GLFWGammaRamp ramp = GLFWGammaRamp.malloc(stack);
                
                ramp.size(gammaRamp.size);
                MemUtil.memCopy(gammaRamp.red, ramp.red());
                MemUtil.memCopy(gammaRamp.green, ramp.green());
                MemUtil.memCopy(gammaRamp.blue, ramp.blue());
                
                glfwSetGammaRamp(this.handle, ramp);
            }
        });
    }
    
    /**
     * Generates a gamma ramp and sets it for the monitor.
     * <p>
     * This function generates an appropriately sized gamma ramp from the
     * specified exponent and then calls {@link #gammaRamp} with it. The value
     * must be a finite number greater than zero.
     * <p>
     * The software controlled gamma ramp is applied <em>in addition</em> to
     * the hardware gamma correction, which today is usually an approximation
     * of sRGB gamma. This means that setting a perfectly linear ramp, or gamma
     * 1.0, will produce the default (usually sRGB-like) behavior.
     * <p>
     * For gamma correct rendering with OpenGL or OpenGL ES, see the
     * {@link org.lwjgl.glfw.GLFW#GLFW_SRGB_CAPABLE SRGB_CAPABLE} hint.
     *
     * @param gamma the desired exponent
     */
    public void gammaRamp(float gamma)
    {
        Engine.Delegator.waitRunTask(() -> glfwSetGamma(this.handle, gamma));
    }
    
    public int windowOverlap(@NotNull Window window)
    {
        VideoMode current = videoMode();
        
        int mx = x();
        int my = y();
        int mw = current.width;
        int mh = current.height;
        
        int wx = window.x();
        int wy = window.y();
        int ww = window.width();
        int wh = window.height();
        
        return Math.max(0, Math.min(wx + ww, mx + mw) - Math.max(wx, mx)) *
               Math.max(0, Math.min(wy + wh, my + mh) - Math.max(wy, my));
    }
    
    public static final class VideoMode
    {
        private static final HashMap<Integer, VideoMode> CACHE = new HashMap<>();
        
        @NotNull
        private static VideoMode get(@NotNull GLFWVidMode vidMode)
        {
            int hash = Objects.hash(vidMode.width(),
                                    vidMode.height(),
                                    vidMode.redBits(),
                                    vidMode.greenBits(),
                                    vidMode.blueBits(),
                                    vidMode.refreshRate());
            return VideoMode.CACHE.computeIfAbsent(hash, h -> new VideoMode(vidMode));
        }
        
        /**
         * The resolution of a video mode is specified in screen coordinates, not pixels.
         */
        public final int width, height;
        
        /**
         * The bit depth of the video mode.
         */
        public final int redBits, greenBits, blueBits;
        
        /**
         * The refresh rate, in Hz, of the video mode.
         */
        public final int refreshRate;
        
        private VideoMode(@NotNull GLFWVidMode vidMode)
        {
            this.width  = vidMode.width();
            this.height = vidMode.height();
            
            this.redBits   = vidMode.redBits();
            this.greenBits = vidMode.greenBits();
            this.blueBits  = vidMode.blueBits();
            
            this.refreshRate = vidMode.refreshRate();
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VideoMode videoMode = (VideoMode) o;
            return this.width == videoMode.width &&
                   this.height == videoMode.height &&
                   this.redBits == videoMode.redBits &&
                   this.greenBits == videoMode.greenBits &&
                   this.blueBits == videoMode.blueBits &&
                   this.refreshRate == videoMode.refreshRate;
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(this.width,
                                this.height,
                                this.redBits,
                                this.greenBits,
                                this.blueBits,
                                this.refreshRate);
        }
        
        @Override
        @NotNull
        public String toString()
        {
            return "VideoMode{" +
                   "size=(" + this.width + ", " + this.height + ')' +
                   ", " +
                   "bits=(" + this.redBits + ", " + this.greenBits + ", " + this.blueBits + ')' +
                   ", " +
                   "refreshRate=" + this.refreshRate +
                   '}';
        }
    }
    
    public static final class GammaRamp
    {
        public final int size;
        
        public final short[] red, green, blue;
        
        private GammaRamp(@NotNull GLFWGammaRamp glfwGammaRamp)
        {
            this.size = glfwGammaRamp.size();
            
            this.red   = new short[this.size];
            this.green = new short[this.size];
            this.blue  = new short[this.size];
            
            MemUtil.memCopy(glfwGammaRamp.red(), this.red);
            MemUtil.memCopy(glfwGammaRamp.green(), this.green);
            MemUtil.memCopy(glfwGammaRamp.blue(), this.blue);
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GammaRamp gammaRamp = (GammaRamp) o;
            return this.size == gammaRamp.size &&
                   Arrays.equals(this.red, gammaRamp.red) &&
                   Arrays.equals(this.green, gammaRamp.green) &&
                   Arrays.equals(this.blue, gammaRamp.blue);
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(this.size,
                                Arrays.hashCode(this.red),
                                Arrays.hashCode(this.green),
                                Arrays.hashCode(this.blue));
        }
        
        @Override
        @NotNull
        public String toString()
        {
            return "GammaRamp{" + "size=" + this.size + '}';
        }
    }
}
