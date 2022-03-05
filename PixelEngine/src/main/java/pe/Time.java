package pe;

public final class Time
{
    static long start; // The System time that the engine was started
    
    static long currFrameTimestamp;  // The time of the current frame (ns)
    static long lastFrameTimestamp;  // The time of the last frame (ns)
    
    static boolean paused; // If the engine is paused
    
    static long frameTimeRaw;    // The time taken to render the last frame (ns)
    static long frameTime;       // actualFrameTime -> low-pass filtered (ns)
    static long frameTimeTarget; // The target time taken to send on rendering a frame (ns)
    
    static long engineTime;       // The time that since the start of the engine (ns)
    static long engineFrameCount; // The number of frames that have been rendered
    
    // static long start; // The time the engine started
    //
    // static boolean paused; // If time is paused
    //
    // static long frameRateActual; // The actual frame rate that the engine achieved
    // static long frameRateTarget; // The target frame rate that the engine will try to achieve
    //
    // static long totalFrameTime;  // The total time the engine has been not paused
    // static long totalFrameCount; // The total number of frames that the engine has rendered
    //
    // static long current; // The current time of the frame
    // static long delta;   // The time since the last frame
    //
    // static long drawTime;   // The time of the last frame
    // static long lastUpdate; // The time of the last title update
    //
    // static long updateFreq; // The frequency to update the frame time data
    //
    // static long totalTime;   // The total time since the last update
    // static int  totalFrames; // The number of frames since the last update
    //
    // static long minTime; // The min time that a frame took since the last update
    // static long maxTime; // The max time that a frame took since the last update
    //
    // static double avgFrameTime;
    // static double minFrameTime;
    // static double maxFrameTime;
    
    // static String getTimeString()
    // {
    //     return String.format("FPS(%s) SPF(Avg: %s us, Min: %s us, Max: %s us)",
    //                          Time.frameRateActual,
    //                          Time.avgFrameTime,
    //                          Time.minFrameTime,
    //                          Time.maxFrameTime);
    // }
    
    static void init()
    {
        Time.start = System.nanoTime();
        
        Time.paused = false;
        
        // Time.updateFreq = 1_000_000_000L / 4L;
        //
        // Time.totalTime   = 0;
        // Time.totalFrames = 0;
        //
        // Time.minTime = Long.MAX_VALUE;
        // Time.maxTime = Long.MIN_VALUE;
        //
        // Time.avgFrameTime = 0.0;
        // Time.minFrameTime = 0.0;
        // Time.maxFrameTime = 0.0;
    }
    
    static boolean startFrame()
    {
        Time.currFrameTimestamp = Time.getRawNS();
        if (Time.deltaNS() >= Time.frameTimeTarget)
        {
            Time.lastFrameTimestamp = Time.currFrameTimestamp;
            return true;
        }
        return false;
    }
    
    static void endFrame()
    {
        if (!Time.paused)
        {
            long smoothing = 20;
            
            Time.frameTimeRaw = Time.getRawNS() - Time.currFrameTimestamp;
            Time.frameTime += Time.deltaNS() * (Time.frameTimeRaw - Time.frameTime) / smoothing;
            
            Time.engineTime += Time.frameTimeRaw;
            Time.engineFrameCount++;
        }
    }
    
    // static boolean shouldUpdate()
    // {
    //     if (Time.current - Time.lastUpdate >= Time.updateFreq && Time.totalFrames > 0 && !Time.paused)
    //     {
    //         Time.lastUpdate = Time.current;
    //
    //         long timePerFrame = Time.totalTime / Time.totalFrames;
    //
    //         Time.frameRateActual = 1_000_000_000L / timePerFrame;
    //
    //         Time.avgFrameTime = timePerFrame / 1_000D;
    //         Time.minFrameTime = Time.minTime / 1_000D;
    //         Time.maxFrameTime = Time.maxTime / 1_000D;
    //
    //         Time.totalTime   = 0;
    //         Time.totalFrames = 0;
    //
    //         Time.minTime = Long.MAX_VALUE;
    //         Time.maxTime = Long.MIN_VALUE;
    //
    //         return true;
    //     }
    //     return false;
    // }
    
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
        return Time.currFrameTimestamp - Time.lastFrameTimestamp;
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
        return Time.frameTimeRaw;
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
        return Time.frameTime;
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
    
    // /**
    //  * Sets the number of times a second to update the frame time information.
    //  *
    //  * @param times The number of times a second. Default: 4
    //  */
    // public static void updateFrequency(int times)
    // {
    //     if (times <= 0) times = 4;
    //     Time.updateFreq = 1_000_000_000L / times;
    // }
}
