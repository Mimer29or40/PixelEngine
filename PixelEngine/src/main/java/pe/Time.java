package pe;

public final class Time
{
    static long start; // The System time that the engine was started
    
    static long currFrameTimestamp;  // The time of the current frame (ns)
    static long lastFrameTimestamp;  // The time of the last frame (ns)
    static long deltaFrameTimestamp;  // The time of the last frame (ns)
    
    static boolean paused; // If the engine is paused
    
    static final long[] frameTimes    = new long[512]; // The time taken to render the last frame (ns)
    static final long[] frameTimesRaw = new long[512]; // actualFrameTime -> low-pass filtered (ns)
    static       long   frameTimeTarget; // The target time taken to send on rendering a frame (ns)
    
    static long engineTime;       // The time that since the start of the engine (ns)
    static long engineFrameCount; // The number of frames that have been rendered
    
    static void init()
    {
        Time.start = System.nanoTime();
        
        Time.paused = false;
    }
    
    static boolean startFrame()
    {
        Time.currFrameTimestamp  = Time.getRawNS();
        Time.deltaFrameTimestamp = Time.currFrameTimestamp - Time.lastFrameTimestamp;
        if (Time.deltaNS() >= Time.frameTimeTarget)
        {
            Time.lastFrameTimestamp = Time.currFrameTimestamp;
            return true;
        }
        return false;
    }
    
    @SuppressWarnings("SuspiciousSystemArraycopy")
    static void endFrame()
    {
        if (!Time.paused)
        {
            System.arraycopy(Time.frameTimes, 0, Time.frameTimes, 1, Time.frameTimes.length - 1);
            System.arraycopy(Time.frameTimesRaw, 0, Time.frameTimesRaw, 1, Time.frameTimesRaw.length - 1);
            
            long smoothing = 1;
            
            Time.frameTimesRaw[0] = Time.deltaNS();
            Time.frameTimes[0] += Time.deltaNS() * (Time.frameTimesRaw[0] - Time.frameTimes[0]) / (smoothing * 1_000_000_000L);
            
            Time.engineTime += Time.deltaNS();
            Time.engineFrameCount++;
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
     * @return The raw engine time in nanoseconds. Unaffected by pausing.
     */
    public static long getRawNS()
    {
        return Time.start > 0 ? System.nanoTime() - Time.start : -1L;
    }
    
    /**
     * @return The raw engine time. Unaffected by pausing.
     */
    public static double getRaw()
    {
        return (double) Time.getRawNS() / 1_000_000_000D;
    }
    
    /**
     * @return The engine time in nanoseconds.
     */
    public static long getNS()
    {
        return Time.engineTime;
    }
    
    /**
     * @return The engine time.
     */
    public static double get()
    {
        return (double) Time.getNS() / 1_000_000_000D;
    }
    
    /**
     * @return The time in nanoseconds since the last render frame.
     */
    public static long deltaNS()
    {
        return Time.deltaFrameTimestamp;
    }
    
    /**
     * @return The time since the last render frame.
     */
    public static double delta()
    {
        return (double) Time.deltaNS() / 1_000_000_000D;
    }
    
    /**
     * @return The actual time the last frame took to render in nanoseconds.
     */
    public static long frameTimeRawNS()
    {
        return Time.frameTimesRaw[0];
    }
    
    /**
     * @return The actual time the last frame took to render.
     */
    public static double frameTimeRaw()
    {
        return (double) Time.frameTimeRawNS() / 1_000_000_000D;
    }
    
    /**
     * @return The low-pass filtered time the last frame took to render in nanoseconds.
     */
    public static long frameTimeNS()
    {
        return Time.frameTimes[0];
    }
    
    /**
     * @return The low-pass filtered time the last frame took to render.
     */
    public static double frameTime()
    {
        return (double) Time.frameTimeNS() / 1_000_000_000D;
    }
    
    /**
     * @return The total number of rendered frames.
     */
    public static long frameCount()
    {
        return Time.engineFrameCount;
    }
    
    /**
     * @return The actual frame rate of the last frame.
     */
    public static int frameRateRaw()
    {
        return (int) (1_000_000_000L / Time.frameTimeRawNS());
    }
    
    /**
     * @return The low-pass filtered frame rate of the last frame.
     */
    public static int frameRate()
    {
        return (int) (1_000_000_000L / Time.frameTimeNS());
    }
    
    /**
     * Sets the target frame rate of the engine. If {@code frameRate < 0}, then
     * no limit is assumed.
     *
     * @param frameRate The new frame rate.
     */
    public static void frameRate(int frameRate)
    {
        Time.frameTimeTarget = frameRate > 0 ? 1_000_000_000L / (long) frameRate : 0L;
    }
}
