package pe.event;

import pe.Joystick;

public interface EventJoystickButtonDown extends EventInputDeviceInputDown, EventJoystickButton
{
    final class _EventJoystickButtonDown extends AbstractEventJoystickButton implements EventJoystickButtonDown
    {
        private _EventJoystickButtonDown(long time, Joystick.Index joystick, Joystick.Button button)
        {
            super(time, joystick, button);
        }
    }
    
    static EventJoystickButtonDown create(long time, Joystick.Index joystick, Joystick.Button button)
    {
        return new _EventJoystickButtonDown(time, joystick, button);
    }
}
