package pe.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class Property<T> implements Supplier<T>
{
    private final String name;
    
    private T           value;
    private Supplier<T> getter;
    private Usage       usage;
    
    public Property(String name, T initial)
    {
        this.name  = name;
        this.value = initial;
        this.usage = Usage.VALUE;
    }
    
    public Property(String name, @NotNull Supplier<T> initial)
    {
        this.name   = name;
        this.getter = initial;
        this.usage  = Usage.GETTER;
    }
    
    @Override
    public String toString()
    {
        return "Property{" + "name='" + this.name + '\'' + ", value=" + get() + '}';
    }
    
    @Override
    public T get()
    {
        return switch (this.usage)
                {
                    case VALUE -> this.value;
                    case GETTER -> this.getter.get();
                };
    }
    
    public void set(T value)
    {
        this.value = value;
        this.usage = Usage.VALUE;
    }
    
    public void set(@NotNull Supplier<T> value)
    {
        this.getter = value;
        this.usage  = Usage.GETTER;
    }
    
    private enum Usage
    {
        VALUE, GETTER
    }
}
