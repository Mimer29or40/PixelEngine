package pe.engine.event;

public interface EventWindowClosed extends EventWindow
{
    final class _EventWindowClosed extends AbstractEventWindow implements EventWindowClosed
    {
        _EventWindowClosed(long time)
        {
            super(time);
        }
    }
    
    static EventWindowClosed create(long time)
    {
        return new _EventWindowClosed(time);
    }
}
