package pe.event;

import pe.Joystick;

public interface EventJoystick extends EventInputDevice
{
    @EventProperty(printName = false)
    Joystick.Index joystick();
}
