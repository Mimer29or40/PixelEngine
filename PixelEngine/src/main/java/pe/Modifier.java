package pe;

import org.jetbrains.annotations.NotNull;
import rutils.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static pe.Engine.Delegator;

public enum Modifier
{
    SHIFT(GLFW_MOD_SHIFT),
    CONTROL(GLFW_MOD_CONTROL),
    ALT(GLFW_MOD_ALT),
    SUPER(GLFW_MOD_SUPER),
    CAPS_LOCK(GLFW_MOD_CAPS_LOCK),
    NUM_LOCK(GLFW_MOD_NUM_LOCK),
    ;
    
    private static final Logger LOGGER = new Logger();
    
    static int activeMods = 0;
    
    public static int activeMods()
    {
        return Modifier.activeMods;
    }
    
    private static boolean lockMods = false;
    
    /**
     * @return Retrieves the lock mods flag.
     */
    public static boolean lockMods()
    {
        return Modifier.lockMods;
    }
    
    /**
     * Sets the lock mods flag. Set {@code true} to enable lock key modifier
     * bits, or {@code false} to disable them. If enabled, callbacks that
     * receive modifier bits will also have the {@link Modifier#CAPS_LOCK}
     * set when the event was generated with Caps Lock on, and the
     * {@link Modifier#NUM_LOCK} set when Num Lock was on.
     *
     * @param lockMods {@code true} to enable lockMods mode, otherwise {@code false}.
     */
    public static void lockMods(boolean lockMods)
    {
        Modifier.LOGGER.finest("Setting Lock Mods State:", lockMods);
        
        Modifier.lockMods = lockMods;
        Delegator.runTask(() -> glfwSetInputMode(Window.get().handle, GLFW_LOCK_KEY_MODS, lockMods ? GLFW_TRUE : GLFW_FALSE));
    }
    
    private final int value;
    
    Modifier(int value)
    {
        this.value = value;
    }
    
    static void setup()
    {
        lockMods(false);
    }
    
    /**
     * Checks if this modifier is active.
     *
     * @return {@code true} if this modifier is active, otherwise {@code false}
     */
    public boolean isActive()
    {
        return (Modifier.activeMods & this.value) > 0;
    }
    
    /**
     * Checks to see if any of the provided modifiers are set.
     * <p>
     * If no modifiers are provided, then it will return {@code true} if any
     * modifiers are currently active.
     *
     * @param modifiers The modifiers to query.
     * @return {@code true} if any of the provided modifiers are active.
     */
    public static boolean any(Modifier @NotNull ... modifiers)
    {
        if (modifiers.length == 0) return Modifier.activeMods > 0;
        int query = 0;
        for (Modifier modifier : modifiers) query |= modifier.value;
        return (Modifier.activeMods & query) > 0;
    }
    
    /**
     * Checks to see if all the provided modifiers are set.
     * <p>
     * If no modifiers are provided, then it will return {@code true} if and
     * only if no modifiers are currently active.
     *
     * @param modifiers The modifiers to query.
     * @return {@code true} if and only if the provided modifiers are active.
     */
    public static boolean all(Modifier @NotNull ... modifiers)
    {
        if (modifiers.length == 0) return Modifier.activeMods == 0;
        int query = 0;
        for (Modifier modifier : modifiers) query |= modifier.value;
        return (Modifier.activeMods & query) == query;
    }
    
    /**
     * Checks to see if and only if the provided modifiers are set.
     * <p>
     * If no modifiers are provided, then it will return {@code true} if and
     * only if no modifiers are currently active.
     *
     * @param modifiers The modifiers to query.
     * @return {@code true} if and only if the provided modifiers are active.
     */
    public static boolean only(Modifier @NotNull ... modifiers)
    {
        if (modifiers.length == 0) return Modifier.activeMods == 0;
        int query = 0;
        for (Modifier modifier : modifiers) query |= modifier.value;
        return Modifier.activeMods == query;
    }
}
