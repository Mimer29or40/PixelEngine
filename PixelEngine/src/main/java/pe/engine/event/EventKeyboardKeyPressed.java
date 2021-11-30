package pe.engine.event;

import pe.engine.Keyboard;

public interface EventKeyboardKeyPressed extends EventInputDeviceInputPressed, EventKeyboardKey
{
    final class _EventKeyboardKeyPressed extends AbstractEventKeyboardKey implements EventKeyboardKeyPressed
    {
        private final boolean doublePressed;
        
        private _EventKeyboardKeyPressed(long time, Keyboard.Key key, boolean doublePressed)
        {
            super(time, key);
            
            this.doublePressed = doublePressed;
        }
        
        @Override
        public boolean doublePressed()
        {
            return this.doublePressed;
        }
    }
    
    static EventKeyboardKeyPressed create(long time, Keyboard.Key key, boolean doublePressed)
    {
        return new _EventKeyboardKeyPressed(time, key, doublePressed);
    }
}
