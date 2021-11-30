package pe.engine.event;

import pe.engine.Joystick;

public interface EventJoystickButton extends EventInputDeviceInput, EventJoystick
{
    @EventProperty
    Joystick.Button button();
}
