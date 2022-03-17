package pe.event;

import pe.Window;

public interface EventMouse extends EventInputDevice
{
    @EventProperty(printName = false)
    Window window();
}
