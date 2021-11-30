package pe.engine.event;

import pe.engine.Keyboard;

public interface EventKeyboardKeyUp extends EventInputDeviceInputUp, EventKeyboardKey
{
    final class _EventKeyboardKeyUp extends AbstractEventKeyboardKey implements EventKeyboardKeyUp
    {
        private _EventKeyboardKeyUp(long time, Keyboard.Key key)
        {
            super(time, key);
        }
    }
    
    static EventKeyboardKeyUp create(long time, Keyboard.Key key)
    {
        return new _EventKeyboardKeyUp(time, key);
    }
}
