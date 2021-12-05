package pe.event;

public interface EventMouseEntered extends EventMouse
{
    @EventProperty
    boolean entered();
    
    final class _EventMouseEntered extends AbstractEventInputDevice implements EventMouseEntered
    {
        private final boolean entered;
        
        private _EventMouseEntered(long time, boolean entered)
        {
            super(time);
            
            this.entered = entered;
        }
        
        @Override
        public boolean entered()
        {
            return this.entered;
        }
    }
    
    static EventMouseEntered create(long time, boolean entered)
    {
        return new _EventMouseEntered(time, entered);
    }
}
