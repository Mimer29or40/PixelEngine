package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonRepeated extends EventInputDeviceInputRepeated, EventMouseButton
{
    final class _EventMouseButtonRepeated extends AbstractEventMouseButton implements EventMouseButtonRepeated
    {
        private _EventMouseButtonRepeated(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos)
        {
            super(time, button, absPos, pos);
        }
    }
    
    static EventMouseButtonRepeated create(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos)
    {
        return new _EventMouseButtonRepeated(time, button, absPos, pos);
    }
}
