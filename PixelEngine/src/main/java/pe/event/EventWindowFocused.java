package pe.event;

import pe.Window;

public interface EventWindowFocused extends EventWindow
{
    @EventProperty
    boolean focused();
    
    final class _EventWindowFocused extends AbstractEventWindow implements EventWindowFocused
    {
        private final boolean focused;
        
        private _EventWindowFocused(long time, Window window, boolean focused)
        {
            super(time, window);
            
            this.focused = focused;
        }
        
        @Override
        public boolean focused()
        {
            return this.focused;
        }
    }
    
    static EventWindowFocused create(long time, Window window, boolean focused)
    {
        return new _EventWindowFocused(time, window, focused);
    }
}
