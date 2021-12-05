package pe.event;

import pe.Joystick;

public interface EventJoystickButtonUp extends EventInputDeviceInputUp, EventJoystickButton
{
    final class _EventJoystickButtonUp extends AbstractEventJoystickButton implements EventJoystickButtonUp
    {
        private _EventJoystickButtonUp(long time, Joystick joystick, Joystick.Button button)
        {
            super(time, joystick, button);
        }
    }
    
    static EventJoystickButtonUp create(long time, Joystick joystick, Joystick.Button button)
    {
        return new _EventJoystickButtonUp(time, joystick, button);
    }
}
