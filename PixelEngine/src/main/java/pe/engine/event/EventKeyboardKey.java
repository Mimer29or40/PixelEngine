package pe.engine.event;

import pe.engine.Keyboard;

public interface EventKeyboardKey extends EventInputDeviceInput, EventKeyboard
{
    @EventProperty(printName = false)
    Keyboard.Key key();
}
