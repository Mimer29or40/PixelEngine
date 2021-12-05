package pe.event;

import pe.Keyboard;

public interface EventKeyboardKeyHeld extends EventInputDeviceInputHeld, EventKeyboardKey
{
    final class _EventKeyboardKeyHeld extends AbstractEventKeyboardKey implements EventKeyboardKeyHeld
    {
        private _EventKeyboardKeyHeld(long time, Keyboard.Key key)
        {
            super(time, key);
        }
    }
    
    static EventKeyboardKeyHeld create(long time, Keyboard.Key key)
    {
        return new _EventKeyboardKeyHeld(time, key);
    }
}
