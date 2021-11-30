package pe.engine;

import rutils.Logger;

import java.util.logging.Level;

public class EngineTest extends Engine
{
    @Override
    protected void setup()
    {
        size(100, 100, 4, 4);
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
    
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        Logger.setLevel(Level.FINEST);
        start(new EngineTest());
    }
}
