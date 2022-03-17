package pe.event;

import pe.Keyboard;
import pe.Window;

public interface EventKeyboardKeyHeld extends EventInputDeviceInputHeld, EventKeyboardKey
{
    final class _EventKeyboardKeyHeld extends AbstractEventKeyboardKey implements EventKeyboardKeyHeld
    {
        private _EventKeyboardKeyHeld(long time, Window window, Keyboard.Key key)
        {
            super(time, window, key);
        }
    }
    
    static EventKeyboardKeyHeld create(long time, Window window, Keyboard.Key key)
    {
        return new _EventKeyboardKeyHeld(time, window, key);
    }
}
