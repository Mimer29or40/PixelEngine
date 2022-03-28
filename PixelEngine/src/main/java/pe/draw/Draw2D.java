package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import pe.font.CharData;
import pe.font.TextState;
import pe.render.DrawMode;
import pe.render.GL;
import pe.render.GLBatch;
import pe.render.GLTexture;
import rutils.Logger;
import rutils.Math;

import java.util.ArrayList;

public abstract class Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    protected static GLTexture texture;
    
    protected static double u0, v0, u1, v1;
    
    private static final GLBatch.Vertex VERTEX0, VERTEX1, VERTEX2, VERTEX3;
    
    static
    {
        texture = GL.defaultTexture();
        
        u0 = 0.0;
        v0 = 0.0;
        u1 = 1.0;
        v1 = 1.0;
        
        VERTEX0 = new GLBatch.Vertex();
        VERTEX1 = new GLBatch.Vertex();
        VERTEX2 = new GLBatch.Vertex();
        VERTEX3 = new GLBatch.Vertex();
    }
    
    public static void setTexture(@Nullable GLTexture texture)
    {
        Draw2D.texture = texture;
    }
    
    public static void setUV(double u0, double v0, double u1, double v1)
    {
        Draw2D.u0 = u0;
        Draw2D.v0 = v0;
        Draw2D.u1 = u1;
        Draw2D.v1 = v1;
    }
    
    protected static void drawPoint(double x, double y, double thickness, int r, int g, int b, int a)
    {
        if (thickness <= 1.0)
        {
            GLBatch.checkBuffer(2);
            
            GLBatch.begin(DrawMode.LINES);
            
            GLBatch.pos(x, y);
            GLBatch.color(r, g, b, a);
            
            GLBatch.pos(x + 1, y + 1);
            GLBatch.color(r, g, b, a);
            
            GLBatch.end();
        }
        else
        {
            fillEllipse(x, y, thickness, thickness, 0, Math.PI2, 0.0, 0.0, 0.0, segments(thickness, thickness), r, g, b, a, r, g, b, a);
        }
    }
    
    protected static void drawLine(double x0, double y0, double x1, double y1, double thickness, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1)
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        
        if (thickness <= 1.0)
        {
            GLBatch.checkBuffer(2);
            
            GLBatch.setTexture(Draw2D.texture);
            
            GLBatch.begin(DrawMode.LINES);
            
            GLBatch.pos(x0, y0);
            GLBatch.color(r0, g0, b0, a0);
            
            GLBatch.pos(x1, y1);
            GLBatch.color(r1, g1, b1, a1);
            
            GLBatch.end();
        }
        else if (dx != 0.0 || dy != 0.0)
        {
            double l  = Math.sqrt(dx * dx + dy * dy);
            double s  = thickness / (2 * l);
            double nx = -dy * s;
            double ny = dx * s;
            
            GLBatch.checkBuffer(6);
            
            GLBatch.setTexture(Draw2D.texture);
            
            GLBatch.begin(DrawMode.TRIANGLES);
            
            Draw2D.VERTEX0.clear().pos(x1 + nx, y1 + ny).color(r1, g1, b1, a1);
            Draw2D.VERTEX1.clear().pos(x0 + nx, y0 + ny).color(r0, g0, b0, a0);
            Draw2D.VERTEX2.clear().pos(x0 - nx, y0 - ny).color(r0, g0, b0, a0);
            Draw2D.VERTEX3.clear().pos(x1 - nx, y1 - ny).color(r1, g1, b1, a1);
            
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX2, Draw2D.VERTEX3);
            
            GLBatch.end();
        }
    }
    
    protected static void drawLines(double[] points, double thickness, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1)
    {
        int pointsCount = (points.length >> 1) - 2;
        
        if (pointsCount < 2)
        {
            Draw2D.LOGGER.warning("DrawLines: Invalid points array:", points);
            return;
        }
        
        int _r0 = r0;
        int _g0 = g0;
        int _b0 = b0;
        int _a0 = a0;
        int _r1, _g1, _b1, _a1;
        
        if (thickness <= 1)
        {
            GLBatch.checkBuffer(pointsCount * 2);
            
            GLBatch.begin(DrawMode.LINES);
            
            for (int i = 1; i < pointsCount; i++)
            {
                int p0 = i << 1;
                int p1 = (i + 1) << 1;
                
                double lerp = (double) i / (pointsCount - 1);
                
                _r1 = r0 != r1 ? Math.lerp(r0, r1, lerp) : r1;
                _g1 = g0 != g1 ? Math.lerp(g0, g1, lerp) : g1;
                _b1 = b0 != b1 ? Math.lerp(b0, b1, lerp) : b1;
                _a1 = a0 != a1 ? Math.lerp(a0, a1, lerp) : a1;
                
                GLBatch.vertex(Draw2D.VERTEX0.clear().pos(points[p0], points[p0 + 1]).color(_r0, _g0, _b0, _a0));
                GLBatch.vertex(Draw2D.VERTEX1.clear().pos(points[p1], points[p1 + 1]).color(_r1, _g1, _b1, _a1));
                
                _r0 = _r1;
                _g0 = _g1;
                _b0 = _b1;
                _a0 = _a1;
            }
        }
        else
        {
            // TODO - Rounded, Mitered, Sharp
            
            thickness *= 0.5;
            
            GLBatch.checkBuffer(pointsCount * 9);
            
            GLBatch.setTexture(Draw2D.texture);
            
            GLBatch.begin(DrawMode.TRIANGLES);
            
            int     p0, p1, p2, p3;
            double  p1x, p1y, p2x, p2y;
            double  v0x, v0y, v1x, v1y, v2x, v2y;
            double  v0ux, v0uy, v1ux, v1uy, v2ux, v2uy;
            double  n0x, n0y, n1x, n1y, n2x, n2y;
            double  o0x, o0y, o1x, o1y, o2x, o2y, o3x, o3y, o4x, o4y;
            double  lerp, temp;
            boolean buttStart, buttEnd, drawBevel;
            for (int i = 1; i < pointsCount; i++)
            {
                p0 = (i - 1) << 1;
                p1 = i << 1;
                p2 = (i + 1) << 1;
                p3 = (i + 2) << 1;
                
                lerp = (double) i / (pointsCount - 1);
                
                _r1 = r0 != r1 ? Math.lerp(r0, r1, lerp) : r1;
                _g1 = g0 != g1 ? Math.lerp(g0, g1, lerp) : g1;
                _b1 = b0 != b1 ? Math.lerp(b0, b1, lerp) : b1;
                _a1 = a0 != a1 ? Math.lerp(a0, a1, lerp) : a1;
                
                p1x = points[p1];
                p1y = points[p1 + 1];
                p2x = points[p2];
                p2y = points[p2 + 1];
                
                v0x = p1x - points[p0];
                v0y = p1y - points[p0 + 1];
                v1x = p2x - p1x;
                v1y = p2y - p1y;
                v2x = points[p3] - p2x;
                v2y = points[p3 + 1] - p2y;
                
                temp = Math.sqrt(v1x * v1x + v1y * v1y);
                v1ux = v1x / temp;
                v1uy = v1y / temp;
                n1x  = thickness * -v1uy;
                n1y  = thickness * v1ux;
                
                // Line Start
                if (buttStart = (p0 == p1 || (v0x == 0.0 && v0y == 0.0)))
                {
                    v0ux = v1ux;
                    v0uy = v1uy;
                    n0x  = n1x;
                    n0y  = n1y;
                }
                else
                {
                    temp = Math.sqrt(v0x * v0x + v0y * v0y);
                    v0ux = v0x / temp;
                    v0uy = v0y / temp;
                    n0x  = thickness * -v0uy;
                    n0y  = thickness * v0ux;
                }
                
                // Line End
                if (buttEnd = (p2 == p3 || (v2x == 0.0 && v2y == 0.0)))
                {
                    v2ux = v1ux;
                    v2uy = v1uy;
                    n2x  = n1x;
                    n2y  = n1y;
                }
                else
                {
                    temp = Math.sqrt(v2x * v2x + v2y * v2y);
                    v2ux = v2x / temp;
                    v2uy = v2y / temp;
                    n2x  = thickness * -v2uy;
                    n2y  = thickness * v2ux;
                }
                
                // Butt Start
                o0x = 0.0;
                o0y = 0.0;
                o1x = p1x + n1x;
                o1y = p1y + n1y;
                o2x = p1x - n1x;
                o2y = p1y - n1y;
                
                // Generates Bevel at Joint
                if (drawBevel = !(buttStart || Math.abs(v0ux * v1ux + v0uy * v1uy) > 0.999999))
                {
                    if (n0x * v1x + n0y * v1y > 0)
                    {
                        o0x = p1x - n0x;
                        o0y = p1y - n0y;
                        
                        temp = ((n0x - n1x) * v0y - (n0y - n1y) * v0x) / (v1x * v0y - v1y * v0x);
                        if ((temp * v1x) * (temp * v1x) + (temp * v1y) * (temp * v1y) < v0x * v0x + v0y * v0y)
                        {
                            o1x += temp * v1x;
                            o1y += temp * v1y;
                        }
                    }
                    else
                    {
                        o0x = p1x + n0x;
                        o0y = p1y + n0y;
                        
                        temp = ((n1x - n0x) * v0y - (n1y - n0y) * v0x) / (v1x * v0y - v1y * v0x);
                        if ((temp * v1x) * (temp * v1x) + (temp * v1y) * (temp * v1y) < v0x * v0x + v0y * v0y)
                        {
                            o2x += temp * v1x;
                            o2y += temp * v1y;
                        }
                    }
                }
                
                // Butt End
                o3x = p2x + n1x;
                o3y = p2y + n1y;
                o4x = p2x - n1x;
                o4y = p2y - n1y;
                
                // Generates Bevel at Joint
                if (!buttEnd && Math.abs(v1ux * v2ux + v1uy * v2uy) <= 0.999999)
                {
                    if (n1x * v2x + n1y * v2y > 0)
                    {
                        temp = ((n2x - n1x) * v2y - (n2y - n1y) * v2x) / (v1x * v2y - v1y * v2x);
                        if ((temp * v1x) * (temp * v1x) + (temp * v1y) * (temp * v1y) < v2x * v2x + v2y * v2y)
                        {
                            o3x += temp * v1x;
                            o3y += temp * v1y;
                        }
                    }
                    else
                    {
                        temp = ((n1x - n2x) * v2y - (n1y - n2y) * v2x) / (v1x * v2y - v1y * v2x);
                        if ((temp * v1x) * (temp * v1x) + (temp * v1y) * (temp * v1y) < v2x * v2x + v2y * v2y)
                        {
                            o4x += temp * v1x;
                            o4y += temp * v1y;
                        }
                    }
                }
                
                temp = (o3x - o1x) * v1x + (o3y - o1y) * v1y;
                if (temp <= 0.0)
                {
                    o1x = p1x + n1x;
                    o1y = p1y + n1y;
                    o3x = p2x + n1x;
                    o3y = p2y + n1y;
                }
                
                temp = (o4x - o2x) * v1x + (o4y - o2y) * v1y;
                if (temp <= 0.0)
                {
                    o2x = p1x - n1x;
                    o2y = p1y - n1y;
                    o4x = p2x - n1x;
                    o4y = p2y - n1y;
                }
                
                if (drawBevel)
                {
                    Draw2D.VERTEX0.clear().pos(o0x, o0y).texCoord(Draw2D.u0, Draw2D.v0).color(_r0, _g0, _b0, _a0);
                    Draw2D.VERTEX1.clear().pos(o1x, o1y).texCoord(Draw2D.u0, Draw2D.v1).color(_r0, _g0, _b0, _a0);
                    Draw2D.VERTEX2.clear().pos(o2x, o2y).texCoord(Draw2D.u1, Draw2D.v1).color(_r0, _g0, _b0, _a0);
                    
                    windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
                }
                
                // Generates Line Strip
                Draw2D.VERTEX0.clear().pos(o2x, o2y).texCoord(Draw2D.u0, Draw2D.v0).color(_r0, _g0, _b0, _a0);
                Draw2D.VERTEX1.clear().pos(o1x, o1y).texCoord(Draw2D.u0, Draw2D.v1).color(_r0, _g0, _b0, _a0);
                Draw2D.VERTEX2.clear().pos(o3x, o3y).texCoord(Draw2D.u1, Draw2D.v1).color(_r1, _g1, _b1, _a1);
                Draw2D.VERTEX3.clear().pos(o4x, o4y).texCoord(Draw2D.u1, Draw2D.v1).color(_r1, _g1, _b1, _a1);
                
                windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
                windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX2, Draw2D.VERTEX3);
                
                _r0 = _r1;
                _g0 = _g1;
                _b0 = _b1;
                _a0 = _a1;
            }
        }
        GLBatch.end();
    }
    
    protected static void fillTriangle(double x0, double y0, double x1, double y1, double x2, double y2, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2)
    {
        GLBatch.checkBuffer(3);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        Draw2D.VERTEX0.clear().pos(x0, y0).texCoord(Draw2D.u0, Draw2D.v0).color(r0, g0, b0, a0);
        Draw2D.VERTEX1.clear().pos(x1, y1).texCoord(Draw2D.u0, Draw2D.v1).color(r1, g1, b1, a1);
        Draw2D.VERTEX2.clear().pos(x2, y2).texCoord(Draw2D.u1, Draw2D.v1).color(r2, g2, b2, a2);
        
        windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
        
        GLBatch.end();
    }
    
    protected static void fillQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2, int r3, int g3, int b3, int a3)
    {
        GLBatch.checkBuffer(4);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        Draw2D.VERTEX0.clear().pos(x0, y0).texCoord(Draw2D.u0, Draw2D.v0).color(r0, g0, b0, a0);
        Draw2D.VERTEX1.clear().pos(x1, y1).texCoord(Draw2D.u0, Draw2D.v1).color(r1, g1, b1, a1);
        Draw2D.VERTEX2.clear().pos(x2, y2).texCoord(Draw2D.u1, Draw2D.v1).color(r2, g2, b2, a2);
        Draw2D.VERTEX3.clear().pos(x3, y3).texCoord(Draw2D.u1, Draw2D.v0).color(r3, g3, b3, a3);
        
        windQuad();
        
        GLBatch.end();
    }
    
    protected static void drawRect(double x, double y, double w, double h, double thickness, double radius, double originX, double originY, double angle, int r, int g, int b, int a)
    {
        double halfW = w * 0.5;
        double halfH = h * 0.5;
        
        if (Double.compare(radius, 0.0) <= 0)
        {
            double x0, y0, x1, y1, x2, y2, x3, y3;
            
            // Only calculate rotation if needed
            if (Math.equals(angle, 0.0, 1e-6))
            {
                x0 = x - halfW;
                y0 = y - halfH;
                
                x1 = x - halfW;
                y1 = y + halfH;
                
                x2 = x + halfW;
                y2 = y + halfH;
                
                x3 = x + halfW;
                y3 = y - halfH;
            }
            else
            {
                double s = Math.sin(angle);
                double c = Math.cos(angle);
                
                double minCX = (-halfW - originX) * c;
                double maxCX = (halfW - originX) * c;
                double minSX = (-halfW - originX) * s;
                double maxSX = (halfW - originX) * s;
                double minCY = (-halfH - originY) * c;
                double maxCY = (halfH - originY) * c;
                double minSY = (-halfH - originY) * s;
                double maxSY = (halfH - originY) * s;
                
                x0 = x + originX + minCX - minSY;
                y0 = y + originY + minSX + minCY;
                
                x1 = x + originX + minCX - maxSY;
                y1 = y + originY + minSX + maxCY;
                
                x2 = x + originX + maxCX - maxSY;
                y2 = y + originY + maxSX + maxCY;
                
                x3 = x + originX + maxCX - minSY;
                y3 = y + originY + maxSX + minCY;
            }
            
            double[] points = {
                    x3, y3,
                    x0, y0,
                    x1, y1,
                    x2, y2,
                    x3, y3,
                    x0, y0,
                    x1, y1
            };
            drawLines(points, thickness, r, g, b, a, r, g, b, a);
        }
        else
        {
            if (Math.equals(angle, 0.0, 1e-6))
            {
                // double x0, y0, x1, y1;
                //
                // // Left Edge
                // x0 = x - halfW;
                // y0 = y - halfH + radius;
                // x1 = x - halfW;
                // y1 = y + halfH - radius;
                // drawLine(x0, y0, x1, y1, thickness, r, g, b, a, r, g, b, a);
                //
                // // Bottom Edge
                // x0 = x - halfW + radius;
                // y0 = y + halfH;
                // x1 = x + halfW - radius;
                // y1 = y + halfH;
                // drawLine(x0, y0, x1, y1, thickness, r, g, b, a, r, g, b, a);
                //
                // // Right Edge
                // x0 = x + halfW;
                // y0 = y + halfH - radius;
                // x1 = x + halfW;
                // y1 = y - halfH + radius;
                // drawLine(x0, y0, x1, y1, thickness, r, g, b, a, r, g, b, a);
                //
                // // Top Edge
                // x0 = x + halfW - radius;
                // y0 = y - halfH;
                // x1 = x - halfW + radius;
                // y1 = y - halfH;
                // drawLine(x0, y0, x1, y1, thickness, r, g, b, a, r, g, b, a);
                //
                // int    segments = segments(radius, radius, 0, Math.PI_2);
                // double start, stop;
                //
                // // Top-Left Corner
                // x0    = x - halfW + radius;
                // y0    = y - halfH + radius;
                // start = Math.PI;
                // stop  = 3 * Math.PI_2;
                // drawEllipse(x0, y0, radius, radius, thickness, start, stop, 0, 0, 0, segments, r, g, b, a);
                //
                // // Bottom-Left Corner
                // x0    = x - halfW + radius;
                // y0    = y + halfH - radius;
                // start = Math.PI_2;
                // stop  = Math.PI;
                // drawEllipse(x0, y0, radius, radius, thickness, start, stop, 0, 0, 0, segments, r, g, b, a);
                //
                // // Bottom-Right Corner
                // x0    = x + halfW - radius;
                // y0    = y + halfH - radius;
                // start = 0;
                // stop  = Math.PI_2;
                // drawEllipse(x0, y0, radius, radius, thickness, start, stop, 0, 0, 0, segments, r, g, b, a);
                //
                // // Top-Left Corner
                // x0    = x + halfW - radius;
                // y0    = y - halfH + radius;
                // start = 3 * Math.PI_2;
                // stop  = Math.PI2;
                // drawEllipse(x0, y0, radius, radius, thickness, start, stop, 0, 0, 0, segments, r, g, b, a);
                
                // int segments = segments(radius, radius, 0, Math.PI_2);
                int segments = 4;
                
            }
            else
            {
            
            }
        }
    }
    
    protected static void drawEllipse(double x, double y, double rx, double ry, double thickness, double start, double stop, double originX, double originY, double angle, int segments, int r, int g, int b, int a)
    {
        while (start > Math.PI2 + 1e-6) start -= Math.PI2;
        while (start < -Math.PI2 - 1e-6) start += Math.PI2;
        
        while (stop > Math.PI2 + 1e-6) stop -= Math.PI2;
        while (stop < -Math.PI2 - 1e-6) stop += Math.PI2;
        
        if (Math.equals(start, stop, 1e-6)) return;
        
        if (start > stop)
        {
            double temp = start;
            start = stop;
            stop  = temp;
        }
        
        if (segments < 3) segments = segments(rx, ry, start, stop);
        
        double[] points = new double[(segments + 2 + 1) << 1];
        
        double theta;
        
        boolean shouldRotate = !Math.equals(angle, 0.0, 1e-6);
        double  tempX, tempY;
        
        double s = shouldRotate ? Math.sin(angle) : 0.0;
        double c = shouldRotate ? Math.cos(angle) : 0.0;
        
        double px, py;
        
        // Segments + 2 to make sure that the end segments are beveled
        for (int i = -1, index = 0; i <= segments + 1; i++)
        {
            // theta = start + i * inc;
            theta = Math.map(i, 0, segments, start, stop);
            
            px = Math.cos(theta) * rx;
            py = Math.sin(theta) * ry;
            if (shouldRotate)
            {
                tempX = px - originX;
                tempY = py - originY;
                px    = tempX * c - tempY * s + originX;
                py    = tempX * s + tempY * c + originY;
            }
            px += x;
            py += y;
            
            // Add Generated points to array.
            points[index++] = px;
            points[index++] = py;
        }
        // points[0] = points[2];
        // points[1] = points[3];
        // points[points.length - 2] = points[points.length - 4];
        // points[points.length - 1] = points[points.length - 3];
        
        drawLines(points, thickness, r, g, b, a, r, g, b, a);
    }
    
    protected static void fillEllipse(double x, double y, double rx, double ry, double start, double stop, double originX, double originY, double angle, int segments, int ri, int gi, int bi, int ai, int ro, int go, int bo, int ao)
    {
        while (start > Math.PI2 + 1e-6) start -= Math.PI2;
        while (start < -Math.PI2 - 1e-6) start += Math.PI2;
        
        while (stop > Math.PI2 + 1e-6) stop -= Math.PI2;
        while (stop < -Math.PI2 - 1e-6) stop += Math.PI2;
        
        if (Math.equals(start, stop, 1e-6)) return;
        
        if (start > stop)
        {
            double temp = start;
            start = stop;
            stop  = temp;
        }
        
        if (segments < 3) segments = segments(rx, ry, start, stop);
        
        double inc   = (stop - start) / segments;
        double theta = start;
        
        double  tempX, tempY;
        boolean shouldRotate = !Math.equals(angle, 0.0, 1e-6);
        
        double s = shouldRotate ? Math.sin(angle) : 0;
        double c = shouldRotate ? Math.cos(angle) : 0;
        
        double cx, cy;
        double p0x, p0y, p1x, p1y;
        
        cx  = 0.0;
        cy  = 0.0;
        p0x = Math.cos(theta) * rx;
        p0y = Math.sin(theta) * ry;
        if (shouldRotate)
        {
            tempX = cx - originX;
            tempY = cy - originY;
            cx    = tempX * c - tempY * s + originX;
            cy    = tempX * s + tempY * c + originY;
            tempX = p0x - originX;
            tempY = p0y - originY;
            p0x   = tempX * c - tempY * s + originX;
            p0y   = tempX * s + tempY * c + originY;
        }
        cx += x;
        cy += y;
        p0x += x;
        p0y += y;
        
        GLBatch.checkBuffer(segments * 3);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        Draw2D.VERTEX0.clear().pos(cx, cy).texCoord(Draw2D.u1, Draw2D.v1).color(ri, gi, bi, ai);
        
        for (int i = 0; i < segments; i++)
        {
            theta = start + (i + 1) * inc;
            p1x   = Math.cos(theta) * rx;
            p1y   = Math.sin(theta) * ry;
            if (shouldRotate)
            {
                tempX = p1x - originX;
                tempY = p1y - originY;
                p1x   = tempX * c - tempY * s + originX;
                p1y   = tempX * s + tempY * c + originY;
            }
            p1x += x;
            p1y += y;
            
            Draw2D.VERTEX1.clear().pos(p0x, p0y).texCoord(Draw2D.u0, Draw2D.v0).color(ro, go, bo, ao);
            Draw2D.VERTEX2.clear().pos(p1x, p1y).texCoord(Draw2D.u1, Draw2D.v0).color(ro, go, bo, ao);
            
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            
            p0x = p1x;
            p0y = p1y;
        }
        GLBatch.end();
    }
    
    protected static void fillRing(double x, double y, double rxi, double ryi, double rxo, double ryo, double start, double stop, double originX, double originY, double angle, int segments, int ri, int gi, int bi, int ai, int ro, int go, int bo, int ao)
    {
        while (start > Math.PI2 + 1e-6) start -= Math.PI2;
        while (start < -Math.PI2 - 1e-6) start += Math.PI2;
        
        while (stop > Math.PI2 + 1e-6) stop -= Math.PI2;
        while (stop < -Math.PI2 - 1e-6) stop += Math.PI2;
        
        if (Math.equals(start, stop, 1e-6)) return;
        
        if (start > stop)
        {
            double temp = start;
            start = stop;
            stop  = temp;
        }
        
        if (segments < 3) segments = segments(Math.max(Math.abs(rxi), Math.abs(rxo)), Math.max(Math.abs(ryi), Math.abs(ryo)), start, stop);
        
        double inc   = (stop - start) / segments;
        double theta = start;
        
        double  tempX, tempY;
        boolean shouldRotate = !Math.equals(angle, 0.0, 1e-6);
        
        double s = shouldRotate ? Math.sin(angle) : 0;
        double c = shouldRotate ? Math.cos(angle) : 0;
        
        double cos, sin, x0i, y0i, x0o, y0o, x1i, y1i, x1o, y1o;
        
        cos = Math.cos(theta);
        sin = Math.sin(theta);
        x0i = cos * rxi;
        y0i = sin * ryi;
        x0o = cos * rxo;
        y0o = sin * ryo;
        if (shouldRotate)
        {
            tempX = x0i - originX;
            tempY = y0i - originY;
            x0i   = tempX * c - tempY * s + originX;
            y0i   = tempX * s + tempY * c + originY;
            tempX = x0o - originX;
            tempY = y0o - originY;
            x0o   = tempX * c - tempY * s + originX;
            y0o   = tempX * s + tempY * c + originY;
        }
        x0i += x;
        y0i += y;
        x0o += x;
        y0o += y;
        
        GLBatch.checkBuffer(segments * 4);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        for (int i = 0; i < segments; i++)
        {
            theta = start + (i + 1) * inc;
            cos   = Math.cos(theta);
            sin   = Math.sin(theta);
            x1i   = cos * rxi;
            y1i   = sin * ryi;
            x1o   = cos * rxo;
            y1o   = sin * ryo;
            if (shouldRotate)
            {
                tempX = x1i - originX;
                tempY = y1i - originY;
                x1i   = tempX * c - tempY * s + originX;
                y1i   = tempX * s + tempY * c + originY;
                tempX = x1o - originX;
                tempY = y1o - originY;
                x1o   = tempX * c - tempY * s + originX;
                y1o   = tempX * s + tempY * c + originY;
            }
            x1i += x;
            y1i += y;
            x1o += x;
            y1o += y;
            
            Draw2D.VERTEX0.clear().pos(x0o, y0o).texCoord(Draw2D.u0, Draw2D.v0).color(ro, go, bo, ao);
            Draw2D.VERTEX1.clear().pos(x0i, y0i).texCoord(Draw2D.u0, Draw2D.v1).color(ri, gi, bi, ai);
            Draw2D.VERTEX2.clear().pos(x1i, y1i).texCoord(Draw2D.u1, Draw2D.v1).color(ri, gi, bi, ai);
            Draw2D.VERTEX3.clear().pos(x1o, y1o).texCoord(Draw2D.u1, Draw2D.v0).color(ro, go, bo, ao);
            
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX2, Draw2D.VERTEX3);
            
            x0i = x1i;
            y0i = y1i;
            x0o = x1o;
            y0o = y1o;
        }
        GLBatch.end();
    }
    
    protected static void drawTexture(@NotNull GLTexture texture, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double u0, double v0, double u1, double v1, double u2, double v2, double u3, double v3, int r, int g, int b, int a)
    {
        GLBatch.checkBuffer(4); // Make sure there is enough free space on the GLBatch buffer
        
        GLBatch.setTexture(texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        Draw2D.VERTEX0.clear().pos(x0, y0).texCoord(u0, v0).color(r, g, b, a);
        Draw2D.VERTEX1.clear().pos(x1, y1).texCoord(u1, v1).color(r, g, b, a);
        Draw2D.VERTEX2.clear().pos(x2, y2).texCoord(u2, v2).color(r, g, b, a);
        Draw2D.VERTEX3.clear().pos(x3, y3).texCoord(u3, v3).color(r, g, b, a);
        
        windQuad();
        
        GLBatch.end();
    }
    
    protected static void drawTexture(@NotNull GLTexture texture, double srcX, double srcY, double srcW, double srcH, double dstX, double dstY, double dstW, double dstH, double originX, double originY, double angle, int r, int g, int b, int a)
    {
        double width  = texture.width();
        double height = texture.height();
        
        boolean flipX = false;
        
        if (srcW < 0)
        {
            flipX = true;
            srcW *= -1;
        }
        if (srcH < 0) srcY -= srcH;
        
        double x0, y0, x1, y1, x2, y2, x3, y3;
        
        // Only calculate rotation if needed
        if (Math.equals(angle, 0.0, 1e-6))
        {
            double dx = dstX - originX;
            double dy = dstY - originY;
            
            x0 = dx;
            y0 = dy;
            
            x1 = dx;
            y1 = dy + dstH;
            
            x2 = dx + dstW;
            y2 = dy + dstH;
            
            x3 = dx + dstW;
            y3 = dy;
        }
        else
        {
            double sin = Math.sin(angle);
            double cos = Math.cos(angle);
            double dx  = -originX;
            double dy  = -originY;
            
            x0 = dstX + dx * cos - dy * sin;
            y0 = dstY + dx * sin + dy * cos;
            
            x1 = dstX + dx * cos - (dy + dstH) * sin;
            y1 = dstY + dx * sin + (dy + dstH) * cos;
            
            x2 = dstX + (dx + dstW) * cos - (dy + dstH) * sin;
            y2 = dstY + (dx + dstW) * sin + (dy + dstH) * cos;
            
            x3 = dstX + (dx + dstW) * cos - dy * sin;
            y3 = dstY + (dx + dstW) * sin + dy * cos;
        }
        
        double u0 = (srcX + (flipX ? srcW : 0)) / width;
        double v0 = srcY / height;
        double u1 = (srcX + (flipX ? 0 : srcW)) / width;
        double v1 = (srcY + srcH) / height;
        
        drawTexture(texture, x0, y0, x1, y1, x2, y2, x3, y3, u0, v0, u0, v1, u1, v1, u1, v0, r, g, b, a);
    }
    
    public void drawText(@NotNull TextState state, @NotNull String line, double x, double y)
    {
        double scale = state.currFont.scale(state.size);
        
        ArrayList<GLBatch.VertexGroup> textVertices = new ArrayList<>();
        ArrayList<GLBatch.VertexGroup> quadVertices = new ArrayList<>();
        
        CharData prevChar = null, currChar;
        for (int i = 0, n = line.length(); i < n; i++)
        {
            char character = line.charAt(i);
            
            if (state.handleModifier(character)) continue;
            
            state.changeFont();
            
            currChar = state.currFont.charData.get(character);
            
            x += state.currFont.getKernAdvanceUnscaled(prevChar, currChar) * scale;
            
            double x0 = x + currChar.x0Unscaled() * scale;
            double y0 = y + currChar.y0Unscaled() * scale;
            double x1 = x + currChar.x1Unscaled() * scale;
            double y1 = y + currChar.y1Unscaled() * scale;
            
            textVertices.add(new GLBatch.VertexGroup(
                    state.currFont.texture,
                    new GLBatch.Vertex()
                            .pos(x0, y0) // v0
                            .texCoord(currChar.u0(), currChar.v0())
                            .color(state.textR, state.textG, state.textB, state.textA),
                    new GLBatch.Vertex()
                            .pos(x0, y1) // v1
                            .texCoord(currChar.u0(), currChar.v1())
                            .color(state.textR, state.textG, state.textB, state.textA),
                    new GLBatch.Vertex()
                            .pos(x1, y1) // v2
                            .texCoord(currChar.u1(), currChar.v1())
                            .color(state.textR, state.textG, state.textB, state.textA),
                    new GLBatch.Vertex()
                            .pos(x1, y0) // v3
                            .texCoord(currChar.u1(), currChar.v0())
                            .color(state.textR, state.textG, state.textB, state.textA)
            ));
            
            double advance = currChar.advanceWidthUnscaled() * scale;
            
            if (state.backA != 0)
            {
                x0 = x;
                y0 = y;
                x1 = x0 + advance;
                y1 = y0 + (state.currFont.ascentUnscaled - state.currFont.descentUnscaled) * scale;
                
                quadVertices.add(new GLBatch.VertexGroup(
                        null,
                        new GLBatch.Vertex()
                                .pos(x0, y0) // v0
                                .color(state.backR, state.backG, state.backB, state.backA),
                        new GLBatch.Vertex()
                                .pos(x0, y1) // v1
                                .color(state.backR, state.backG, state.backB, state.backA),
                        new GLBatch.Vertex()
                                .pos(x1, y1) // v2
                                .color(state.backR, state.backG, state.backB, state.backA),
                        new GLBatch.Vertex()
                                .pos(x1, y0) // v3
                                .color(state.backR, state.backG, state.backB, state.backA)
                ));
            }
            
            if (state.underline)
            {
                x0 = x;
                y0 = y + state.currFont.ascentUnscaled * scale * 1.05F;
                x1 = x0 + advance;
                y1 = y0 + (100 * scale);
                
                textVertices.add(new GLBatch.VertexGroup(
                        null,
                        new GLBatch.Vertex()
                                .pos(x0, y0) // v0
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x0, y1) // v1
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x1, y1) // v2
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x1, y0) // v3
                                .color(state.textR, state.textG, state.textB, state.textA)
                ));
            }
            
            if (state.strike)
            {
                x0 = x;
                y0 = y + state.currFont.ascentUnscaled * scale * 0.65F;
                x1 = x0 + advance;
                y1 = y0 + (100 * scale);
                
                textVertices.add(new GLBatch.VertexGroup(
                        null,
                        new GLBatch.Vertex()
                                .pos(x0, y0) // v0
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x0, y1) // v1
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x1, y1) // v2
                                .color(state.textR, state.textG, state.textB, state.textA),
                        new GLBatch.Vertex()
                                .pos(x1, y0) // v3
                                .color(state.textR, state.textG, state.textB, state.textA)
                ));
            }
            
            x += advance;
            
            prevChar = currChar;
        }
        
        for (GLBatch.VertexGroup vertexGroup : quadVertices)
        {
            GLBatch.Vertex[] vertices = vertexGroup.points();
            
            GLBatch.checkBuffer(6);
            GLBatch.setTexture(GL.defaultTexture());
            GLBatch.begin(DrawMode.TRIANGLES);
            GLBatch.vertex(vertices[0]);
            GLBatch.vertex(vertices[1]);
            GLBatch.vertex(vertices[2]);
            GLBatch.vertex(vertices[0]);
            GLBatch.vertex(vertices[2]);
            GLBatch.vertex(vertices[3]);
            GLBatch.end();
        }
        
        for (GLBatch.VertexGroup vertexGroup : textVertices)
        {
            GLBatch.Vertex[] vertices = vertexGroup.points();
            
            GLBatch.checkBuffer(6);
            GLBatch.setTexture(vertexGroup.texture());
            GLBatch.begin(DrawMode.TRIANGLES);
            GLBatch.vertex(vertices[0]);
            GLBatch.vertex(vertices[1]);
            GLBatch.vertex(vertices[2]);
            GLBatch.vertex(vertices[0]);
            GLBatch.vertex(vertices[2]);
            GLBatch.vertex(vertices[3]);
            GLBatch.end();
        }
    }
    
    protected static int segments(double rx, double ry)
    {
        return segments(rx, ry, 0, Math.PI2);
    }
    
    protected static int segments(double rx, double ry, double start, double stop)
    {
        return Math.clamp((int) (Math.max(Math.abs(rx), Math.abs(ry)) * (stop - start) / Math.PI2), 8, 48);
    }
    
    private static void windTriangle(GLBatch.Vertex v0, GLBatch.Vertex v1, GLBatch.Vertex v2)
    {
        GLBatch.vertex(v0);
        
        double cross = (v1.x - v0.x) * (v2.y - v1.y) - (v1.y - v0.y) * (v2.x - v1.x);
        if (cross > 0.0)
        {
            GLBatch.vertex(v2);
            GLBatch.vertex(v1);
        }
        else
        {
            GLBatch.vertex(v1);
            GLBatch.vertex(v2);
        }
    }
    
    private static void windQuad()
    {
        double rd = (Draw2D.VERTEX2.x - Draw2D.VERTEX0.x) * (Draw2D.VERTEX3.y - Draw2D.VERTEX1.y) -
                    (Draw2D.VERTEX3.x - Draw2D.VERTEX1.x) * (Draw2D.VERTEX2.y - Draw2D.VERTEX0.y);
        
        if (rd == 0.0) return;
        
        rd = 1.0 / rd;
        
        double cx = 0.0, cy = 0.0;
        
        double rn = ((Draw2D.VERTEX3.x - Draw2D.VERTEX1.x) * (Draw2D.VERTEX0.y - Draw2D.VERTEX1.y) -
                     (Draw2D.VERTEX3.y - Draw2D.VERTEX1.y) * (Draw2D.VERTEX0.x - Draw2D.VERTEX1.x)) * rd;
        double sn = ((Draw2D.VERTEX2.x - Draw2D.VERTEX0.x) * (Draw2D.VERTEX0.y - Draw2D.VERTEX1.y) -
                     (Draw2D.VERTEX2.y - Draw2D.VERTEX0.y) * (Draw2D.VERTEX0.x - Draw2D.VERTEX1.x)) * rd;
        
        if (!(rn < 0.0 || rn > 1.0 || sn < 0.0 || sn > 1.0))
        {
            cx = Draw2D.VERTEX0.x + rn * (Draw2D.VERTEX2.x - Draw2D.VERTEX0.x);
            cy = Draw2D.VERTEX0.y + rn * (Draw2D.VERTEX2.y - Draw2D.VERTEX0.y);
        }
        
        double d0 = Math.sqrt((Draw2D.VERTEX0.x - cx) * (Draw2D.VERTEX0.x - cx) +
                              (Draw2D.VERTEX0.y - cy) * (Draw2D.VERTEX0.y - cy));
        double d1 = Math.sqrt((Draw2D.VERTEX1.x - cx) * (Draw2D.VERTEX1.x - cx) +
                              (Draw2D.VERTEX1.y - cy) * (Draw2D.VERTEX1.y - cy));
        double d2 = Math.sqrt((Draw2D.VERTEX2.x - cx) * (Draw2D.VERTEX2.x - cx) +
                              (Draw2D.VERTEX2.y - cy) * (Draw2D.VERTEX2.y - cy));
        double d3 = Math.sqrt((Draw2D.VERTEX3.x - cx) * (Draw2D.VERTEX3.x - cx) +
                              (Draw2D.VERTEX3.y - cy) * (Draw2D.VERTEX3.y - cy));
        
        Draw2D.VERTEX0.q = d2 == 0.0 ? 1.0 : (d0 + d2) / d2;
        Draw2D.VERTEX1.q = d3 == 0.0 ? 1.0 : (d1 + d3) / d3;
        Draw2D.VERTEX2.q = d0 == 0.0 ? 1.0 : (d2 + d0) / d0;
        Draw2D.VERTEX3.q = d1 == 0.0 ? 1.0 : (d3 + d1) / d1;
        
        double cross012 = (Draw2D.VERTEX1.x - Draw2D.VERTEX0.x) * (Draw2D.VERTEX2.y - Draw2D.VERTEX1.y) -
                          (Draw2D.VERTEX1.y - Draw2D.VERTEX0.y) * (Draw2D.VERTEX2.x - Draw2D.VERTEX1.x);
        double cross032 = (Draw2D.VERTEX3.x - Draw2D.VERTEX0.x) * (Draw2D.VERTEX2.y - Draw2D.VERTEX3.y) -
                          (Draw2D.VERTEX3.y - Draw2D.VERTEX0.y) * (Draw2D.VERTEX2.x - Draw2D.VERTEX3.x);
        
        if (cross012 < 0.0 && cross032 > 0.0)
        {
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX2, Draw2D.VERTEX3);
        }
        else
        {
            windTriangle(Draw2D.VERTEX3, Draw2D.VERTEX2, Draw2D.VERTEX1);
            windTriangle(Draw2D.VERTEX3, Draw2D.VERTEX1, Draw2D.VERTEX0);
        }
    }
    
    public Draw2D()
    {
        reset();
    }
    
    protected abstract void reset();
    
    protected abstract void check();
    
    protected abstract void drawImpl();
    
    public void draw()
    {
        Draw2D.LOGGER.finest(this);
        
        check();
        
        drawImpl();
        
        reset();
    }
}
