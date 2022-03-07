package pe.event;

import pe.Joystick;

public interface EventJoystickAxis extends EventJoystick
{
    @EventProperty
    Joystick.Axis axis();
    
    @EventProperty
    double value();
    
    @EventProperty
    double delta();
    
    final class _EventJoystickAxis extends AbstractEventJoystick implements EventJoystickAxis
    {
        private final Joystick.Axis axis;
        private final double        value;
        private final double        delta;
        
        private _EventJoystickAxis(long time, Joystick.Index joystick, Joystick.Axis axis, double value, double delta)
        {
            super(time, joystick);
            
            this.axis  = axis;
            this.value = value;
            this.delta = delta;
        }
        
        @Override
        public Joystick.Axis axis()
        {
            return this.axis;
        }
        
        @Override
        public double value()
        {
            return this.value;
        }
        
        @Override
        public double delta()
        {
            return this.delta;
        }
    }
    
    static EventJoystickAxis create(long time, Joystick.Index joystick, Joystick.Axis axis, double value, double delta)
    {
        return new _EventJoystickAxis(time, joystick, axis, value, delta);
    }
}
