package pe.event;

import pe.Joystick;

public interface EventJoystickButtonDown extends EventInputDeviceInputDown, EventJoystickButton
{
    final class _EventJoystickButtonDown extends AbstractEventJoystickButton implements EventJoystickButtonDown
    {
        private final int downCount;
        
        private _EventJoystickButtonDown(long time, Joystick.Index joystick, Joystick.Button button, int downCount)
        {
            super(time, joystick, button);
            
            this.downCount = downCount;
        }
        
        @Override
        public int downCount()
        {
            return this.downCount;
        }
    }
    
    static EventJoystickButtonDown create(long time, Joystick.Index joystick, Joystick.Button button, int downCount)
    {
        return new _EventJoystickButtonDown(time, joystick, button, downCount);
    }
}
