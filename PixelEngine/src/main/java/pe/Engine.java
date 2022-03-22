package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.Colorc;
import pe.draw.*;
import pe.event.Event;
import pe.render.*;
import pe.texture.Image;
import pe.util.Random;
import rutils.Logger;
import rutils.group.Pair;

import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.*;

public abstract class Engine
{
    private static final Logger LOGGER = new Logger();
    
    static Engine  instance;
    static boolean mainThreadRunning, renderThreadRunning;
    
    static Random random;
    
    static boolean wireframe = false;
    
    static int vertices = 0;
    static int draws    = 0;
    
    static String screenshot = null;
    
    public static final class Events
    {
        private static final Logger LOGGER = new Logger();
        
        private static final Set<Event> events = new HashSet<>();
        
        private static void events()
        {
            Events.events.clear();
        }
        
        public static void post(Event event)
        {
            Events.LOGGER.finest("Posting", event);
            
            Events.events.add(event);
        }
        
        @Unmodifiable
        @NotNull
        public static Set<Event> get()
        {
            return get(Event.class);
        }
        
        @SuppressWarnings("unchecked")
        @Unmodifiable
        @NotNull
        public static <E extends Event> Set<E> get(Class<E> eventType)
        {
            Set<E> events = new HashSet<>();
            for (Event event : Events.events)
            {
                Class<? extends Event> eventClass = event.getClass();
                if (eventType.isAssignableFrom(eventClass) && !event.consumed()) events.add((E) event);
            }
            return Collections.unmodifiableSet(events);
        }
    }
    
    private static final class IO
    {
        private static void setup(int width, int height, double pixelWidth, double pixelHeight)
        {
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                IntBuffer major = stack.mallocInt(1);
                IntBuffer minor = stack.mallocInt(1);
                IntBuffer rev   = stack.mallocInt(1);
                
                glfwGetVersion(major, minor, rev);
                
                Engine.LOGGER.fine("GLFW Initialization %s.%s.%s", major.get(), minor.get(), rev.get());
                Engine.LOGGER.finer("RWJGLUtil Compiled to '%s'", glfwGetVersionString());
            }
            
            Engine.LOGGER.finer("GLFW Setup");
            if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");
            
            final Map<Integer, String> errorCodes = APIUtil.apiClassTokens((field, value) -> 0x10000 < value && value < 0x20000, null, org.lwjgl.glfw.GLFW.class);
            glfwSetErrorCallback((error, description) -> {
                StringBuilder message = new StringBuilder();
                message.append("[LWJGL] ").append(errorCodes.get(error)).append(" error\n");
                message.append("\tDescription : ").append(MemoryUtil.memUTF8(description)).append('\n');
                message.append("\tStacktrace  :\n");
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 4; i < stack.length; i++) message.append("\t\t").append(stack[i].toString()).append('\n');
                Engine.LOGGER.severe(message.toString());
            });
            
            Monitor.setup();
            Window.setup(width, height, pixelWidth, pixelHeight);
            Mouse.setup();
            Keyboard.setup();
            Joystick.setup();
            
            GLState.setup();
            
            Layer.setup(width, height);
            
            int r = Layer.primary().width() >> 1;
            int l = -r;
            int b = Layer.primary().height() >> 1;
            int t = -b;
            
            GLBatch.get().matrixMode(MatrixMode.PROJECTION);
            GLBatch.get().loadIdentity();
            GLBatch.get().ortho(l, r, b, t, 1.0, -1.0);
            
            GLBatch.get().matrixMode(MatrixMode.VIEW);
            GLBatch.get().loadIdentity();
            GLBatch.get().translate(l, t, 0.0);
            
            GLBatch.get().matrixMode(MatrixMode.MODEL);
            GLBatch.get().loadIdentity();
            
            GLBatch.get().matrixMode(MatrixMode.NORMAL);
            GLBatch.get().loadIdentity();
            
            GLBatch.get().colorMode(ColorMode.DIFFUSE);
            GLBatch.get().loadWhite();
            
            GLBatch.get().colorMode(ColorMode.SPECULAR);
            GLBatch.get().loadWhite();
            
            GLBatch.get().colorMode(ColorMode.AMBIENT);
            GLBatch.get().loadWhite();
        }
        
        private static void events()
        {
            long time = Time.getNS();
            
            Mouse.events(time);
            Keyboard.events(time);
            Joystick.events(time);
            Window.events(time);
            
            Layer.events();
        }
        
        private static void destroy()
        {
            Layer.destroy();
            
            GLState.destroy();
            
            Joystick.destroy();
            Keyboard.destroy();
            Mouse.destroy();
            Window.destroy();
            Monitor.destroy();
        }
    }
    
    public static final class Delegator
    {
        private static final Deque<Runnable>                    run               = new ArrayDeque<>();
        private static final Deque<Runnable>                    waitRun           = new ArrayDeque<>();
        private static final BlockingQueue<Optional<Exception>> waitRunResults    = new SynchronousQueue<>();
        private static final Deque<Supplier<?>>                 waitReturn        = new ArrayDeque<>();
        private static final BlockingQueue<Pair<?, Exception>>  waitReturnResults = new SynchronousQueue<>();
        
        private static void runTasks()
        {
            while (!Delegator.run.isEmpty())
            {
                Runnable task = Delegator.run.poll();
                try
                {
                    task.run();
                }
                catch (Exception e)
                {
                    Engine.LOGGER.severe("An exception occurred while trying to run task.");
                    Engine.LOGGER.severe(e);
                }
            }
            
            while (!Delegator.waitRun.isEmpty())
            {
                Runnable task = Delegator.waitRun.poll();
                
                Exception e = null;
                try
                {
                    task.run();
                }
                catch (Exception exception)
                {
                    e = exception;
                }
                //noinspection StatementWithEmptyBody
                while (!Delegator.waitRunResults.offer(Optional.ofNullable(e))) ;
            }
            
            while (!Delegator.waitReturn.isEmpty())
            {
                Supplier<?> task = Delegator.waitReturn.poll();
                
                Object    result = null;
                Exception except = null;
                try
                {
                    result = task.get();
                }
                catch (Exception e)
                {
                    except = e;
                }
                //noinspection StatementWithEmptyBody
                while (!Delegator.waitReturnResults.offer(new Pair<>(result, except))) ;
            }
        }
        
        public static void runTask(@NotNull Runnable task)
        {
            if (Thread.currentThread().getName().equals("main"))
            {
                task.run();
                return;
            }
            
            Delegator.run.offer(task);
        }
        
        public static void waitRunTask(@NotNull Runnable task)
        {
            if (Thread.currentThread().getName().equals("main"))
            {
                task.run();
                return;
            }
            
            Delegator.waitRun.offer(task);
            
            try
            {
                Optional<Exception> result = Delegator.waitRunResults.take();
                if (result.isPresent()) throw new RuntimeException(result.get());
            }
            catch (InterruptedException e)
            {
                Engine.LOGGER.warning("Run task was interrupted.");
            }
        }
        
        public static <T> @Nullable T waitReturnTask(@NotNull Supplier<T> task)
        {
            if (Thread.currentThread().getName().equals("main")) return task.get();
            
            Delegator.waitReturn.offer(task);
            
            try
            {
                Pair<?, Exception> result = Delegator.waitReturnResults.take();
                if (result.b != null) throw new RuntimeException(result.b);
                //noinspection unchecked
                return (T) result.a;
            }
            catch (InterruptedException e)
            {
                Engine.LOGGER.warning("Return task was interrupted.");
            }
            return null;
        }
    }
    
    public static final class Draw
    {
        private static final DrawPoint2D         DRAW_POINT_2D          = new DrawPoint2D();
        private static final DrawLine2D          DRAW_LINE_2D           = new DrawLine2D();
        private static final DrawLines2D         DRAW_LINES_2D          = new DrawLines2D();
        private static final DrawBezier2D        DRAW_BEZIER_2D         = new DrawBezier2D();
        private static final DrawTriangle2D      DRAW_TRIANGLE_2D       = new DrawTriangle2D();
        private static final FillTriangle2D      FILL_TRIANGLE_2D       = new FillTriangle2D();
        private static final DrawQuad2D          DRAW_QUAD_2D           = new DrawQuad2D();
        private static final FillQuad2D          FILL_QUAD_2D           = new FillQuad2D();
        private static final DrawRect2D          DRAW_RECT_2D           = new DrawRect2D();
        private static final FillRect2D          FILL_RECT_2D           = new FillRect2D();
        private static final DrawEllipse2D       DRAW_ELLIPSE_2D        = new DrawEllipse2D();
        private static final FillEllipse2D       FILL_ELLIPSE_2D        = new FillEllipse2D();
        private static final DrawRing2D          DRAW_RING_2D           = new DrawRing2D();
        private static final FillRing2D          FILL_RING_2D           = new FillRing2D();
        private static final DrawTexture2D       DRAW_TEXTURE_2D        = new DrawTexture2D();
        private static final DrawTextureWarped2D DRAW_TEXTURE_WARPED_2D = new DrawTextureWarped2D();
        
        public static void clearBackground(@NotNull Colorc color)
        {
            GLState.clearColor(color.rf(), color.gf(), color.bf(), color.af());
            GLState.clearScreenBuffers();
        }
        
        public static DrawPoint2D point2D()
        {
            return Draw.DRAW_POINT_2D;
        }
        
        public static DrawLine2D line2D()
        {
            return Draw.DRAW_LINE_2D;
        }
        
        public static DrawLines2D lines2D()
        {
            return Draw.DRAW_LINES_2D;
        }
        
        public static DrawBezier2D bezier2D()
        {
            return Draw.DRAW_BEZIER_2D;
        }
        
        public static DrawTriangle2D drawTriangle2D()
        {
            return Draw.DRAW_TRIANGLE_2D;
        }
        
        public static FillTriangle2D fillTriangle2D()
        {
            return Draw.FILL_TRIANGLE_2D;
        }
        
        public static DrawQuad2D drawQuad2D()
        {
            return Draw.DRAW_QUAD_2D;
        }
        
        public static FillQuad2D fillQuad2D()
        {
            return Draw.FILL_QUAD_2D;
        }
        
        public static DrawRect2D drawRect2D()
        {
            return Draw.DRAW_RECT_2D;
        }
        
        public static FillRect2D fillRect2D()
        {
            return Draw.FILL_RECT_2D;
        }
        
        public static DrawEllipse2D drawEllipse2D()
        {
            return Draw.DRAW_ELLIPSE_2D;
        }
        
        public static FillEllipse2D fillEllipse2D()
        {
            return Draw.FILL_ELLIPSE_2D;
        }
        
        public static DrawRing2D drawRing2D()
        {
            return Draw.DRAW_RING_2D;
        }
        
        public static FillRing2D fillRing2D()
        {
            return Draw.FILL_RING_2D;
        }
        
        public static DrawTexture2D drawTexture2D()
        {
            return Draw.DRAW_TEXTURE_2D;
        }
        
        public static DrawTextureWarped2D drawTextureWarped2D()
        {
            return Draw.DRAW_TEXTURE_WARPED_2D;
        }
    }
    
    // ----------------------
    // -- Engine Functions --
    // ----------------------
    
    protected static void start(@NotNull Engine instance)
    {
        Thread.currentThread().setName("main");
        Engine.LOGGER.info("Starting");
        
        Extension.registerDefaultExtensions();
        
        try
        {
            if (Engine.instance != null) throw new IllegalStateException("Cannot call 'start' more that once.");
            
            Engine.instance            = instance;
            Engine.mainThreadRunning   = true;
            Engine.renderThreadRunning = true;
            Engine.random              = new Random();
            
            Time.setup();
            // Delegator.setup(); // TODO
            
            Extension.stage(Extension.Stage.PRE_SETUP);
            
            Engine.LOGGER.info("Instance Setup");
            Engine.instance.setup();
            
            Extension.stage(Extension.Stage.POST_SETUP);
            
            if (Window.isOpen())
            {
                Window.unbindContext();
                
                final CountDownLatch latch = new CountDownLatch(1);
                
                new Thread(() -> {
                    try
                    {
                        Window.makeCurrent();
                        
                        Extension.stage(Extension.Stage.RENDER_SETUP);
                        
                        while (Engine.renderThreadRunning)
                        {
                            if (Time.startFrame())
                            {
                                Extension.stage(Extension.Stage.PRE_FRAME);
                                
                                // TODO Profiler Start Frame
                                
                                Extension.stage(Extension.Stage.PRE_EVENTS);
                                
                                Events.events();
                                IO.events();
                                
                                Extension.stage(Extension.Stage.POST_EVENTS);
                                
                                // GUI.handleEvents();
                                // Debug.handleEvents();
                                // NuklearGUI.handleEvents();
                                // ImGUI.handleEvents();
                                
                                if (!Time.paused)
                                {
                                    GLFramebuffer.bind(Layer.primary.framebuffer);
                                    GLProgram.bind(null);
                                    
                                    GLState.defaultState();
                                    GLState.wireframe(Engine.wireframe);
                                    
                                    GLBatch.bind(null);
                                    GLBatch defaultBatch = GLBatch.get();
                                    defaultBatch.start();
                                    
                                    int r = Layer.primary().width() >> 1;
                                    int l = -r;
                                    int b = Layer.primary().height() >> 1;
                                    int t = -b;
                                    
                                    defaultBatch.matrixMode(MatrixMode.PROJECTION);
                                    defaultBatch.loadIdentity();
                                    defaultBatch.ortho(l, r, b, t, 1.0, -1.0);
                                    
                                    defaultBatch.matrixMode(MatrixMode.VIEW);
                                    defaultBatch.loadIdentity();
                                    defaultBatch.translate(l, t, 0.0);
                                    
                                    defaultBatch.matrixMode(MatrixMode.MODEL);
                                    defaultBatch.loadIdentity();
                                    
                                    defaultBatch.matrixMode(MatrixMode.NORMAL);
                                    defaultBatch.loadIdentity();
                                    
                                    defaultBatch.colorMode(ColorMode.DIFFUSE);
                                    defaultBatch.loadWhite();
                                    
                                    defaultBatch.colorMode(ColorMode.SPECULAR);
                                    defaultBatch.loadWhite();
                                    
                                    defaultBatch.colorMode(ColorMode.AMBIENT);
                                    defaultBatch.loadWhite();
                                    
                                    // Engine.renderer.start(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extension.stage(Extension.Stage.PRE_DRAW);
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Engine.instance.draw(Time.delta());
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extension.stage(Extension.Stage.POST_DRAW);
                                    // Engine.renderer.pop(); // TODO
                                    
                                    GLBatch.BatchStats stats = defaultBatch.stop();
                                    
                                    // Engine.renderer.finish(); // TODO
                                    
                                    Engine.vertices = stats.vertices();
                                    Engine.draws    = stats.draws();
                                }
                                
                                Layer.draw();
                                // GUI.draw();
                                // Debug.draw();
                                // NuklearGUI.draw();
                                // ImGUI.draw();
                                
                                Window.swap();
                                
                                if (Engine.screenshot != null)
                                {
                                    String fileName = Engine.screenshot + (!Engine.screenshot.endsWith(".png") ? ".png" : "");
                                    
                                    int w = Window.framebufferWidth();
                                    int h = Window.framebufferHeight();
                                    
                                    Color.Buffer data = GLState.readFrontBuffer(0, 0, w, h);
                                    
                                    Image image = Image.load(data, w, h, 1, data.format());
                                    image.export(fileName);
                                    image.delete();
                                    
                                    Engine.screenshot = null;
                                }
                                
                                // TODO Profiler End Frame
                                
                                Time.endFrame();
                                
                                Extension.stage(Extension.Stage.POST_FRAME);
                            }
                            
                            // TODO
                            // if (Time.shouldUpdate())
                            // {
                            //     Window.primary.title(String.format("Engine - %s - %s", Engine.instance.name, Time.getTimeString()));
                            //
                            //     // Debug.update();
                            // }
                            
                            Thread.yield();
                        }
                    }
                    catch (Throwable e)
                    {
                        Engine.LOGGER.severe(e);
                    }
                    finally
                    {
                        Extension.stageCatch(Extension.Stage.RENDER_DESTROY);
                        
                        // GUI.destroy();
                        // Debug.destroy();
                        // NuklearGUI.destroy();
                        // ImGUI.destroy();
                        
                        IO.destroy();
                        
                        Engine.mainThreadRunning = false;
                        
                        latch.countDown();
                    }
                }, "render").start();
                
                while (Engine.mainThreadRunning)
                {
                    glfwPollEvents();
                    
                    Joystick.pollCallbackEmulation();
                    
                    Delegator.runTasks();
                    
                    Thread.yield();
                }
                latch.await();
            }
        }
        catch (Throwable e)
        {
            Engine.LOGGER.severe(e);
        }
        finally
        {
            Extension.stageCatch(Extension.Stage.PRE_DESTROY);
            
            Engine.LOGGER.info("Instance Destroy");
            Engine.instance.destroy();
            
            Extension.stageCatch(Extension.Stage.POST_DESTROY);
            
            org.lwjgl.opengl.GL.destroy();
            glfwTerminate();
        }
        
        Engine.LOGGER.info("Finished");
    }
    
    public static void stop()
    {
        Engine.renderThreadRunning = false;
    }
    
    protected static void size(int width, int height, double pixelWidth, double pixelHeight)
    {
        IO.setup(width, height, pixelWidth, pixelHeight);
        
        // GUI.setup();
        // Debug.setup();
        // NuklearGUI.setup();
        // ImGUI.setup();
    }
    
    protected static void size(int width, int height)
    {
        size(width, height, 4, 4);
    }
    
    public static void takeScreenShot()
    {
        Engine.screenshot = String.format("Screenshot - %s.png", Time.timeStamp());
    }
    
    // -----------------------
    // -- Engine Properties --
    // -----------------------
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    final String name;
    
    public Engine()
    {
        String className = getClass().getSimpleName();
        
        StringBuilder name = new StringBuilder();
        for (int i = 0, n = className.length(); i < n; i++)
        {
            char ch = className.charAt(i);
            if (i > 0 && (Character.isDigit(ch) || Character.isUpperCase(ch))) name.append(' ');
            name.append(ch == '_' ? " - " : ch);
        }
        this.name = name.toString();
    }
    
    /**
     * Called once when the engine has been fully initialized.
     */
    protected abstract void setup();
    
    /**
     * Called once every frame, unless the engine is paused.
     *
     * @param elapsedTime The time in seconds since the last frame.
     */
    protected void draw(double elapsedTime) {}
    
    /**
     * Called once before the engine is destroyed
     */
    protected void destroy() {}
}
