package pe.gui;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.nuklear.NkContext;

public class GUILayoutSpace extends GUILayout // TODO
{
    // nk_layout_space_begin           Begins a new layouting space that allows to specify each widgets position and size
    // nk_layout_space_push            Pushes position and size of the next widget in own coordinate space either as pixel or ratio
    // nk_layout_space_end             Marks the end of the layouting space
    // nk_layout_space_bounds          Callable after nk_layout_space_begin and returns total space allocated
    // nk_layout_space_to_screen       Converts vector from nk_layout_space coordinate space into screen space
    // nk_layout_space_to_local        Converts vector from screen space into nk_layout_space coordinates
    // nk_layout_space_rect_to_screen  Converts rectangle from nk_layout_space coordinate space into screen space
    // nk_layout_space_rect_to_local   Converts rectangle from screen space into nk_layout_space coordinates
    
    @Override
    public void layout(@NotNull NkContext ctx)
    {
    
    }
}
