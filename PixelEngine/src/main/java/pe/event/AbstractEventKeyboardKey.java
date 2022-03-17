package pe.event;

import pe.Keyboard;
import pe.Window;

abstract class AbstractEventKeyboardKey extends AbstractEventKeyboard implements EventKeyboardKey
{
    private final Keyboard.Key key;
    
    AbstractEventKeyboardKey(long time, Window window, Keyboard.Key key)
    {
        super(time, window);
        
        this.key = key;
    }
    
    @Override
    public Keyboard.Key key()
    {
        return this.key;
    }
}
