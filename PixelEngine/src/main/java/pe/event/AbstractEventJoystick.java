package pe.event;

import pe.Joystick;

abstract class AbstractEventJoystick extends AbstractEventInputDevice implements EventJoystick
{
    private final Joystick.Index joystick;
    
    AbstractEventJoystick(long time, Joystick.Index joystick)
    {
        super(time);
        
        this.joystick = joystick;
    }
    
    @Override
    public Joystick.Index joystick()
    {
        return this.joystick;
    }
}
