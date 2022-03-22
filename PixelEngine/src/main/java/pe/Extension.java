package pe;

import org.jetbrains.annotations.NotNull;
import rutils.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public class Extension
{
    private static final Logger LOGGER = new Logger();
    
    private static final Map<String, Map<Stage, Method>> EXTENSIONS = new HashMap<>();
    
    static void stage(@NotNull Stage stage) throws Throwable
    {
        Extension.LOGGER.log(stage.logLevel, stage.name());
        
        for (String extension : Extension.EXTENSIONS.keySet())
        {
            Map<Stage, Method> methodMap = Extension.EXTENSIONS.get(extension);
            
            if (methodMap.containsKey(stage))
            {
                Extension.LOGGER.log(stage.logLevel, extension, stage.name());
                invokeMethod(methodMap.get(stage));
            }
        }
    }
    
    static void stageCatch(@NotNull Stage stage)
    {
        Extension.LOGGER.log(stage.logLevel, stage.name());
        
        for (String extension : Extension.EXTENSIONS.keySet())
        {
            Map<Stage, Method> methodMap = Extension.EXTENSIONS.get(extension);
            
            if (methodMap.containsKey(stage))
            {
                Extension.LOGGER.log(stage.logLevel, extension, stage.name());
                try
                {
                    invokeMethod(methodMap.get(stage));
                }
                catch (Throwable e)
                {
                    Extension.LOGGER.severe(e);
                }
            }
        }
    }
    
    public static void register(@NotNull Class<? extends Extension> clazz)
    {
        final String name = clazz.getSimpleName();
        if (Extension.EXTENSIONS.containsKey(name))
        {
            Extension.LOGGER.warning("Extension \"%s\" already registered.", name);
            return;
        }
        
        Extension.LOGGER.info("Registering Extension:", name);
        
        Map<Stage, Method> methodMap = new EnumMap<>(Stage.class);
        for (Method m : clazz.getDeclaredMethods())
        {
            if (!m.isAnnotationPresent(StageMethod.class)) continue;
            if (!Modifier.isStatic(m.getModifiers()))
            {
                throw new RuntimeException("Extension Stage Method \"" + m.getName() + "\" must be static.");
            }
            StageMethod stageMethod = m.getAnnotation(StageMethod.class);
            Stage       stage       = stageMethod.stage();
            if (methodMap.containsKey(stage))
            {
                throw new RuntimeException(stage + " is defined twice.");
            }
            methodMap.put(stage, m);
        }
        Extension.EXTENSIONS.put(name, methodMap);
    }
    
    public static void registerDefaultExtensions()
    {
        register(EXT_GIF.class);
    }
    
    private static void invokeMethod(@NotNull Method method) throws Throwable
    {
        try
        {
            method.invoke(null);
        }
        catch (InvocationTargetException e)
        {
            throw e.getCause();
        }
    }
    
    public enum Stage
    {
        PRE_SETUP(Level.FINE),
        POST_SETUP(Level.FINE),
        RENDER_SETUP(Level.FINE),
        PRE_FRAME(Level.FINEST),
        PRE_EVENTS(Level.FINEST),
        POST_EVENTS(Level.FINEST),
        PRE_DRAW(Level.FINEST),
        POST_DRAW(Level.FINEST),
        POST_FRAME(Level.FINEST),
        RENDER_DESTROY(Level.FINE),
        PRE_DESTROY(Level.FINE),
        POST_DESTROY(Level.FINE),
        ;
        
        private final Level logLevel;
        
        Stage(Level logLevel)
        {
            this.logLevel = logLevel;
        }
    }
    
    @Retention(value = RUNTIME)
    @Target(value = METHOD)
    protected @interface StageMethod
    {
        Stage stage();
    }
}
