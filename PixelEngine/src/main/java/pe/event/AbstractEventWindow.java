package pe.event;

abstract class AbstractEventWindow extends AbstractEvent implements EventWindow
{
    AbstractEventWindow(long time)
    {
        super(time);
    }
}
