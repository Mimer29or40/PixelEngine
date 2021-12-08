package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.color.Colorc;
import pe.draw.*;
import pe.event.Event;
import pe.render.*;
import pe.util.Random;
import rutils.Logger;
import rutils.group.Pair;
import rutils.group.Triple;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Supplier;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.stb.STBEasyFont.*;

public abstract class Engine
{
    private static final Logger LOGGER = new Logger();
    
    protected static Engine  instance;
    protected static boolean running;
    protected static Random  random;
    
    protected static final Vector2i screenSize = new Vector2i();
    protected static final Vector2i pixelSize  = new Vector2i();
    
    protected static boolean windowEnabled = false;
    
    public static final class Time
    {
        private static long start; // The time the engine started
        
        private static boolean paused; // If time is paused
        
        private static long frameRate;       // The actual frame rate that the engine achieved
        private static long frameRateTarget; // The target frame rate that the engine will try to achieve
        
        private static long totalFrameTime;  // The total time the engine has been not paused
        private static long totalFrameCount; // The total number of frames that the engine has rendered
        
        private static long current;    // The current time of the frame
        private static long delta;      // The time since the last frame
        private static long lastDraw;   // The time of the last frame
        private static long lastUpdate; // The time of the last title update
        
        private static long updateFreq; // The frequency to update the frame time data
        
        private static long notificationDur; // Time in seconds to display a notification.
        
        private static long totalTime;   // The total time since the last update
        private static int  totalFrames; // The number of frames since the last update
        
        private static long minTime; // The min time that a frame took since the last update
        private static long maxTime; // The max time that a frame took since the last update
        
        private static double avgFrameTime;
        private static double minFrameTime;
        private static double maxFrameTime;
        
        private static String getTimeString()
        {
            return String.format("FPS(%s) SPF(Avg: %s us, Min: %s us, Max: %s us)",
                                 Time.frameRate,
                                 Time.avgFrameTime,
                                 Time.minFrameTime,
                                 Time.maxFrameTime);
        }
        
        private static void init()
        {
            Time.start = System.nanoTime();
            
            Time.paused = false;
            
            Time.updateFreq = 1_000_000_000L / 4L;
            
            Time.notificationDur = (long) (1_000_000_000L * 2.0);
            
            Time.totalTime   = 0;
            Time.totalFrames = 0;
            
            Time.minTime = Long.MAX_VALUE;
            Time.maxTime = Long.MIN_VALUE;
            
            Time.avgFrameTime = 0.0;
            Time.minFrameTime = 0.0;
            Time.maxFrameTime = 0.0;
        }
        
        private static boolean shouldDraw()
        {
            Time.delta = Time.current - Time.lastDraw;
            if (Time.delta >= Time.frameRateTarget)
            {
                Time.lastDraw = Time.current;
                
                return true;
            }
            return false;
        }
        
        private static boolean shouldUpdate()
        {
            if (Time.current - Time.lastUpdate >= Time.updateFreq && Time.totalFrames > 0 && !Time.paused)
            {
                Time.lastUpdate = Time.current;
                
                long timePerFrame = Time.totalTime / Time.totalFrames;
                
                Time.frameRate = 1_000_000_000L / timePerFrame;
                
                Time.avgFrameTime = timePerFrame / 1_000D;
                Time.minFrameTime = Time.minTime / 1_000D;
                Time.maxFrameTime = Time.maxTime / 1_000D;
                
                Time.totalTime   = 0;
                Time.totalFrames = 0;
                
                Time.minTime = Long.MAX_VALUE;
                Time.maxTime = Long.MIN_VALUE;
                
                return true;
            }
            return false;
        }
        
        private static void incFrame()
        {
            if (!Time.paused)
            {
                long delta = Time.nanosecondsActual() - Time.current;
                Time.minTime = Math.min(Time.minTime, delta);
                Time.maxTime = Math.max(Time.maxTime, delta);
                Time.totalFrameTime += delta;
                Time.totalFrameCount++;
                Time.totalTime += delta;
                Time.totalFrames++;
            }
        }
        
        /**
         * @return {@code true} if the engine is paused.
         */
        public static boolean paused()
        {
            return Time.paused;
        }
        
        /**
         * @return The current frame rate.
         */
        public static int frameRate()
        {
            return (int) Time.frameRate;
        }
        
        /**
         * Sets the frame rate to try to run at. Use zero for no limit.
         *
         * @param frameRate The new frame rate.
         */
        public static void frameRate(int frameRate)
        {
            Time.frameRateTarget = frameRate > 0 ? 1_000_000_000L / (long) frameRate : 0L;
        }
        
        /**
         * @return The time that the engine has spent running and not paused.
         */
        public static long frameTime()
        {
            return Time.totalFrameTime;
        }
        
        /**
         * @return The current frame that engine is on.
         */
        public static long frameCount()
        {
            return Time.totalFrameCount;
        }
        
        /**
         * Sets the number of times a second to update the frame time information.
         *
         * @param times The number of times a second. Default: 4
         */
        public static void updateFrequency(int times)
        {
            if (times <= 0) times = 4;
            Time.updateFreq = 1_000_000_000L / times;
        }
        
        /**
         * Sets the time, in seconds, to show a notification message.
         *
         * @param seconds The time to show a notification. Default: 2.0
         */
        public static void notificationDuration(double seconds)
        {
            if (seconds <= 0) seconds = 2.0;
            Time.notificationDur = (long) (1_000_000_000L * seconds);
        }
        
        /**
         * @return The time in nanoseconds that the engine has been running and not paused
         */
        public static long nanoseconds()
        {
            return Time.totalFrameTime;
        }
        
        /**
         * @return The time in microseconds that the engine has been running and not paused
         */
        public static double microseconds()
        {
            return nanoseconds() / 1_000D;
        }
        
        /**
         * @return The time in milliseconds that the engine has been running and not paused
         */
        public static double milliseconds()
        {
            return nanoseconds() / 1_000_000D;
        }
        
        /**
         * @return The time in seconds that the engine has been running and not paused
         */
        public static double seconds()
        {
            return nanoseconds() / 1_000_000_000D;
        }
        
        /**
         * @return The actual time in nanoseconds since the engine started
         */
        public static long nanosecondsActual()
        {
            return Time.start > 0 ? System.nanoTime() - Time.start : -1L;
        }
        
        /**
         * @return The actual time in microseconds since the engine started
         */
        public static double microsecondsActual()
        {
            return nanosecondsActual() / 1_000D;
        }
        
        /**
         * @return The actual time in milliseconds since the engine started
         */
        public static double millisecondsActual()
        {
            return nanosecondsActual() / 1_000_000D;
        }
        
        /**
         * @return The actual time in seconds since the engine started
         */
        public static double secondsActual()
        {
            return nanosecondsActual() / 1_000_000_000D;
        }
    }
    
    public static final class Events
    {
        private static final Logger LOGGER = new Logger();
        
        private static final Set<Event> events = new HashSet<>();
        
        private static void postEvents()
        {
            Events.events.clear();
            
            Mouse.get().postEvents(Time.current, Time.delta);
            Keyboard.get().postEvents(Time.current, Time.delta);
            Joystick.postEvents(Time.current, Time.delta);
            Window.get().postEvents(Time.current, Time.delta);
        }
        
        public static void post(Event event)
        {
            Events.LOGGER.finest("Posting", event);
            
            Events.events.add(event);
        }
        
        public static Set<Event> get()
        {
            return Set.copyOf(Events.events);
        }
        
        @SuppressWarnings("unchecked")
        public static <E extends Event> List<E> get(Class<E> eventType)
        {
            List<E> events = new ArrayList<>();
            for (Event event : Events.events)
            {
                Class<? extends Event> eventClass = event.getClass();
                if (eventType.isAssignableFrom(eventClass)) events.add((E) event);
            }
            return Collections.unmodifiableList(events);
        }
    }
    
    protected static class Extensions
    {
        private static final Logger LOGGER = new Logger();
        // TODO
        
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
        private static final Deque<Runnable>                         run               = new ArrayDeque<>();
        private static final Deque<Runnable>                         waitRun           = new ArrayDeque<>();
        private static final BlockingQueue<Pair<Integer, Exception>> waitRunResults    = new SynchronousQueue<>();
        private static final Deque<Supplier<Object>>                 waitReturn        = new ArrayDeque<>();
        private static final BlockingQueue<Pair<Object, Exception>>  waitReturnResults = new SynchronousQueue<>();
        
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
                
                int       result = 0;
                Exception except = null;
                try
                {
                    task.run();
                }
                catch (Exception e)
                {
                    result = 1;
                    except = e;
                }
                //noinspection ResultOfMethodCallIgnored
                Delegator.waitRunResults.offer(new Pair<>(result, except));
            }
            
            while (!Delegator.waitReturn.isEmpty())
            {
                Supplier<Object> task = Delegator.waitReturn.poll();
                
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
                //noinspection ResultOfMethodCallIgnored
                Delegator.waitReturnResults.offer(new Pair<>(result, except));
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
                Pair<Integer, Exception> result = Delegator.waitRunResults.take();
                if (result.a != 0) throw new RuntimeException(result.b);
            }
            catch (InterruptedException e)
            {
                Engine.LOGGER.warning("Run task was interrupted.");
            }
        }
        
        public static <T> @Nullable T waitReturnTask(@NotNull Supplier<T> task)
        {
            if (Thread.currentThread().getName().equals("main")) return task.get();
            
            //noinspection unchecked
            Delegator.waitReturn.offer((Supplier<Object>) task);
            
            try
            {
                Pair<Object, Exception> result = Delegator.waitReturnResults.take();
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
        private static final Vector2i pos  = new Vector2i();
        private static final Vector2i size = new Vector2i();
        
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
        
        private static void update()
        {
            double aspect = (double) (Engine.screenSize.x * Engine.pixelSize.x) / (double) (Engine.screenSize.y * Engine.pixelSize.y);
            
            int frameWidth  = Window.get().framebufferWidth();
            int frameHeight = Window.get().framebufferHeight();
            
            Viewport.size.set(frameWidth, (int) (frameWidth / aspect));
            if (Viewport.size.y > frameHeight) Viewport.size.set((int) (frameHeight * aspect), frameHeight);
            Viewport.pos.set((frameWidth - Viewport.size.x) >> 1, (frameHeight - Viewport.size.y) >> 1);
            
            Engine.pixelSize.x = Math.max(Viewport.size.x / Engine.screenSize.x, 1);
            Engine.pixelSize.y = Math.max(Viewport.size.y / Engine.screenSize.y, 1);
            
            GL33.glViewport(Viewport.x(), Viewport.y(), Viewport.width(), Viewport.height()); // TODO - Remove this
        }
    }
    
    public static final class Debug
    {
        private static boolean enabled;
        
        private static GLProgram     program;
        private static GLVertexArray vertexArray;
        private static Matrix4d      pv;
        
        private static Color textColor;
        private static Color backColor;
        
        private static final List<Triple<Integer, Integer, String>> lines = new ArrayList<>();
        
        private static String notification;
        private static long   notificationTime;
        
        private static void setup()
        {
            Debug.enabled = true;
            
            String vert = """
                          #version 330
                          layout(location = 0) in vec3 aPos;
                          layout(location = 1) in vec4 aCol;
                          uniform mat4 pv;
                          out vec4 color;
                          void main(void) {
                              color = aCol;
                              gl_Position = pv * vec4(aPos, 1.0);
                          }
                          """;
            String frag = """
                          #version 330
                          in vec4 color;
                          out vec4 FragColor;
                          void main(void) {
                              FragColor = color;
                          }
                          """;
            Debug.program = GLProgram.loadFromCode(vert, null, frag);
            
            int elementsCount = 1024;
            
            IntBuffer indices = MemoryUtil.memCallocInt(elementsCount * 6); // 6 int per quad (indices)
            for (int i = 0; i < elementsCount; ++i)
            {
                indices.put(4 * i);
                indices.put(4 * i + 1);
                indices.put(4 * i + 2);
                indices.put(4 * i);
                indices.put(4 * i + 2);
                indices.put(4 * i + 3);
            }
            indices.flip();
            
            Debug.vertexArray = GLVertexArray.builder()
                                             .buffer(elementsCount, Usage.DYNAMIC_DRAW,
                                                     new GLAttribute(GLType.FLOAT, 3, false),
                                                     new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                             // .indexBuffer(indices, Usage.STATIC_DRAW)
                                             .build();
            MemoryUtil.memFree(indices);
            
            Debug.pv = new Matrix4d();
            
            Debug.textColor = Color.create(ColorFormat.RGBA).set(255, 255, 255, 255);
            Debug.backColor = Color.create(ColorFormat.RGBA).set(255, 255, 255, 0x54);
        }
        
        private static void destroy()
        {
            Debug.program.delete();
            Debug.vertexArray.delete();
            
            Debug.textColor.free();
            Debug.backColor.free();
        }
        
        private static void handleEvents()
        {
            if (Keyboard.get().down(Keyboard.Key.F11) && Modifier.all(Modifier.CONTROL, Modifier.ALT, Modifier.SHIFT))
            {
                Debug.enabled = !Debug.enabled;
                Debug.notification(Debug.enabled ? "Debug Mode: On" : "Debug Mode: Off");
            }
            if (Keyboard.get().down(Keyboard.Key.F12) && Modifier.all(Modifier.CONTROL, Modifier.ALT, Modifier.SHIFT))
            {
                Time.paused = !Time.paused;
                Debug.notification(Time.paused ? "Engine Paused" : "Engine Unpaused");
            }
        }
        
        private static void draw()
        {
            if (Debug.notification != null && Time.current - Debug.notificationTime < Time.notificationDur)
            {
                int x = (Window.get().framebufferWidth() - textWidth(Debug.notification)) >> 1;
                int y = (Window.get().framebufferHeight() - textHeight(Debug.notification)) >> 1;
                
                drawText(x, y, Debug.notification);
            }
            if (Debug.enabled)
            {
                String line;
                drawText(0, 0, line = String.format("Frame: %s", Time.totalFrameCount));
                drawText(0, textHeight(line), String.format("Time: %.3f", Time.totalFrameTime / 1_000_000_000D));
            }
            if (!Debug.lines.isEmpty())
            {
                int fbWidth  = Window.get().framebufferWidth();
                int fbHeight = Window.get().framebufferHeight();
                
                GL33.glViewport(0, 0, fbWidth, fbHeight);
                
                GLProgram.bind(Debug.program);
                GLProgram.Uniform.mat4("pv", Debug.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
                
                GLVertexArray.bind(Debug.vertexArray);
                GLState.winding(Winding.CW);
                GLState.depthMode(DepthMode.ALWAYS);
                
                try (MemoryStack stack = MemoryStack.stackPush())
                {
                    ByteBuffer buffer = stack.malloc((int) Debug.vertexArray.buffer(0).size());
                    
                    ByteBuffer textColor = Debug.textColor.toBuffer();
                    ByteBuffer backColor = Debug.backColor.toBuffer();
                    for (Triple<Integer, Integer, String> line : Debug.lines)
                    {
                        float x1 = line.a;
                        float y1 = line.b;
                        float x2 = line.a + Debug.textWidth(line.c) + 2;
                        float y2 = line.b + Debug.textHeight(line.c);
                        
                        buffer.putFloat(x1);
                        buffer.putFloat(y1);
                        buffer.putFloat(0F);
                        buffer.put(backColor.clear());
                        buffer.putFloat(x2);
                        buffer.putFloat(y1);
                        buffer.putFloat(0F);
                        buffer.put(backColor.clear());
                        buffer.putFloat(x2);
                        buffer.putFloat(y2);
                        buffer.putFloat(0F);
                        buffer.put(backColor.clear());
                        buffer.putFloat(x1);
                        buffer.putFloat(y2);
                        buffer.putFloat(0F);
                        buffer.put(backColor.clear());
                        
                        int quads = stb_easy_font_print(line.a + 2, line.b + 2, line.c, textColor, buffer);
                        
                        Debug.vertexArray.buffer(0).set(buffer.clear());
                        Debug.vertexArray.draw(DrawMode.QUADS, 4 + quads * 4);
                    }
                }
                
                Debug.lines.clear();
            }
        }
        
        /**
         * @return {@code true} if debug mode is enabled
         */
        public static boolean enabled()
        {
            return Debug.enabled;
        }
        
        /**
         * Sets the color of the text when rendering debug text.
         *
         * @param color The new color.
         */
        public static void textColor(Colorc color)
        {
            Debug.textColor.set(color);
        }
        
        /**
         * Sets the color of the background when rendering debug text.
         *
         * @param color The new color.
         */
        public static void backColor(Colorc color)
        {
            Debug.backColor.set(color);
        }
        
        /**
         * Draws Debug text to the screen. The coordinates passed in will not be affected by any transformations.
         *
         * @param x    The x coordinate of the top left point if the text.
         * @param y    The y coordinate of the top left point if the text.
         * @param text The text to render.
         */
        public static void drawText(int x, int y, String text)
        {
            Debug.lines.add(new Triple<>(x, y, text));
        }
        
        public static void notification(String notification)
        {
            Debug.notification     = notification;
            Debug.notificationTime = Time.nanosecondsActual();
        }
        
        /**
         * Gets the width in pixels of the provided text.
         *
         * @param text The text
         * @return The width in pixels
         */
        public static int textWidth(String text)
        {
            return stb_easy_font_width(text);
        }
        
        /**
         * Gets the height in pixels of the provided text.
         *
         * @param text The text
         * @return The height in pixels
         */
        public static int textHeight(String text)
        {
            return stb_easy_font_height(text);
        }
        
        // /**
        //  * @return The Engine's Profiler instance.
        //  */
        // public static Profiler profiler()
        // {
        //     return Debug.profiler;
        // }
    }
    
    public static final class Draw
    {
        private static final Draw2DPoint    DRAW_2D_POINT    = new Draw2DPoint();
        private static final Draw2DLine     DRAW_2D_LINE     = new Draw2DLine();
        private static final Draw2DLines    DRAW_2D_LINES    = new Draw2DLines();
        private static final Draw2DTriangle DRAW_2D_TRIANGLE = new Draw2DTriangle();
        private static final Fill2DTriangle FILL_2D_TRIANGLE = new Fill2DTriangle();
        private static final Draw2DTexture  DRAW_2D_TEXTURE  = new Draw2DTexture();
        
        public static void clearBackground(@NotNull Colorc color)
        {
            GLState.clearColor(color.rf(), color.gf(), color.bf(), color.af());
            GLState.clearScreenBuffers();
        }
        
        public static Draw2DPoint point2D()
        {
            return Draw.DRAW_2D_POINT;
        }
        
        public static Draw2DLine line2D()
        {
            return Draw.DRAW_2D_LINE;
        }
        
        public static Draw2DLines lines2D()
        {
            return Draw.DRAW_2D_LINES;
        }
        
        public static Draw2DTriangle drawTriangle2D()
        {
            return Draw.DRAW_2D_TRIANGLE;
        }
        
        public static Fill2DTriangle fillTriangle2D()
        {
            return Draw.FILL_2D_TRIANGLE;
        }
        
        public static Draw2DTexture drawTexture2D()
        {
            return Draw.DRAW_2D_TEXTURE;
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
            
            Time.init();
            
            Extensions.preSetup();
            
            Engine.LOGGER.info("Instance Setup");
            Engine.instance.setup();
            
            Extensions.postSetup();
            
            if (Engine.windowEnabled)
            {
                Window.get().unmakeCurrent();
                
                final CountDownLatch latch = new CountDownLatch(1);
                
                new Thread(() -> {
                    try
                    {
                        Window.get().makeCurrent();
                        
                        Extensions.renderSetup();
                        
                        while (Engine.running)
                        {
                            Time.current = Time.nanosecondsActual();
                            
                            if (Time.shouldDraw())
                            {
                                // TODO Profiler Start Frame
                                
                                Extensions.preEvents();
                                
                                Events.postEvents();
                                
                                Extensions.postEvents();
                                
                                Debug.handleEvents();
                                
                                Viewport.update();
                                
                                GLFramebuffer.bind(null);
                                GLProgram.bind(null);
                                GLTexture.bind(null);
                                
                                GLState.defaultState();
                                
                                if (!Time.paused)
                                {
                                    // TODO - Bind RenderTarget Here
                                    
                                    // Engine.renderer.start(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extensions.preDraw();
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Engine.instance.draw(Time.delta / 1_000_000_000D);
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // Engine.renderer.push(); // TODO
                                    Extensions.postDraw();
                                    // Engine.renderer.pop(); // TODO
                                    
                                    // GLState.disable(GLState.DEPTH_TEST);
                                    // GLState.drawRenderBatch(); // Update and draw internal render batch
                                    
                                    // Engine.renderer.finish(); // TODO
                                    
                                    GLBatch.get().draw();
                                }
                                
                                // TODO - Draw to Default FrameBuffer
                                
                                Debug.draw();
                                
                                Window.get().swap();
                                
                                Time.incFrame();
                                
                                // Layers.draw();
                                
                                // TODO Profiler End Frame
                            }
                            
                            // if (Engine.screenshot != null)
                            // {
                            //     String fileName = Engine.screenshot + (!Engine.screenshot.endsWith(".png") ? ".png" : "");
                            //
                            //     int w = Engine.viewSize.left;
                            //     int h = Engine.viewSize.bottom;
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
                            
                            if (Time.shouldUpdate())
                            {
                                Window.get().title(String.format("Engine - %s - %s", Engine.instance.name, Time.getTimeString()));
                                
                                // Debug.update();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        Engine.LOGGER.severe(e);
                    }
                    finally
                    {
                        Extensions.renderDestroy();
                        
                        // Renderer.destroy(); // TODO
                        // Layers.destroy();
                        Debug.destroy();
                        
                        GLState.destroy();
                        
                        Window.get().unmakeCurrent();
                        
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
            
            Window.destroy();
            
            org.lwjgl.opengl.GL.destroy();
            
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
        Mouse.setup();
        Keyboard.setup();
        Joystick.setup();
        
        // Engine.LOGGER.finer("Initializing OpenGL Context");
        Window.get().makeCurrent();
        
        GLState.setup();
        
        // Renderer.init(); // TODO
        // Layers.init();
        Debug.setup();
        
        // Engine.renderer = new Renderer(Layers.layers[0]); // TODO
        
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
        for (int i = 0; i < className.length(); i++)
        {
            char ch = className.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) name.append(' ');
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
