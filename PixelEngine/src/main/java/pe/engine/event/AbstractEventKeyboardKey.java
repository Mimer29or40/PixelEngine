package pe.engine.event;

import pe.engine.Keyboard;

abstract class AbstractEventKeyboardKey extends AbstractEventInputDevice implements EventKeyboardKey
{
    private final Keyboard.Key key;
    
    AbstractEventKeyboardKey(long time, Keyboard.Key key)
    {
        super(time);
        
        this.key = key;
    }
    
    @Override
    public Keyboard.Key key()
    {
        return this.key;
    }
}
