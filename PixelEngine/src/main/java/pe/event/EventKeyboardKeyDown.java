package pe.event;

import pe.Keyboard;

public interface EventKeyboardKeyDown extends EventInputDeviceInputDown, EventKeyboardKey
{
    final class _EventKeyboardKeyDown extends AbstractEventKeyboardKey implements EventKeyboardKeyDown
    {
        private _EventKeyboardKeyDown(long time, Keyboard.Key key)
        {
            super(time, key);
        }
    }
    
    static EventKeyboardKeyDown create(long time, Keyboard.Key key)
    {
        return new _EventKeyboardKeyDown(time, key);
    }
}
