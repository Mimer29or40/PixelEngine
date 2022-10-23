package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.event.Event;
import pe.font.Font;
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
        private static void setup(String name, int width, int height, double pixelWidth, double pixelHeight)
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
            Window.setup(name, width, height, pixelWidth, pixelHeight);
            Mouse.setup();
            Keyboard.setup();
            Joystick.setup();
            
            GL.setup();
            
            Font.setup();
            
            Debug.setup();
        }
        
        private static void events()
        {
            long time = Time.getNS();
            
            Mouse.events(time);
            Keyboard.events(time);
            Joystick.events(time);
            Window.events(time);
        }
        
        private static void destroy()
        {
            Debug.destroy();
            
            Font.destroy();
            
            GL.destroy();
            
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
    
    // ----------------------
    // -- Engine Functions --
    // ----------------------
    
    public static void stop()
    {
        Engine.renderThreadRunning = false;
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
    
    public final String name;
    
    private final CountDownLatch latch = new CountDownLatch(1);
    
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
    protected abstract void draw(double elapsedTime);
    
    /**
     * Called once before the engine is destroyed
     */
    protected abstract void destroy();
    
    protected void start(int width, int height, double pixelWidth, double pixelHeight)
    {
        Thread.currentThread().setName("main");
        Engine.LOGGER.info("Starting");
        
        Extension.registerDefaultExtensions();
        
        try
        {
            Engine.mainThreadRunning   = true;
            Engine.renderThreadRunning = true;
            Engine.random              = new Random();
            
            Time.setup();
            // Delegator.setup(); // TODO
            
            Extension.stage(Extension.Stage.PRE_SETUP);
            
            IO.setup(this.name, width, height, pixelWidth, pixelHeight);
            
            Engine.LOGGER.info("Instance Setup");
            this.setup();
            
            Extension.stage(Extension.Stage.POST_SETUP);
            
            Window.unbindContext();
            
            new Thread(this::renderThread, "render").start();
            
            while (Engine.mainThreadRunning)
            {
                glfwPollEvents();
                
                Joystick.pollCallbackEmulation();
                
                Delegator.runTasks();
                
                Thread.yield();
            }
            this.latch.await();
        }
        catch (Throwable e)
        {
            Engine.LOGGER.severe(e);
        }
        finally
        {
            Extension.stageCatch(Extension.Stage.PRE_DESTROY);
            
            Engine.LOGGER.info("Instance Destroy");
            this.destroy();
            
            IO.destroy();
            
            Extension.stageCatch(Extension.Stage.POST_DESTROY);
            
            org.lwjgl.opengl.GL.destroy();
            glfwTerminate();
        }
        
        Engine.LOGGER.info("Finished");
    }
    
    protected void start(int width, int height)
    {
        this.start(width, height, 4, 4);
    }
    
    private void renderThread()
    {
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
                    
                    Debug.handleEvents();
                    
                    if (!Time.paused)
                    {
                        GLFramebuffer.bind(null);
                        GLProgram.bind(null);
                        
                        GL.defaultState();
                        GL.wireframe(Engine.wireframe);
                        
                        GL.clearScreenBuffers(ScreenBuffer.COLOR);
                        
                        GLBatch.bind(null);
                        
                        int r = GLFramebuffer.currentWidth() >> 1;
                        int l = -r;
                        int b = GLFramebuffer.currentHeight() >> 1;
                        int t = -b;
                        
                        GLBatch.projection().setOrtho(l, r, b, t, 1.0, -1.0);
                        GLBatch.view().identity().translate(l, t, 0.0);
                        GLBatch.model().identity();
                        GLBatch.normal().identity();
                        
                        GLBatch.diffuse().set(Color.WHITE);
                        GLBatch.specular().set(Color.WHITE);
                        GLBatch.ambient().set(Color.WHITE);
                        
                        GLBatch.push();
                        Extension.stage(Extension.Stage.PRE_DRAW);
                        GLBatch.pop();
                        
                        GLBatch.push();
                        this.draw(Time.delta());
                        GLBatch.pop();
                        
                        GLBatch.push();
                        Extension.stage(Extension.Stage.POST_DRAW);
                        GLBatch.pop();
                        
                        Debug.draw();
                        
                        GLBatch.BatchStats stats = GLBatch.stats();
                        
                        Engine.vertices = stats.vertices();
                        Engine.draws    = stats.draws();
                    }
                    
                    Window.swap();
                    
                    if (Engine.screenshot != null)
                    {
                        String fileName = Engine.screenshot + (!Engine.screenshot.endsWith(".png") ? ".png" : "");
                        
                        int w = Window.framebufferWidth();
                        int h = Window.framebufferHeight();
                        
                        Color.Buffer data = GL.readFrontBuffer(0, 0, w, h);
                        
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
            Engine.mainThreadRunning = false;
            
            Extension.stageCatch(Extension.Stage.RENDER_DESTROY);
            
            this.latch.countDown();
        }
    }
}
