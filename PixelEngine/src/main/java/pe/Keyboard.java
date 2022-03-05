package pe;

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
    
    private static Keyboard INSTANCE;
    
    public static Keyboard get()
    {
        return Keyboard.INSTANCE;
    }
    
    static void setup()
    {
        Keyboard.LOGGER.fine("Setup");
        
        Keyboard.INSTANCE = new Keyboard();
        
        glfwSetKeyCallback(Window.get().handle, Keyboard::keyCallback);
        glfwSetCharCallback(Window.get().handle, Keyboard::charCallback);
    }
    
    // -------------------- Objects -------------------- //
    
    final Map<Key, Input> keyMap = new EnumMap<>(Key.class);
    
    // -------------------- Callback Objects -------------------- //
    
    final Queue<String> _charChanges = new ConcurrentLinkedQueue<>();
    
    final Queue<Pair<Key, Integer>> _keyStateChanges = new ConcurrentLinkedQueue<>();
    
    private Keyboard()
    {
        for (Key key : Key.values()) this.keyMap.put(key, new Input());
    }
    
    /**
     * Sets the sticky keys flag. If sticky mouse buttons are enabled, a mouse
     * button press will ensure that {@link EventKeyboardKeyPressed} is posted
     * even if the mouse button had been released before the call. This is
     * useful when you are only interested in whether mouse buttons have been
     * pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public void sticky(boolean sticky)
    {
        Keyboard.LOGGER.finest("Setting Sticky Flag:", sticky);
        
        Delegator.runTask(() -> glfwSetInputMode(Window.get().handle, GLFW_STICKY_KEYS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves the sticky keys flag.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean sticky()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.get().handle, GLFW_STICKY_KEYS) == GLFW_TRUE);
    }
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time The system time in nanoseconds.
     */
    @SuppressWarnings("ConstantConditions")
    void processEvents(long time)
    {
        String typed;
        while ((typed = this._charChanges.poll()) != null)
        {
            Engine.Events.post(EventKeyboardTyped.create(time, typed));
        }
        
        Pair<Key, Integer> keyStateChange;
        while ((keyStateChange = this._keyStateChanges.poll()) != null)
        {
            Input keyObj = this.keyMap.get(keyStateChange.getA());
            
            keyObj._state = keyStateChange.getB();
        }
        
        for (Key key : this.keyMap.keySet())
        {
            Input input = this.keyMap.get(key);
            
            input.state  = input._state;
            input._state = -1;
            switch (input.state)
            {
                case GLFW_PRESS -> {
                    input.held     = true;
                    input.holdTime = time + Input.holdFrequencyL();
                    Engine.Events.post(EventKeyboardKeyDown.create(time, key));
                }
                case GLFW_RELEASE -> {
                    input.held     = false;
                    input.holdTime = Long.MAX_VALUE;
                    Engine.Events.post(EventKeyboardKeyUp.create(time, key));
                    
                    if (time - input.pressTime < Input.doublePressedDelayL())
                    {
                        input.pressTime = 0;
                        Engine.Events.post(EventKeyboardKeyPressed.create(time, key, true));
                    }
                    else
                    {
                        input.pressTime = time;
                        Engine.Events.post(EventKeyboardKeyPressed.create(time, key, false));
                    }
                }
                case GLFW_REPEAT -> Engine.Events.post(EventKeyboardKeyRepeated.create(time, key));
            }
            if (input.held && time - input.holdTime >= Input.holdFrequencyL())
            {
                input.holdTime += Input.holdFrequencyL();
                Engine.Events.post(EventKeyboardKeyHeld.create(time, key));
            }
        }
    }
    
    public boolean down(Key key)
    {
        return this.keyMap.get(key).state == GLFW_PRESS;
    }
    
    public boolean up(Key key)
    {
        return this.keyMap.get(key).state == GLFW_RELEASE;
    }
    
    public boolean repeat(Key key)
    {
        return this.keyMap.get(key).state == GLFW_REPEAT;
    }
    
    public boolean held(Key key)
    {
        return this.keyMap.get(key).held;
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
    
    private static void keyCallback(long handle, int key, int scancode, int action, int mods)
    {
        Keyboard.INSTANCE._keyStateChanges.offer(new Pair<>(Keyboard.Key.get(key, scancode), action));
        
        Modifier.activeMods = mods;
    }
    
    private static void charCallback(long handle, int codePoint)
    {
        Keyboard.INSTANCE._charChanges.offer(Character.toString(codePoint));
    }
}
