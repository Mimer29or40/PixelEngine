package pe.event;

import pe.Monitor;

public interface EventMonitor extends Event
{
    @EventProperty(printName = false)
    Monitor monitor();
}
