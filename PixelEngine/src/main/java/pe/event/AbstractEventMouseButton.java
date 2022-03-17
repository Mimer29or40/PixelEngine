package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

abstract class AbstractEventMouseButton extends AbstractEventMouse implements EventMouseButton
{
    private final Mouse.Button button;
    private final Vector2d     pos;
    
    AbstractEventMouseButton(long time, Window window, Mouse.Button button, Vector2dc pos)
    {
        super(time, window);
        
        this.button = button;
        this.pos    = new Vector2d(pos);
    }
    
    @Override
    public Mouse.Button button()
    {
        return this.button;
    }
    
    @Override
    public Vector2dc pos()
    {
        return this.pos;
    }
}
