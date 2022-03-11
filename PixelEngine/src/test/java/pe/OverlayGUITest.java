package pe;

import org.joml.Vector2d;
import pe.color.Color;
import pe.color.Colorc;
import pe.event.EventMouseButtonHeld;
import pe.gui.*;
import pe.util.Property;
import rutils.Logger;
import rutils.Math;
import rutils.group.Pair;

import java.util.List;
import java.util.logging.Level;

public class OverlayGUITest extends Engine
{
    Vector2d p0 = new Vector2d();
    Vector2d p1 = new Vector2d();
    Vector2d p2 = new Vector2d();
    
    GUIWindow testWindow;
    GUIWindow testWindow1;
    
    // Property<Integer> propX;
    // Property<Integer> propY;
    // Property<Integer> propW;
    // Property<Integer> propH;
    
    @Override
    protected void setup()
    {
        size(200, 200, 4, 4);
        
        testWindow = new GUIWindow("Test GUIWindow", 30, 30, 230, 250);
        testWindow.enable(WindowFlag.BORDER,
                          WindowFlag.MOVABLE,
                          WindowFlag.SCALABLE,
                          WindowFlag.MINIMIZABLE,
                          WindowFlag.TITLE);
        OverlayGUI.addWindow(testWindow);
    
        testWindow1 = new GUIWindow("Test GUIWindow1", 90, 30, 230, 250);
        testWindow1.enable(WindowFlag.BORDER,
                           WindowFlag.MOVABLE,
                           WindowFlag.SCALABLE,
                           WindowFlag.MINIMIZABLE,
                           WindowFlag.TITLE);
        OverlayGUI.addWindow(testWindow1);
        
        GUIDebugWindow debugWindow = new GUIDebugWindow("Debug", 0, 0, 300, 500);
        debugWindow.enable(WindowFlag.BORDER,
                           WindowFlag.MOVABLE,
                           WindowFlag.SCALABLE,
                           WindowFlag.MINIMIZABLE,
                           WindowFlag.TITLE);
        debugWindow.target = testWindow;
        OverlayGUI.addWindow(debugWindow);
        
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        var list = List.of(
                new Pair<>(Keyboard.Key.Q, WindowFlag.BORDER),
                new Pair<>(Keyboard.Key.W, WindowFlag.MOVABLE),
                new Pair<>(Keyboard.Key.E, WindowFlag.SCALABLE),
                new Pair<>(Keyboard.Key.R, WindowFlag.CLOSABLE),
                new Pair<>(Keyboard.Key.T, WindowFlag.MINIMIZABLE),
                new Pair<>(Keyboard.Key.Y, WindowFlag.NO_SCROLLBAR),
                new Pair<>(Keyboard.Key.U, WindowFlag.TITLE),
                new Pair<>(Keyboard.Key.I, WindowFlag.SCROLL_AUTO_HIDE),
                new Pair<>(Keyboard.Key.O, WindowFlag.BACKGROUND),
                new Pair<>(Keyboard.Key.P, WindowFlag.SCALE_LEFT)
                // new Pair<>(Keyboard.Key.L_BRACKET, pe.gui.GUIWindow.Flag.NO_INPUT)
                          );
        for (var pair : list)
        {
            if (Keyboard.down(pair.a))
            {
                if (Modifier.all(Modifier.SHIFT))
                {
                    testWindow.disable(pair.b);
                }
                else
                {
                    testWindow.enable(pair.b);
                }
            }
        }
        
        // testWindow.pos.set(propX.get(), propY.get());
        // testWindow.size.set(propW.get(), propH.get());
    
        Colorc color = Color.BLACK;
        for (EventMouseButtonHeld event : Events.get(EventMouseButtonHeld.class))
        {
            color = Color.GREEN;
            break;
        }
        Draw.clearBackground(color);
        
        double time = Time.get() * 0.5;
        
        double sizeX = screenWidth() * 0.33;
        double sizeY = screenHeight() * 0.33;
        
        double angle, cos, sin;
        
        angle = time;
        cos   = sizeX * Math.cos(angle);
        sin   = sizeY * Math.sin(angle);
        p0.set(screenSize()).mul(0.5).add(cos, sin);
        
        angle = time + Math.PI_3;
        cos   = sizeX * Math.cos(angle);
        sin   = sizeY * Math.sin(angle);
        p1.set(screenSize()).mul(0.5).sub(cos, sin);
        
        angle = time - Math.PI_3;
        cos   = sizeX * Math.cos(angle);
        sin   = sizeY * Math.sin(angle);
        p2.set(screenSize()).mul(0.5).sub(cos, sin);
        
        Draw.line2D().point0(p0).point1(p1).thickness(1.0).color(Color.WHITE).draw();
        Draw.line2D().point0(p0).point1(p2).thickness(1.0).color(Color.WHITE).draw();
    
        // propX.set((int) testWindow.x());
        // propY.set((int) testWindow.y());
        // propW.set((int) testWindow.width());
        // propH.set((int) testWindow.height());
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        start(new OverlayGUITest());
    }
}
