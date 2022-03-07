package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonUp extends EventInputDeviceInputUp, EventMouseButton
{
    final class _EventMouseButtonUp extends AbstractEventMouseButton implements EventMouseButtonUp
    {
        private _EventMouseButtonUp(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos)
        {
            super(time, button, absPos, pos);
        }
    }
    
    static EventMouseButtonUp create(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos)
    {
        return new _EventMouseButtonUp(time, button, absPos, pos);
    }
}
