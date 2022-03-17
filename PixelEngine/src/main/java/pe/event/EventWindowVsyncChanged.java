package pe.event;

import pe.Window;

public interface EventWindowVsyncChanged extends EventWindow
{
    @EventProperty
    boolean vsync();
    
    final class _EventWindowVsyncChanged extends AbstractEventWindow implements EventWindowVsyncChanged
    {
        private final boolean vsync;
        
        private _EventWindowVsyncChanged(long time, Window window, boolean vsync)
        {
            super(time, window);
            
            this.vsync = vsync;
        }
        
        @Override
        public boolean vsync()
        {
            return this.vsync;
        }
    }
    
    static EventWindowVsyncChanged create(long time, Window window, boolean vsync)
    {
        return new _EventWindowVsyncChanged(time, window, vsync);
    }
}
