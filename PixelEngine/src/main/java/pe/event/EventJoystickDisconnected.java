package pe.event;

import pe.Joystick;

public interface EventJoystickDisconnected extends EventJoystick
{
    final class _EventJoystickDisconnected extends AbstractEventJoystick implements EventJoystickDisconnected
    {
        private _EventJoystickDisconnected(long time, Joystick.Index joystick)
        {
            super(time, joystick);
        }
    }
    
    static EventJoystickDisconnected create(long time, Joystick.Index joystick)
    {
        return new _EventJoystickDisconnected(time, joystick);
    }
}
