package pe.event;

import pe.Keyboard;
import pe.Window;

public interface EventKeyboardKeyDown extends EventInputDeviceInputDown, EventKeyboardKey
{
    final class _EventKeyboardKeyDown extends AbstractEventKeyboardKey implements EventKeyboardKeyDown
    {
        private final int downCount;
        
        private _EventKeyboardKeyDown(long time, Window window, Keyboard.Key key, int downCount)
        {
            super(time, window, key);
            
            this.downCount = downCount;
        }
        
        @Override
        public int downCount()
        {
            return this.downCount;
        }
    }
    
    static EventKeyboardKeyDown create(long time, Window window, Keyboard.Key key, int downCount)
    {
        return new _EventKeyboardKeyDown(time, window, key, downCount);
    }
}
