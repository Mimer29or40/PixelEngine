package pe.event;

import pe.Monitor;

abstract class AbstractEventMonitor extends AbstractEvent implements EventMonitor
{
    private final Monitor monitor;
    
    AbstractEventMonitor(long time, Monitor monitor)
    {
        super(time);
        
        this.monitor = monitor;
    }
    
    @Override
    public Monitor monitor()
    {
        return this.monitor;
    }
}
