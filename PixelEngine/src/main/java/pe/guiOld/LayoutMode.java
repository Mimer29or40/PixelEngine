package pe.guiOld;

import static org.lwjgl.nuklear.Nuklear.NK_DYNAMIC;
import static org.lwjgl.nuklear.Nuklear.NK_STATIC;

public enum LayoutMode
{
    DYNAMIC(NK_DYNAMIC),
    STATIC(NK_STATIC),
    VARIABLE(NK_STATIC),
    ;
    
    final int value;
    
    LayoutMode(int value)
    {
        this.value = value;
    }
}
