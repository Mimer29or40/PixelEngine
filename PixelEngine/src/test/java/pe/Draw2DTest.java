package pe;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import pe.color.Color;
import pe.event.*;
import pe.texture.Image;
import pe.texture.Texture;
import rutils.Logger;
import rutils.Math;

import java.util.logging.Level;

public class Draw2DTest extends Engine
{
    private Keyboard.Key state = Keyboard.Key.F9;
    
    // DrawPoint2D
    private Vector2d[] draggablePoint;
    private int        draggingPoint = -1;
    
    // DrawLine2D
    private Vector2d[] draggableLine;
    private int        draggingLine = -1;
    
    // DrawLines2D
    private Vector2d[] draggableLines;
    private int        draggingLines = -1;
    
    // DrawBezier2D
    private Vector2d[] draggableBezier;
    private int        draggingBezier = -1;
    
    // DrawTriangle2D
    private Vector2d[] draggableTriangle;
    private int        draggingTriangle = -1;
    
    // DrawQuad2D
    private Vector2d[] draggableQuad;
    private int        draggingQuad = -1;
    
    // DrawRect2D
    private Vector2d[] draggableRect;
    private int        draggingRect = -1;
    
    // DrawEllipse2D
    private Vector2d[] draggableEllipse;
    private int        draggingEllipse = -1;
    
    // DrawRing2D
    private Vector2d[] draggableRing;
    private int        draggingRing = -1;
    
    // DrawQuad2D
    private Vector2d[] draggableTexture;
    private int        draggingTexture = -1;
    
    // DrawTexture2D
    private Texture texture;
    
    private boolean toggle;
    
    private int hValue = 1, vValue = 1;
    
    @Override
    protected void setup()
    {
        size(800, 800, 1, 1);
        
        draggablePoint = new Vector2d[16];
        for (int i = 0, n = draggablePoint.length; i < n; i++)
        {
            double angle = (double) i / (n - 1) * Math.PI2;
            
            double cos = Math.cos(angle) * screenWidth() * 0.25;
            double sin = Math.sin(angle) * screenHeight() * 0.25;
            
            draggablePoint[i] = new Vector2d(cos + screenWidth() * 0.5, sin + screenHeight() * 0.5);
        }
        
        draggableLine = new Vector2d[] {
                new Vector2d(screenWidth() * 0.5, screenHeight() * 0.5),
                new Vector2d(10, 10),
                new Vector2d(10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, 10),
                };
        
        draggableLines = new Vector2d[] {
                new Vector2d(10, screenHeight() * 0.5),
                new Vector2d(screenWidth() * 0.5 - 10, 10),
                new Vector2d(screenWidth() * 0.5 + 10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                };
        
        draggableBezier = new Vector2d[] {
                new Vector2d(10, screenHeight() * 0.5),
                new Vector2d(screenWidth() * 0.5 - 10, 10),
                new Vector2d(screenWidth() * 0.5 + 10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                };
        
        draggableTriangle = new Vector2d[] {
                new Vector2d(10, screenHeight() * 0.5),
                new Vector2d(screenWidth() * 0.5 - 10, 10),
                new Vector2d(screenWidth() * 0.5 + 10, screenHeight() - 10),
                };
        
        draggableQuad = new Vector2d[] {
                new Vector2d(10, 10),
                new Vector2d(10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, 10),
                };
        
        draggableRect = new Vector2d[] {
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() * 0.5, screenHeight() * 0.5),
                new Vector2d(screenWidth() - 10, screenHeight() * 0.5),
                };
        
        draggableEllipse = new Vector2d[] {
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() * 0.5, screenHeight() * 0.5),
                new Vector2d(screenWidth() - 10, screenHeight() * 0.5),
                };
        
        draggableRing = new Vector2d[] {
                new Vector2d(screenWidth() * 0.5 + 10, screenHeight() * 0.5 + 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() * 0.5, screenHeight() * 0.5),
                new Vector2d(screenWidth() - 10, screenHeight() * 0.5),
                };
        
        draggableTexture = new Vector2d[] {
                new Vector2d(10, 10),
                new Vector2d(10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, screenHeight() - 10),
                new Vector2d(screenWidth() - 10, 10),
                };
        
        Image image = Image.genColorGradient(30, 30, Color.BLUE, Color.MAGENTA, Color.CYAN, Color.WHITE);
        texture = Texture.load(image);
        image.delete();
    }
    
    private void handleInput()
    {
        for (Event event : Events.get())
        {
            if (event instanceof EventKeyboardKeyDown keyDown)
            {
                switch (keyDown.key())
                {
                    case LEFT -> Debug.notification("H Value: " + --hValue);
                    case RIGHT -> Debug.notification("H Value: " + ++hValue);
                    case DOWN -> Debug.notification("V Value: " + --vValue);
                    case UP -> Debug.notification("V Value: " + ++vValue);
                    case SPACE -> toggle = !toggle;
                    case ESCAPE -> stop();
                    case L_SHIFT -> {}
                    default -> {if (Modifier.all()) state = keyDown.key();}
                }
            }
            else if (event instanceof EventKeyboardKeyRepeated keyRepeated)
            {
                if (!Modifier.all(Modifier.CONTROL))
                {
                    switch (keyRepeated.key())
                    {
                        case LEFT -> Debug.notification("H Value: " + --hValue);
                        case RIGHT -> Debug.notification("H Value: " + ++hValue);
                        case DOWN -> Debug.notification("V Value: " + --vValue);
                        case UP -> Debug.notification("V Value: " + ++vValue);
                    }
                }
            }
            else if (event instanceof EventKeyboardKeyHeld keyHeld)
            {
                if (Modifier.all(Modifier.CONTROL))
                {
                    switch (keyHeld.key())
                    {
                        case LEFT -> Debug.notification("H Value: " + --hValue);
                        case RIGHT -> Debug.notification("H Value: " + ++hValue);
                        case DOWN -> Debug.notification("V Value: " + --vValue);
                        case UP -> Debug.notification("V Value: " + ++vValue);
                    }
                }
            }
            else if (event instanceof EventMouseButtonDown buttonDown)
            {
                draggingPoint    = -1;
                draggingLine     = -1;
                draggingLines    = -1;
                draggingBezier   = -1;
                draggingTriangle = -1;
                draggingQuad     = -1;
                draggingRect     = -1;
                draggingEllipse  = -1;
                draggingRing     = -1;
                draggingTexture  = -1;
                switch (state)
                {
                    case F1 -> draggingPoint = mouseDown(draggablePoint, buttonDown.pos());
                    case F2 -> draggingLine = mouseDown(draggableLine, buttonDown.pos());
                    case F3 -> draggingLines = mouseDown(draggableLines, buttonDown.pos());
                    case F4 -> draggingBezier = mouseDown(draggableBezier, buttonDown.pos());
                    case F5 -> draggingTriangle = mouseDown(draggableTriangle, buttonDown.pos());
                    case F6 -> draggingQuad = mouseDown(draggableQuad, buttonDown.pos());
                    case F7 -> draggingRect = mouseDown(draggableRect, buttonDown.pos());
                    case F8 -> draggingEllipse = mouseDown(draggableEllipse, buttonDown.pos());
                    case F9 -> draggingRing = mouseDown(draggableRing, buttonDown.pos());
                    case F10 -> draggingTexture = mouseDown(draggableTexture, buttonDown.pos());
                }
            }
            else if (event instanceof EventMouseButtonUp)
            {
                draggingPoint    = -1;
                draggingLine     = -1;
                draggingLines    = -1;
                draggingBezier   = -1;
                draggingTriangle = -1;
                draggingQuad     = -1;
                draggingRect     = -1;
                draggingEllipse  = -1;
                draggingRing     = -1;
                draggingTexture  = -1;
            }
            else if (event instanceof EventMouseMoved mouseMoved)
            {
                switch (state)
                {
                    case F1 -> mouseMoved(draggablePoint, draggingPoint, mouseMoved.pos());
                    case F2 -> mouseMoved(draggableLine, draggingLine, mouseMoved.pos());
                    case F3 -> mouseMoved(draggableLines, draggingLines, mouseMoved.pos());
                    case F4 -> mouseMoved(draggableBezier, draggingBezier, mouseMoved.pos());
                    case F5 -> mouseMoved(draggableTriangle, draggingTriangle, mouseMoved.pos());
                    case F6 -> mouseMoved(draggableQuad, draggingQuad, mouseMoved.pos());
                    case F7 -> mouseMoved(draggableRect, draggingRect, mouseMoved.pos());
                    case F8 -> mouseMoved(draggableEllipse, draggingEllipse, mouseMoved.pos());
                    case F9 -> mouseMoved(draggableRing, draggingRing, mouseMoved.pos());
                    case F10 -> mouseMoved(draggableTexture, draggingTexture, mouseMoved.pos());
                }
            }
        }
    }
    
    private int mouseDown(Vector2d[] draggable, Vector2dc pos)
    {
        for (int i = 0; i < draggable.length; i++)
        {
            double x = pos.x() - draggable[i].x;
            double y = pos.y() - draggable[i].y;
            
            if (Math.sqrt(x * x + y * y) < 10)
            {
                return i;
            }
        }
        return -1;
    }
    
    private void mouseMoved(Vector2d[] draggable, int dragging, Vector2dc pos)
    {
        if (dragging != -1)
        {
            Debug.notification("Pos: " + pos);
            draggable[dragging].set(pos);
        }
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        handleInput();
        
        Draw.clearBackground(Color.BLACK);
        
        double thickness = Math.map(Math.max(hValue, 1), 0, 100, 0, 10);
        
        switch (state)
        {
            case F1 -> {
                int r, g, b, a;
                for (Vector2d vector2d : draggablePoint)
                {
                    r = random.nextInt(0, 255);
                    g = random.nextInt(0, 255);
                    b = random.nextInt(0, 255);
                    a = random.nextInt(0, 255);
                    
                    Draw.point2D().point(vector2d).thickness(thickness).color(r, g, b, a).draw();
                }
            }
            case F2 -> {
                Draw.line2D().point0(draggableLine[0]).point1(draggableLine[1]).thickness(thickness).color0(Color.BLANK).color1(Color.GREEN).draw();
                Draw.line2D().point0(draggableLine[0]).point1(draggableLine[2]).thickness(thickness).color0(Color.BLANK).color1(Color.RED).draw();
                Draw.line2D().point0(draggableLine[0]).point1(draggableLine[3]).thickness(thickness).color0(Color.BLANK).color1(Color.BLUE).draw();
                Draw.line2D().point0(draggableLine[0]).point1(draggableLine[4]).thickness(thickness).color0(Color.BLANK).color1(Color.WHITE).draw();
                
                for (Vector2d point : draggableLine)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F3 -> {
                if (toggle)
                {
                    int steps = Math.max(vValue, 2);
                    
                    double[] points = new double[steps * 2];
                    
                    double x, y;
                    for (int i = 0; i < steps; i++)
                    {
                        x = (double) i / (steps - 1);
                        // y = 4.0 * x * (x - 1) + 1;
                        // y = x < 0.5 ? 4 * x * x * x : 1 - 4 * (1 - x) * (1 - x) * (1 - x);
                        y = 8 * (x) * (x - 0.5) * (x - 1) + 0.5;
                        
                        points[(i << 1)]     = x * screenWidth();
                        points[(i << 1) + 1] = y * screenHeight();
                    }
                    
                    Draw.lines2D().points(points).thickness(thickness).color0(Color.CYAN).color1(Color.MAGENTA).draw();
                }
                else
                {
                    Draw.lines2D().points(draggableLines).thickness(thickness).color0(Color.RED).color1(Color.GREEN).draw();
                    
                    for (Vector2d point : draggableLines)
                    {
                        Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                    }
                }
            }
            case F4 -> {
                Draw.bezier2D().points(draggableBezier).thickness(thickness).color0(Color.GREEN).color1(Color.YELLOW).draw();
                
                for (Vector2d point : draggableBezier)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F5 -> {
                if (toggle)
                {
                    Draw.fillTriangle2D()
                        .point0(draggableTriangle[0])
                        .point1(draggableTriangle[1])
                        .point2(draggableTriangle[2])
                        .color(Color.YELLOW)
                        .draw();
                }
                else
                {
                    Draw.fillTriangle2D()
                        .point0(draggableTriangle[0])
                        .point1(draggableTriangle[1])
                        .point2(draggableTriangle[2])
                        .color0(Color.RED)
                        .color1(Color.GREEN)
                        .color2(Color.BLUE)
                        .draw();
                }
                Draw.drawTriangle2D()
                    .point0(draggableTriangle[0])
                    .point1(draggableTriangle[1])
                    .point2(draggableTriangle[2])
                    .thickness(thickness)
                    .color(Color.GREY)
                    .draw();
                
                for (Vector2d point : draggableTriangle)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F6 -> {
                if (toggle)
                {
                    Draw.fillQuad2D()
                        .point0(draggableQuad[0])
                        .point1(draggableQuad[1])
                        .point2(draggableQuad[2])
                        .point3(draggableQuad[3])
                        .color(Color.YELLOW)
                        .draw();
                }
                else
                {
                    Draw.fillQuad2D()
                        .point0(draggableQuad[0])
                        .point1(draggableQuad[1])
                        .point2(draggableQuad[2])
                        .point3(draggableQuad[3])
                        .color0(Color.RED)
                        .color1(Color.GREEN)
                        .color2(Color.BLUE)
                        .color3(Color.WHITE)
                        .draw();
                }
                Draw.drawQuad2D()
                    .point0(draggableQuad[0])
                    .point1(draggableQuad[1])
                    .point2(draggableQuad[2])
                    .point3(draggableQuad[3])
                    .thickness(thickness)
                    .color(Color.GREY)
                    .draw();
                
                for (Vector2d point : draggableQuad)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F7 -> {
                double cx = screenWidth() * 0.5;
                double cy = screenHeight() * 0.5;
                
                double width  = draggableRect[0].x - cx;
                double height = draggableRect[0].y - cy;
                
                double ox = draggableRect[1].x - cx;
                double oy = draggableRect[1].y - cy;
                
                double rotation = Math.atan2(draggableRect[2].y - draggableRect[1].y, draggableRect[2].x - draggableRect[1].x);
                
                Draw.fillRect2D()
                    .point(cx, cy)
                    .size(width, height)
                    .origin(ox, oy)
                    .angle(rotation)
                    .gradientH(Color.BLUE, Color.LIGHT_GREEN)
                    .draw();
                
                Draw.drawRect2D()
                    .point(cx, cy)
                    .size(width, height)
                    .thickness(thickness)
                    .origin(ox, oy)
                    .angle(rotation)
                    .color(Color.DARKER_RED)
                    .draw();
                
                for (Vector2d point : draggableRect)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F8 -> {
                double cx = screenWidth() * 0.5;
                double cy = screenHeight() * 0.5;
                
                double rx = draggableEllipse[0].x - cx;
                double ry = draggableEllipse[0].y - cy;
                
                double ox = draggableEllipse[1].x - cx;
                double oy = draggableEllipse[1].y - cy;
                
                double rotation = Math.atan2(draggableEllipse[2].y - draggableEllipse[1].y, draggableEllipse[2].x - draggableEllipse[1].x);
                
                if (toggle)
                {
                    Draw.fillEllipse2D()
                        .center(cx, cy)
                        .radius(rx, ry)
                        .origin(ox, oy)
                        .angles(0, Math.toRadians(vValue))
                        .angle(rotation)
                        .color(Color.CYAN)
                        .draw();
                }
                else
                {
                    Draw.fillEllipse2D()
                        .center(cx, cy)
                        .radius(rx, ry)
                        .origin(ox, oy)
                        .angles(0, Math.toRadians(vValue))
                        .angle(rotation)
                        .colorInner(Color.GRAY)
                        .colorOuter(Color.YELLOW)
                        .draw();
                }
                
                Draw.drawEllipse2D()
                    .center(cx, cy)
                    .radius(rx, ry)
                    .thickness(thickness)
                    .origin(ox, oy)
                    .angles(0, Math.toRadians(vValue))
                    .angle(rotation)
                    .color(Color.DARKER_BLUE)
                    .draw();
                
                for (Vector2d point : draggableEllipse)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F9 -> {
                double cx = screenWidth() * 0.5;
                double cy = screenHeight() * 0.5;
                
                double rxi = draggableRing[0].x - cx;
                double ryi = draggableRing[0].y - cy;
                
                double rxo = draggableRing[1].x - cx;
                double ryo = draggableRing[1].y - cy;
                
                double ox = draggableRing[2].x - cx;
                double oy = draggableRing[2].y - cy;
                
                double rotation = Math.atan2(draggableRing[3].y - draggableRing[2].y, draggableRing[3].x - draggableRing[2].x);
                
                if (toggle)
                {
                    Draw.fillRing2D()
                        .center(cx, cy)
                        .radiusInner(rxi, ryi)
                        .radiusOuter(rxo, ryo)
                        .origin(ox, oy)
                        .angles(0, Math.toRadians(vValue))
                        .angle(rotation)
                        .color(Color.DARK_MAGENTA)
                        .draw();
                }
                else
                {
                    Draw.fillRing2D()
                        .center(cx, cy)
                        .radiusInner(rxi, ryi)
                        .radiusOuter(rxo, ryo)
                        .origin(ox, oy)
                        .angles(0, Math.toRadians(vValue))
                        .angle(rotation)
                        .colorInner(Color.DARK_RED)
                        .colorOuter(Color.LIGHT_GREEN)
                        .draw();
                }
                
                Draw.drawRing2D()
                    .center(cx, cy)
                    .radiusInner(rxi, ryi)
                    .radiusOuter(rxo, ryo)
                    .thickness(thickness)
                    .origin(ox, oy)
                    .angles(0, Math.toRadians(vValue))
                    .angle(rotation)
                    .color(Color.DARKER_BLUE)
                    .draw();
                
                for (Vector2d point : draggableRing)
                {
                    Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                }
            }
            case F10 -> {
                if (toggle)
                {
                    double rotation = Math.toRadians(hValue);
                    
                    double cx = screenWidth() >> 1;
                    double cy = screenHeight() >> 1;
                    
                    double mul = Math.map(vValue, 0, 1000, 0, 10);
                    
                    double w = texture.width() * mul;
                    double h = texture.height() * mul;
                    
                    Draw.drawTexture2D()
                        .texture(texture)
                        .dst(cx, cy, w, h)
                        .origin(w * 0.5, h * 0.5)
                        .angle(rotation)
                        .draw();
                }
                else
                {
                    Draw.drawTextureWarped2D()
                        .texture(texture)
                        .point0(draggableTexture[0])
                        .point1(draggableTexture[1])
                        .point2(draggableTexture[2])
                        .point3(draggableTexture[3])
                        .draw();
                    
                    for (Vector2d point : draggableTexture)
                    {
                        Draw.point2D().point(point).thickness(5).color(Color.WHITE).draw();
                    }
                }
            }
        }
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        start(new Draw2DTest());
    }
}
