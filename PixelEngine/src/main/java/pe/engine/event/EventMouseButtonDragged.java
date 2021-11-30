package pe.engine.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.engine.Mouse;

public interface EventMouseButtonDragged extends EventMouseButton
{
    @EventProperty
    Vector2dc rel();
    
    default double dx()
    {
        return this.rel().x();
    }
    
    default double dy()
    {
        return this.rel().y();
    }
    
    @EventProperty
    Vector2dc dragStart();
    
    default double dragStartX()
    {
        return this.dragStart().x();
    }
    
    default double dragStartY()
    {
        return this.dragStart().y();
    }
    
    final class _EventMouseButtonDragged extends AbstractEventMouseButton implements EventMouseButtonDragged
    {
        private final Vector2d rel;
        private final Vector2d dragStart;
        
        private _EventMouseButtonDragged(long time, Mouse.Button button, Vector2dc pos, Vector2dc rel, Vector2dc dragStart)
        {
            super(time, button, pos);
            
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
    
    static EventMouseButtonDragged create(long time, Mouse.Button button, Vector2dc pos, Vector2dc rel, Vector2dc dragStart)
    {
        return new _EventMouseButtonDragged(time, button, pos, rel, dragStart);
    }
}
