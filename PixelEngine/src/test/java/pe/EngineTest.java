package pe;

import pe.color.Color;
import rutils.Logger;

import java.util.logging.Level;

public class EngineTest extends Engine
{
    @Override
    protected void setup()
    {
    
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        Draw.clearBackground(Color.BACKGROUND_GRAY);
        
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
        
        if (Keyboard.down(Keyboard.Key.R)) Mouse.pos(10, 10);
        if (Keyboard.down(Keyboard.Key.A)) Mouse.pos(10, 10);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        Logger.addLogFile("out/console.log");
        Extension.register(ExtTest.class);
        new EngineTest().start(100, 100, 4, 4);
    }
    
    static class ExtTest extends Extension
    {
        @StageMethod(stage = Stage.PRE_SETUP)
        static void preSetup()
        {
            System.out.println("preSetup");
        }
        
        @StageMethod(stage = Stage.POST_SETUP)
        static void postSetup()
        {
            System.out.println("postSetup");
        }
        
        @StageMethod(stage = Stage.RENDER_SETUP)
        static void renderSetup()
        {
            // throw new RuntimeException("Test Exception");
        }
        
        @StageMethod(stage = Stage.POST_DESTROY)
        static void postDestroy()
        {
            // throw new RuntimeException("Test Exception");
        }
        
        private static void notAStage()
        {
            System.out.println("notAStage");
        }
    }
}
