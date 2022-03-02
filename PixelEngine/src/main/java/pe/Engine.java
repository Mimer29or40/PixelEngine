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
import rutils.Math;
import rutils.group.Pair;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
        }
        
        private static void draw()
        {
            GLProgram.bind(Viewport.program);
            
            GLTexture.bind(Viewport.framebuffer.color());
            Viewport.vertexArray.draw(DrawMode.TRIANGLES, 0, 6);
        }
    }
    
    public static final class Debug
    {
        private static final class KeyLayout
        {
            final int x, y, width, height, tx, ty;
            final String text;
            
            private KeyLayout(int x, int y, int width, int height, String text)
            {
                this.x      = x;
                this.y      = y;
                this.width  = width;
                this.height = height;
                this.text   = text;
                this.tx     = x + (width - textWidth(text)) / 2 + 1;
                this.ty     = y + (height - textHeight(text)) / 2 + 2;
            }
        }
        
        private static boolean enabled;
        private static boolean wireframe;
        
        private static int vertices;
        private static int draws;
        
        private static GLProgram     program;
        private static ByteBuffer    vertexBuffer;
        private static GLVertexArray vertexArray;
        private static Matrix4d      pv;
        
        private static Color textColor;
        private static Color backColor;
        
        private static final List<Object[]> toRender = new ArrayList<>();
        
        private static String notification;
        private static long   notificationTime;
        
        private static final EnumMap<Keyboard.Key, KeyLayout> layoutMap = new EnumMap<>(Keyboard.Key.class);
        
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
            
            int vertexLimit = 4096;
            
            IntBuffer indices = MemoryUtil.memAllocInt(vertexLimit * 6); // 6 vertices per quad (indices)
            for (int i = 0; i < vertexLimit; ++i)
            {
                indices.put(4 * i);
                indices.put(4 * i + 1);
                indices.put(4 * i + 2);
                indices.put(4 * i);
                indices.put(4 * i + 2);
                indices.put(4 * i + 3);
            }
            
            Debug.vertexBuffer = MemoryUtil.memAlloc(vertexLimit * (Float.BYTES * 3 + Byte.BYTES * 4));
            
            Debug.vertexArray = GLVertexArray.builder()
                                             .buffer(Debug.vertexBuffer, Usage.DYNAMIC_DRAW,
                                                     new GLAttribute(GLType.FLOAT, 3, false),
                                                     new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                             .indexBuffer(indices.clear(), Usage.STATIC_DRAW)
                                             .build();
            MemoryUtil.memFree(indices);
            
            Debug.pv = new Matrix4d();
            
            Debug.textColor = Color.create(ColorFormat.RGBA).set(255, 255, 255, 255);
            Debug.backColor = Color.create(ColorFormat.RGBA).set(255, 255, 255, 0x54);
            
            int x, y;
            int gap  = 2;
            int size = 24;
            
            x = 0;
            y = 0;
            Debug.layoutMap.put(Keyboard.Key.ESCAPE, new KeyLayout(x, y, size, size, "Esc"));
            x += size + gap + 22;
            Debug.layoutMap.put(Keyboard.Key.F1, new KeyLayout(x, y, size, size, "F1"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F2, new KeyLayout(x, y, size, size, "F2"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F3, new KeyLayout(x, y, size, size, "F3"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F4, new KeyLayout(x, y, size, size, "F4"));
            x += size + gap + 20;
            Debug.layoutMap.put(Keyboard.Key.F5, new KeyLayout(x, y, size, size, "F5"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F6, new KeyLayout(x, y, size, size, "F6"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F7, new KeyLayout(x, y, size, size, "F7"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F8, new KeyLayout(x, y, size, size, "F8"));
            x += size + gap + 20;
            Debug.layoutMap.put(Keyboard.Key.F9, new KeyLayout(x, y, size, size, "F9"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F10, new KeyLayout(x, y, size, size, "F10"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F11, new KeyLayout(x, y, size, size, "F11"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F12, new KeyLayout(x, y, size, size, "F12"));
            x += size + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.PRINT_SCREEN, new KeyLayout(x, y, size, size, "PrSc"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.SCROLL_LOCK, new KeyLayout(x, y, size, size, "Scrl"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.PAUSE, new KeyLayout(x, y, size, size, "Pse"));
            
            x = 0;
            y += size + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.GRAVE, new KeyLayout(x, y, size, size, "~\n`"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K1, new KeyLayout(x, y, size, size, "1"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K2, new KeyLayout(x, y, size, size, "2"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K3, new KeyLayout(x, y, size, size, "3"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K4, new KeyLayout(x, y, size, size, "4"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K5, new KeyLayout(x, y, size, size, "5"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K6, new KeyLayout(x, y, size, size, "6"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K7, new KeyLayout(x, y, size, size, "7"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K8, new KeyLayout(x, y, size, size, "8"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K9, new KeyLayout(x, y, size, size, "9"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K0, new KeyLayout(x, y, size, size, "0"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.MINUS, new KeyLayout(x, y, size, size, "_\n-"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.EQUAL, new KeyLayout(x, y, size, size, "+\n="));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.BACKSPACE, new KeyLayout(x, y, 60, size, "Back"));
            x += 60 + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.INSERT, new KeyLayout(x, y, size, size, "Ins"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.HOME, new KeyLayout(x, y, size, size, "Hme"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.PAGE_UP, new KeyLayout(x, y, size, size, "PgUp"));
            x += size + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.NUM_LOCK, new KeyLayout(x, y, size, size, "Num"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_DIVIDE, new KeyLayout(x, y, size, size, "/"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_MULTIPLY, new KeyLayout(x, y, size, size, "*"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_SUBTRACT, new KeyLayout(x, y, size, size, "-"));
            
            x = 0;
            y += size + gap;
            Debug.layoutMap.put(Keyboard.Key.TAB, new KeyLayout(x, y, 42, size, "Tab"));
            x += 42 + gap;
            Debug.layoutMap.put(Keyboard.Key.Q, new KeyLayout(x, y, size, size, "Q"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.W, new KeyLayout(x, y, size, size, "W"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.E, new KeyLayout(x, y, size, size, "E"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.R, new KeyLayout(x, y, size, size, "R"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.T, new KeyLayout(x, y, size, size, "T"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.Y, new KeyLayout(x, y, size, size, "Y"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.U, new KeyLayout(x, y, size, size, "U"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.I, new KeyLayout(x, y, size, size, "I"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.O, new KeyLayout(x, y, size, size, "O"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.P, new KeyLayout(x, y, size, size, "P"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.L_BRACKET, new KeyLayout(x, y, size, size, "{\n["));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.R_BRACKET, new KeyLayout(x, y, size, size, "}\n}"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.BACKSLASH, new KeyLayout(x, y, 42, size, "|\n\\"));
            x += 42 + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.DELETE, new KeyLayout(x, y, size, size, "Del"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.END, new KeyLayout(x, y, size, size, "End"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.PAGE_DOWN, new KeyLayout(x, y, size, size, "PgDn"));
            x += size + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.KP_7, new KeyLayout(x, y, size, size, "7"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_8, new KeyLayout(x, y, size, size, "8"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_9, new KeyLayout(x, y, size, size, "9"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_ADD, new KeyLayout(x, y, size, size * 2 + gap, "+"));
            
            x = 0;
            y += size + gap;
            Debug.layoutMap.put(Keyboard.Key.CAPS_LOCK, new KeyLayout(x, y, 46, size, "Caps"));
            x += 46 + gap;
            Debug.layoutMap.put(Keyboard.Key.A, new KeyLayout(x, y, size, size, "A"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.S, new KeyLayout(x, y, size, size, "S"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.D, new KeyLayout(x, y, size, size, "D"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.F, new KeyLayout(x, y, size, size, "F"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.G, new KeyLayout(x, y, size, size, "G"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.H, new KeyLayout(x, y, size, size, "H"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.J, new KeyLayout(x, y, size, size, "J"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.K, new KeyLayout(x, y, size, size, "K"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.L, new KeyLayout(x, y, size, size, "L"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.SEMICOLON, new KeyLayout(x, y, size, size, ":\n;"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.APOSTROPHE, new KeyLayout(x, y, size, size, "\"\n'"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.ENTER, new KeyLayout(x, y, 64, size, "<-|"));
            x += 64 + (size + gap * 2) * 3;
            Debug.layoutMap.put(Keyboard.Key.KP_4, new KeyLayout(x, y, size, size, "4"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_5, new KeyLayout(x, y, size, size, "5"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_6, new KeyLayout(x, y, size, size, "6"));
    
            x = 0;
            y += size + gap;
            Debug.layoutMap.put(Keyboard.Key.L_SHIFT, new KeyLayout(x, y, 64, size, "Shift"));
            x += 64 + gap;
            Debug.layoutMap.put(Keyboard.Key.Z, new KeyLayout(x, y, size, size, "Z"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.X, new KeyLayout(x, y, size, size, "X"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.C, new KeyLayout(x, y, size, size, "C"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.V, new KeyLayout(x, y, size, size, "V"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.B, new KeyLayout(x, y, size, size, "B"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.N, new KeyLayout(x, y, size, size, "N"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.M, new KeyLayout(x, y, size, size, "M"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.COMMA, new KeyLayout(x, y, size, size, "<\n,"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.PERIOD, new KeyLayout(x, y, size, size, ">\n."));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.SLASH, new KeyLayout(x, y, size, size, "?\n/"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.R_SHIFT, new KeyLayout(x, y, 72, size, "Shift"));
            x += 72 + gap * 3 + size;
            Debug.layoutMap.put(Keyboard.Key.UP, new KeyLayout(x, y, size, size, "/\\"));
            x += size + gap * 3 + size;
            Debug.layoutMap.put(Keyboard.Key.KP_1, new KeyLayout(x, y, size, size, "1"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_2, new KeyLayout(x, y, size, size, "2"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_3, new KeyLayout(x, y, size, size, "3"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.KP_ENTER, new KeyLayout(x, y, size, size * 2 + gap, "<-|"));
    
            x = 0;
            y += size + gap;
            Debug.layoutMap.put(Keyboard.Key.L_CONTROL, new KeyLayout(x, y, 42, size, "Ctrl"));
            x += 42 + gap;
            Debug.layoutMap.put(Keyboard.Key.L_SUPER, new KeyLayout(x, y, 30, size, "Super"));
            x += 30 + gap;
            Debug.layoutMap.put(Keyboard.Key.L_ALT, new KeyLayout(x, y, 30, size, "Alt"));
            x += 30 + gap;
            Debug.layoutMap.put(Keyboard.Key.SPACE, new KeyLayout(x, y, 150, size, "Space"));
            x += 150 + gap;
            Debug.layoutMap.put(Keyboard.Key.R_ALT, new KeyLayout(x, y, 30, size, "Alt"));
            x += 30 + gap;
            Debug.layoutMap.put(Keyboard.Key.UNKNOWN, new KeyLayout(x, y, 30, size, "Func"));
            x += 30 + gap;
            Debug.layoutMap.put(Keyboard.Key.MENU, new KeyLayout(x, y, 30, size, "Menu"));
            x += 30 + gap;
            Debug.layoutMap.put(Keyboard.Key.R_CONTROL, new KeyLayout(x, y, 42, size, "Ctrl"));
            x += 42 + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.LEFT, new KeyLayout(x, y, size, size, "{-"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.DOWN, new KeyLayout(x, y, size, size, "\\/"));
            x += size + gap;
            Debug.layoutMap.put(Keyboard.Key.RIGHT, new KeyLayout(x, y, size, size, "-}"));
            x += size + gap * 2;
            Debug.layoutMap.put(Keyboard.Key.KP_0, new KeyLayout(x, y, size * 2 + gap, size, "0"));
            x += (size + gap) * 2;
            Debug.layoutMap.put(Keyboard.Key.KP_DECIMAL, new KeyLayout(x, y, size, size, "."));
        }
        
        private static void destroy()
        {
            Debug.program.delete();
            MemoryUtil.memFree(Debug.vertexBuffer);
            Debug.vertexArray.delete();
            
            Debug.textColor.free();
            Debug.backColor.free();
        }
        
        private static void handleEvents()
        {
            if (Keyboard.get().down(Keyboard.Key.F10) && Modifier.all(Modifier.CONTROL, Modifier.ALT, Modifier.SHIFT))
            {
                Debug.wireframe = !Debug.wireframe;
                Debug.notification(Debug.wireframe ? "Wireframe Mode: On" : "Wireframe Mode: Off");
            }
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
                
                drawTextWithBackground(x, y, Debug.notification, null, null);
            }
            if (Debug.enabled)
            {
                Collector<CharSequence, ?, String> collect = Collectors.joining(", ", "[", "]");
                
                EnumSet<Modifier>     mods    = EnumSet.complementOf(EnumSet.of(Modifier.ANY));
                EnumSet<Mouse.Button> buttons = EnumSet.complementOf(EnumSet.of(Mouse.Button.UNKNOWN));
                EnumSet<Keyboard.Key> keys    = EnumSet.complementOf(EnumSet.of(Keyboard.Key.UNKNOWN));
                
                String[] lines = {
                        String.format("Frame: %s", Time.totalFrameCount),
                        String.format("Time: %.3f", Time.totalFrameTime / 1_000_000_000D),
                        String.format("Wireframe: %s", Debug.wireframe),
                        String.format("Vertex Count: %s", Debug.vertices),
                        String.format("Draw Calls: %s", Debug.draws),
                        String.format("Active Modifiers: %s", mods.stream().filter(Modifier::isActive).map(Enum::name).collect(collect)),
                        String.format("Mouse Position: (%s, %s)", Math.round(Mouse.get().x(), 3), Math.round(Mouse.get().y(), 3)),
                        String.format("Mouse Buttons Down: %s", buttons.stream().filter(Mouse.get()::held).map(Enum::name).collect(collect)),
                        String.format("Keyboard Keys Down: %s", keys.stream().filter(Keyboard.get()::held).map(Enum::name).collect(collect)),
                        };
                
                int x = 0, y = 0;
                for (String line : lines)
                {
                    drawTextWithBackground(x, y, line, null, null);
                    y += textHeight(line);
                }
                
                for (Keyboard.Key key : Debug.layoutMap.keySet())
                {
                    KeyLayout l = Debug.layoutMap.get(key);
                    Colorc    c = Keyboard.get().held(key) ? Color.LIGHT_GRAY : Color.GRAY;
                    
                    drawQuad(x + l.x, y + l.y, l.width, l.height, c);
                    drawText(x + l.tx, y + l.ty, l.text, Color.WHITE);
                }
                
            }
            if (!Debug.toRender.isEmpty())
            {
                int fbWidth  = Window.get().framebufferWidth();
                int fbHeight = Window.get().framebufferHeight();
                
                GL33.glViewport(0, 0, fbWidth, fbHeight);
                
                GLProgram.bind(Debug.program);
                GLProgram.Uniform.mat4("pv", Debug.pv.setOrtho(0, fbWidth, fbHeight, 0, -1, 1));
                
                GLState.winding(Winding.CW);
                
                int quads = 0;
                for (Object[] renderData : Debug.toRender)
                {
                    switch ((String) renderData[0])
                    {
                        case "quad" -> {
                            float x1 = (float) renderData[1];
                            float y1 = (float) renderData[2];
                            float x2 = x1 + (float) renderData[3];
                            float y2 = y1 + (float) renderData[4];
                            int   c  = (int) renderData[5];
                            
                            // 64 bytes per quad.
                            if (Debug.vertexBuffer.remaining() < 64)
                            {
                                Debug.vertexArray.buffer(0).set(0, Debug.vertexBuffer.clear());
                                Debug.vertexArray.draw(DrawMode.TRIANGLES, quads * 6);
                                quads = 0;
                            }
                            
                            Debug.vertexBuffer.putFloat(x1);
                            Debug.vertexBuffer.putFloat(y1);
                            Debug.vertexBuffer.putFloat(0F);
                            Debug.vertexBuffer.putInt(c);
                            Debug.vertexBuffer.putFloat(x2);
                            Debug.vertexBuffer.putFloat(y1);
                            Debug.vertexBuffer.putFloat(0F);
                            Debug.vertexBuffer.putInt(c);
                            Debug.vertexBuffer.putFloat(x2);
                            Debug.vertexBuffer.putFloat(y2);
                            Debug.vertexBuffer.putFloat(0F);
                            Debug.vertexBuffer.putInt(c);
                            Debug.vertexBuffer.putFloat(x1);
                            Debug.vertexBuffer.putFloat(y2);
                            Debug.vertexBuffer.putFloat(0F);
                            Debug.vertexBuffer.putInt(c);
                            
                            quads++;
                        }
                        case "text" -> {
                            float  x  = (float) renderData[1];
                            float  y  = (float) renderData[2];
                            String t  = (String) renderData[3];
                            int    ct = (int) renderData[4];
                            
                            // 11 quads max * 64 bytes per quad.
                            if (Debug.vertexBuffer.remaining() < t.length() * 704)
                            {
                                Debug.vertexArray.buffer(0).set(0, Debug.vertexBuffer.clear());
                                Debug.vertexArray.draw(DrawMode.TRIANGLES, quads * 6);
                                quads = 0;
                            }
                            
                            int newQuads;
                            try (MemoryStack stack = MemoryStack.stackPush())
                            {
                                ByteBuffer textColor = stack.malloc(4).putInt(0, ct);
                                newQuads = stb_easy_font_print(x, y, t, textColor, Debug.vertexBuffer);
                                Debug.vertexBuffer.position(Debug.vertexBuffer.position() + newQuads * 64);
                            }
                            
                            quads += newQuads;
                        }
                    }
                }
                
                Debug.vertexArray.buffer(0).set(0, Debug.vertexBuffer.clear());
                Debug.vertexArray.draw(DrawMode.TRIANGLES, quads * 6);
                
                Debug.toRender.clear();
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
        public static void defaultTextColor(Colorc color)
        {
            Debug.textColor.set(color);
        }
        
        /**
         * Sets the color of the background when rendering debug text.
         *
         * @param color The new color.
         */
        public static void defaultBackgroundColor(Colorc color)
        {
            Debug.backColor.set(color);
        }
        
        /**
         * Draws a colored quad to the screen.
         *
         * @param x      The x coordinate of the top left point if the quad.
         * @param y      The y coordinate of the top left point if the quad.
         * @param width  The width of the quad.
         * @param height The height of the quad.
         * @param color  The color of the quad.
         */
        public static void drawQuad(int x, int y, int width, int height, @NotNull Colorc color)
        {
            Debug.toRender.add(new Object[] {"quad", (float) x, (float) y, (float) width, (float) height, color.toInt()});
        }
        
        /**
         * Draws Debug text to the screen with a background.
         *
         * @param x         The x coordinate of the top left point if the text.
         * @param y         The y coordinate of the top left point if the text.
         * @param text      The text to render.
         * @param textColor The color of the text.
         */
        public static void drawText(int x, int y, String text, @Nullable Colorc textColor)
        {
            if (textColor == null) textColor = Debug.textColor;
            Debug.toRender.add(new Object[] {"text", (float) x, (float) y, text, textColor.toInt()});
        }
        
        /**
         * Draws Debug text to the screen with a background.
         *
         * @param x               The x coordinate of the top left point if the text.
         * @param y               The y coordinate of the top left point if the text.
         * @param text            The text to render.
         * @param textColor       The color of the text.
         * @param backgroundColor The color of the background.
         */
        public static void drawTextWithBackground(int x, int y, String text, @Nullable Colorc textColor, @Nullable Colorc backgroundColor)
        {
            if (textColor == null) textColor = Debug.textColor;
            if (backgroundColor == null) backgroundColor = Debug.backColor;
            
            float width  = Debug.textWidth(text) + 2;
            float height = Debug.textHeight(text);
            Debug.toRender.add(new Object[] {"quad", (float) x, (float) y, width, height, backgroundColor.toInt()});
            Debug.toRender.add(new Object[] {"text", (float) x + 2, (float) y + 2, text, textColor.toInt()});
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
                                
                                if (!Time.paused)
                                {
                                    GLFramebuffer.bind(Viewport.framebuffer);
                                    
                                    GLState.defaultState();
                                    GLState.wireframe(Debug.wireframe);
                                    
                                    GLProgram.bind(null);
                                    
                                    GLBatch.get().start();
                                    
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
                                    
                                    GLBatch.BatchStats stats = GLBatch.get().stop();
                                    
                                    Debug.vertices = stats.vertices();
                                    Debug.draws    = stats.draws();
                                }
                                
                                Viewport.update();
                                
                                GLFramebuffer.bind(null);
                                
                                GLState.defaultState();
                                GLState.depthMode(DepthMode.NONE);
                                
                                GLState.clearScreenBuffers(EnumSet.of(ScreenBuffer.COLOR));
                                
                                Viewport.draw();
                                
                                Debug.draw();
                                
                                Window.get().swap();
                                
                                Time.incFrame();
                                
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
                        Viewport.destroy();
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
        Viewport.setup();
        Debug.setup();
        
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
