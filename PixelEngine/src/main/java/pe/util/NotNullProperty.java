package pe.util;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class NotNullProperty<T> extends Property<T>
{
    public NotNullProperty(String name, @NotNull T initial)
    {
        super(name, initial);
    }
    
    public NotNullProperty(String name, @NotNull Supplier<@NotNull T> initial)
    {
        super(name, initial);
    }
    
    @Override
    @NotNull
    public T get()
    {
        return super.get();
    }
    
    public void set(@NotNull T value)
    {
        super.set(value);
    }
    
    public void set(@NotNull Supplier<@NotNull T> value)
    {
        super.set(value);
    }
}
