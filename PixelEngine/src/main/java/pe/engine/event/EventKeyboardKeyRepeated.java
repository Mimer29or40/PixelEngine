package pe.engine.event;

import pe.engine.Keyboard;

public interface EventKeyboardKeyRepeated extends EventInputDeviceInputRepeated, EventKeyboardKey
{
    final class _EventKeyboardKeyRepeated extends AbstractEventKeyboardKey implements EventKeyboardKeyRepeated
    {
        private _EventKeyboardKeyRepeated(long time, Keyboard.Key key)
        {
            super(time, key);
        }
    }
    
    static EventKeyboardKeyRepeated create(long time, Keyboard.Key key)
    {
        return new _EventKeyboardKeyRepeated(time, key);
    }
}
