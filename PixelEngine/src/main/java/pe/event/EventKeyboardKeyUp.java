package pe.event;

import pe.Keyboard;
import pe.Window;

public interface EventKeyboardKeyUp extends EventInputDeviceInputUp, EventKeyboardKey
{
    final class _EventKeyboardKeyUp extends AbstractEventKeyboardKey implements EventKeyboardKeyUp
    {
        private _EventKeyboardKeyUp(long time, Window window, Keyboard.Key key)
        {
            super(time, window, key);
        }
    }
    
    static EventKeyboardKeyUp create(long time, Window window, Keyboard.Key key)
    {
        return new _EventKeyboardKeyUp(time, window, key);
    }
}
