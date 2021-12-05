package pe.event;

public interface Event
{
    @EventProperty(format = "%.3f")
    double time();
}
