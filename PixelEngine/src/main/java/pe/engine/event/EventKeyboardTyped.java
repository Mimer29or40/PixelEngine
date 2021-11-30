package pe.engine.event;

public interface EventKeyboardTyped extends EventKeyboard
{
    @EventProperty
    String typed();
    
    final class _EventKeyboardTyped extends AbstractEventInputDevice implements EventKeyboardTyped
    {
        private final String typed;
        
        private _EventKeyboardTyped(long time, String typed)
        {
            super(time);
            
            this.typed = typed;
        }
        
        @Override
        public String typed()
        {
            return this.typed;
        }
    }
    
    static EventKeyboardTyped create(long time, String typed)
    {
        return new _EventKeyboardTyped(time, typed);
    }
}
