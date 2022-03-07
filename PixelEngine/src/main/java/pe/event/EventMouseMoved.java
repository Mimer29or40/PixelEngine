package pe.event;

import org.joml.Vector2d;
import org.joml.Vector2dc;

public interface EventMouseMoved extends EventMouse
{
    @EventProperty
    Vector2dc absPos();
    
    default double absX()
    {
        return absPos().x();
    }
    
    default double absY()
    {
        return absPos().y();
    }
    
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
    
    final class _EventMouseMoved extends AbstractEventInputDevice implements EventMouseMoved
    {
        private final Vector2d absPos;
        private final Vector2d pos;
        private final Vector2d absRel;
        private final Vector2d rel;
        
        private _EventMouseMoved(long time, Vector2dc absPos, Vector2dc pos, Vector2dc absRel, Vector2dc rel)
        {
            super(time);
            
            this.absPos = new Vector2d(absPos);
            this.pos    = new Vector2d(pos);
            this.absRel = new Vector2d(absRel);
            this.rel    = new Vector2d(rel);
        }
        
        @Override
        public Vector2dc absPos()
        {
            return this.absPos;
        }
        
        @Override
        public Vector2dc pos()
        {
            return this.pos;
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
    }
    
    static EventMouseMoved create(long time, Vector2dc absPos, Vector2dc pos, Vector2dc absRel, Vector2dc rel)
    {
        return new _EventMouseMoved(time, absPos, pos, absRel, rel);
    }
}
