package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryUtil;
import pe.color.Colorc;
import pe.draw.*;
import pe.event.Event;
import pe.render.*;
import pe.util.Random;
import rutils.Logger;
import rutils.Math;
import rutils.group.Pair;

import java.nio.FloatBuffer;
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
    static boolean running;
    static Random  random;
    
    static final Vector2i screenSize = new Vector2i();
    static final Vector2i pixelSize  = new Vector2i();
    
    static boolean windowEnabled = false;
    
    static boolean wireframe = false;
    
    static int vertices = 0;
    static int draws    = 0;
    
    public static final class Events
    {
        private static final Logger LOGGER = new Logger();
        
        private static final Set<Event> events = new HashSet<>();
        
        private static void processEvents()
        {
            Events.events.clear();
            
            long time = Time.getNS();
            
            Mouse.processEvents(time);
            Keyboard.processEvents(time);
            Joystick.processEvents(time);
            Window.processEvents(time);
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
    
    protected static class Extensions // TODO
    {
        private static final Logger LOGGER = new Logger();
        
        private static void preSetup()
        {
            Extensions.LOGGER.fine("Pre Setup");
        }
        
        private static void postSetup()
        {
            Extensions.LOGGER.fine("Post Setup");
        }
        
        private static void renderSetup()
        {
            Extensions.LOGGER.fine("Render Setup");
        }
        
        private static void preEvents()
        {
        }
        
        private static void postEvents()
        {
        }
        
        private static void preDraw()
        {
        }
        
        private static void postDraw()
        {
        }
        
        private static void renderDestroy()
        {
            Extensions.LOGGER.fine("Render Destroy");
        }
        
        private static void preDestroy()
        {
            Extensions.LOGGER.fine("Pre Destroy");
        }
        
        private static void postDestroy()
        {
            Extensions.LOGGER.fine("Post Destroy");
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
    
    public static final class Viewport
    {
        private static GLProgram     program;
        private static GLVertexArray vertexArray;
        private static GLFramebuffer framebuffer;
        
        private static final Vector2i pos  = new Vector2i();
        private static final Vector2i size = new Vector2i();
        
        private static void setup()
        {
            String vert = """
                          #version 330
                          layout(location = 0) in vec2 POSITION;
                          layout(location = 1) in vec2 TEXCOORD;
                          out vec2 texCoord;
                          void main(void)
                          {
                              texCoord = TEXCOORD;
                              gl_Position = vec4(POSITION, 0.0, 1.0);
                          }
                          """;
            String frag = """
                          #version 330
                          uniform sampler2D tex;
                          in vec2 texCoord;
                          out vec4 FragColor;
                          void main(void)
                          {
                              FragColor = texture(tex, texCoord);
                          }
                          """;
            Viewport.program = GLProgram.loadFromCode(vert, null, frag);
            
            FloatBuffer vertices = MemoryUtil.memCallocFloat(16).put(new float[] {
                    -1.0F, +1.0F, 0.0F, 1.0F,
                    -1.0F, -1.0F, 0.0F, 0.0F,
                    +1.0F, -1.0F, 1.0F, 0.0F,
                    +1.0F, +1.0F, 1.0F, 1.0F,
                    });
            IntBuffer indices = MemoryUtil.memCallocInt(6).put(new int[] {
                    0, 1, 2, 0, 2, 3
            });
            Viewport.vertexArray = GLVertexArray.builder()
                                                .buffer(vertices.clear(), Usage.STATIC_DRAW,
                                                        new GLAttribute(GLType.FLOAT, 2, false),
                                                        new GLAttribute(GLType.FLOAT, 2, false))
                                                .indexBuffer(indices.clear(), Usage.STATIC_DRAW)
                                                .build();
            MemoryUtil.memFree(vertices);
            MemoryUtil.memFree(indices);
            
            Viewport.framebuffer = GLFramebuffer.load(Engine.screenSize.x, Engine.screenSize.y);
        }
        
        private static void destroy()
        {
            Viewport.program.delete();
            Viewport.vertexArray.delete();
            Viewport.framebuffer.delete();
        }
        
        public static @NotNull Vector2ic pos()
        {
            return Viewport.pos;
        }
        
        public static int x()
        {
            return Viewport.pos.x;
        }
        
        public static int y()
        {
            return Viewport.pos.y;
        }
        
        public static @NotNull Vector2ic size()
        {
            return Viewport.size;
        }
        
        public static int width()
        {
            return Viewport.size.x;
        }
        
        public static int height()
        {
            return Viewport.size.y;
        }
        
        private static void draw()
        {
            double aspect = (double) (Engine.screenSize.x * Engine.pixelSize.x) / (double) (Engine.screenSize.y * Engine.pixelSize.y);
            
            int frameWidth  = Window.framebufferWidth();
            int frameHeight = Window.framebufferHeight();
            
            Viewport.size.set(frameWidth, (int) (frameWidth / aspect));
            if (Viewport.size.y > frameHeight) Viewport.size.set((int) (frameHeight * aspect), frameHeight);
            Viewport.pos.set((frameWidth - Viewport.size.x) >> 1, (frameHeight - Viewport.size.y) >> 1);
            
            Engine.pixelSize.x = Math.max(Viewport.size.x / Engine.screenSize.x, 1);
            Engine.pixelSize.y = Math.max(Viewport.size.y / Engine.screenSize.y, 1);
            
            GLFramebuffer.bind(null);
            GLProgram.bind(Viewport.program);
            
            GLState.defaultState();
            GLState.viewport(Viewport.pos.x, Viewport.pos.y, Viewport.size.x, Viewport.size.y);
            GLState.depthMode(DepthMode.NONE);
            
            GLState.clearScreenBuffers(ScreenBuffer.COLOR);
            
            GLTexture.bind(Viewport.framebuffer.color());
            Viewport.vertexArray.drawElements(DrawMode.TRIANGLES, 6);
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
        
        try
        {
            if (Engine.instance != null) throw new IllegalStateException("Cannot call 'start' more that once.");
            
            Engine.instance = instance;
            Engine.running  = true;
            Engine.random   = new Random();
            
            Time.setup();
            // Delegator.setup(); // TODO
            
            Extensions.preSetup();
            
            Engine.LOGGER.info("Instance Setup");
            Engine.instance.setup();
            
            Extensions.postSetup();
            
            if (Engine.windowEnabled)
            {
                Window.unmakeCurrent();
                
                final CountDownLatch latch = new CountDownLatch(1);
                
                new Thread(() -> {
                    try
                    {
                        Window.makeCurrent();
                        
                        Extensions.renderSetup();
                        
                        while (Engine.running)
                        {
                            if (Time.startFrame())
                            {
                                // TODO Profiler Start Frame
                                
                                Extensions.preEvents();
                                
                                Events.processEvents();
                                
                                Extensions.postEvents();
                                
                                // GUI.handleEvents();
                                Debug.handleEvents();
                                NuklearGUI.handleEvents();
                                
                                if (!Time.paused)
                                {
                                    GLFramebuffer.bind(Viewport.framebuffer);
                                    GLProgram.bind(null);
                                    
                                    GLState.defaultState();
                                    GLState.wireframe(Engine.wireframe);
                                    
                                    GLBatch.get().start();
                                    
                                    // Engine.renderer.start(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extensions.preDraw();
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Engine.instance.draw(Time.delta());
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extensions.postDraw();
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // GLState.disable(GLState.DEPTH_TEST);
                                    // GLState.drawRenderBatch(); // Update and draw internal render batch
                                    
                                    // Engine.renderer.finish(); // TODO
                                    
                                    GLBatch.BatchStats stats = GLBatch.get().stop();
                                    
                                    Engine.vertices = stats.vertices();
                                    Engine.draws    = stats.draws();
                                }
                                
                                Viewport.draw();
                                // GUI.draw();
                                // Debug.draw();
                                NuklearGUI.draw();
                                
                                Window.swap();
                                
                                // TODO Profiler End Frame
                                
                                Time.endFrame();
                            }
                            
                            // if (Engine.screenshot != null)
                            // {
                            //     String fileName = Engine.screenshot + (!Engine.screenshot.endsWith(".png") ? ".png" : "");
                            //
                            //     int w = Engine.viewSize.x;
                            //     int h = Engine.viewSize.y;
                            //     int c = 3;
                            //
                            //     int stride = w * c;
                            //
                            //     ByteBuffer buf = MemoryUtil.memAlloc(w * h * c);
                            //     GL33.glReadBuffer(GL33.GL_FRONT);
                            //     GL33.glReadPixels(0, 0, w, h, GL33.GL_RGB, GL33.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(buf));
                            //
                            //     byte[] tmp1 = new byte[stride], tmp2 = new byte[stride];
                            //     for (int i = 0, n = h >> 1, col1, col2; i < n; i++)
                            //     {
                            //         col1 = i * stride;
                            //         col2 = (h - i - 1) * stride;
                            //         buf.get(col1, tmp1);
                            //         buf.get(col2, tmp2);
                            //         buf.put(col1, tmp2);
                            //         buf.put(col2, tmp1);
                            //     }
                            //
                            //     if (!stbi_write_png(fileName, w, h, c, buf, stride)) Engine.LOGGER.severe("Could not take screen shot");
                            //     MemoryUtil.memFree(buf);
                            //
                            //     Engine.screenshot = null;
                            // }
                            
                            // TODO
                            // if (Time.shouldUpdate())
                            // {
                            //     Window.title(String.format("Engine - %s - %s", Engine.instance.name, Time.getTimeString()));
                            //
                            //     // Debug.update();
                            // }
                            
                            Thread.yield();
                        }
                    }
                    catch (Exception e)
                    {
                        Engine.LOGGER.severe(e);
                    }
                    finally
                    {
                        Extensions.renderDestroy();
                        
                        Viewport.destroy();
                        // GUI.destroy();
                        Debug.destroy();
                        NuklearGUI.destroy();
                        
                        GLState.destroy();
                        
                        Window.unmakeCurrent();
                        
                        Window.destroy();
                        
                        Engine.running = false;
                        
                        latch.countDown();
                    }
                }, "render").start();
                
                while (Engine.running)
                {
                    glfwPollEvents();
                    
                    Joystick.pollCallbackEmulation();
                    
                    Delegator.runTasks();
                    
                    Thread.yield();
                }
                latch.await();
            }
        }
        catch (Exception e)
        {
            Engine.LOGGER.severe(e);
        }
        finally
        {
            Extensions.preDestroy();
            
            Engine.LOGGER.info("Instance Destroy");
            Engine.instance.destroy();
            
            Extensions.postDestroy();
            
            glfwTerminate();
        }
        
        Engine.LOGGER.info("Finished");
    }
    
    public static void stop()
    {
        Engine.running = false;
    }
    
    protected static void size(int screenW, int screenH, int pixelW, int pixelH)
    {
        Engine.screenSize.set(screenW, screenH);
        if (Engine.screenSize.x <= 0 || Engine.screenSize.y <= 0) throw new IllegalArgumentException("Screen dimension must be > 0");
        Engine.LOGGER.fine("Screen Size:", Engine.screenSize);
        
        Engine.pixelSize.set(pixelW, pixelH);
        if (Engine.pixelSize.x <= 0 || Engine.pixelSize.y <= 0) throw new IllegalArgumentException("Pixel dimension must be > 0");
        Engine.LOGGER.fine("Pixel Size:", Engine.pixelSize);
        
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
        
        Window.setup();
        Modifier.setup();
        Mouse.setup();
        Keyboard.setup();
        Joystick.setup();
        
        Window.makeCurrent();
        Window.title("Engine - " + Engine.instance.name);
        
        GLState.setup();
        
        Viewport.setup();
        // GUI.setup();
        Debug.setup();
        NuklearGUI.setup();
        
        Engine.windowEnabled = true;
    }
    
    protected static void size(int screenW, int screenH)
    {
        size(screenW, screenH, 4, 4);
    }
    
    // -----------------------
    // -- Engine Properties --
    // -----------------------
    
    /**
     * @return The read-only screen size vector in screen pixels. This will be the values passed in to the {@link #size} function.
     */
    public static @NotNull Vector2ic screenSize()
    {
        return Engine.screenSize;
    }
    
    /**
     * @return The screen width in screen pixels. This will be the value passed in to the {@link #size} function.
     */
    public static int screenWidth()
    {
        return Engine.screenSize.x;
    }
    
    /**
     * @return The screen height in screen pixels. This will be the value passed in to the {@link #size} function.
     */
    public static int screenHeight()
    {
        return Engine.screenSize.y;
    }
    
    /**
     * @return The read-only pixel size vector in actual pixels. This will be the values passed in to the {@link #size} function.
     */
    public static @NotNull Vector2ic pixelSize()
    {
        return Engine.pixelSize;
    }
    
    /**
     * @return The pixel width in actual pixels. This will be the value passed in to the {@link #size} function.
     */
    public static int pixelWidth()
    {
        return Engine.pixelSize.x;
    }
    
    /**
     * @return The pixel height in actual pixels. This will be the value passed in to the {@link #size} function.
     */
    public static int pixelHeight()
    {
        return Engine.pixelSize.y;
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    private final String name;
    
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
