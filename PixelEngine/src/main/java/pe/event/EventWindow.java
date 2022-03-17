package pe.event;

import pe.Window;

public interface EventWindow extends Event
{
    @EventProperty(printName = false)
    Window window();
}
