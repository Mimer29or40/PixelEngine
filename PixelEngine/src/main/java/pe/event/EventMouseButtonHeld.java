package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

public interface EventMouseButtonHeld extends EventInputDeviceInputHeld, EventMouseButton
{
    final class _EventMouseButtonHeld extends AbstractEventMouseButton implements EventMouseButtonHeld
    {
        private _EventMouseButtonHeld(long time, Window window, Mouse.Button button, Vector2dc pos)
        {
            super(time, window, button, pos);
        }
    }
    
    static EventMouseButtonHeld create(long time, Window window, Mouse.Button button, Vector2dc pos)
    {
        return new _EventMouseButtonHeld(time, window, button, pos);
    }
}
