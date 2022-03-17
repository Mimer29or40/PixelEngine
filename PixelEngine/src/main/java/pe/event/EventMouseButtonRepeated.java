package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

public interface EventMouseButtonRepeated extends EventInputDeviceInputRepeated, EventMouseButton
{
    final class _EventMouseButtonRepeated extends AbstractEventMouseButton implements EventMouseButtonRepeated
    {
        private _EventMouseButtonRepeated(long time, Window window, Mouse.Button button, Vector2dc pos)
        {
            super(time, window, button, pos);
        }
    }
    
    static EventMouseButtonRepeated create(long time, Window window, Mouse.Button button, Vector2dc pos)
    {
        return new _EventMouseButtonRepeated(time, window, button, pos);
    }
}
