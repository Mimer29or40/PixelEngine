package pe.event;

import pe.Window;

abstract class AbstractEventKeyboard extends AbstractEventInputDevice implements EventKeyboard
{
    private final Window window;
    
    AbstractEventKeyboard(long time, Window window)
    {
        super(time);
        
        this.window = window;
    }
    
    @Override
    public Window window()
    {
        return this.window;
    }
}
