package pe.event;

import pe.Joystick;

abstract class AbstractEventJoystick extends AbstractEventInputDevice implements EventJoystick
{
    private final Joystick joystick;
    
    AbstractEventJoystick(long time, Joystick joystick)
    {
        super(time);
        
        this.joystick = joystick;
    }
    
    @Override
    public Joystick joystick()
    {
        return this.joystick;
    }
}