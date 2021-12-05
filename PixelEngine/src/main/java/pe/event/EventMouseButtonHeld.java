package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonHeld extends EventInputDeviceInputHeld, EventMouseButton
{
    final class _EventMouseButtonHeld extends AbstractEventMouseButton implements EventMouseButtonHeld
    {
        private _EventMouseButtonHeld(long time, Mouse.Button button, Vector2dc pos)
        {
            super(time, button, pos);
        }
    }
    
    static EventMouseButtonHeld create(long time, Mouse.Button button, Vector2dc pos)
    {
        return new _EventMouseButtonHeld(time, button, pos);
    }
}
