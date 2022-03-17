package pe.event;

import pe.Window;

public interface EventWindowRefreshed extends EventWindow
{
    final class _EventWindowRefreshed extends AbstractEventWindow implements EventWindowRefreshed
    {
        _EventWindowRefreshed(long time, Window window)
        {
            super(time, window);
        }
    }
    
    static EventWindowRefreshed create(long time, Window window)
    {
        return new _EventWindowRefreshed(time, window);
    }
}
