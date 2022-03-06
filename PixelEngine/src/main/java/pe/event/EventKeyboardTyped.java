package pe.event;

public interface EventKeyboardTyped extends EventKeyboard
{
    @EventProperty
    int codePoint();
    
    @EventProperty
    String typed();
    
    final class _EventKeyboardTyped extends AbstractEventInputDevice implements EventKeyboardTyped
    {
        private final int    codePoint;
        private final String typed;
        
        private _EventKeyboardTyped(long time, int codePoint)
        {
            super(time);
            
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
    
    static EventKeyboardTyped create(long time, int codePoint)
    {
        return new _EventKeyboardTyped(time, codePoint);
    }
}
