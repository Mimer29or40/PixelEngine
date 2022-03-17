package pe.event;

import pe.Window;

public interface EventWindowClosed extends EventWindow
{
    final class _EventWindowClosed extends AbstractEventWindow implements EventWindowClosed
    {
        _EventWindowClosed(long time, Window window)
        {
            super(time, window);
        }
    }
    
    static EventWindowClosed create(long time, Window window)
    {
        return new _EventWindowClosed(time, window);
    }
}
