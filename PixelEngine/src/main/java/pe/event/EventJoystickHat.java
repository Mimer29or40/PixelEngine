package pe.event;

import pe.Joystick;

public interface EventJoystickHat extends EventJoystick
{
    @EventProperty
    Joystick.Hat hat();
    
    @EventProperty
    Joystick.HatDirection state();
    
    final class _EventJoystickHat extends AbstractEventJoystick implements EventJoystickHat
    {
        private final Joystick.Hat          hat;
        private final Joystick.HatDirection state;
        
        private _EventJoystickHat(long time, Joystick.Index joystick, Joystick.Hat hat, Joystick.HatDirection state)
        {
            super(time, joystick);
            
            this.hat   = hat;
            this.state = state;
        }
        
        @Override
        public Joystick.Hat hat()
        {
            return this.hat;
        }
        
        @Override
        public Joystick.HatDirection state()
        {
            return this.state;
        }
    }
    
    static EventJoystickHat create(long time, Joystick.Index joystick, Joystick.Hat hat, Joystick.HatDirection state)
    {
        return new _EventJoystickHat(time, joystick, hat, state);
    }
}
