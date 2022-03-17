package pe.event;

import pe.Window;

public interface EventKeyboardTyped extends EventKeyboard
{
    @EventProperty
    int codePoint();
    
    @EventProperty
    String typed();
    
    final class _EventKeyboardTyped extends AbstractEventKeyboard implements EventKeyboardTyped
    {
        private final int    codePoint;
        private final String typed;
        
        private _EventKeyboardTyped(long time, Window window, int codePoint)
        {
            super(time, window);
            
            this.codePoint = codePoint;
            this.typed     = Character.toString(codePoint);
        }
        
        @Override
        public int codePoint()
        {
            return this.codePoint;
        }
        
        @Override
        public String typed()
        {
            return this.typed;
        }
    }
    
    static EventKeyboardTyped create(long time, Window window, int codePoint)
    {
        return new _EventKeyboardTyped(time, window, codePoint);
    }
}
