package pe.event;

import pe.Window;

public interface EventWindowMaximized extends EventWindow
{
    @EventProperty
    boolean maximized();
    
    final class _EventWindowMaximized extends AbstractEventWindow implements EventWindowMaximized
    {
        private final boolean maximized;
        
        private _EventWindowMaximized(long time, Window window, boolean maximized)
        {
            super(time, window);
            
            this.maximized = maximized;
        }
        
        @Override
        public boolean maximized()
        {
            return this.maximized;
        }
    }
    
    static EventWindowMaximized create(long time, Window window, boolean maximized)
    {
        return new _EventWindowMaximized(time, window, maximized);
    }
}
