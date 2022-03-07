package pe.event;

import pe.Joystick;

public interface EventJoystickButtonHeld extends EventInputDeviceInputHeld, EventJoystickButton
{
    final class _EventJoystickButtonHeld extends AbstractEventJoystickButton implements EventJoystickButtonHeld
    {
        private _EventJoystickButtonHeld(long time, Joystick.Index joystick, Joystick.Button button)
        {
            super(time, joystick, button);
        }
    }
    
    static EventJoystickButtonHeld create(long time, Joystick.Index joystick, Joystick.Button button)
    {
        return new _EventJoystickButtonHeld(time, joystick, button);
    }
}
