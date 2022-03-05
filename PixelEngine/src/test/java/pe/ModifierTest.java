package pe;

import pe.color.Color;
import pe.render.GLState;
import rutils.Logger;

import java.util.logging.Level;

public class ModifierTest extends Engine
{
    @Override
    protected void setup()
    {
        size(100, 100, 4, 4);
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        if (Modifier.any()) System.out.println("Any Modifiers");
        if (Modifier.any(Modifier.SHIFT)) System.out.println("Any SHIFT");
        if (Modifier.any(Modifier.CONTROL)) System.out.println("Any CONTROL");
        if (Modifier.any(Modifier.SHIFT, Modifier.CONTROL)) System.out.println("Any SHIFT, CONTROL");
        
        if (Modifier.all()) System.out.println("All No Modifiers");
        if (Modifier.all(Modifier.SHIFT)) System.out.println("All SHIFT");
        if (Modifier.all(Modifier.CONTROL)) System.out.println("All CONTROL");
        if (Modifier.all(Modifier.SHIFT, Modifier.CONTROL)) System.out.println("All SHIFT, CONTROL");
    
        if (Modifier.only()) System.out.println("Only No Modifiers");
        if (Modifier.only(Modifier.SHIFT)) System.out.println("Only SHIFT");
        if (Modifier.only(Modifier.CONTROL)) System.out.println("Only CONTROL");
        if (Modifier.only(Modifier.SHIFT, Modifier.CONTROL)) System.out.println("Only SHIFT, CONTROL");
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        start(new ModifierTest());
    }
}
