package pe.event;

import pe.Joystick;

public interface EventJoystickButtonPressed extends EventInputDeviceInputPressed, EventJoystickButton
{
    final class _EventJoystickButtonPressed extends AbstractEventJoystickButton implements EventJoystickButtonPressed
    {
        private final boolean doublePressed;
        
        private _EventJoystickButtonPressed(long time, Joystick joystick, Joystick.Button button, boolean doublePressed)
        {
            super(time, joystick, button);
            
            this.doublePressed = doublePressed;
        }
        
        @Override
        public boolean doublePressed()
        {
            return this.doublePressed;
        }
    }
    
    static EventJoystickButtonPressed create(long time, Joystick joystick, Joystick.Button button, boolean doublePressed)
    {
        return new _EventJoystickButtonPressed(time, joystick, button, doublePressed);
    }
}
