package pe.debug;

import pe.shape.AABB2i;

public abstract class DebugElement
{
    public final AABB2i rect = new AABB2i();
    
    public abstract void handleEvents();
    
    public abstract void draw(int contentX, int contentY, int contentW, int contentH);
}
