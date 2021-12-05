package pe.event;

import pe.Keyboard;

public interface EventKeyboardKey extends EventInputDeviceInput, EventKeyboard
{
    @EventProperty(printName = false)
    Keyboard.Key key();
}
