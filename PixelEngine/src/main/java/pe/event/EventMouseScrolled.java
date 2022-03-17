package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Window;

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
    
    final class _EventMouseScrolled extends AbstractEventMouse implements EventMouseScrolled
    {
        private final Vector2d scroll;
        
        private _EventMouseScrolled(long time, Window window, Vector2dc scroll)
        {
            super(time, window);
            
            this.scroll = new Vector2d(scroll);
        }
        
        @Override
        public Vector2dc scroll()
        {
            return this.scroll;
        }
    }
    
    static EventMouseScrolled create(long time, Window window, Vector2dc scroll)
    {
        return new _EventMouseScrolled(time, window, scroll);
    }
}
