package pe.event;

import pe.Joystick;

public interface EventJoystickConnected extends EventJoystick
{
    final class _EventJoystickConnected extends AbstractEventJoystick implements EventJoystickConnected
    {
        private _EventJoystickConnected(long time, Joystick.Index joystick)
        {
            super(time, joystick);
        }
    }
    
    static EventJoystickConnected create(long time, Joystick.Index joystick)
    {
        return new _EventJoystickConnected(time, joystick);
    }
}
