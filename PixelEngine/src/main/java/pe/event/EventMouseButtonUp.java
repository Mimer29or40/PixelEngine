package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

public interface EventMouseButtonUp extends EventInputDeviceInputUp, EventMouseButton
{
    final class _EventMouseButtonUp extends AbstractEventMouseButton implements EventMouseButtonUp
    {
        private _EventMouseButtonUp(long time, Window window, Mouse.Button button, Vector2dc pos)
        {
            super(time, window, button, pos);
        }
    }
    
    static EventMouseButtonUp create(long time, Window window, Mouse.Button button, Vector2dc pos)
    {
        return new _EventMouseButtonUp(time, window, button, pos);
    }
}
