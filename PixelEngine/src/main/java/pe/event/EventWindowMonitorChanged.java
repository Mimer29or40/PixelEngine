package pe.event;

import pe.Monitor;
import pe.Window;

public interface EventWindowMonitorChanged extends EventWindow
{
    @EventProperty(printName = false)
    Monitor previous();
    
    @EventProperty(printName = false)
    Monitor current();
    
    final class _EventWindowMonitorChanged extends AbstractEventWindow implements EventWindowMonitorChanged
    {
        private final Monitor previous;
        private final Monitor current;
        
        _EventWindowMonitorChanged(long time, Window window, Monitor previous, Monitor current)
        {
            super(time, window);
            
            this.previous = previous;
            this.current  = current;
        }
        
        @Override
        public Monitor previous()
        {
            return this.previous;
        }
        
        @Override
        public Monitor current()
        {
            return this.current;
        }
    }
    
    static EventWindowMonitorChanged create(long time, Window window, Monitor previous, Monitor current)
    {
        return new _EventWindowMonitorChanged(time, window, previous, current);
    }
}
