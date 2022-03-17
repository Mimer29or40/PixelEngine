package pe.event;

import pe.Window;

public interface EventMouseEntered extends EventMouse
{
    @EventProperty
    boolean entered();
    
    final class _EventMouseEntered extends AbstractEventMouse implements EventMouseEntered
    {
        private final boolean entered;
        
        private _EventMouseEntered(long time, Window window, boolean entered)
        {
            super(time, window);
            
            this.entered = entered;
        }
        
        @Override
        public boolean entered()
        {
            return this.entered;
        }
    }
    
    static EventMouseEntered create(long time, Window window, boolean entered)
    {
        return new _EventMouseEntered(time, window, entered);
    }
}
