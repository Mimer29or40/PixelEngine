package pe.guiOld;

import static org.lwjgl.nuklear.Nuklear.*;

public enum WindowFlag
{
    /**
     * Draws a border around the window to visually separate window from the background
     */
    BORDER(NK_WINDOW_BORDER),
    
    /**
     * The movable flag indicates that a window can be moved by user input or by dragging the window header
     */
    MOVABLE(NK_WINDOW_MOVABLE),
    
    /**
     * The scalable flag indicates that a window can be scaled by user input by dragging a scaler icon at the button of the window
     */
    SCALABLE(NK_WINDOW_SCALABLE),
    
    /**
     * Adds a closable icon into the header
     */
    CLOSABLE(NK_WINDOW_CLOSABLE),
    
    /**
     * Adds a minimize icon into the header
     */
    MINIMIZABLE(NK_WINDOW_MINIMIZABLE),
    
    /**
     * Removes the scrollbar from the window
     */
    NO_SCROLLBAR(NK_WINDOW_NO_SCROLLBAR),
    
    /**
     * Forces a header at the top at the window showing the title
     */
    TITLE(NK_WINDOW_TITLE),
    
    // TODO - Need to set ctx.delta_time_seconds before this will work
    // Setting ctx.delta_time_seconds not implemented in LWJGL
    /**
     * Automatically hides the window scrollbar if no user interaction: also requires delta time in nk_context to be set each frame
     */
    SCROLL_AUTO_HIDE(NK_WINDOW_SCROLL_AUTO_HIDE),
    
    /**
     * Always keep window in the background
     */
    BACKGROUND(NK_WINDOW_BACKGROUND),
    
    /**
     * Puts window scaler in the left-bottom corner instead right-bottom
     */
    SCALE_LEFT(NK_WINDOW_SCALE_LEFT),
    
    // TODO - Toggling this will crash
    /**
     * Prevents window of scaling, moving or getting focus
     */
    NO_INPUT(NK_WINDOW_NO_INPUT),
    ;
    
    final int value;
    
    WindowFlag(int value)
    {
        this.value = value;
    }
}
