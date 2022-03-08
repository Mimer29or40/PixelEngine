package pe.event;

import pe.Keyboard;

public interface EventKeyboardKeyDown extends EventInputDeviceInputDown, EventKeyboardKey
{
    final class _EventKeyboardKeyDown extends AbstractEventKeyboardKey implements EventKeyboardKeyDown
    {
        private final int downCount;
        
        private _EventKeyboardKeyDown(long time, Keyboard.Key key, int downCount)
        {
            super(time, key);
            
            this.downCount = downCount;
        }
        
        @Override
        public int downCount()
        {
            return this.downCount;
        }
    }
    
    static EventKeyboardKeyDown create(long time, Keyboard.Key key, int downCount)
    {
        return new _EventKeyboardKeyDown(time, key, downCount);
    }
}
