package pe.engine.event;

import pe.engine.Joystick;

abstract class AbstractEventJoystickButton extends AbstractEventJoystick implements EventJoystickButton
{
    private final Joystick.Button button;
    
    AbstractEventJoystickButton(long time, Joystick joystick, Joystick.Button button)
    {
        super(time, joystick);
        
        this.button = button;
    }
    
    @Override
    public Joystick.Button button()
    {
        return this.button;
    }
}
