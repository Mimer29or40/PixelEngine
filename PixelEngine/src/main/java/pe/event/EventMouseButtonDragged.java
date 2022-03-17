package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Mouse;
import pe.Window;

public interface EventMouseButtonDragged extends EventMouseButton
{
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
    
    @EventProperty
    Vector2dc dragStart();
    
    default double dragStartX()
    {
        return dragStart().x();
    }
    
    default double dragStartY()
    {
        return dragStart().y();
    }
    
    final class _EventMouseButtonDragged extends AbstractEventMouseButton implements EventMouseButtonDragged
    {
        private final Vector2d rel;
        private final Vector2d dragStart;
        
        private _EventMouseButtonDragged(long time, Window window, Mouse.Button button, Vector2dc pos, Vector2dc rel, Vector2dc dragStart)
        {
            super(time, window, button, pos);
            
            this.rel       = new Vector2d(rel);
            this.dragStart = new Vector2d(dragStart);
        }
        
        @Override
        public Vector2dc rel()
        {
            return this.rel;
        }
        
        @Override
        public Vector2dc dragStart()
        {
            return this.dragStart;
        }
    }
    
    static EventMouseButtonDragged create(long time, Window window, Mouse.Button button, Vector2dc pos, Vector2dc rel, Vector2dc dragStart)
    {
        return new _EventMouseButtonDragged(time, window, button, pos, rel, dragStart);
    }
}
