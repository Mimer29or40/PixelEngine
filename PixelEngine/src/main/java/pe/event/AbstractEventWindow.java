package pe.event;

import pe.Window;

abstract class AbstractEventWindow extends AbstractEvent implements EventWindow
{
    private final Window window;
    
    AbstractEventWindow(long time, Window window)
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
