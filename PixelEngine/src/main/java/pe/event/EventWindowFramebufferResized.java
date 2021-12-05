package pe.event;

import org.joml.Vector2i;
import org.joml.Vector2ic;

public interface EventWindowFramebufferResized extends EventWindow
{
    @EventProperty
    Vector2ic size();
    
    default int width()
    {
        return size().x();
    }
    
    default int height()
    {
        return size().y();
    }
    
    @EventProperty
    Vector2ic rel();
    
    default int dWidth()
    {
        return rel().x();
    }
    
    default int dHeight()
    {
        return rel().y();
    }
    
    final class _EventWindowFramebufferResized extends AbstractEventWindow implements EventWindowFramebufferResized
    {
        private final Vector2i size;
        private final Vector2i rel;
        
        private _EventWindowFramebufferResized(long time, Vector2ic size, Vector2ic rel)
        {
            super(time);
            
            this.size = new Vector2i(size);
            this.rel  = new Vector2i(rel);
        }
        
        @Override
        public Vector2ic size()
        {
            return this.size;
        }
        
        @Override
        public Vector2ic rel()
        {
            return this.rel;
        }
    }
    
    static EventWindowFramebufferResized create(long time, Vector2ic size, Vector2ic rel)
    {
        return new _EventWindowFramebufferResized(time, size, rel);
    }
}
