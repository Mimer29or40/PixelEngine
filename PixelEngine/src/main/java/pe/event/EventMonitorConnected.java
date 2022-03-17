package pe.event;

import pe.Monitor;

public interface EventMonitorConnected extends EventMonitor
{
    final class _EventMonitorConnected extends AbstractEventMonitor implements EventMonitorConnected
    {
        _EventMonitorConnected(long time, Monitor monitor)
        {
            super(time, monitor);
        }
    }
    
    static EventMonitorConnected create(long time, Monitor monitor)
    {
        return new _EventMonitorConnected(time, monitor);
    }
}
