package pe.event;

import pe.Monitor;

public interface EventMonitorDisconnected extends EventMonitor
{
    final class _EventMonitorDisconnected extends AbstractEventMonitor implements EventMonitorDisconnected
    {
        _EventMonitorDisconnected(long time, Monitor monitor)
        {
            super(time, monitor);
        }
    }
    
    static EventMonitorDisconnected create(long time, Monitor monitor)
    {
        return new _EventMonitorDisconnected(time, monitor);
    }
}
