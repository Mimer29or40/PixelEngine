package pe.event;

import pe.Window;

abstract class AbstractEventMouse extends AbstractEventInputDevice implements EventMouse
{
    private final Window window;
    
    AbstractEventMouse(long time, Window window)
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
