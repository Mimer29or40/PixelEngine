package pe.event;

import pe.Keyboard;
import pe.Window;

public interface EventKeyboardKeyRepeated extends EventInputDeviceInputRepeated, EventKeyboardKey
{
    final class _EventKeyboardKeyRepeated extends AbstractEventKeyboardKey implements EventKeyboardKeyRepeated
    {
        private _EventKeyboardKeyRepeated(long time, Window window, Keyboard.Key key)
        {
            super(time, window, key);
        }
    }
    
    static EventKeyboardKeyRepeated create(long time, Window window, Keyboard.Key key)
    {
        return new _EventKeyboardKeyRepeated(time, window, key);
    }
}
