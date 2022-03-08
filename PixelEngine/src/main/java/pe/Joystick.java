package pe;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.MemoryStack;
import pe.event.*;
import rutils.Logger;
import rutils.group.Pair;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public final class Joystick
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Joystick Callback Emulation -------------------- //
    
    private static final float[][] JOYSTICK_AXIS_STATES   = new float[Index.LOOKUP.size()][];
    private static final byte[][]  JOYSTICK_BUTTON_STATES = new byte[Index.LOOKUP.size()][];
    private static final byte[][]  JOYSTICK_HAT_STATES    = new byte[Index.LOOKUP.size()][];
    
    static void setupCallbackEmulation()
    {
        for (int jid = 0; jid < Index.LOOKUP.size(); jid++)
        {
            if (glfwJoystickIsGamepad(jid))
            {
                try (MemoryStack stack = MemoryStack.stackPush())
                {
                    GLFWGamepadState state = GLFWGamepadState.malloc(stack);
                    
                    if (glfwGetGamepadState(jid, state))
                    {
                        FloatBuffer axes = state.axes();
                        Joystick.JOYSTICK_AXIS_STATES[jid] = new float[axes.remaining()];
                        
                        ByteBuffer buttons = state.buttons();
                        Joystick.JOYSTICK_BUTTON_STATES[jid] = new byte[buttons.remaining()];
                    }
                    else
                    {
                        Joystick.JOYSTICK_AXIS_STATES[jid]   = new float[0];
                        Joystick.JOYSTICK_BUTTON_STATES[jid] = new byte[0];
                    }
                }
            }
            else
            {
                FloatBuffer axes = glfwGetJoystickAxes(jid);
                Joystick.JOYSTICK_AXIS_STATES[jid] = axes != null ? new float[axes.remaining()] : new float[0];
                
                ByteBuffer buttons = glfwGetJoystickButtons(jid);
                Joystick.JOYSTICK_BUTTON_STATES[jid] = buttons != null ? new byte[buttons.remaining()] : new byte[0];
            }
            
            ByteBuffer hats = glfwGetJoystickHats(jid);
            Joystick.JOYSTICK_HAT_STATES[jid] = hats != null ? new byte[hats.remaining()] : new byte[0];
        }
    }
    
    static void pollCallbackEmulation()
    {
        int   n, axis, button, hat;
        float newAxis;
        byte  newButton, newHat;
        
        for (int jid = 0; jid < Index.LOOKUP.size(); jid++)
        {
            if (glfwJoystickPresent(jid))
            {
                FloatBuffer axes    = null;
                ByteBuffer  buttons = null;
                ByteBuffer  hats    = glfwGetJoystickHats(jid);
                
                if (!glfwJoystickIsGamepad(jid))
                {
                    axes    = glfwGetJoystickAxes(jid);
                    buttons = glfwGetJoystickButtons(jid);
                }
                else
                {
                    try (MemoryStack stack = MemoryStack.stackPush())
                    {
                        GLFWGamepadState state = GLFWGamepadState.malloc(stack);
                        
                        if (glfwGetGamepadState(jid, state))
                        {
                            axes    = state.axes();
                            buttons = state.buttons();
                        }
                    }
                }
                
                if (axes != null)
                {
                    n = axes.remaining();
                    if (Joystick.JOYSTICK_AXIS_STATES[jid].length != n) Joystick.JOYSTICK_AXIS_STATES[jid] = new float[n];
                    for (axis = 0; axis < n; axis++)
                    {
                        if (Float.compare(Joystick.JOYSTICK_AXIS_STATES[jid][axis], newAxis = axes.get(axis)) != 0)
                        {
                            Joystick.JOYSTICK_AXIS_STATES[jid][axis] = newAxis;
                            if (!glfwJoystickPresent(jid)) break;
                            Joystick.axisCallback(jid, axis, Joystick.JOYSTICK_AXIS_STATES[jid][axis]);
                        }
                    }
                }
                if (buttons != null)
                {
                    n = buttons.remaining();
                    if (Joystick.JOYSTICK_BUTTON_STATES[jid].length != n) Joystick.JOYSTICK_BUTTON_STATES[jid] = new byte[n];
                    for (button = 0; button < n; button++)
                    {
                        if (Float.compare(Joystick.JOYSTICK_BUTTON_STATES[jid][button], newButton = buttons.get(button)) != 0)
                        {
                            Joystick.JOYSTICK_BUTTON_STATES[jid][button] = newButton;
                            if (!glfwJoystickPresent(jid)) break;
                            Joystick.buttonCallback(jid, button, Joystick.JOYSTICK_BUTTON_STATES[jid][button]);
                        }
                    }
                }
                if (hats != null)
                {
                    n = hats.remaining();
                    if (Joystick.JOYSTICK_HAT_STATES[jid].length != n) Joystick.JOYSTICK_HAT_STATES[jid] = new byte[n];
                    for (hat = 0; hat < n; hat++)
                    {
                        if (Float.compare(Joystick.JOYSTICK_HAT_STATES[jid][hat], newHat = hats.get(hat)) != 0)
                        {
                            Joystick.JOYSTICK_HAT_STATES[jid][hat] = newHat;
                            if (!glfwJoystickPresent(jid)) break;
                            Joystick.hatCallback(jid, hat, Joystick.JOYSTICK_HAT_STATES[jid][hat]);
                        }
                    }
                }
            }
        }
    }
    
    // -------------------- Static Objects -------------------- //
    
    private static final Map<Index, Joystick> INSTANCES = new EnumMap<>(Index.class);
    
    static void setup()
    {
        Joystick.LOGGER.fine("Setup");
        
        glfwSetJoystickCallback(Joystick::callback);
        
        for (int jid = GLFW_JOYSTICK_1; jid < GLFW_JOYSTICK_LAST; jid++)
        {
            Joystick.INSTANCES.put(Index.get(jid), glfwJoystickPresent(jid) ? new Joystick(jid) : null);
        }
        
        setupCallbackEmulation();
    }
    
    // -------------------- Instance Objects -------------------- //
    
    final int jid;
    
    final boolean gamepad;
    final String  guid;
    
    final String name;
    
    final Map<Axis, AxisInput> axisMap   = new EnumMap<>(Axis.class);
    final Map<Button, Input>   buttonMap = new EnumMap<>(Button.class);
    final Map<Hat, Input>      hatMap    = new EnumMap<>(Hat.class);
    
    // -------------------- Callback Objects -------------------- //
    final Queue<Pair<Axis, Float>>     axisStateChanges   = new ConcurrentLinkedQueue<>();
    final Queue<Pair<Button, Integer>> buttonStateChanges = new ConcurrentLinkedQueue<>();
    
    final Queue<Pair<Hat, Integer>> hatStateChanges = new ConcurrentLinkedQueue<>();
    
    private Joystick(int jid)
    {
        this.jid = jid;
        
        this.gamepad = glfwJoystickIsGamepad(this.jid);
        this.guid    = glfwGetJoystickGUID(this.jid);
        
        if (this.gamepad)
        {
            this.name = glfwGetGamepadName(this.jid);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GLFWGamepadState state = GLFWGamepadState.malloc(stack);
                
                if (glfwGetGamepadState(jid, state))
                {
                    FloatBuffer axes = state.axes();
                    for (int i = 0, n = axes.remaining(); i < n; i++) this.axisMap.put(Axis.get(i), new AxisInput(axes.get(i)));
                    
                    ByteBuffer buttons = state.buttons();
                    for (int i = 0, n = buttons.remaining(); i < n; i++) this.buttonMap.put(Button.get(i), new Input());
                }
                else
                {
                    throw new RuntimeException("Gamepad is not connected.");
                }
            }
        }
        else
        {
            this.name = glfwGetJoystickName(this.jid);
            
            FloatBuffer axes = Objects.requireNonNull(glfwGetJoystickAxes(this.jid), "Joystick is not connected.");
            for (int i = 0, n = axes.remaining(); i < n; i++) this.axisMap.put(Axis.get(i), new AxisInput(axes.get(i)));
            
            ByteBuffer buttons = Objects.requireNonNull(glfwGetJoystickButtons(this.jid), "Joystick is not connected.");
            for (int i = 0, n = buttons.remaining(); i < n; i++) this.buttonMap.put(Button.get(i), new Input());
        }
        
        ByteBuffer hats = Objects.requireNonNull(glfwGetJoystickHats(this.jid), "Joystick is not connected.");
        for (int i = 0, n = hats.remaining(); i < n; i++) this.hatMap.put(Hat.get(i), new Input());
        
        Joystick.LOGGER.finer("Created", this);
    }
    
    /**
     * Returns whether the specified joystick is both present and has a gamepad mapping.
     *
     * @return {@code true} if a joystick is both present and has a gamepad mapping or {@code false} otherwise
     */
    public static boolean isGamepad(Index index)
    {
        return Joystick.INSTANCES.get(index).gamepad;
    }
    
    /**
     * Returns the name, encoded as UTF-8, of the specified joystick.
     *
     * @return the UTF-8 encoded name of the joystick, or {@code NULL} if the joystick is not present
     */
    @Nullable
    public static String name(Index index)
    {
        return Joystick.INSTANCES.get(index).name;
    }
    
    /**
     * Returns the SDL compatible GUID, as a UTF-8 encoded hexadecimal string,
     * of the specified joystick.
     * <p>
     * The GUID is what connects a joystick to a gamepad mapping. A connected
     * joystick will always have a GUID even if there is no gamepad mapping
     * assigned to it.
     * <p>
     * The GUID uses the format introduced in SDL 2.0.5. This GUID tries to
     * uniquely identify the make and model of a joystick but does not identify
     * a specific unit, e.g. all wired Xbox 360 controllers will have the same
     * GUID on that platform. The GUID for a unit may vary between platforms
     * depending on what hardware information the platform specific APIs
     * provide.
     *
     * @return the UTF-8 encoded GUID of the joystick, or {@code NULL} if the joystick is not present or an error occurred
     */
    @Nullable
    public static String guid(Index index)
    {
        return Joystick.INSTANCES.get(index).guid;
    }
    
    // TODO - State Query Methods
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time The system time in nanoseconds.
     */
    @SuppressWarnings("ConstantConditions")
    static void processEvents(long time)
    {
        for (Index index : Joystick.INSTANCES.keySet())
        {
            Joystick joystick = Joystick.INSTANCES.get(index);
            
            if (joystick == null) continue;
            
            Pair<Axis, Float> axisStateChange;
            while ((axisStateChange = joystick.axisStateChanges.poll()) != null)
            {
                Axis axis = axisStateChange.getA();
                
                AxisInput axisObj = joystick.axisMap.get(axis);
                
                axisObj._value = axisStateChange.getB();
                if (Double.compare(axisObj.value, axisObj._value) != 0)
                {
                    double delta = axisObj._value - axisObj.value;
                    axisObj.value = axisObj._value;
                    Engine.Events.post(EventJoystickAxis.create(time, index, axis, axisObj.value, delta));
                }
            }
            
            Pair<Button, Integer> buttonStateChange;
            while ((buttonStateChange = joystick.buttonStateChanges.poll()) != null)
            {
                Input buttonObj = joystick.buttonMap.get(buttonStateChange.getA());
                
                buttonObj._state = buttonStateChange.getB();
            }
            
            for (Button button : joystick.buttonMap.keySet())
            {
                Input input = joystick.buttonMap.get(button);
                
                input.state  = input._state;
                input._state = -1;
                switch (input.state)
                {
                    case GLFW_PRESS -> {
                        boolean inc = time - input.downTime < Input.doublePressedDelayL();
    
                        input.held     = true;
                        input.heldTime = time + Input.holdFrequencyL();
                        input.downTime = time;
                        input.downCount = inc ? input.downCount + 1 : 1;
                        Engine.Events.post(EventJoystickButtonDown.create(time, index, button, input.downCount));
                    }
                    case GLFW_RELEASE -> {
                        input.held     = false;
                        input.heldTime = Long.MAX_VALUE;
                        Engine.Events.post(EventJoystickButtonUp.create(time, index, button));
                    }
                    case GLFW_REPEAT -> Engine.Events.post(EventJoystickButtonRepeated.create(time, index, button));
                }
                if (input.held && time - input.heldTime >= Input.holdFrequencyL())
                {
                    input.heldTime += Input.holdFrequencyL();
                    Engine.Events.post(EventJoystickButtonHeld.create(time, index, button));
                }
            }
            
            Pair<Hat, Integer> hatStateChange;
            while ((hatStateChange = joystick.hatStateChanges.poll()) != null)
            {
                Hat hat = hatStateChange.getA();
                
                Input hatObj = joystick.hatMap.get(hat);
                
                hatObj._state = hatStateChange.getB();
                if (hatObj.state != hatObj._state)
                {
                    hatObj.state = hatObj._state;
                    Engine.Events.post(EventJoystickHat.create(time, index, hat, HatDirection.get(hatObj.state)));
                }
            }
        }
    }
    
    protected static final class AxisInput
    {
        double _value, value;
        
        AxisInput(double initial)
        {
            this.value = this._value = initial;
        }
    }
    
    public enum Index
    {
        ONE(GLFW_JOYSTICK_1),
        TWO(GLFW_JOYSTICK_2),
        THREE(GLFW_JOYSTICK_3),
        FOUR(GLFW_JOYSTICK_4),
        FIVE(GLFW_JOYSTICK_5),
        SIX(GLFW_JOYSTICK_6),
        SEVEN(GLFW_JOYSTICK_7),
        EIGHT(GLFW_JOYSTICK_8),
        NINE(GLFW_JOYSTICK_9),
        TEN(GLFW_JOYSTICK_10),
        ELEVEN(GLFW_JOYSTICK_11),
        TWELVE(GLFW_JOYSTICK_12),
        THIRTEEN(GLFW_JOYSTICK_13),
        FOURTEEN(GLFW_JOYSTICK_14),
        FIFTEEN(GLFW_JOYSTICK_15),
        SIXTEEN(GLFW_JOYSTICK_16),
        ;
        
        private static final Map<Integer, Index> LOOKUP = new HashMap<>();
        
        private final int index;
        
        Index(int index)
        {
            this.index = index;
        }
        
        public static Index get(int value)
        {
            return Index.LOOKUP.getOrDefault(value, Index.ONE);
        }
        
        static
        {
            for (Index axis : values())
            {
                Index.LOOKUP.put(axis.index, axis);
            }
        }
    }
    
    public enum Axis
    {
        NONE(-1),
        
        LEFT_X(GLFW_GAMEPAD_AXIS_LEFT_X),
        LEFT_Y(GLFW_GAMEPAD_AXIS_LEFT_Y),
        
        RIGHT_X(GLFW_GAMEPAD_AXIS_RIGHT_X),
        RIGHT_Y(GLFW_GAMEPAD_AXIS_RIGHT_Y),
        
        LEFT_TRIGGER(GLFW_GAMEPAD_AXIS_LEFT_TRIGGER),
        RIGHT_TRIGGER(GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER),
        ;
        
        private static final Map<Integer, Axis> LOOKUP = new HashMap<>();
        
        private final int id;
        
        Axis(int id)
        {
            this.id = id;
        }
        
        public static Axis get(int value)
        {
            return Axis.LOOKUP.getOrDefault(value, Axis.NONE);
        }
        
        static
        {
            for (Axis axis : values())
            {
                Axis.LOOKUP.put(axis.id, axis);
            }
        }
    }
    
    public enum Button
    {
        NONE(-1),
        
        A(GLFW_GAMEPAD_BUTTON_A),
        B(GLFW_GAMEPAD_BUTTON_B),
        X(GLFW_GAMEPAD_BUTTON_X),
        Y(GLFW_GAMEPAD_BUTTON_Y),
        
        LEFT_BUMPER(GLFW_GAMEPAD_BUTTON_LEFT_BUMPER),
        RIGHT_BUMPER(GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER),
        
        BACK(GLFW_GAMEPAD_BUTTON_BACK),
        START(GLFW_GAMEPAD_BUTTON_START),
        GUIDE(GLFW_GAMEPAD_BUTTON_GUIDE),
        
        LEFT_THUMB(GLFW_GAMEPAD_BUTTON_LEFT_THUMB),
        RIGHT_THUMB(GLFW_GAMEPAD_BUTTON_RIGHT_THUMB),
        
        DPAD_UP(GLFW_GAMEPAD_BUTTON_DPAD_UP),
        DPAD_RIGHT(GLFW_GAMEPAD_BUTTON_DPAD_RIGHT),
        DPAD_DOWN(GLFW_GAMEPAD_BUTTON_DPAD_DOWN),
        DPAD_LEFT(GLFW_GAMEPAD_BUTTON_DPAD_LEFT),
        
        CROSS(GLFW_GAMEPAD_BUTTON_CROSS),
        CIRCLE(GLFW_GAMEPAD_BUTTON_CIRCLE),
        SQUARE(GLFW_GAMEPAD_BUTTON_SQUARE),
        TRIANGLE(GLFW_GAMEPAD_BUTTON_TRIANGLE),
        ;
        
        private static final Map<Integer, Button> LOOKUP = new HashMap<>();
        
        private final int id;
        
        Button(int id)
        {
            this.id = id;
        }
        
        public static Button get(int value)
        {
            return Button.LOOKUP.getOrDefault(value, Button.NONE);
        }
        
        static
        {
            for (Button button : values())
            {
                Button.LOOKUP.put(button.id, button);
            }
        }
    }
    
    public enum Hat
    {
        NONE(-1),
        
        LEFT(0),
        RIGHT(1),
        ;
        
        private static final Map<Integer, Hat> LOOKUP = new HashMap<>();
        
        private final int id;
        
        Hat(int id)
        {
            this.id = id;
        }
        
        public static Hat get(int value)
        {
            return Hat.LOOKUP.getOrDefault(value, Hat.NONE);
        }
        
        static
        {
            for (Hat hat : values())
            {
                Hat.LOOKUP.put(hat.id, hat);
            }
        }
        
    }
    
    public enum HatDirection
    {
        CENTERED(GLFW_HAT_CENTERED),
        
        UP(GLFW_HAT_UP),
        RIGHT(GLFW_HAT_RIGHT),
        DOWN(GLFW_HAT_DOWN),
        LEFT(GLFW_HAT_LEFT),
        
        RIGHT_UP(GLFW_HAT_RIGHT_UP),
        RIGHT_DOWN(GLFW_HAT_RIGHT_DOWN),
        LEFT_UP(GLFW_HAT_LEFT_UP),
        LEFT_DOWN(GLFW_HAT_LEFT_DOWN),
        
        UP_DOWN(GLFW_HAT_UP | GLFW_HAT_DOWN),
        RIGHT_LEFT(GLFW_HAT_RIGHT | GLFW_HAT_LEFT),
        
        RIGHT_UP_LEFT(GLFW_HAT_RIGHT | GLFW_HAT_UP | GLFW_HAT_LEFT),
        RIGHT_DOWN_LEFT(GLFW_HAT_RIGHT | GLFW_HAT_DOWN | GLFW_HAT_LEFT),
        UP_RIGHT_DOWN(GLFW_HAT_UP | GLFW_HAT_RIGHT | GLFW_HAT_DOWN),
        UP_LEFT_DOWN(GLFW_HAT_UP | GLFW_HAT_LEFT | GLFW_HAT_DOWN),
        
        ALL(GLFW_HAT_UP | GLFW_HAT_RIGHT | GLFW_HAT_DOWN | GLFW_HAT_LEFT),
        
        ;
        
        private final int ref;
        
        HatDirection(int ref)
        {
            this.ref = ref;
        }
        
        public static HatDirection get(int value)
        {
            for (HatDirection hat : HatDirection.values()) if ((value & hat.ref) == value) return hat;
            return HatDirection.CENTERED;
        }
    }
    
    private static void callback(int jid, int event)
    {
        switch (event)
        {
            case GLFW_CONNECTED -> Joystick.INSTANCES.put(Index.get(jid), new Joystick(jid));
            case GLFW_DISCONNECTED -> Joystick.INSTANCES.put(Index.get(jid), null);
        }
    }
    
    private static void axisCallback(int jid, int axis, float value)
    {
        Joystick joystick = Joystick.INSTANCES.get(Index.get(jid));
        
        joystick.axisStateChanges.offer(new Pair<>(Axis.get(axis), value));
    }
    
    private static void buttonCallback(int jid, int button, int action)
    {
        Joystick joystick = Joystick.INSTANCES.get(Index.get(jid));
        
        joystick.buttonStateChanges.offer(new Pair<>(Button.get(button), action));
    }
    
    private static void hatCallback(int jid, int hat, int action)
    {
        Joystick joystick = Joystick.INSTANCES.get(Index.get(jid));
        
        joystick.hatStateChanges.offer(new Pair<>(Hat.get(hat), action));
    }
}
