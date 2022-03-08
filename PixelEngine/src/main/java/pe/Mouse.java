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
import static pe.Engine.Delegator;

public final class Mouse
{
    private static final Logger LOGGER = new Logger();
    
    // -------------------- Static Objects -------------------- //
    
    static final Map<Button, ButtonInput> buttonMap = new EnumMap<>(Button.class);
    
    // -------------------- Callback Objects -------------------- //
    
    static boolean entered;
    static boolean _entered;
    
    static final Vector2d absPos  = new Vector2d();
    static final Vector2d _absPos = new Vector2d();
    
    static final Vector2d pos  = new Vector2d();
    static final Vector2d _pos = new Vector2d();
    
    static final Vector2d absRel = new Vector2d();
    
    static final Vector2d rel = new Vector2d();
    
    static final Vector2d scroll  = new Vector2d();
    static final Vector2d _scroll = new Vector2d();
    
    static final Queue<Pair<Button, Integer>> _buttonStateChanges = new ConcurrentLinkedQueue<>();
    
    static void setup()
    {
        Mouse.LOGGER.fine("Setup");
        
        for (Button button : Button.values()) Mouse.buttonMap.put(button, new ButtonInput());
        
        glfwSetCursorEnterCallback(Window.handle, Mouse::enteredCallback);
        glfwSetCursorPosCallback(Window.handle, Mouse::posCallback);
        glfwSetScrollCallback(Window.handle, Mouse::scrollCallback);
        glfwSetMouseButtonCallback(Window.handle, Mouse::buttonCallback);
    }
    
    private Mouse() {}
    
    // -------------------- Properties -------------------- //
    
    /**
     * Makes the cursor visible and behaving normally.
     */
    public static void show()
    {
        Mouse.LOGGER.finest("Showing");
        
        Delegator.runTask(() -> {
            if (glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED) Mouse._pos.set(Mouse.pos.set(Window.size()).mul(0.5));
            glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        });
    }
    
    /**
     * @return Retrieves if the mouse is visible and behaving normally in its window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isShown()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_NORMAL);
    }
    
    /**
     * Makes the cursor invisible when it is over the content area of the
     * window but does not restrict the cursor from leaving.
     */
    public static void hide()
    {
        Mouse.LOGGER.finest("Hiding");
        
        Delegator.runTask(() -> glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN));
    }
    
    /**
     * @return Retrieves if the mouse is hidden over its window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isHidden()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_HIDDEN);
    }
    
    /**
     * Hides and grabs the cursor, providing virtual and unlimited cursor
     * movement. This is useful for implementing for example 3D camera
     * controls.
     */
    public static void capture()
    {
        Mouse.LOGGER.finest("Capturing");
        
        Mouse._pos.set(Mouse.pos.set(Window.width() * 0.5, Window.height() * 0.5));
        Delegator.runTask(() -> {
            glfwSetCursorPos(Window.handle, Mouse._pos.x, Mouse._pos.y);
            glfwSetInputMode(Window.handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        });
    }
    
    /**
     * @return Retrieves if the mouse is captured by its window.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean isCaptured()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED);
    }
    
    /**
     * Sets the raw mouse motion flag. Set {@code true} to enable raw (unscaled
     * and un-accelerated) mouse motion when the cursor is disabled, or
     * {@code false} to disable it. If raw motion is not supported, attempting
     * to set this will log a warning.
     *
     * @param rawInput {@code true} to enable raw mouse motion mode, otherwise {@code false}.
     */
    public static void rawInput(boolean rawInput)
    {
        Mouse.LOGGER.finest("Setting Raw Input Flag:", rawInput);
        
        Delegator.runTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return;
            }
            glfwSetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION, rawInput ? GLFW_TRUE : GLFW_FALSE);
        });
    }
    
    /**
     * @return Retrieves the raw mouse motion flag.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean rawInputEnabled()
    {
        return Delegator.waitReturnTask(() -> {
            if (!glfwRawMouseMotionSupported())
            {
                Mouse.LOGGER.warning("Raw Mouse Motion is not support on", Platform.get());
                return false;
            }
            return glfwGetInputMode(Window.handle, GLFW_RAW_MOUSE_MOTION) == GLFW_TRUE;
        });
    }
    
    /**
     * Sets the sticky mouse buttons flag. If sticky mouse buttons are enabled,
     * a mouse button press will ensure that a {@link EventMouseButtonUp} is
     * posted with a {@link EventMouseButtonDown} even if the mouse button had
     * been released before the call. This is useful when you are only
     * interested in whether mouse buttons have been pressed but not when or in
     * which order.
     *
     * @param sticky {@code true} to enable sticky mode, otherwise {@code false}.
     */
    public static void sticky(boolean sticky)
    {
        Mouse.LOGGER.finest("Setting Sticky Flag:", sticky);
        
        Delegator.runTask(() -> glfwSetInputMode(Window.handle, GLFW_STICKY_MOUSE_BUTTONS, sticky ? GLFW_TRUE : GLFW_FALSE));
    }
    
    /**
     * @return Retrieves the sticky mouse buttons flag.
     */
    @SuppressWarnings("ConstantConditions")
    public static boolean stickyEnabled()
    {
        return Delegator.waitReturnTask(() -> glfwGetInputMode(Window.handle, GLFW_STICKY_MOUSE_BUTTONS) == GLFW_TRUE);
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
        Mouse.LOGGER.finest("Setting Shape:", shape);
        
        Delegator.waitRunTask(() -> glfwSetCursor(Window.handle, shape != null ? shape.handle : NULL));
    }
    
    // -------------------- Callback Related Things -------------------- //
    
    /**
     * Returns the position of the cursor, in window coordinates, relative to
     * the upper-left corner of the last window the mouse was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The position of the cursor, in window coordinates, relative to the upper-left corner of the last window the mouse was reported in.
     */
    @NotNull
    public static Vector2dc absPos()
    {
        return Mouse.absPos;
    }
    
    /**
     * Returns the y position of the cursor, in window coordinates, relative to
     * the upper-left corner of the last window the mouse was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The x position of the cursor, in window coordinates, relative to the upper-left corner of the last window the mouse was reported in.
     */
    public static double absX()
    {
        return Mouse.absPos.x;
    }
    
    /**
     * Returns the y position of the cursor, in window coordinates, relative to
     * the upper-left corner of the last window the mouse was reported in.
     * <p>
     * If the cursor is captured (with {@link #capture()} ) then the cursor
     * position is unbounded and limited only by the minimum and maximum values
     * of a <b>{@code double}</b>.
     * <p>
     * The coordinates can be converted to their integer equivalents with the
     * {@link Math#floor} function. Casting directly to an integer type works
     * for positive coordinates, but fails for negative ones.
     *
     * @return The y position of the cursor, in screen coordinates, relative to the upper-left corner of the last window the mouse was reported in.
     */
    public static double absY()
    {
        return Mouse.absPos.y;
    }
    
    /**
     * Sets the position, in window coordinates, of the cursor relative to the
     * upper-left corner of the window. The window must have input focus. If
     * the window does not have input focus when this function is called, it
     * fails silently.
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
     * @param x The x-coordinate of the upper-left corner of the window.
     * @param y The y-coordinate of the upper-left corner of the window.
     */
    public static void absPos(double x, double y)
    {
        Mouse.LOGGER.finest("Setting Position: (%s, %s)", x, y);
        
        Delegator.waitRunTask(() -> glfwSetCursorPos(Window.handle, x, y));
    }
    
    /**
     * Sets the position, in window coordinates, of the cursor relative to the
     * upper-left corner of the window. The window must have input focus. If
     * the window does not have input focus when this function is called, it
     * fails silently.
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
    public static void absPos(@NotNull Vector2ic pos)
    {
        absPos(pos.x(), pos.y());
    }
    
    /**
     * Sets the position, in window coordinates, of the cursor relative to the
     * upper-left corner of the window. The window must have input focus. If
     * the window does not have input focus when this function is called, it
     * fails silently.
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
     * @param pos The position of the upper-left corner of the window.
     */
    public static void absPos(@NotNull Vector2dc pos)
    {
        absPos(pos.x(), pos.y());
    }
    
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
    @NotNull
    public static Vector2dc pos()
    {
        return Mouse.pos;
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
    public static double x()
    {
        return Mouse.pos.x;
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
    public static double y()
    {
        return Mouse.pos.y;
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
    public static void pos(double x, double y)
    {
        Mouse.LOGGER.finest("Setting Position: (%s, %s)", x, y);
        
        double absX = (x * (double) Engine.Viewport.width() / (double) Engine.screenSize.x) + Engine.Viewport.x();
        double absY = (y * (double) Engine.Viewport.height() / (double) Engine.screenSize.y) + Engine.Viewport.y();
        
        Delegator.waitRunTask(() -> glfwSetCursorPos(Window.handle, absX, absY));
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
    public static void pos(@NotNull Vector2ic pos)
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
    public static void pos(@NotNull Vector2dc pos)
    {
        pos(pos.x(), pos.y());
    }
    
    /**
     * Returns the difference in position of the cursor, in window coordinates,
     * relative to the upper-left corner of the last window the mouse was
     * reported in since the last time the mouse was updated.
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
     * @return The difference in position of the cursor, in window coordinates, since the last time the mouse was updated.
     */
    @NotNull
    public static Vector2dc absRel()
    {
        return Mouse.absRel;
    }
    
    /**
     * Returns the difference in x position of the cursor, in window
     * coordinates, relative to the upper-left corner of the last window the
     * mouse was reported in since the last time the mouse was updated.
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
     * @return The difference in x position of the cursor, in window coordinates, since the last time the mouse was updated.
     */
    public static double absDx()
    {
        return Mouse.absRel.x;
    }
    
    /**
     * Returns the difference in y position of the cursor, in window
     * coordinates, relative to the upper-left corner of the last window the
     * mouse was reported in since the last time the mouse was updated.
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
     * @return The difference in y position of the cursor, in window coordinates, since the last time the mouse was updated.
     */
    public static double absDy()
    {
        return Mouse.absRel.y;
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
    @NotNull
    public static Vector2dc rel()
    {
        return Mouse.rel;
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
    public static double dx()
    {
        return Mouse.rel.x;
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
    public static double dy()
    {
        return Mouse.rel.y;
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
    @NotNull
    public static Vector2dc scroll()
    {
        return Mouse.scroll;
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
    public static double scrollX()
    {
        return Mouse.scroll.x;
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
    public static double scrollY()
    {
        return Mouse.scroll.y;
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
        boolean entered = false;
        if (Mouse.entered != Mouse._entered)
        {
            Mouse.entered = Mouse._entered;
            if (Mouse.entered)
            {
                entered = true;
                
                Mouse.absPos.set(Mouse._absPos);
            }
            Engine.Events.post(EventMouseEntered.create(time, Mouse.entered));
        }
        
        Mouse.absRel.set(0);
        if (Double.compare(Mouse.absPos.x, Mouse._absPos.x) != 0 || Double.compare(Mouse.absPos.y, Mouse._absPos.y) != 0 || entered)
        {
            Mouse._absPos.sub(Mouse.absPos, Mouse.absRel);
            Mouse.absPos.set(Mouse._absPos);
            
            Mouse._pos.x = (Mouse._absPos.x - Engine.Viewport.x()) * (double) Engine.screenSize.x / (double) Engine.Viewport.width();
            Mouse._pos.y = (Mouse._absPos.y - Engine.Viewport.y()) * (double) Engine.screenSize.y / (double) Engine.Viewport.height();
            
            Mouse._pos.sub(Mouse.pos, Mouse.rel);
            Mouse.pos.set(Mouse._pos);
            Engine.Events.post(EventMouseMoved.create(time, Mouse.absPos, Mouse.pos, Mouse.rel, Mouse.absRel));
        }
        
        Mouse.scroll.set(0);
        if (Double.compare(Mouse.scroll.x, Mouse._scroll.x) != 0 || Double.compare(Mouse.scroll.y, Mouse._scroll.y) != 0)
        {
            Mouse.scroll.set(Mouse._scroll);
            Mouse._scroll.set(0);
            Engine.Events.post(EventMouseScrolled.create(time, Mouse.scroll));
        }
        
        Pair<Button, Integer> buttonStateChange;
        while ((buttonStateChange = Mouse._buttonStateChanges.poll()) != null)
        {
            ButtonInput buttonObj = Mouse.buttonMap.get(buttonStateChange.getA());
            
            buttonObj._state = buttonStateChange.getB();
        }
        
        for (Button button : Mouse.buttonMap.keySet())
        {
            ButtonInput input = Mouse.buttonMap.get(button);
            
            input.state  = input._state;
            input._state = -1;
            switch (input.state)
            {
                case GLFW_PRESS -> {
                    int tolerance = 2;
                    
                    boolean inc = Math.abs(Mouse.absPos.x - input.absDownPos.x) < tolerance &&
                                  Math.abs(Mouse.absPos.y - input.absDownPos.y) < tolerance &&
                                  time - input.downTime < Input.doublePressedDelayL();
                    
                    input.held     = true;
                    input.heldTime = time + Input.holdFrequencyL();
                    input.downTime = time;
                    input.downCount = inc ? input.downCount + 1 : 1;
                    input.absDownPos.set(Mouse.absPos);
                    input.downPos.set(Mouse.pos);
                    Engine.Events.post(EventMouseButtonDown.create(time, button, Mouse.absPos, Mouse.pos, input.downCount));
                }
                case GLFW_RELEASE -> {
                    input.held     = false;
                    input.heldTime = Long.MAX_VALUE;
                    Engine.Events.post(EventMouseButtonUp.create(time, button, Mouse.absPos, Mouse.pos));
                }
                case GLFW_REPEAT -> Engine.Events.post(EventMouseButtonRepeated.create(time, button, Mouse.absPos, Mouse.pos));
            }
            if (input.held)
            {
                if (time - input.heldTime >= Input.holdFrequencyL())
                {
                    input.heldTime += Input.holdFrequencyL();
                    Engine.Events.post(EventMouseButtonHeld.create(time, button, Mouse.absPos, Mouse.pos));
                }
                if (Mouse.rel.x != 0 || Mouse.rel.y != 0) Engine.Events.post(EventMouseButtonDragged.create(time, button, Mouse.absPos, Mouse.pos, Mouse.absRel, Mouse.rel, input.absDownPos, input.downPos));
            }
        }
    }
    
    public static boolean down(Button button)
    {
        return Mouse.buttonMap.get(button).state == GLFW_PRESS;
    }
    
    public static boolean up(Button button)
    {
        return Mouse.buttonMap.get(button).state == GLFW_RELEASE;
    }
    
    public static boolean repeat(Button button)
    {
        return Mouse.buttonMap.get(button).state == GLFW_REPEAT;
    }
    
    public static boolean held(Button button)
    {
        return Mouse.buttonMap.get(button).held;
    }
    
    protected static final class ButtonInput extends Input
    {
        final Vector2d absDownPos = new Vector2d();
        final Vector2d downPos    = new Vector2d();
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
        Mouse._entered = entered;
    }
    
    private static void posCallback(long handle, double x, double y)
    {
        Mouse._absPos.set(x, y);
    }
    
    private static void scrollCallback(long handle, double dx, double dy)
    {
        Mouse._scroll.add(dx, dy);
    }
    
    private static void buttonCallback(long handle, int button, int action, int mods)
    {
        Mouse._buttonStateChanges.offer(new Pair<>(Mouse.Button.get(button), action));
        
        Modifier.activeMods = mods;
    }
}
