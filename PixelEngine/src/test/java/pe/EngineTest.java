package pe;

import pe.render.*;
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
        GLState.clearScreenBuffers();
        
        // double x = Mouse.x();
        // double y = Mouse.y();
        //
        // GLBatch.get().matrix.mode(MatrixMode.MODEL);
        // GLBatch.get().matrix.translate(x, y, 0.0);
        
        Draw.fillTriangle2D()
            .point0(50.0, 25.0)
            .color0(255, 0, 0, 255)
            .point1(25.0, 50.0)
            .color1(0, 255, 0, 255)
            .point2(75.0, 50.0)
            .color2(0, 0, 255, 255)
            .draw();
        
        Draw.drawTriangle2D()
            .point0(25.0, 25.0)
            .point1(50.0, 50.0)
            .point2(75.0, 25.0)
            .thickness(1.0)
            .draw();
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        start(new EngineTest());
    }
}
