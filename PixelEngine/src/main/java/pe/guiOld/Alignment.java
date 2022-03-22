package pe.guiOld;

import static org.lwjgl.nuklear.Nuklear.*;

public enum Alignment
{
    TOP_LEFT(NK_TEXT_ALIGN_TOP | NK_TEXT_ALIGN_LEFT),
    TOP(NK_TEXT_ALIGN_TOP | NK_TEXT_ALIGN_CENTERED),
    TOP_RIGHT(NK_TEXT_ALIGN_TOP | NK_TEXT_ALIGN_RIGHT),
    
    LEFT(NK_TEXT_ALIGN_MIDDLE | NK_TEXT_ALIGN_LEFT),
    CENTER(NK_TEXT_ALIGN_MIDDLE | NK_TEXT_ALIGN_CENTERED),
    RIGHT(NK_TEXT_ALIGN_MIDDLE | NK_TEXT_ALIGN_RIGHT),
    
    BOTTOM_LEFT(NK_TEXT_ALIGN_BOTTOM | NK_TEXT_ALIGN_LEFT),
    BOTTOM(NK_TEXT_ALIGN_BOTTOM | NK_TEXT_ALIGN_CENTERED),
    BOTTOM_RIGHT(NK_TEXT_ALIGN_BOTTOM | NK_TEXT_ALIGN_RIGHT),
    ;
    
    final int value;
    
    Alignment(int value)
    {
        this.value = value;
    }
}