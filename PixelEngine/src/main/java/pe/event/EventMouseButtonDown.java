package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonDown extends EventInputDeviceInputDown, EventMouseButton
{
    final class _EventMouseButtonDown extends AbstractEventMouseButton implements EventMouseButtonDown
    {
        private _EventMouseButtonDown(long time, Mouse.Button button, Vector2dc pos)
        {
            super(time, button, pos);
        }
    }
    
    static EventMouseButtonDown create(long time, Mouse.Button button, Vector2dc pos)
    {
        return new _EventMouseButtonDown(time, button, pos);
    }
}
