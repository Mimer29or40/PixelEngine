package pe.event;

public interface Event
{
    @EventProperty(format = "%.3f")
    double time();
    
    @EventProperty(printName = false)
    boolean consumed();
    
    void consume();
}
