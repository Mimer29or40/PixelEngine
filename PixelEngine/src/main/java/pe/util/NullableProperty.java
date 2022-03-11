package pe.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class NullableProperty<T> extends Property<T>
{
    public NullableProperty(String name, @Nullable T initial)
    {
        super(name, initial);
    }
    
    public NullableProperty(String name, @NotNull Supplier<@Nullable T> initial)
    {
        super(name, initial);
    }
    
    @Override
    @Nullable
    public T get()
    {
        return super.get();
    }
    
    public void set(@Nullable T value)
    {
        super.set(value);
    }
    
    public void set(@NotNull Supplier<@Nullable T> value)
    {
        super.set(value);
    }
}
