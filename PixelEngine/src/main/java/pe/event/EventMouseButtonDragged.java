package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.Mouse;

public interface EventMouseButtonDragged extends EventMouseButton
{
    @EventProperty
    Vector2dc absRel();
    
    default double absDx()
    {
        return absRel().x();
    }
    
    default double absDy()
    {
        return absRel().y();
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
    
    @EventProperty
    Vector2dc absDragStart();
    
    default double absDragStartX()
    {
        return absDragStart().x();
    }
    
    default double absDragStartY()
    {
        return absDragStart().y();
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
        private final Vector2d absRel;
        private final Vector2d rel;
        private final Vector2d absDragStart;
        private final Vector2d dragStart;
        
        private _EventMouseButtonDragged(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos, Vector2dc absRel, Vector2dc rel, Vector2dc absDragStart, Vector2dc dragStart)
        {
            super(time, button, absPos, pos);
            
            this.rel       = new Vector2d(rel);
            this.absRel    = new Vector2d(absRel);
            this.absDragStart = new Vector2d(absDragStart);
            this.dragStart = new Vector2d(dragStart);
        }
        
        @Override
        public Vector2dc absRel()
        {
            return this.absRel;
        }
        
        @Override
        public Vector2dc rel()
        {
            return this.rel;
        }
        
        @Override
        public Vector2dc absDragStart()
        {
            return this.absDragStart;
        }
        
        @Override
        public Vector2dc dragStart()
        {
            return this.dragStart;
        }
    }
    
    static EventMouseButtonDragged create(long time, Mouse.Button button, Vector2dc absPos, Vector2dc pos, Vector2dc absRel, Vector2dc rel, Vector2dc absDragStart, Vector2dc dragStart)
    {
        return new _EventMouseButtonDragged(time, button, absPos, pos, absRel, rel, absDragStart, dragStart);
    }
}
