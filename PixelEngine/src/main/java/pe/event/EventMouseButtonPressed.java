package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonPressed extends EventInputDeviceInputPressed, EventMouseButton
{
    final class _EventMouseButtonPressed extends AbstractEventMouseButton implements EventMouseButtonPressed
    {
        private final boolean doublePressed;
        
        private _EventMouseButtonPressed(long time, Mouse.Button button, Vector2dc pos, boolean doublePressed)
        {
            super(time, button, pos);
            
            this.doublePressed = doublePressed;
        }
        
        @Override
        public boolean doublePressed()
        {
            return this.doublePressed;
        }
    }
    
    static EventMouseButtonPressed create(long time, Mouse.Button button, Vector2dc pos, boolean doublePressed)
    {
        return new _EventMouseButtonPressed(time, button, pos, doublePressed);
    }
}
