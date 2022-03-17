package pe;

import org.jetbrains.annotations.NotNull;
import pe.event.*;
import rutils.Logger;
import rutils.group.Pair;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;
import static pe.Engine.Delegator;

public final class Keyboard
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Static Objects -------------------- //
    
    static void setup()
    {
        Keyboard.LOGGER.fine("Setup");
        
        for (Key key : Key.values()) Keyboard.keyState.put(key, new Input());
    }
    
    static void destroy()
    {
        Keyboard.LOGGER.fine("Destroy");
    }
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time The system time in nanoseconds.
     */
    @SuppressWarnings("ConstantConditions")
    static void processEvents(long time)
    {
        Keyboard.charBuffer.setLength(0);
        
        Integer codePoint;
        while ((codePoint = Keyboard.charChanges.poll()) != null)
        {
            Keyboard.charBuffer.appendCodePoint(codePoint);
            Engine.Events.post(EventKeyboardTyped.create(time, Keyboard.window, codePoint));
        }
        
        Pair<Key, Integer> keyStateChange;
        while ((keyStateChange = Keyboard.keyStateChanges.poll()) != null)
        {
            Input keyObj = Keyboard.keyState.get(keyStateChange.getA());
            
            keyObj._state = keyStateChange.getB();
        }
        
        for (Key key : Keyboard.keyState.keySet())
        {
            Input input = Keyboard.keyState.get(key);
            
            input.state  = input._state;
            input._state = -1;
            switch (input.state)
            {
                case GLFW_PRESS -> {
                    boolean inc = time - input.downTime < Input.doublePressedDelayL();
                    
                    input.held      = true;
                    input.heldTime  = time + Input.holdFrequencyL();
                    input.downTime  = time;
                    input.downCount = inc ? input.downCount + 1 : 1;
                    Engine.Events.post(EventKeyboardKeyDown.create(time, Keyboard.window, key, input.downCount));
                }
                case GLFW_RELEASE -> {
                    input.held     = false;
                    input.heldTime = Long.MAX_VALUE;
                    Engine.Events.post(EventKeyboardKeyUp.create(time, Keyboard.window, key));
                }
                case GLFW_REPEAT -> Engine.Events.post(EventKeyboardKeyRepeated.create(time, Keyboard.window, key));
            }
            if (input.held && time - input.heldTime >= Input.holdFrequencyL())
            {
                input.heldTime += Input.holdFrequencyL();
                Engine.Events.post(EventKeyboardKeyHeld.create(time, Keyboard.window, key));
            }
        }
    }
    
    // -------------------- Instance -------------------- //
    
    static Window window;
    
    static final StringBuffer   charBuffer  = new StringBuffer();
    static final Queue<Integer> charChanges = new ConcurrentLinkedQueue<>();
    
    static final Map<Key, Input>           keyState        = new EnumMap<>(Key.class);
    static final Queue<Pair<Key, Integer>> keyStateChanges = new ConcurrentLinkedQueue<>();
    
    private Keyboard() {}
    
    // -------------------- Properties -------------------- //
    
    /**
     * Sets the sticky keys flag in the specified window. If sticky key are
     * enabled, a keyboard key press will ensure that {@link EventKeyboardKeyUp}
     * is posted with a {@link EventKeyboardKeyDown} even if the key had been
     * released before the call. This is useful when you are only interested in
     * whether keys have been pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public static void sticky(@NotNull Window window, boolean sticky)
    {
        Keyboard.LOGGER.finest("Setting Sticky Flag for %s: %s", window, sticky);
        
        Delegator.runTask(() -> glfwSetInputMode(window.handle, GLFW_STICKY_KEYS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * Sets the sticky keys flag in the main window. If sticky key are enabled,
     * a keyboard key press will ensure that {@link EventKeyboardKeyUp} is
     * posted with a {@link EventKeyboardKeyDown} even if the key had been
     * released before the call. This is useful when you are only interested in
     * whether keys have been pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public static void sticky(boolean sticky)
    {
        sticky(Window.primary, sticky);
    }
    
    /**
     * @return Retrieves the sticky keys flag.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean sticky(@NotNull Window window)
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(window.handle, GLFW_STICKY_KEYS) == GLFW_TRUE);
    }
    
    /**
     * @return Retrieves the sticky keys flag.
     */
    public static boolean sticky()
    {
        return sticky(Window.primary);
    }
    
    // -------------------- State Properties -------------------- //
    
    /**
     * @return The string of chars that were typed in the specified window.
     */
    @NotNull
    public static String charsTypes(@NotNull Window window)
    {
        return Keyboard.window == window ? Keyboard.charBuffer.toString() : "";
    }
    
    /**
     * @return The string of chars that were typed in the main window.
     */
    @NotNull
    public static String charsTypes()
    {
        return charsTypes(Window.primary);
    }
    
    /**
     * @return If the provided key is in the down state in the provided window.
     */
    public static boolean down(@NotNull Window window, Key key)
    {
        return Keyboard.window == window && Keyboard.keyState.get(key).state == GLFW_PRESS;
    }
    
    /**
     * @return If the provided key is in the down state in the main window.
     */
    public static boolean down(Key key)
    {
        return down(Window.primary, key);
    }
    
    /**
     * @return If the provided key is in the down state in any window.
     */
    public static boolean downAny(Key key)
    {
        return down(Keyboard.window, key);
    }
    
    /**
     * @return If the provided key is in the up state in the provided window.
     */
    public static boolean up(@NotNull Window window, Key key)
    {
        return Keyboard.window == window && Keyboard.keyState.get(key).state == GLFW_RELEASE;
    }
    
    /**
     * @return If the provided key is in the up state in the main window.
     */
    public static boolean up(Key key)
    {
        return up(Window.primary, key);
    }
    
    /**
     * @return If the provided key is in the up state in any window.
     */
    public static boolean upAny(Key key)
    {
        return up(Keyboard.window, key);
    }
    
    /**
     * @return If the provided key is in the repeat state in the provided window.
     */
    public static boolean repeat(@NotNull Window window, Key key)
    {
        return Keyboard.window == window && Keyboard.keyState.get(key).state == GLFW_REPEAT;
    }
    
    /**
     * @return If the provided key is in the repeat state in the main window.
     */
    public static boolean repeat(Key key)
    {
        return repeat(Window.primary, key);
    }
    
    /**
     * @return If the provided key is in the repeat state in any window.
     */
    public static boolean repeatAny(Key key)
    {
        return repeat(Keyboard.window, key);
    }
    
    /**
     * @return If the provided key is in the held state in the provided window.
     */
    public static boolean held(@NotNull Window window, Key key)
    {
        return Keyboard.window == window && Keyboard.keyState.get(key).held;
    }
    
    /**
     * @return If the provided key is in the held state in the main window.
     */
    public static boolean held(Key key)
    {
        return held(Window.primary, key);
    }
    
    /**
     * @return If the provided key is in the held state in any window.
     */
    public static boolean heldAny(Key key)
    {
        return held(Keyboard.window, key);
    }
    
    public enum Key
    {
        UNKNOWN(GLFW_KEY_UNKNOWN),
        
        A(GLFW_KEY_A),
        B(GLFW_KEY_B),
        C(GLFW_KEY_C),
        D(GLFW_KEY_D),
        E(GLFW_KEY_E),
        F(GLFW_KEY_F),
        G(GLFW_KEY_G),
        H(GLFW_KEY_H),
        I(GLFW_KEY_I),
        J(GLFW_KEY_J),
        K(GLFW_KEY_K),
        L(GLFW_KEY_L),
        M(GLFW_KEY_M),
        N(GLFW_KEY_N),
        O(GLFW_KEY_O),
        P(GLFW_KEY_P),
        Q(GLFW_KEY_Q),
        R(GLFW_KEY_R),
        S(GLFW_KEY_S),
        T(GLFW_KEY_T),
        U(GLFW_KEY_U),
        V(GLFW_KEY_V),
        W(GLFW_KEY_W),
        X(GLFW_KEY_X),
        Y(GLFW_KEY_Y),
        Z(GLFW_KEY_Z),
        
        K1(GLFW_KEY_1),
        K2(GLFW_KEY_2),
        K3(GLFW_KEY_3),
        K4(GLFW_KEY_4),
        K5(GLFW_KEY_5),
        K6(GLFW_KEY_6),
        K7(GLFW_KEY_7),
        K8(GLFW_KEY_8),
        K9(GLFW_KEY_9),
        K0(GLFW_KEY_0),
        
        GRAVE(GLFW_KEY_GRAVE_ACCENT),
        MINUS(GLFW_KEY_MINUS),
        EQUAL(GLFW_KEY_EQUAL),
        L_BRACKET(GLFW_KEY_LEFT_BRACKET),
        R_BRACKET(GLFW_KEY_RIGHT_BRACKET),
        BACKSLASH(GLFW_KEY_BACKSLASH),
        SEMICOLON(GLFW_KEY_SEMICOLON),
        APOSTROPHE(GLFW_KEY_APOSTROPHE),
        COMMA(GLFW_KEY_COMMA),
        PERIOD(GLFW_KEY_PERIOD),
        SLASH(GLFW_KEY_SLASH),
        
        F1(GLFW_KEY_F1),
        F2(GLFW_KEY_F2),
        F3(GLFW_KEY_F3),
        F4(GLFW_KEY_F4),
        F5(GLFW_KEY_F5),
        F6(GLFW_KEY_F6),
        F7(GLFW_KEY_F7),
        F8(GLFW_KEY_F8),
        F9(GLFW_KEY_F9),
        F10(GLFW_KEY_F10),
        F11(GLFW_KEY_F11),
        F12(GLFW_KEY_F12),
        F13(GLFW_KEY_F13),
        F14(GLFW_KEY_F14),
        F15(GLFW_KEY_F15),
        F16(GLFW_KEY_F16),
        F17(GLFW_KEY_F17),
        F18(GLFW_KEY_F18),
        F19(GLFW_KEY_F19),
        F20(GLFW_KEY_F20),
        F21(GLFW_KEY_F21),
        F22(GLFW_KEY_F22),
        F23(GLFW_KEY_F23),
        F24(GLFW_KEY_F24),
        F25(GLFW_KEY_F25),
        
        UP(GLFW_KEY_UP),
        DOWN(GLFW_KEY_DOWN),
        LEFT(GLFW_KEY_LEFT),
        RIGHT(GLFW_KEY_RIGHT),
        
        TAB(GLFW_KEY_TAB),
        CAPS_LOCK(GLFW_KEY_CAPS_LOCK),
        ENTER(GLFW_KEY_ENTER),
        BACKSPACE(GLFW_KEY_BACKSPACE),
        SPACE(GLFW_KEY_SPACE),
        
        L_SHIFT(GLFW_KEY_LEFT_SHIFT),
        R_SHIFT(GLFW_KEY_RIGHT_SHIFT),
        L_CONTROL(GLFW_KEY_LEFT_CONTROL),
        R_CONTROL(GLFW_KEY_RIGHT_CONTROL),
        L_ALT(GLFW_KEY_LEFT_ALT),
        R_ALT(GLFW_KEY_RIGHT_ALT),
        L_SUPER(GLFW_KEY_LEFT_SUPER),
        R_SUPER(GLFW_KEY_RIGHT_SUPER),
        
        MENU(GLFW_KEY_MENU),
        ESCAPE(GLFW_KEY_ESCAPE),
        PRINT_SCREEN(GLFW_KEY_PRINT_SCREEN),
        SCROLL_LOCK(GLFW_KEY_SCROLL_LOCK),
        PAUSE(GLFW_KEY_PAUSE),
        INSERT(GLFW_KEY_INSERT),
        DELETE(GLFW_KEY_DELETE),
        HOME(GLFW_KEY_HOME),
        END(GLFW_KEY_END),
        PAGE_UP(GLFW_KEY_PAGE_UP),
        PAGE_DOWN(GLFW_KEY_PAGE_DOWN),
        
        KP_0(GLFW_KEY_KP_0),
        KP_1(GLFW_KEY_KP_1),
        KP_2(GLFW_KEY_KP_2),
        KP_3(GLFW_KEY_KP_3),
        KP_4(GLFW_KEY_KP_4),
        KP_5(GLFW_KEY_KP_5),
        KP_6(GLFW_KEY_KP_6),
        KP_7(GLFW_KEY_KP_7),
        KP_8(GLFW_KEY_KP_8),
        KP_9(GLFW_KEY_KP_9),
        
        NUM_LOCK(GLFW_KEY_NUM_LOCK),
        KP_DIVIDE(GLFW_KEY_KP_DIVIDE),
        KP_MULTIPLY(GLFW_KEY_KP_MULTIPLY),
        KP_SUBTRACT(GLFW_KEY_KP_SUBTRACT),
        KP_ADD(GLFW_KEY_KP_ADD),
        KP_DECIMAL(GLFW_KEY_KP_DECIMAL),
        KP_EQUAL(GLFW_KEY_KP_EQUAL),
        KP_ENTER(GLFW_KEY_KP_ENTER),
        
        WORLD_1(GLFW_KEY_WORLD_1),
        WORLD_2(GLFW_KEY_WORLD_2),
        
        ;
        
        private static final HashMap<Integer, Key> KEY_MAP      = new HashMap<>();
        private static final HashMap<Integer, Key> SCANCODE_MAP = new HashMap<>();
        private static final HashMap<String, Key>  NAME_MAP     = new HashMap<>();
        
        final int ref, scancode;
        
        Key(int ref)
        {
            this.ref      = ref;
            this.scancode = ref >= 0 ? glfwGetKeyScancode(ref) : -1;
        }
        
        /**
         * @return Gets the ButtonInput that corresponds to the GLFW constant.
         */
        public static Key get(String key)
        {
            Key k;
            if ((k = Key.NAME_MAP.get(key)) != null) return k;
            return Key.UNKNOWN;
        }
        
        /**
         * @return Gets the ButtonInput that corresponds to the GLFW constant.
         */
        public static Key get(int key, int scancode)
        {
            Key k;
            if ((k = Key.KEY_MAP.get(key)) != null) return k;
            if ((k = Key.SCANCODE_MAP.get(scancode)) != null) return k;
            return Key.UNKNOWN;
        }
        
        static
        {
            for (Key key : values())
            {
                Key.KEY_MAP.put(key.ref, key);
                if (key.scancode >= 0) Key.SCANCODE_MAP.put(key.scancode, key);
                Key.NAME_MAP.put(key.name(), key);
            }
        }
    }
}
