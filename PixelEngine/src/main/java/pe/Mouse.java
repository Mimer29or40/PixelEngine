package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import pe.event.*;
import rutils.Logger;
import rutils.group.Pair;

import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.lwjgl.glfw.GLFW.*;

public final class Mouse
{
    private static final Logger LOGGER = new Logger();
    
    static void setup()
    {
        Mouse.LOGGER.fine("Setup");
        
        for (Button button : Button.values()) Mouse.buttonState.put(button, new ButtonInput());
    }
    
    static void destroy()
    {
        Mouse.LOGGER.fine("Destroy");
    }
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time The system time in nanoseconds.
     */
    @SuppressWarnings("ConstantConditions")
    static void events(long time)
    {
        boolean entered = false;
        if (Mouse.entered != Mouse.enteredChanges)
        {
            Mouse.entered = Mouse.enteredChanges;
            if (Mouse.entered)
            {
                entered = true;
                
                Mouse.pos.set(Mouse.posChanges);
            }
            Engine.Events.post(EventMouseEntered.create(time, Mouse.window, Mouse.entered));
        }
        
        Mouse.rel.set(0);
        if (Double.compare(Mouse.pos.x, Mouse.posChanges.x) != 0 || Double.compare(Mouse.pos.y, Mouse.posChanges.y) != 0 || entered)
        {
            Mouse.posChanges.sub(Mouse.pos, Mouse.rel);
            Mouse.pos.set(Mouse.posChanges);
            
            Engine.Events.post(EventMouseMoved.create(time, Mouse.window, Mouse.pos, Mouse.rel));
        }
        
        Mouse.scroll.set(0);
        if (Double.compare(Mouse.scroll.x, Mouse.scrollChanges.x) != 0 || Double.compare(Mouse.scroll.y, Mouse.scrollChanges.y) != 0)
        {
            Mouse.scroll.set(Mouse.scrollChanges);
            Mouse.scrollChanges.set(0);
            Engine.Events.post(EventMouseScrolled.create(time, Mouse.window, Mouse.scroll));
        }
        
        Pair<Mouse.Button, Integer> buttonStateChange;
        while ((buttonStateChange = Mouse.buttonStateChanges.poll()) != null)
        {
            Mouse.ButtonInput buttonObj = Mouse.buttonState.get(buttonStateChange.getA());
            
            buttonObj._state = buttonStateChange.getB();
        }
        
        for (Mouse.Button button : Mouse.buttonState.keySet())
        {
            Mouse.ButtonInput input = Mouse.buttonState.get(button);
            
            input.state  = input._state;
            input._state = -1;
            switch (input.state)
            {
                case GLFW_PRESS ->
                {
                    int tolerance = 2;
                    
                    boolean inc = Math.abs(Mouse.pos.x - input.downPos.x) < tolerance &&
                                  Math.abs(Mouse.pos.y - input.downPos.y) < tolerance &&
                                  time - input.downTime < Input.doublePressedDelayL();
                    
                    input.held      = true;
                    input.heldTime  = time + Input.holdFrequencyL();
                    input.downTime  = time;
                    input.downCount = inc ? input.downCount + 1 : 1;
                    input.downPos.set(Mouse.pos);
                    Engine.Events.post(EventMouseButtonDown.create(time, Mouse.window, button, Mouse.pos, input.downCount));
                }
                case GLFW_RELEASE ->
                {
                    input.held     = false;
                    input.heldTime = Long.MAX_VALUE;
                    Engine.Events.post(EventMouseButtonUp.create(time, Mouse.window, button, Mouse.pos));
                }
                case GLFW_REPEAT -> Engine.Events.post(EventMouseButtonRepeated.create(time, Mouse.window, button, Mouse.pos));
            }
            if (input.held)
            {
                if (time - input.heldTime >= Input.holdFrequencyL())
                {
                    input.heldTime += Input.holdFrequencyL();
                    Engine.Events.post(EventMouseButtonHeld.create(time, Mouse.window, button, Mouse.pos));
                }
                if (Mouse.rel.x != 0 || Mouse.rel.y != 0) Engine.Events.post(EventMouseButtonDragged.create(time, Mouse.window, button, Mouse.pos, Mouse.rel, input.downPos));
            }
        }
    }
    
    // -------------------- Instance -------------------- //
    
    static final Map<Long, @Nullable Shape> shapes = new HashMap<>();
    
    // -------------------- State Objects -------------------- //
    
    static Window window;
    
    static boolean entered        = false;
    static boolean enteredChanges = false;
    
    static final Vector2d pos        = new Vector2d();
    static final Vector2d posChanges = new Vector2d();
    
    static final Vector2d rel = new Vector2d();
    
    static final Vector2d scroll        = new Vector2d();
    static final Vector2d scrollChanges = new Vector2d();
    
    static final Map<Button, ButtonInput>     buttonState        = new EnumMap<>(Button.class);
    static final Queue<Pair<Button, Integer>> buttonStateChanges = new ConcurrentLinkedQueue<>();
    
    private Mouse() {}
    
    // -------------------- Properties -------------------- //
    
    /**
     * @return Retrieves if the mouse is visible and behaving normally in the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isShown()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL);
    }
    
    /**
     * Makes the cursor visible and behaving normally in the specified window.
     */
    public static void show()
    {
        Mouse.LOGGER.finest("Showing in", Window.primary);
        
        Engine.Delegator.runTask(() -> {
            if (glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED)
            {
                Mouse.pos.set(Mouse.posChanges.set(Window.bounds().size()).mul(0.5));
            }
            glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        });
    }
    
    /**
     * @return Retrieves if the mouse is hidden in the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isHidden()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_HIDDEN);
    }
    
    /**
     * Makes the cursor invisible when it is over the content area of the
     * specified window but does not restrict the cursor from leaving.
     */
    public static void hide()
    {
        Mouse.LOGGER.finest("Hiding in", Window.primary);
        
        Engine.Delegator.runTask(() -> glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN));
    }
    
    /**
     * @return Retrieves if the mouse is captured in the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isCaptured()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED);
    }
    
    /**
     * Hides and grabs the cursor in the specified window, providing virtual
     * and unlimited cursor movement. This is useful for implementing for
     * example 3D camera controls.
     */
    public static void capture()
    {
        Mouse.LOGGER.finest("Capturing in", Window.primary);
        
        Engine.Delegator.runTask(() -> {
            Mouse.pos.set(Mouse.posChanges.set(Window.bounds().size()).mul(0.5));
            glfwSetCursorPos(Window.handle, Mouse.posChanges.x, Mouse.posChanges.y);
            glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        });
    }
    
    /**
     * @return Retrieves the raw mouse motion flag for the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isRawInput()
    {
        return Engine.Delegator.waitReturnTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return false;
            }
            return glfwGetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
        });
    }
    
    /**
     * Sets the raw mouse motion flag for the specified window. Set
     * {@code true} to enable raw (unscaled and un-accelerated) mouse motion
     * when the cursor is disabled, or {@code false} to disable it. If raw
     * motion is not supported, attempting to set Mouse will log a warning.
     *
     * @param rawInput {@code true} to enable raw mouse motion mode, otherwise {@code false}.
     */
    public static void rawInput(boolean rawInput)
    {
        Mouse.LOGGER.finest("Setting Raw Input Flag for %s: %s", Window.primary, rawInput);
        
        Engine.Delegator.runTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return;
            }
            glfwSetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION, rawInput ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    /**
     * @return Retrieves the sticky mouse buttons flag for the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isSticky()
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_STICKY_MOUSE_BUTTONS) == GLFW_TRUE);
    }
    
    /**
     * Sets the sticky mouse buttons flag for the specified window. If sticky
     * mouse buttons are enabled, a mouse button press will ensure that a
     * {@link EventMouseButtonUp} is posted with a {@link EventMouseButtonDown}
     * even if the mouse button had been released before the call. This is
     * useful when you are only interested in whether mouse buttons have been
     * pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public static void sticky(boolean sticky)
    {
        Mouse.LOGGER.finest("Setting Sticky Flag for %s: %s", window, sticky);
        
        Engine.Delegator.runTask(() -> glfwSetInputMode(Window.handle, GLFW_STICKY_MOUSE_BUTTONS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return The current shape of the mouse or {@code null} if set to the default arrow cursor for the specified window.
     */
    @Nullable
    public static Shape shape()
    {
        return Mouse.shapes.getOrDefault(Window.handle, null);
    }
    
    /**
     * Sets the cursor image to be used when the cursor is over the content
     * area of the window. The set cursor will only be visible when the cursor
     * mode of the window {@link #isShown()}.
     * <p>
     * On some platforms, the set cursor may not be visible unless the
     * window also has input focus.
     *
     * @param shape the cursor to set, or {@code null} to switch back to the default arrow cursor
     */
    public static void shape(@Nullable Shape shape)
    {
        Mouse.LOGGER.finest("Setting Shape for %s: %s", Window.primary, shape);
        
        Engine.Delegator.runTask(() -> {
            Mouse.shapes.put(Window.handle, shape);
            glfwSetCursor(Window.handle, shape != null ? shape.handle : MemoryUtil.NULL);
        });
    }
    
    // -------------------- State Properties -------------------- //
    
    /**
     * @return If the mouse is in the window specified.
     */
    public static boolean entered()
    {
        return Mouse.entered;
    }
    
    /**
     * @return The position of the cursor, in viewport coordinates, relative to the upper-left corner of the viewport of the last window it was reported in.
     */
    @NotNull
    public static Vector2dc pos()
    {
        return Mouse.pos;
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the specified window.
     *
     * @param x The x position of the cursor
     * @param y The y position of the cursor
     */
    public static void pos(double x, double y)
    {
        Mouse.LOGGER.finest("Setting Position in %s: (%s, %s)", Window.primary, x, y);
        
        Engine.Delegator.waitRunTask(() -> glfwSetCursorPos(Window.handle, x, y));
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the specified window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Vector2ic pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the specified window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Vector2dc pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * @return The x position of the cursor, in viewport coordinates, relative to the upper-left corner of the viewport of the last window it was reported in.
     */
    public static double x()
    {
        return Mouse.pos.x;
    }
    
    /**
     * @return The y position of the cursor, in viewport coordinates, relative to the upper-left corner of the viewport of the last window it was reported in.
     */
    public static double y()
    {
        return Mouse.pos.y;
    }
    
    /**
     * @return The difference in position of the cursor, in viewport coordinates, since the last frame of the last window it was reported in.
     */
    @NotNull
    public static Vector2dc rel()
    {
        return Mouse.rel;
    }
    
    /**
     * @return The difference in x position of the cursor, in viewport coordinates, since the last frame of the last window it was reported in.
     */
    public static double dx()
    {
        return Mouse.rel.x;
    }
    
    /**
     * @return The difference in y position of the cursor, in viewport coordinates, since the last frame of the last window it was reported in.
     */
    public static double dy()
    {
        return Mouse.rel.y;
    }
    
    /**
     * @return The amount that the mouse wheel, or touch-pad, was scrolled since the last frame of the last window it was reported in.
     */
    @NotNull
    public static Vector2dc scroll()
    {
        return Mouse.scroll;
    }
    
    /**
     * @return The amount that the mouse wheel, or touch-pad, was scrolled horizontally since the last frame of the last window it was reported in.
     */
    public static double scrollX()
    {
        return Mouse.scroll.x;
    }
    
    /**
     * @return The amount that the mouse wheel, or touch-pad, was scrolled vertically since the last frame of the last window it was reported in.
     */
    public static double scrollY()
    {
        return Mouse.scroll.y;
    }
    
    /**
     * @return If the provided button is in the down state in the provided window.
     */
    public static boolean down(@NotNull Button button)
    {
        return Mouse.buttonState.get(button).state == GLFW_PRESS;
    }
    
    /**
     * @return If the provided button is in the up state in the provided window.
     */
    public static boolean up(@NotNull Button button)
    {
        return Mouse.buttonState.get(button).state == GLFW_RELEASE;
    }
    
    /**
     * @return If the provided button is in the repeat state in the provided window.
     */
    public static boolean repeat(@NotNull Button button)
    {
        return Mouse.buttonState.get(button).state == GLFW_REPEAT;
    }
    
    /**
     * @return If the provided button is in the held state in the provided window.
     */
    public static boolean held(@NotNull Button button)
    {
        return Mouse.buttonState.get(button).held;
    }
    
    // -------------------- State Updating -------------------- //
    
    protected static final class ButtonInput extends Input
    {
        final Vector2d downPos = new Vector2d();
    }
    
    public enum Button
    {
        UNKNOWN(-1),
        
        LEFT(GLFW_MOUSE_BUTTON_LEFT),
        RIGHT(GLFW_MOUSE_BUTTON_RIGHT),
        MIDDLE(GLFW_MOUSE_BUTTON_MIDDLE),
        
        FOUR(GLFW_MOUSE_BUTTON_4),
        FIVE(GLFW_MOUSE_BUTTON_5),
        SIX(GLFW_MOUSE_BUTTON_6),
        SEVEN(GLFW_MOUSE_BUTTON_7),
        EIGHT(GLFW_MOUSE_BUTTON_8),
        
        ;
        
        private static final HashMap<Integer, Button> BUTTON_MAP = new HashMap<>();
        
        final int ref;
        
        Button(int ref)
        {
            this.ref = ref;
        }
        
        /**
         * @return Gets the ButtonInput that corresponds to the GLFW constant.
         */
        public static Button valueOf(int ref)
        {
            return Button.BUTTON_MAP.getOrDefault(ref, Button.UNKNOWN);
        }
        
        static
        {
            for (Button button : values())
            {
                Button.BUTTON_MAP.put(button.ref, button);
            }
        }
    }
    
    public static class Shape
    {
        public static final Shape ARROW_CURSOR       = new Shape("ARROW", GLFW_ARROW_CURSOR);
        public static final Shape IBEAM_CURSOR       = new Shape("IBEAM", GLFW_IBEAM_CURSOR);
        public static final Shape CROSSHAIR_CURSOR   = new Shape("CROSSHAIR", GLFW_CROSSHAIR_CURSOR);
        public static final Shape HAND_CURSOR        = new Shape("HAND", GLFW_POINTING_HAND_CURSOR);
        public static final Shape RESIZE_EW_CURSOR   = new Shape("RESIZE_EW", GLFW_RESIZE_EW_CURSOR);
        public static final Shape RESIZE_NS_CURSOR   = new Shape("RESIZE_NS", GLFW_RESIZE_NS_CURSOR);
        public static final Shape RESIZE_NWSE_CURSOR = new Shape("RESIZE_NWSE", GLFW_RESIZE_NWSE_CURSOR);
        public static final Shape RESIZE_NESW_CURSOR = new Shape("RESIZE_NESW", GLFW_RESIZE_NESW_CURSOR);
        public static final Shape RESIZE_ALL_CURSOR  = new Shape("RESIZE_ALL", GLFW_RESIZE_ALL_CURSOR);
        public static final Shape NOT_ALLOWED_CURSOR = new Shape("NOT_ALLOWED", GLFW_NOT_ALLOWED_CURSOR);
        
        private final String name;
        private final long   handle;
        
        private Shape(String name, int shape)
        {
            this.name = name;
            //noinspection ConstantConditions
            this.handle = Engine.Delegator.waitReturnTask(() -> glfwCreateStandardCursor(shape));
        }
        
        public Shape(@NotNull String name, int width, int height, @NotNull ByteBuffer pixels, int xHot, int yHot)
        {
            this.name = name;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GLFWImage image = GLFWImage.malloc(stack);
                
                image.width(width);
                image.height(height);
                image.pixels(pixels);
                
                //noinspection ConstantConditions
                this.handle = Engine.Delegator.waitReturnTask(() -> glfwCreateCursor(image, xHot, yHot));
            }
        }
        
        public void destroy()
        {
            Engine.Delegator.runTask(() -> glfwDestroyCursor(this.handle));
        }
        
        @Override
        public String toString()
        {
            return "Shape{" + this.name + '}';
        }
    }
}
