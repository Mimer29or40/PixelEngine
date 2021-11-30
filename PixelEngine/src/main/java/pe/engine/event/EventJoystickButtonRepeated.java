package pe.engine.event;

import pe.engine.Joystick;

public interface EventJoystickButtonRepeated extends EventInputDeviceInputRepeated, EventJoystickButton
{
    final class _EventJoystickButtonRepeated extends AbstractEventJoystickButton implements EventJoystickButtonRepeated
    {
        private _EventJoystickButtonRepeated(long time, Joystick joystick, Joystick.Button button)
        {
            super(time, joystick, button);
        }
    }
    
    static EventJoystickButtonRepeated create(long time, Joystick joystick, Joystick.Button button)
    {
        return new _EventJoystickButtonRepeated(time, joystick, button);
    }
}
