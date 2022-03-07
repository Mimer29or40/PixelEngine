package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButton extends EventInputDeviceInput, EventMouse
{
    @EventProperty(printName = false)
    Mouse.Button button();
    
    @EventProperty
    Vector2dc absPos();
    
    default double absX()
    {
        return absPos().x();
    }
    
    default double absY()
    {
        return absPos().y();
    }
    
    @EventProperty
    Vector2dc pos();
    
    default double x()
    {
        return pos().x();
    }
    
    default double y()
    {
        return pos().y();
    }
}
