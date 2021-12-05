package pe.event;

import rutils.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

abstract class AbstractEvent implements Event
{
    private static final Map<Class<? extends Event>, Set<Method>> METHOD_CACHE = new ConcurrentHashMap<>();
    
    private final double time;
    
    AbstractEvent(long time)
    {
        this.time = time / 1_000_000_000D;
    }
    
    @Override
    public String toString()
    {
        Set<Method> methods;
        synchronized (AbstractEvent.METHOD_CACHE)
        {
            methods = AbstractEvent.METHOD_CACHE.computeIfAbsent(getClass(), c -> ClassUtil.getMethods(c, m -> m.isAnnotationPresent(EventProperty.class)));
        }
        
        StringBuilder s = new StringBuilder(getClass().getSimpleName().replace("_", "")).append("{");
        
        Iterator<Method> iterator = methods.iterator();
        while (true)
        {
            Method        method   = iterator.next();
            EventProperty property = method.getAnnotation(EventProperty.class);
            
            if (property.printName()) s.append(method.getName()).append('=');
            try
            {
                s.append(String.format(property.format(), method.invoke(this)));
            }
            catch (IllegalAccessException | InvocationTargetException ignored)
            {
                s.append(method.getReturnType());
            }
            
            if (iterator.hasNext())
            {
                s.append(", ");
            }
            else
            {
                break;
            }
        }
        return s.append("}").toString();
    }
    
    @Override
    public double time()
    {
        return this.time;
    }
}
