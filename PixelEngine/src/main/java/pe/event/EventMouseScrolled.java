package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;

public interface EventMouseScrolled extends EventMouse
{
    @EventProperty
    Vector2dc scroll();
    
    default double dx()
    {
        return scroll().x();
    }
    
    default double dy()
    {
        return scroll().y();
    }
    
    final class _EventMouseScrolled extends AbstractEventInputDevice implements EventMouseScrolled
    {
        private final Vector2d scroll;
        
        private _EventMouseScrolled(long time, Vector2dc scroll)
        {
            super(time);
            
            this.scroll = new Vector2d(scroll);
        }
        
        @Override
        public Vector2dc scroll()
        {
            return this.scroll;
        }
    }
    
    static EventMouseScrolled create(long time, Vector2dc scroll)
    {
        return new _EventMouseScrolled(time, scroll);
    }
}
