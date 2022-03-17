package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Window;

public interface EventMouseMoved extends EventMouse
{
    @EventProperty
    Vector2dc pos();
    
    default double x()
    {
        return pos().x();
    }
    
    default double y()
    {
        return pos().y();
    }
    
    @EventProperty
    Vector2dc rel();
    
    default double dx()
    {
        return rel().x();
    }
    
    default double dy()
    {
        return rel().y();
    }
    
    final class _EventMouseMoved extends AbstractEventMouse implements EventMouseMoved
    {
        private final Vector2d pos;
        private final Vector2d rel;
        
        private _EventMouseMoved(long time, Window window, Vector2dc pos, Vector2dc rel)
        {
            super(time, window);
            
            this.pos = new Vector2d(pos);
            this.rel = new Vector2d(rel);
        }
        
        @Override
        public Vector2dc pos()
        {
            return this.pos;
        }
        
        @Override
        public Vector2dc rel()
        {
            return this.rel;
        }
    }
    
    static EventMouseMoved create(long time, Window window, Vector2dc pos, Vector2dc rel)
    {
        return new _EventMouseMoved(time, window, pos, rel);
    }
}
