package pe.event;

public interface EventWindowVsyncChanged extends EventWindow
{
    @EventProperty
    boolean vsync();
    
    final class _EventWindowVsyncChanged extends AbstractEventWindow implements EventWindowVsyncChanged
    {
        private final boolean vsync;
        
        private _EventWindowVsyncChanged(long time, boolean vsync)
        {
            super(time);
            
            this.vsync = vsync;
        }
        
        @Override
        public boolean vsync()
        {
            return this.vsync;
        }
    }
    
    static EventWindowVsyncChanged create(long time, boolean maximized)
    {
        return new _EventWindowVsyncChanged(time, maximized);
    }
}
