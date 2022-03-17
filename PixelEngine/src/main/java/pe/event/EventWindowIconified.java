package pe.event;

import pe.Window;

public interface EventWindowIconified extends EventWindow
{
    @EventProperty
    boolean iconified();
    
    final class _EventWindowIconified extends AbstractEventWindow implements EventWindowIconified
    {
        private final boolean iconified;
        
        private _EventWindowIconified(long time, Window window, boolean iconified)
        {
            super(time, window);
            
            this.iconified = iconified;
        }
        
        @Override
        public boolean iconified()
        {
            return this.iconified;
        }
    }
    
    static EventWindowIconified create(long time, Window window, boolean iconified)
    {
        return new _EventWindowIconified(time, window, iconified);
    }
}
