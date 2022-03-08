package pe;

import rutils.Logger;

public class Input
{
    private static final Logger LOGGER = new Logger();
    
    private static long holdFrequency      = 1_000_000L;
    private static long doublePressedDelay = 200_000_000L;
    
    /**
     * @return The frequency, in seconds, that a "Held" event will be generated while an ButtonInput is down.
     */
    public static double holdFrequency()
    {
        return Input.holdFrequency / 1_000_000_000D;
    }
    
    public static long holdFrequencyL()
    {
        return Input.holdFrequency;
    }
    
    /**
     * Sets the frequency, in seconds, that a "Held" event will be generated while an ButtonInput is down.
     *
     * @param holdFrequency The frequency, in seconds, that a "Held" event will be generated while an ButtonInput is down.
     */
    public static void holdFrequency(double holdFrequency)
    {
        LOGGER.finest("Setting InputDevice Hold Frequency:", holdFrequency);
        
        Input.holdFrequency = (long) (holdFrequency * 1_000_000_000L);
    }
    
    /**
     * @return The delay, in seconds, before an ButtonInput is pressed twice to be a double pressed.
     */
    public static double doublePressedDelay()
    {
        return Input.doublePressedDelay / 1_000_000_000D;
    }
    
    public static long doublePressedDelayL()
    {
        return Input.doublePressedDelay;
    }
    
    /**
     * Sets the delay, in seconds, before an ButtonInput is pressed twice to be a double pressed.
     *
     * @param doublePressedDelay The delay, in seconds, before an ButtonInput is pressed twice to be a double pressed.
     */
    public static void doublePressedDelay(double doublePressedDelay)
    {
        LOGGER.finest("Setting InputDevice Double Delay:", doublePressedDelay);
        
        Input.doublePressedDelay = (long) (doublePressedDelay * 1_000_000_000L);
    }
    
    protected int state = -1, _state = -1;
    
    protected boolean held;
    protected long    heldTime = Long.MAX_VALUE;
    
    protected long downTime;
    protected int  downCount;
}
