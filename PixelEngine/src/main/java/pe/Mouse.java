package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2ic;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.system.MemoryStack;
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
import static org.lwjgl.system.MemoryUtil.NULL;

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
                case GLFW_PRESS -> {
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
                case GLFW_RELEASE -> {
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
    public static boolean isShown(@NotNull Window window)
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL);
    }
    
    /**
     * @return Retrieves if the mouse is visible and behaving normally in the main window.
     */
    public static boolean isShown()
    {
        return isShown(Window.primary);
    }
    
    /**
     * Makes the cursor visible and behaving normally in the specified window.
     */
    public static void show(@NotNull Window window)
    {
        Mouse.LOGGER.finest("Showing in", window);
        
        Engine.Delegator.runTask(() -> {
            if (Mouse.window == window && glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED)
            {
                Mouse.pos.set(Mouse.posChanges.set(window.bounds().size()).mul(0.5));
            }
            glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        });
    }
    
    /**
     * Makes the cursor visible and behaving normally in the main window.
     */
    public static void show()
    {
        show(Window.primary);
    }
    
    /**
     * @return Retrieves if the mouse is hidden in the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isHidden(@NotNull Window window)
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_HIDDEN);
    }
    
    /**
     * @return Retrieves if the mouse is hidden in the main window.
     */
    public static boolean isHidden()
    {
        return isHidden(Window.primary);
    }
    
    /**
     * Makes the cursor invisible when it is over the content area of the
     * specified window but does not restrict the cursor from leaving.
     */
    public static void hide(@NotNull Window window)
    {
        Mouse.LOGGER.finest("Hiding in", window);
        
        Engine.Delegator.runTask(() -> glfwSetInputMode(window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN));
    }
    
    /**
     * Makes the cursor invisible when it is over the content area of the main
     * window but does not restrict the cursor from leaving.
     */
    public static void hide()
    {
        hide(Window.primary);
    }
    
    /**
     * @return Retrieves if the mouse is captured in the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isCaptured(@NotNull Window window)
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED);
    }
    
    /**
     * @return Retrieves if the mouse is captured in the main window.
     */
    public static boolean isCaptured()
    {
        return isCaptured(Window.primary);
    }
    
    /**
     * Hides and grabs the cursor in the specified window, providing virtual
     * and unlimited cursor movement. This is useful for implementing for
     * example 3D camera controls.
     */
    public static void capture(@NotNull Window window)
    {
        Mouse.LOGGER.finest("Capturing in", window);
        
        Engine.Delegator.runTask(() -> {
            Mouse.window = window;
            Mouse.pos.set(Mouse.posChanges.set(Mouse.window.bounds().size()).mul(0.5));
            glfwSetCursorPos(Mouse.window.handle, Mouse.posChanges.x, Mouse.posChanges.y);
            glfwSetInputMode(Mouse.window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        });
    }
    
    /**
     * Hides and grabs the cursor in the main window, providing virtual and
     * unlimited cursor movement. This is useful for implementing for example
     * 3D camera controls.
     */
    public static void capture()
    {
        capture(Window.primary);
    }
    
    /**
     * @return Retrieves the raw mouse motion flag for the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isRawInput(@NotNull Window window)
    {
        return Engine.Delegator.waitReturnTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return false;
            }
            return glfwGetInputMode(window.handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
        });
    }
    
    /**
     * @return Retrieves the raw mouse motion flag for the main window.
     */
    public static boolean isRawInput()
    {
        return isRawInput(Window.primary);
    }
    
    /**
     * Sets the raw mouse motion flag for the specified window. Set
     * {@code true} to enable raw (unscaled and un-accelerated) mouse motion
     * when the cursor is disabled, or {@code false} to disable it. If raw
     * motion is not supported, attempting to set Mouse will log a warning.
     *
     * @param rawInput {@code true} to enable raw mouse motion mode, otherwise {@code false}.
     */
    public static void rawInput(@NotNull Window window, boolean rawInput)
    {
        Mouse.LOGGER.finest("Setting Raw Input Flag for %s: %s", window, rawInput);
        
        Engine.Delegator.runTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return;
            }
            glfwSetInputMode(window.handle, GLFW_RAW_MOUSE_MOTION, rawInput ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    /**
     * Sets the raw mouse motion flag for the main window. Set {@code true} to
     * enable raw (unscaled and un-accelerated) mouse motion when the cursor is
     * disabled, or {@code false} to disable it. If raw motion is not
     * supported, attempting to set Mouse will log a warning.
     *
     * @param rawInput {@code true} to enable raw mouse motion mode, otherwise {@code false}.
     */
    public static void rawInput(boolean rawInput)
    {
        rawInput(Window.primary, rawInput);
    }
    
    /**
     * @return Retrieves the sticky mouse buttons flag for the specified window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isSticky(@NotNull Window window)
    {
        return Engine.Delegator.waitReturnTask(() -> glfwGetInputMode(window.handle, GLFW_STICKY_MOUSE_BUTTONS) == GLFW_TRUE);
    }
    
    /**
     * @return Retrieves the sticky mouse buttons flag for the main window.
     */
    public static boolean isSticky()
    {
        return isSticky(Window.primary);
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
    public static void sticky(@NotNull Window window, boolean sticky)
    {
        Mouse.LOGGER.finest("Setting Sticky Flag for %s: %s", window, sticky);
        
        Engine.Delegator.runTask(() -> {
            Mouse.window = window;
            glfwSetInputMode(Mouse.window.handle, GLFW_STICKY_MOUSE_BUTTONS, sticky ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    /**
     * Sets the sticky mouse buttons flag for the main window. If sticky mouse
     * buttons are enabled, a mouse button press will ensure that a
     * {@link EventMouseButtonUp} is posted with a {@link EventMouseButtonDown}
     * even if the mouse button had been released before the call. This is
     * useful when you are only interested in whether mouse buttons have been
     * pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public static void sticky(boolean sticky)
    {
        sticky(Window.primary, sticky);
    }
    
    /**
     * @return The current shape of the mouse or {@code null} if set to the default arrow cursor for the specified window.
     */
    @Nullable
    public static Shape shape(@NotNull Window window)
    {
        return Mouse.shapes.getOrDefault(window.handle, null);
    }
    
    /**
     * @return The current shape of the mouse or {@code null} if set to the default arrow cursor for the main window.
     */
    @Nullable
    public static Shape shape()
    {
        return shape(Window.primary);
    }
    
    /**
     * Sets the cursor image to be used when the cursor is over the content
     * area of the window. The set cursor will only be visible when the cursor
     * mode of the window {@link #isShown(Window)}.
     * <p>
     * On some platforms, the set cursor may not be visible unless the
     * window also has input focus.
     *
     * @param shape the cursor to set, or {@code null} to switch back to the default arrow cursor
     */
    public static void shape(@NotNull Window window, @Nullable Shape shape)
    {
        Mouse.LOGGER.finest("Setting Shape for %s: %s", window, shape);
        
        Engine.Delegator.waitRunTask(() -> {
            Mouse.shapes.put(window.handle, shape);
            glfwSetCursor(window.handle, shape != null ? shape.handle : NULL);
        });
    }
    
    /**
     * Sets the cursor image to be used when the cursor is over the content
     * area of the window. The set cursor will only be visible when the cursor
     * mode of the window {@link #isShown(Window)}.
     * <p>
     * On some platforms, the set cursor may not be visible unless the
     * window also has input focus.
     *
     * @param shape the cursor to set, or {@code null} to switch back to the default arrow cursor
     */
    public static void shape(@Nullable Shape shape)
    {
        shape(Window.primary, shape);
    }
    
    // -------------------- State Properties -------------------- //
    
    /**
     * @return If the mouse is in the window specified.
     */
    public static boolean entered(@NotNull Window window)
    {
        return Mouse.window == window && Mouse.entered;
    }
    
    /**
     * @return If the mouse is in the main window.
     */
    public static boolean entered()
    {
        return entered(Window.primary);
    }
    
    /**
     * @return If the mouse is in any window.
     */
    public static boolean enteredAny()
    {
        return entered(Mouse.window);
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
    public static void pos(@NotNull Window window, double x, double y)
    {
        Mouse.LOGGER.finest("Setting Position in %s: (%s, %s)", window, x, y);
        
        Engine.Delegator.waitRunTask(() -> {
            Mouse.window = window;
            glfwSetCursorPos(window.handle, x, y);
        });
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the specified window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Window window, @NotNull Vector2ic pos)
    {
        pos(window, pos.x(), pos.y());
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the specified window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Window window, @NotNull Vector2dc pos)
    {
        pos(window, pos.x(), pos.y());
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the current window.
     *
     * @param x The x position of the cursor
     * @param y The y position of the cursor
     */
    public static void pos(double x, double y)
    {
        pos(Mouse.window, x, y);
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the current window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Vector2ic pos)
    {
        pos(Mouse.window, pos.x(), pos.y());
    }
    
    /**
     * Sets the cursor position, in viewport coordinates, relative to the upper-left corner of the content area in the current window.
     *
     * @param pos The position of the cursor
     */
    public static void pos(@NotNull Vector2dc pos)
    {
        pos(Mouse.window, pos.x(), pos.y());
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
    public static boolean down(@NotNull Window window, @NotNull Button button)
    {
        return Mouse.window == window && Mouse.buttonState.get(button).state == GLFW_PRESS;
    }
    
    /**
     * @return If the provided button is in the down state in the main window.
     */
    public static boolean down(@NotNull Button button)
    {
        return down(Window.primary, button);
    }
    
    /**
     * @return If the provided button is in the down state.
     */
    public static boolean downAny(@NotNull Button button)
    {
        return down(Mouse.window, button);
    }
    
    /**
     * @return If the provided button is in the up state in the provided window.
     */
    public static boolean up(@NotNull Window window, @NotNull Button button)
    {
        return Mouse.window == window && Mouse.buttonState.get(button).state == GLFW_RELEASE;
    }
    
    /**
     * @return If the provided button is in the up state in the main window.
     */
    public static boolean up(@NotNull Button button)
    {
        return up(Window.primary, button);
    }
    
    /**
     * @return If the provided button is in the up state.
     */
    public static boolean upAny(@NotNull Button button)
    {
        return up(Mouse.window, button);
    }
    
    /**
     * @return If the provided button is in the repeat state in the provided window.
     */
    public static boolean repeat(@NotNull Window window, @NotNull Button button)
    {
        return Mouse.window == window && Mouse.buttonState.get(button).state == GLFW_REPEAT;
    }
    
    /**
     * @return If the provided button is in the repeat state in the main window.
     */
    public static boolean repeat(@NotNull Button button)
    {
        return repeat(Window.primary, button);
    }
    
    /**
     * @return If the provided button is in the repeat state.
     */
    public static boolean repeatAny(@NotNull Button button)
    {
        return repeat(Mouse.window, button);
    }
    
    /**
     * @return If the provided button is in the held state in the provided window.
     */
    public static boolean held(@NotNull Window window, @NotNull Button button)
    {
        return Mouse.window == window && Mouse.buttonState.get(button).held;
    }
    
    /**
     * @return If the provided button is in the held state in the main window.
     */
    public static boolean held(@NotNull Button button)
    {
        return held(Window.primary, button);
    }
    
    /**
     * @return If the provided button is in the held state.
     */
    public static boolean heldAny(@NotNull Button button)
    {
        return held(Mouse.window, button);
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
        public static final Shape ARROW_CURSOR     = new Shape("ARROW", GLFW_ARROW_CURSOR);
        public static final Shape IBEAM_CURSOR     = new Shape("IBEAM", GLFW_IBEAM_CURSOR);
        public static final Shape CROSSHAIR_CURSOR = new Shape("CROSSHAIR", GLFW_CROSSHAIR_CURSOR);
        public static final Shape HAND_CURSOR      = new Shape("HAND", GLFW_HAND_CURSOR);
        public static final Shape HRESIZE_CURSOR   = new Shape("HRESIZE", GLFW_HRESIZE_CURSOR);
        public static final Shape VRESIZE_CURSOR   = new Shape("VRESIZE", GLFW_VRESIZE_CURSOR);
        
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
