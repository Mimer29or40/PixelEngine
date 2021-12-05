package pe.event;

abstract class AbstractEventInputDevice extends AbstractEvent implements EventInputDevice
{
    AbstractEventInputDevice(long time)
    {
        super(time);
    }
}
