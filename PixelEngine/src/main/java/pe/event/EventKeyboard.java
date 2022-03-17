package pe.event;

import pe.Window;

public interface EventKeyboard extends EventInputDevice
{
    @EventProperty(printName = false)
    Window window();
}
