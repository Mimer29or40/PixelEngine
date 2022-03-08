package pe.event;

import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonDown extends EventInputDeviceInputDown, EventMouseButton
{
    final class _EventMouseButtonDown extends AbstractEventMouseButton implements EventMouseButtonDown
    {
        private final int downCount;
        
        private _EventMouseButtonDown(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos, int downCount)
        {
            super(time, button, absPos, pos);
            
            this.downCount = downCount;
        }
        
        @Override
        public int downCount()
        {
            return this.downCount;
        }
    }
    
    static EventMouseButtonDown create(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos, int downCount)
    {
        return new _EventMouseButtonDown(time, button, absPos, pos, downCount);
    }
}
