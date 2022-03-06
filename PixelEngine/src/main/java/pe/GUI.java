package pe;

import org.lwjgl.nuklear.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.event.*;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwSetClipboardString;
import static org.lwjgl.glfw.GLFW.nglfwGetClipboardString;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackGet;

public final class GUI
{
    static NkAllocator alloc;
    
    static NkContext  ctx;  // Create a Nuklear context, it is used everywhere.
    static NkUserFont default_font = NkUserFont.create(); // This is the Nuklear font object used for rendering text.
    
    static NkBuffer          cmds         = NkBuffer.create(); // Stores a list of drawing commands that will be passed to OpenGL to render the interface.
    static NkDrawNullTexture null_texture = NkDrawNullTexture.create(); // An empty texture used for drawing.
    
    static void setup()
    {
        GUI.alloc = NkAllocator.malloc();
        GUI.alloc.alloc((handle, old, size) -> MemoryUtil.nmemAllocChecked(size));
        GUI.alloc.mfree((handle, ptr) -> MemoryUtil.nmemFree(ptr));
        
        GUI.ctx = NkContext.malloc();
    
        nk_init(GUI.ctx, GUI.alloc, null);
        GUI.ctx.clip()
           .copy((handle, text, len) -> {
               if (len == 0) return;
               
               try (MemoryStack stack = MemoryStack.stackPush())
               {
                   ByteBuffer str = stack.malloc(len + 1);
                   MemoryUtil.memCopy(text, MemoryUtil.memAddress(str), len);
                   str.put(len, (byte) 0);
    
                   Window.get().setClipboard(str);
               }
           })
           .paste((handle, edit) -> {
               long text =  Window.get().getClipboardRaw();
               if (text != MemoryUtil.NULL)
               {
                   nnk_textedit_paste(edit, text, nnk_strlen(text));
               }
           });
        
        // This is here because sometimes the mouse started grabbed.
        nk_input_begin(GUI.ctx);
        nk_input_end(GUI.ctx);
    }
    
    static void destroy()
    {
        GUI.ctx.free();
    
        GUI.alloc.free();
    }
    
    static void handleEvents()
    {
        nk_input_begin(GUI.ctx);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            for (Event event : Engine.Events.get())
            {
                if (event instanceof EventMouseMoved mMoved)
                {
                    nk_input_motion(GUI.ctx, (int) mMoved.x(), (int) mMoved.y());
                }
                else if (event instanceof EventMouseScrolled mScrolled)
                {
                    NkVec2 scroll = NkVec2.malloc(stack).x((float) mScrolled.dx()).y((float) mScrolled.dx());
                    nk_input_scroll(GUI.ctx, scroll);
                }
                else if (event instanceof EventMouseButtonDown mbDown)
                {
                    mouseButtonInput(mbDown.button(), mbDown.x(), mbDown.y(), true);
                }
                else if (event instanceof EventMouseButtonUp mbUp)
                {
                    mouseButtonInput(mbUp.button(), mbUp.x(), mbUp.y(), false);
                    nk_input_button(GUI.ctx, NK_BUTTON_DOUBLE, (int) mbUp.x(), (int) mbUp.y(), false);
                }
                else if (event instanceof EventMouseButtonPressed mbPressed)
                {
                    if (mbPressed.doublePressed())
                    {
                        nk_input_button(GUI.ctx, NK_BUTTON_DOUBLE, (int) mbPressed.x(), (int) mbPressed.y(), true);
                    }
                }
                else if (event instanceof EventKeyboardKeyDown kkDown)
                {
                    keyboardKeyInput(kkDown.key(), true);
                }
                else if (event instanceof EventKeyboardKeyUp kkUp)
                {
                    keyboardKeyInput(kkUp.key(), false);
                }
                else if (event instanceof EventKeyboardTyped kTyped)
                {
                    nk_input_unicode(GUI.ctx, kTyped.codePoint());
                }
            }
        }
        
        NkMouse mouse = GUI.ctx.input().mouse();
        if (mouse.grab())
        {
            Mouse.get().hide();
        }
        else if (mouse.grabbed())
        {
            float prevX = mouse.prev().x();
            float prevY = mouse.prev().y();
            Mouse.get().pos(prevX, prevY);
            mouse.pos().x(prevX);
            mouse.pos().y(prevY);
        }
        else if (mouse.ungrab())
        {
            Mouse.get().show();
        }
        
        nk_input_end(GUI.ctx);
    }
    
    static void draw()
    {
    
    }
    
    private static void mouseButtonInput(Mouse.Button button, double x, double y, boolean down)
    {
        switch (button)
        {
            case LEFT -> nk_input_button(GUI.ctx, NK_BUTTON_LEFT, (int) x, (int) y, down);
            case RIGHT -> nk_input_button(GUI.ctx, NK_BUTTON_RIGHT, (int) x, (int) y, down);
            case MIDDLE -> nk_input_button(GUI.ctx, NK_BUTTON_MIDDLE, (int) x, (int) y, down);
        }
    }
    
    private static void keyboardKeyInput(Keyboard.Key key, boolean down)
    {
        // NK_KEY_TEXT_INSERT_MODE
        // NK_KEY_TEXT_REPLACE_MODE
        // NK_KEY_TEXT_RESET_MODE
        
        switch (key)
        {
            case L_SHIFT, R_SHIFT -> nk_input_key(GUI.ctx, NK_KEY_SHIFT, down);
            case L_CONTROL, R_CONTROL -> nk_input_key(GUI.ctx, NK_KEY_CTRL, down);
            case DELETE -> nk_input_key(GUI.ctx, NK_KEY_DEL, down);
            case ENTER, KP_ENTER -> nk_input_key(GUI.ctx, NK_KEY_ENTER, down);
            case TAB -> nk_input_key(GUI.ctx, NK_KEY_TAB, down);
            case BACKSPACE -> nk_input_key(GUI.ctx, NK_KEY_BACKSPACE, down);
            case UP -> nk_input_key(GUI.ctx, NK_KEY_UP, down);
            case DOWN -> nk_input_key(GUI.ctx, NK_KEY_DOWN, down);
            case LEFT -> nk_input_key(ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_LEFT : NK_KEY_LEFT, down);
            case RIGHT -> nk_input_key(GUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_WORD_RIGHT : NK_KEY_RIGHT, down);
            case HOME -> {
                nk_input_key(GUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_START : NK_KEY_TEXT_LINE_START, down);
                nk_input_key(GUI.ctx, NK_KEY_SCROLL_START, down);
            }
            case END -> {
                nk_input_key(GUI.ctx, Modifier.only(Modifier.CONTROL) ? NK_KEY_TEXT_END : NK_KEY_TEXT_LINE_END, down);
                nk_input_key(GUI.ctx, NK_KEY_SCROLL_END, down);
            }
            case PAGE_UP -> nk_input_key(GUI.ctx, NK_KEY_SCROLL_UP, down);
            case PAGE_DOWN -> nk_input_key(GUI.ctx, NK_KEY_SCROLL_DOWN, down);
            case C -> nk_input_key(GUI.ctx, NK_KEY_COPY, Modifier.only(Modifier.CONTROL) && down);
            case X -> nk_input_key(GUI.ctx, NK_KEY_CUT, Modifier.only(Modifier.CONTROL) && down);
            case V -> nk_input_key(GUI.ctx, NK_KEY_PASTE, Modifier.only(Modifier.CONTROL) && down);
            case Z -> nk_input_key(GUI.ctx, NK_KEY_TEXT_UNDO, Modifier.only(Modifier.CONTROL) && down);
            case Y -> nk_input_key(GUI.ctx, NK_KEY_TEXT_REDO, Modifier.only(Modifier.CONTROL) && down);
            case A -> nk_input_key(GUI.ctx, NK_KEY_TEXT_SELECT_ALL, Modifier.only(Modifier.CONTROL) && down);
        }
    }
}
