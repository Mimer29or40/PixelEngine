package pe;

import org.joml.Vector2d;
import pe.color.Color;
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
    
        testWindow1 = new GUIWindow("Test GUIWindow1", 90, 30, 230, 250);
        testWindow1.enable(WindowFlag.BORDER,
                           WindowFlag.MOVABLE,
                           WindowFlag.SCALABLE,
                           WindowFlag.MINIMIZABLE,
                           WindowFlag.TITLE);
        
        // GUILayout rows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        // {
        //     rows.add(new GUILabel(() -> "Title: " + testWindow.title()));
        //
        //     GUITree treeBounds = new GUITree(GUITree.Type.TAB, "Bounds");
        //     {
        //         GUILayout treeBoundsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        //         {
        //             GUIPropertyInt intProp;
        //
        //             intProp = treeBoundsRows.add(new GUIPropertyInt("X Pos", testWindow1.pos.x, 0, Window.framebufferWidth(), 10, 1));
        //             propX   = intProp.value;
        //             intProp.max.set(Window::framebufferWidth);
        //             intProp = treeBoundsRows.add(new GUIPropertyInt("Y Pos", testWindow1.pos.y, 0, Window.framebufferHeight(), 10, 1));
        //             propY   = intProp.value;
        //             intProp.max.set(Window::framebufferHeight);
        //             intProp = treeBoundsRows.add(new GUIPropertyInt("Width", testWindow1.size.x, 0, Window.framebufferWidth(), 10, 1));
        //             propW   = intProp.value;
        //             intProp.max.set(Window::framebufferWidth);
        //             intProp = treeBoundsRows.add(new GUIPropertyInt("Height", testWindow1.size.y, 0, Window.framebufferHeight(), 10, 1));
        //             propH   = intProp.value;
        //             intProp.max.set(Window::framebufferHeight);
        //
        //             GUITree treeContentBounds = new GUITree(GUITree.Type.TAB, "Content Bounds");
        //             {
        //                 GUILayout treeContentBoundsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        //                 {
        //                     treeContentBoundsRows.add(new GUILabel(() -> "Content Pos:   " + testWindow1.contentPos()));
        //                     treeContentBoundsRows.add(new GUILabel(() -> "Content Size:  " + testWindow1.contentSize()));
        //                 }
        //                 treeContentBounds.add(treeContentBoundsRows);
        //             }
        //             treeBoundsRows.add(treeContentBounds);
        //         }
        //         treeBounds.add(treeBoundsRows);
        //     }
        //     rows.add(treeBounds);
        //
        //     GUITree treeScroll = new GUITree(GUITree.Type.TAB, "Scroll");
        //     {
        //         GUILayout treeScrollRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        //         {
        //             treeScrollRows.add(new GUIPropertyDouble("X Scroll", 0, 0, testWindow1.size.x, 10, 1));
        //             treeScrollRows.add(new GUIPropertyDouble("Y Scroll", 0, 0, testWindow1.size.y, 10, 1));
        //         }
        //         treeScroll.add(treeScrollRows);
        //     }
        //     rows.add(treeScroll);
        //
        //     GUITree treeState = new GUITree(GUITree.Type.TAB, "State");
        //     {
        //         GUILayout treeStateRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        //         {
        //             treeStateRows.add(new GUILabel(() -> "Focused: " + testWindow1.focused()));
        //             treeStateRows.add(new GUILabel(() -> "Hovered: " + testWindow1.hovered()));
        //         }
        //         treeState.add(treeStateRows);
        //     }
        //     rows.add(treeState);
        //
        //     GUITree treeFlags = new GUITree(GUITree.Type.TAB, "State");
        //     {
        //         GUILayout treeFlagsRows = new GUILayoutSimple(20, LayoutMode.DYNAMIC, 1.0);
        //         {
        //             for (WindowFlag flag : WindowFlag.values())
        //             {
        //                 treeFlagsRows.add(new GUILabel(() -> String.format("%s: %s", flag.name(), testWindow.isEnabled(flag))));
        //             }
        //         }
        //         treeFlags.add(treeFlagsRows);
        //     }
        //     rows.add(treeFlags);
        // }
        // testWindow.add(rows);
        
        OverlayGUI.addWindow(testWindow);
        OverlayGUI.addWindow(testWindow1);
        GUIDebugWindow debugWindow = new GUIDebugWindow("Debug", 0, 0, 300, 500);
        debugWindow.target = testWindow;
        debugWindow.enable(WindowFlag.BORDER,
                           WindowFlag.MOVABLE,
                           WindowFlag.SCALABLE,
                           WindowFlag.MINIMIZABLE,
                           WindowFlag.TITLE);
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
        
        
        Draw.clearBackground(Color.BLACK);
        
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
