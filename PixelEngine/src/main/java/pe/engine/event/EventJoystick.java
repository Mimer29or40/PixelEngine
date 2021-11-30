package pe.engine.event;

import pe.engine.Joystick;

public interface EventJoystick extends EventInputDevice
{
    @EventProperty(printName = false)
    Joystick joystick();
}
