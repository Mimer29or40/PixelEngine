package pe.event;

import pe.Joystick;

public interface EventJoystickButton extends EventInputDeviceInput, EventJoystick
{
    @EventProperty
    Joystick.Button button();
}
