package pe;

import org.jetbrains.annotations.NotNull;
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
import static pe.Engine.Delegator;

public class Mouse
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Static Objects -------------------- //
    
    private static Mouse INSTANCE;
    
    public static Mouse get()
    {
        return Mouse.INSTANCE;
    }
    
    static void setup()
    {
        Mouse.LOGGER.fine("Setup");
        
        Mouse.INSTANCE = new Mouse();
        
        glfwSetCursorEnterCallback(Window.get().handle, Mouse::enteredCallback);
        glfwSetCursorPosCallback(Window.get().handle, Mouse::posCallback);
        glfwSetScrollCallback(Window.get().handle, Mouse::scrollCallback);
        glfwSetMouseButtonCallback(Window.get().handle, Mouse::buttonCallback);
    }
    
    // -------------------- Objects -------------------- //
    
    private final Map<Button, ButtonInput> buttonMap = new EnumMap<>(Button.class);
    
    // -------------------- Callback Objects -------------------- //
    
    protected boolean entered;
    protected boolean _entered;
    
    protected final Vector2d pos  = new Vector2d();
    protected final Vector2d _pos = new Vector2d();
    
    protected final Vector2d rel = new Vector2d();
    
    protected final Vector2d scroll  = new Vector2d();
    protected final Vector2d _scroll = new Vector2d();
    
    protected final Queue<Pair<Button, Integer>> _buttonStateChanges = new ConcurrentLinkedQueue<>();
    
    private Mouse()
    {
        for (Button button : Button.values()) this.buttonMap.put(button, new ButtonInput());
    }
    
    // -------------------- Properties -------------------- //
    
    /**
     * Makes the cursor visible and behaving normally.
     */
    public void show()
    {
        Mouse.LOGGER.finest("Showing");
        
        Delegator.runTask(() -> {
            if (glfwGetInputMode(Window.get().handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) this._pos.set(this.pos.set(Window.get().size()).mul(0.5));
            glfwSetInputMode(Window.get().handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        });
    }
    
    /**
     * @return Retrieves if the mouse is visible and behaving normally in its window.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean isShown()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.get().handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL);
    }
    
    /**
     * Makes the cursor invisible when it is over the content area of the
     * window but does not restrict the cursor from leaving.
     */
    public void hide()
    {
        Mouse.LOGGER.finest("Hiding");
        
        Delegator.runTask(() -> glfwSetInputMode(Window.get().handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN));
    }
    
    /**
     * @return Retrieves if the mouse is hidden over its window.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean isHidden()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.get().handle, GLFW_CURSOR) == GLFW_CURSOR_HIDDEN);
    }
    
    /**
     * Hides and grabs the cursor, providing virtual and unlimited cursor
     * movement. This is useful for implementing for example 3D camera
     * controls.
     */
    public void capture()
    {
        Mouse.LOGGER.finest("Capturing");
        
        this._pos.set(this.pos.set(Window.get().width() * 0.5, Window.get().height() * 0.5));
        Delegator.runTask(() -> {
            glfwSetCursorPos(Window.get().handle, this._pos.x, this._pos.y);
            glfwSetInputMode(Window.get().handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        });
    }
    
    /**
     * @return Retrieves if the mouse is captured by its window.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean isCaptured()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.get().handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED);
    }
    
    /**
     * Sets the raw mouse motion flag. Set {@code true} to enable raw (unscaled
     * and un-accelerated) mouse motion when the cursor is disabled, or
     * {@code false} to disable it. If raw motion is not supported, attempting
     * to set this will log a warning.
     *
     * @param rawInput {@code true} to enable raw mouse motion mode, otherwise {@code false}.
     */
    public void rawInput(boolean rawInput)
    {
        Mouse.LOGGER.finest("Setting Raw Input Flag:", rawInput);
        
        Delegator.runTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return;
            }
            glfwSetInputMode(Window.get().handle, GLFW_RAW_MOUSE_MOTION, rawInput ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    /**
     * @return Retrieves the raw mouse motion flag.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean rawInputEnabled()
    {
        return Delegator.waitReturnTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return false;
            }
            return glfwGetInputMode(Window.get().handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
        });
    }
    
    /**
     * Sets the sticky mouse buttons flag. If sticky mouse buttons are enabled,
     * a mouse button press will ensure that {@link EventMouseButtonPressed}
     * is posted even if the mouse button had been released before the call.
     * This is useful when you are only interested in whether mouse buttons
     * have been pressed but not when or in which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public void sticky(boolean sticky)
    {
        Mouse.LOGGER.finest("Setting Sticky Flag:", sticky);
        
        Delegator.runTask(() -> glfwSetInputMode(Window.get().handle, GLFW_STICKY_MOUSE_BUTTONS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves the sticky mouse buttons flag.
     */
    @SuppressWarnings("ConstantConditions")
    public boolean stickyEnabled()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.get().handle, GLFW_STICKY_MOUSE_BUTTONS) == GLFW_TRUE);
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
    public void shape(Shape shape)
    {
        Mouse.LOGGER.finest("Setting Shape:", shape);
        
        Delegator.waitRunTask(() -> glfwSetCursor(Window.get().handle, shape != null ? shape.handle : NULL));
    }
    
    // -------------------- Callback Related Things -------------------- //
    
    /**
     * Returns the position of the cursor, in screen coordinates, relative to
     * the upper-left corner of the content area of the last window the mouse
     * was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The position of the cursor, in screen coordinates, relative to the upper-left corner of the content area of the last window the mouse was reported in.
     */
    public @NotNull Vector2dc pos()
    {
        return this.pos;
    }
    
    /**
     * Returns the x position of the cursor, in screen coordinates, relative to
     * the upper-left corner of the content area of the last window the mouse
     * was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The x position of the cursor, in screen coordinates, relative to the upper-left corner of the content area of the last window the mouse was reported in.
     */
    public double x()
    {
        return this.pos.x;
    }
    
    /**
     * Returns the y position of the cursor, in screen coordinates, relative to
     * the upper-left corner of the content area of the last window the mouse
     * was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The y position of the cursor, in screen coordinates, relative to the upper-left corner of the content area of the last window the mouse was reported in.
     */
    public double y()
    {
        return this.pos.y;
    }
    
    /**
     * Sets the position, in screen coordinates, of the cursor relative to the
     * upper-left corner of the content area of the window. The window must
     * have input focus. If the window does not have input focus when this
     * function is called, it fails silently.
     * <p>
     * <b>Do not use this function</b> to implement things like camera
     * controls. GLFW already provides the {@link #capture()} cursor mode that
     * hides the cursor, transparently re-centers it and provides unconstrained
     * cursor motion.
     * <p>
     * If the cursor {@link #isCaptured()} then the cursor position is
     * unconstrained and limited only by the minimum and maximum values of
     * <b>double</b>.
     *
     * @param x The x-coordinate of the upper-left corner of the content area.
     * @param y The y-coordinate of the upper-left corner of the content area.
     */
    public void pos(double x, double y)
    {
        Mouse.LOGGER.finest("Setting Position: (%s, %s)", x, y);
        
        Delegator.waitRunTask(() -> glfwSetCursorPos(Window.get().handle, x, y));
    }
    
    /**
     * Sets the position, in screen coordinates, of the cursor relative to the
     * upper-left corner of the content area of the window. The window must
     * have input focus. If the window does not have input focus when this
     * function is called, it fails silently.
     * <p>
     * <b>Do not use this function</b> to implement things like camera
     * controls. GLFW already provides the {@link #capture()} cursor mode that
     * hides the cursor, transparently re-centers it and provides unconstrained
     * cursor motion.
     * <p>
     * If the cursor {@link #isCaptured()} then the cursor position is
     * unconstrained and limited only by the minimum and maximum values of
     * <b>double</b>.
     *
     * @param pos The position of the upper-left corner of the content area.
     */
    public void pos(@NotNull Vector2ic pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Sets the position, in screen coordinates, of the cursor relative to the
     * upper-left corner of the content area of the window. The window must
     * have input focus. If the window does not have input focus when this
     * function is called, it fails silently.
     * <p>
     * <b>Do not use this function</b> to implement things like camera
     * controls. GLFW already provides the {@link #capture()} cursor mode that
     * hides the cursor, transparently re-centers it and provides unconstrained
     * cursor motion.
     * <p>
     * If the cursor {@link #isCaptured()} then the cursor position is
     * unconstrained and limited only by the minimum and maximum values of
     * <b>double</b>.
     *
     * @param pos The position of the upper-left corner of the content area.
     */
    public void pos(@NotNull Vector2dc pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Returns the difference in position of the cursor, in screen coordinates,
     * relative to the upper-left corner of the content area of the last window
     * the mouse was reported in since the last time the mouse was updated.
     * <p>
     * This will be {@code {0.0, 0.0}} for the majority of the time as the
     * mouse can update more that once per window frame. It would be better to
     * subscribe to {@link EventMouseMoved} events to getBytes actual relative
     * values.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The difference in position of the cursor, in screen coordinates, since the last time the mouse was updated.
     */
    public @NotNull Vector2dc rel()
    {
        return this.rel;
    }
    
    /**
     * Returns the difference in x position of the cursor, in screen
     * coordinates, relative to the upper-left corner of the content area of
     * the last window the mouse was reported in since the last time the mouse
     * was updated.
     * <p>
     * This will be {@code 0.0} for the majority of the time as the mouse can
     * update more that once per window frame. It would be better to subscribe
     * to {@link EventMouseMoved} events to getBytes actual relative values.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The difference in x position of the cursor, in screen coordinates, since the last time the mouse was updated.
     */
    public double dx()
    {
        return this.rel.x;
    }
    
    /**
     * Returns the difference in y position of the cursor, in screen
     * coordinates, relative to the upper-left corner of the content area of
     * the last window the mouse was reported in since the last time the mouse
     * was updated.
     * <p>
     * This will be {@code 0.0} for the majority of the time as the mouse can
     * update more that once per window frame. It would be better to subscribe
     * to {@link EventMouseMoved} events to getBytes actual relative values.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The difference in y position of the cursor, in screen coordinates, since the last time the mouse was updated.
     */
    public double dy()
    {
        return this.rel.y;
    }
    
    /**
     * Returns the amount that the mouse wheel, or touch-pad, was scrolled in
     * last window the mouse was reported in since the last time the mouse was
     * updated.
     * <p>
     * This will be {@code {0.0, 0.0}} for the majority of the time as the
     * mouse can update more that once per window frame. It would be better to
     * subscribe to {@link EventMouseScrolled} events to getBytes actual scrolled
     * values.
     *
     * @return The amount that the mouse wheel, or touch-pad, was scrolled since the last time the mouse was updated.
     */
    public @NotNull Vector2dc scroll()
    {
        return this.scroll;
    }
    
    /**
     * Returns the amount that the mouse wheel, or touch-pad, was scrolled
     * horizontally in last window the mouse was reported in since the last
     * time the mouse was updated.
     * <p>
     * This will be {@code {0.0, 0.0}} for the majority of the time as the
     * mouse can update more that once per window frame. It would be better to
     * subscribe to {@link EventMouseScrolled} events to getBytes actual scrolled
     * values.
     *
     * @return The amount that the mouse wheel, or touch-pad, was scrolled horizontally since the last time the mouse was updated.
     */
    public double scrollX()
    {
        return this.scroll.x;
    }
    
    /**
     * Returns the amount that the mouse wheel, or touch-pad, was scrolled
     * vertically in last window the mouse was reported in since the last time
     * the mouse was updated.
     * <p>
     * This will be {@code {0.0, 0.0}} for the majority of the time as the
     * mouse can update more that once per window frame. It would be better to
     * subscribe to {@link EventMouseScrolled} events to getBytes actual scrolled
     * values.
     *
     * @return The amount that the mouse wheel, or touch-pad, was scrolled vertically since the last time the mouse was updated.
     */
    public double scrollY()
    {
        return this.scroll.y;
    }
    
    /**
     * This method is called by the window it is attached to. This is where
     * events should be posted to when something has changed.
     *
     * @param time   The system time in nanoseconds.
     * @param deltaT The time in nanoseconds since the last time this method was called.
     */
    @SuppressWarnings("ConstantConditions")
    void postEvents(long time, long deltaT)
    {
        boolean entered = false;
        if (this.entered != this._entered)
        {
            this.entered = this._entered;
            if (this.entered)
            {
                entered = true;
                
                this.pos.set(this._pos);
            }
            Engine.Events.post(EventMouseEntered.create(time, this.entered));
        }
        
        this.rel.set(0);
        if (Double.compare(this.pos.x, this._pos.x) != 0 || Double.compare(this.pos.y, this._pos.y) != 0 || entered)
        {
            this._pos.sub(this.pos, this.rel);
            this.pos.set(this._pos);
            Engine.Events.post(EventMouseMoved.create(time, this.pos, this.rel));
        }
        
        this.scroll.set(0);
        if (Double.compare(this.scroll.x, this._scroll.x) != 0 || Double.compare(this.scroll.y, this._scroll.y) != 0)
        {
            this.scroll.set(this._scroll);
            this._scroll.set(0);
            Engine.Events.post(EventMouseScrolled.create(time, this.scroll));
        }
        
        Pair<Button, Integer> buttonStateChange;
        while ((buttonStateChange = this._buttonStateChanges.poll()) != null)
        {
            ButtonInput buttonObj = this.buttonMap.get(buttonStateChange.getA());
            
            buttonObj._state = buttonStateChange.getB();
        }
        
        for (Button button : this.buttonMap.keySet())
        {
            ButtonInput input = this.buttonMap.get(button);
            
            input.state  = input._state;
            input._state = -1;
            switch (input.state)
            {
                case GLFW_PRESS -> {
                    input.held     = true;
                    input.holdTime = time + Input.holdFrequencyL();
                    Engine.Events.post(EventMouseButtonDown.create(time, button, this.pos));
                    
                    input.clickPos.set(this.pos);
                }
                case GLFW_RELEASE -> {
                    input.held     = false;
                    input.holdTime = Long.MAX_VALUE;
                    Engine.Events.post(EventMouseButtonUp.create(time, button, this.pos));
                    
                    boolean inClickRange  = Math.abs(this.pos.x - input.clickPos.x) < 2 && Math.abs(this.pos.y - input.clickPos.y) < 2;
                    boolean inDClickRange = Math.abs(this.pos.x - input.dClickPos.x) < 2 && Math.abs(this.pos.y - input.dClickPos.y) < 2;
                    
                    if (inDClickRange && time - input.pressTime < Input.doublePressedDelayL())
                    {
                        input.pressTime = 0;
                        Engine.Events.post(EventMouseButtonPressed.create(time, button, this.pos, true));
                    }
                    else if (inClickRange)
                    {
                        input.dClickPos.set(this.pos);
                        input.pressTime = time;
                        Engine.Events.post(EventMouseButtonPressed.create(time, button, this.pos, false));
                    }
                }
                case GLFW_REPEAT -> Engine.Events.post(EventMouseButtonRepeated.create(time, button, this.pos));
            }
            if (input.held)
            {
                if (time - input.holdTime >= Input.holdFrequencyL())
                {
                    input.holdTime += Input.holdFrequencyL();
                    Engine.Events.post(EventMouseButtonHeld.create(time, button, this.pos));
                }
                if (this.rel.x != 0 || this.rel.y != 0) Engine.Events.post(EventMouseButtonDragged.create(time, button, this.pos, this.rel, input.clickPos));
            }
        }
    }
    
    public boolean down(Button button)
    {
        return this.buttonMap.get(button).state == GLFW_PRESS;
    }
    
    public boolean up(Button button)
    {
        return this.buttonMap.get(button).state == GLFW_RELEASE;
    }
    
    public boolean repeat(Button button)
    {
        return this.buttonMap.get(button).state == GLFW_REPEAT;
    }
    
    public boolean held(Button button)
    {
        return this.buttonMap.get(button).held;
    }
    
    static final class ButtonInput extends Input
    {
        final Vector2d clickPos  = new Vector2d();
        final Vector2d dClickPos = new Vector2d();
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
        public static Button get(int ref)
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
            this.handle = Delegator.waitReturnTask(() -> glfwCreateStandardCursor(shape));
        }
        
        public Shape(String name, int width, int height, ByteBuffer pixels, int xHot, int yHot)
        {
            this.name = name;
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GLFWImage image = GLFWImage.malloc(stack);
                
                image.width(width);
                image.height(height);
                image.pixels(pixels);
                
                //noinspection ConstantConditions
                this.handle = Delegator.waitReturnTask(() -> glfwCreateCursor(image, xHot, yHot));
            }
        }
        
        public void destroy()
        {
            glfwDestroyCursor(this.handle);
        }
        
        @Override
        public String toString()
        {
            return "Shape{" + this.name + '}';
        }
    }
    
    private static void enteredCallback(long handle, boolean entered)
    {
        Mouse.INSTANCE._entered = entered;
    }
    
    private static void posCallback(long handle, double x, double y)
    {
        x = (x - Engine.Viewport.x()) * (double) Engine.screenSize.x / (double) Engine.Viewport.width();
        y = (y - Engine.Viewport.y()) * (double) Engine.screenSize.y / (double) Engine.Viewport.height();
        
        Mouse.INSTANCE._pos.set(x, y);
    }
    
    private static void scrollCallback(long handle, double dx, double dy)
    {
        Mouse.INSTANCE._scroll.add(dx, dy);
    }
    
    private static void buttonCallback(long handle, int button, int action, int mods)
    {
        Mouse.INSTANCE._buttonStateChanges.offer(new Pair<>(Mouse.Button.get(button), action));
        
        Modifier.activeMods = mods;
    }
}
