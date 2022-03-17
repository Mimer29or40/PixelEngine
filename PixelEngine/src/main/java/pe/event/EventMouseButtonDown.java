package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

public interface EventMouseButtonDown extends EventInputDeviceInputDown, EventMouseButton
{
    final class _EventMouseButtonDown extends AbstractEventMouseButton implements EventMouseButtonDown
    {
        private final int downCount;
        
        private _EventMouseButtonDown(long time, Window window, Mouse.Button button, Vector2dc pos, int downCount)
        {
            super(time, window, button, pos);
            
            this.downCount = downCount;
        }
        
        @Override
        public int downCount()
        {
            return this.downCount;
        }
    }
    
    static EventMouseButtonDown create(long time, Window window, Mouse.Button button, Vector2dc pos, int downCount)
    {
        return new _EventMouseButtonDown(time, window, button, pos, downCount);
    }
}
