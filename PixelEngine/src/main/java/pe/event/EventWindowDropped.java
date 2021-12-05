package pe.event;

import java.nio.file.Path;

public interface EventWindowDropped extends EventWindow
{
    @EventProperty
    Path[] paths();
    
    final class _EventWindowDropped extends AbstractEventWindow implements EventWindowDropped
    {
        private final Path[] paths;
        
        private _EventWindowDropped(long time, Path[] paths)
        {
            super(time);
            
            this.paths = paths;
        }
        
        @Override
        public Path[] paths()
        {
            return this.paths;
        }
    }
    
    static EventWindowDropped create(long time, Path[] paths)
    {
        return new _EventWindowDropped(time, paths);
    }
}
