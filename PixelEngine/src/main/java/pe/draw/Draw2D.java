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
    
    protected static void drawPoint(double x, double y,
                                    double thickness,
                                    int r, int g, int b, int a)
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
            fillEllipse(x, y,
                        thickness, thickness,
                        0, Math.PI2,
                        0.0, 0.0, 0.0,
                        segments(thickness, thickness),
                        r, g, b, a,
                        r, g, b, a,
                        r, g, b, a,
                        r, g, b, a);
        }
    }
    
    protected static void drawLine(double x0, double y0,
                                   double x1, double y1,
                                   double thickness,
                                   int r0, int g0, int b0, int a0,
                                   int r1, int g1, int b1, int a1)
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
    
    protected static void drawLines(double[] points,
                                    double thickness,
                                    int r0, int g0, int b0, int a0,
                                    int r1, int g1, int b1, int a1)
    {
        int segments = (points.length >> 1) - 3;
        
        if (segments < 1)
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
            GLBatch.checkBuffer(segments * 2);
            
            GLBatch.begin(DrawMode.LINES);
            
            int    p0, p1;
            double lerp;
            for (int i = 0; i < segments; i++)
            {
                p0 = (i + 1) << 1;
                p1 = (i + 2) << 1;
                
                lerp = (double) (i + 1) / (double) segments;
                
                _r1 = lerp(r0, r1, lerp);
                _g1 = lerp(g0, g1, lerp);
                _b1 = lerp(b0, b1, lerp);
                _a1 = lerp(a0, a1, lerp);
                
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
            
            GLBatch.checkBuffer(segments * 9);
            
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
            for (int i = 0; i < segments; i++)
            {
                p0 = i << 1;
                p1 = (i + 1) << 1;
                p2 = (i + 2) << 1;
                p3 = (i + 3) << 1;
                
                lerp = (double) (i + 1) / (double) segments;
                
                _r1 = lerp(r0, r1, lerp);
                _g1 = lerp(g0, g1, lerp);
                _b1 = lerp(b0, b1, lerp);
                _a1 = lerp(a0, a1, lerp);
                
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
    
    protected static void fillTriangle(double x0, double y0,
                                       double x1, double y1,
                                       double x2, double y2,
                                       int r0, int g0, int b0, int a0,
                                       int r1, int g1, int b1, int a1,
                                       int r2, int g2, int b2, int a2)
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
    
    protected static void fillQuad(double x0, double y0,
                                   double x1, double y1,
                                   double x2, double y2,
                                   double x3, double y3,
                                   int r0, int g0, int b0, int a0,
                                   int r1, int g1, int b1, int a1,
                                   int r2, int g2, int b2, int a2,
                                   int r3, int g3, int b3, int a3)
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
    
    protected static void drawRect(double x, double y,
                                   double width, double height,
                                   double thickness,
                                   double cornerRadius,
                                   double rotationOriginX, double rotationOriginY, double rotationAngle,
                                   int r, int g, int b, int a)
    {
        double halfW = width * 0.5;
        double halfH = height * 0.5;
        
        if (Double.compare(cornerRadius, 0.0) <= 0)
        {
            double tlX, tlY, trX, trY, brX, brY, blX, blY;
            
            // Only calculate rotation if needed
            if (Math.equals(rotationAngle, 0.0, 1e-6))
            {
                tlX = x - halfW;
                tlY = y - halfH;
                trX = x + halfW;
                trY = y - halfH;
                brX = x + halfW;
                brY = y + halfH;
                blX = x - halfW;
                blY = y + halfH;
            }
            else
            {
                double s = Math.sin(rotationAngle);
                double c = Math.cos(rotationAngle);
                
                double minCX = (-halfW - rotationOriginX) * c;
                double minCY = (-halfH - rotationOriginY) * c;
                double minSX = (-halfW - rotationOriginX) * s;
                double minSY = (-halfH - rotationOriginY) * s;
                double maxCX = (halfW - rotationOriginX) * c;
                double maxCY = (halfH - rotationOriginY) * c;
                double maxSX = (halfW - rotationOriginX) * s;
                double maxSY = (halfH - rotationOriginY) * s;
                
                tlX = x + rotationOriginX + minCX - minSY;
                tlY = y + rotationOriginY + minSX + minCY;
                trX = x + rotationOriginX + maxCX - minSY;
                trY = y + rotationOriginY + maxSX + minCY;
                brX = x + rotationOriginX + maxCX - maxSY;
                brY = y + rotationOriginY + maxSX + maxCY;
                blX = x + rotationOriginX + minCX - maxSY;
                blY = y + rotationOriginY + minSX + maxCY;
            }
            
            double[] points = {
                    blX, blY,
                    tlX, tlY,
                    trX, trY,
                    brX, brY,
                    blX, blY,
                    tlX, tlY,
                    trX, trY
            };
            drawLines(points, thickness, r, g, b, a, r, g, b, a);
        }
        else
        {
            halfW = Math.abs(halfW);
            halfH = Math.abs(halfH);
            
            int segments = segments(cornerRadius, cornerRadius, 0, Math.PI_2);
            
            int      pointCount = ((segments + 1) * 4 + 3) << 1;
            double[] points     = new double[pointCount];
            
            double topLeftCX, topLeftCY;
            double topRightCX, topRightCY;
            double bottomLeftCX, bottomLeftCY;
            double bottomRightCX, bottomRightCY;
            if (Math.equals(rotationAngle, 0.0, 1e-6))
            {
                topLeftCX     = x - halfW + cornerRadius;
                topLeftCY     = y - halfH + cornerRadius;
                topRightCX    = x + halfW - cornerRadius;
                topRightCY    = y - halfH + cornerRadius;
                bottomLeftCX  = x - halfW + cornerRadius;
                bottomLeftCY  = y + halfH - cornerRadius;
                bottomRightCX = x + halfW - cornerRadius;
                bottomRightCY = y + halfH - cornerRadius;
            }
            else
            {
                double s = Math.sin(rotationAngle);
                double c = Math.cos(rotationAngle);
                
                double minCX = (-halfW + cornerRadius - rotationOriginX) * c;
                double minCY = (-halfH + cornerRadius - rotationOriginY) * c;
                double minSX = (-halfW + cornerRadius - rotationOriginX) * s;
                double minSY = (-halfH + cornerRadius - rotationOriginY) * s;
                double maxCX = (halfW - cornerRadius - rotationOriginX) * c;
                double maxCY = (halfH - cornerRadius - rotationOriginY) * c;
                double maxSX = (halfW - cornerRadius - rotationOriginX) * s;
                double maxSY = (halfH - cornerRadius - rotationOriginY) * s;
                
                topLeftCX     = x + rotationOriginX + minCX - minSY;
                topLeftCY     = y + rotationOriginY + minSX + minCY;
                topRightCX    = x + rotationOriginX + maxCX - minSY;
                topRightCY    = y + rotationOriginY + maxSX + minCY;
                bottomLeftCX  = x + rotationOriginX + minCX - maxSY;
                bottomLeftCY  = y + rotationOriginY + minSX + maxCY;
                bottomRightCX = x + rotationOriginX + maxCX - maxSY;
                bottomRightCY = y + rotationOriginY + maxSX + maxCY;
            }
            
            double theta;
            double topLeftX, topLeftY;
            double topRightX, topRightY;
            double bottomLeftX, bottomLeftY;
            double bottomRightX, bottomRightY;
            for (int i = 0, idx, offset = (segments + 1) << 1; i <= segments; i++)
            {
                theta = Math.map(i, 0, segments, rotationAngle, rotationAngle + Math.PI_2);
                
                topLeftX     = topLeftCX + cornerRadius * Math.cos(theta + Math.PI);
                topLeftY     = topLeftCY + cornerRadius * Math.sin(theta + Math.PI);
                topRightX    = topRightCX + cornerRadius * Math.cos(theta + Math.PI_2 + Math.PI);
                topRightY    = topRightCY + cornerRadius * Math.sin(theta + Math.PI_2 + Math.PI);
                bottomLeftX  = bottomLeftCX + cornerRadius * Math.cos(theta + Math.PI_2);
                bottomLeftY  = bottomLeftCY + cornerRadius * Math.sin(theta + Math.PI_2);
                bottomRightX = bottomRightCX + cornerRadius * Math.cos(theta);
                bottomRightY = bottomRightCY + cornerRadius * Math.sin(theta);
                
                idx = (i + 1) << 1;
                
                points[idx]     = topLeftX;
                points[idx + 1] = topLeftY;
                
                idx += offset;
                
                points[idx]     = topRightX;
                points[idx + 1] = topRightY;
                
                idx += offset;
                
                points[idx]     = bottomRightX;
                points[idx + 1] = bottomRightY;
                
                idx += offset;
                
                points[idx]     = bottomLeftX;
                points[idx + 1] = bottomLeftY;
            }
            points[0]              = points[pointCount - 6];
            points[1]              = points[pointCount - 5];
            points[pointCount - 4] = points[2];
            points[pointCount - 3] = points[3];
            points[pointCount - 2] = points[4];
            points[pointCount - 1] = points[5];
            
            drawLines(points, thickness, r, g, b, a, r, g, b, a);
        }
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    protected static void fillRect(double x, double y,
                                   double width, double height,
                                   double cornerRadius,
                                   double rotationOriginX, double rotationOriginY, double rotationAngle,
                                   int topLeftR, int topLeftG, int topLeftB, int topLeftA,
                                   int topRightR, int topRightG, int topRightB, int topRightA,
                                   int bottomLeftR, int bottomLeftG, int bottomLeftB, int bottomLeftA,
                                   int bottomRightR, int bottomRightG, int bottomRightB, int bottomRightA)
    {
        double halfW = width * 0.5;
        double halfH = height * 0.5;
        
        double topLeftX, topLeftY;
        double topRightX, topRightY;
        double bottomLeftX, bottomLeftY;
        double bottomRightX, bottomRightY;
        
        if (Double.compare(cornerRadius, 0.0) <= 0)
        {
            // Only calculate rotation if needed
            if (Math.equals(rotationAngle, 0.0, 1e-6))
            {
                topLeftX     = x - halfW;
                topLeftY     = y - halfH;
                topRightX    = x + halfW;
                topRightY    = y - halfH;
                bottomRightX = x + halfW;
                bottomRightY = y + halfH;
                bottomLeftX  = x - halfW;
                bottomLeftY  = y + halfH;
            }
            else
            {
                double s = Math.sin(rotationAngle);
                double c = Math.cos(rotationAngle);
                
                double minCX = (-halfW - rotationOriginX) * c;
                double minCY = (-halfH - rotationOriginY) * c;
                double minSX = (-halfW - rotationOriginX) * s;
                double minSY = (-halfH - rotationOriginY) * s;
                double maxCX = (halfW - rotationOriginX) * c;
                double maxCY = (halfH - rotationOriginY) * c;
                double maxSX = (halfW - rotationOriginX) * s;
                double maxSY = (halfH - rotationOriginY) * s;
                
                topLeftX     = x + rotationOriginX + minCX - minSY;
                topLeftY     = y + rotationOriginY + minSX + minCY;
                topRightX    = x + rotationOriginX + maxCX - minSY;
                topRightY    = y + rotationOriginY + maxSX + minCY;
                bottomRightX = x + rotationOriginX + maxCX - maxSY;
                bottomRightY = y + rotationOriginY + maxSX + maxCY;
                bottomLeftX  = x + rotationOriginX + minCX - maxSY;
                bottomLeftY  = y + rotationOriginY + minSX + maxCY;
            }
            
            fillQuad(topLeftX, topLeftY, topRightX, topRightY, bottomRightX, bottomRightY, bottomLeftX, bottomLeftY,
                     topLeftR, topLeftG, topLeftB, topLeftA,
                     topRightR, topRightG, topRightB, topRightA,
                     bottomRightR, bottomRightG, bottomRightB, bottomRightA,
                     bottomLeftR, bottomLeftG, bottomLeftB, bottomLeftA);
        }
        else
        {
            double dirHX, dirHY, dirVX, dirVY;
            
            if (Math.equals(rotationAngle, 0.0, 1e-6))
            {
                topLeftX     = x - halfW;
                topLeftY     = y - halfH;
                topRightX    = x + halfW;
                topRightY    = y - halfH;
                bottomLeftX  = x - halfW;
                bottomLeftY  = y + halfH;
                bottomRightX = x + halfW;
                bottomRightY = y + halfH;
                
                dirHX = halfW < 0.0 ? -cornerRadius : +cornerRadius;
                dirHY = 0.0;
                dirVX = 0.0;
                dirVY = halfH < 0.0 ? -cornerRadius : +cornerRadius;
                
            }
            else
            {
                double c = Math.cos(rotationAngle);
                double s = Math.sin(rotationAngle);
                
                double minCX = (-halfW - rotationOriginX) * c;
                double minCY = (-halfH - rotationOriginY) * c;
                double minSX = (-halfW - rotationOriginX) * s;
                double minSY = (-halfH - rotationOriginY) * s;
                double maxCX = (halfW - rotationOriginX) * c;
                double maxCY = (halfH - rotationOriginY) * c;
                double maxSX = (halfW - rotationOriginX) * s;
                double maxSY = (halfH - rotationOriginY) * s;
                
                topLeftX     = x + rotationOriginX + minCX - minSY;
                topLeftY     = y + rotationOriginY + minSX + minCY;
                topRightX    = x + rotationOriginX + maxCX - minSY;
                topRightY    = y + rotationOriginY + maxSX + minCY;
                bottomLeftX  = x + rotationOriginX + minCX - maxSY;
                bottomLeftY  = y + rotationOriginY + minSX + maxCY;
                bottomRightX = x + rotationOriginX + maxCX - maxSY;
                bottomRightY = y + rotationOriginY + maxSX + maxCY;
                
                dirHX = (halfW < 0.0 ? -cornerRadius : +cornerRadius) * c;
                dirHY = (halfW < 0.0 ? -cornerRadius : +cornerRadius) * s;
                dirVX = (halfH < 0.0 ? -cornerRadius : +cornerRadius) * Math.cos(rotationAngle + Math.PI_2);
                dirVY = (halfH < 0.0 ? -cornerRadius : +cornerRadius) * Math.sin(rotationAngle + Math.PI_2);
            }
            
            double topLeftCX     = topLeftX + dirHX + dirVX;
            double topLeftCY     = topLeftY + dirHY + dirVY;
            double topRightCX    = topRightX - dirHX + dirVX;
            double topRightCY    = topRightY - dirHY + dirVY;
            double bottomLeftCX  = bottomLeftX + dirHX - dirVX;
            double bottomLeftCY  = bottomLeftY + dirHY - dirVY;
            double bottomRightCX = bottomRightX - dirHX - dirVX;
            double bottomRightCY = bottomRightY - dirHY - dirVY;
            
            double leftTX = topLeftX + dirVX;
            double leftTY = topLeftY + dirVY;
            double leftBX = bottomLeftX - dirVX;
            double leftBY = bottomLeftY - dirVY;
            
            double topLX = topLeftX + dirHX;
            double topLY = topLeftY + dirHY;
            double topRX = topRightX - dirHX;
            double topRY = topRightY - dirHY;
            
            double rightTX = topRightX + dirVX;
            double rightTY = topRightY + dirVY;
            double rightBX = bottomRightX - dirVX;
            double rightBY = bottomRightY - dirVY;
            
            double bottomLX = bottomLeftX + dirHX;
            double bottomLY = bottomLeftY + dirHY;
            double bottomRX = bottomRightX - dirHX;
            double bottomRY = bottomRightY - dirHY;
            
            double lerpX = cornerRadius / Math.abs(width);
            double lerpY = cornerRadius / Math.abs(height);
            
            int topLeftCR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, lerpX, lerpY);
            int topLeftCG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, lerpX, lerpY);
            int topLeftCB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, lerpX, lerpY);
            int topLeftCA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, lerpX, lerpY);
            
            int topRightCR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0 - lerpX, lerpY);
            int topRightCG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0 - lerpX, lerpY);
            int topRightCB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0 - lerpX, lerpY);
            int topRightCA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0 - lerpX, lerpY);
            
            int bottomLeftCR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, lerpX, 1.0 - lerpY);
            int bottomLeftCG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, lerpX, 1.0 - lerpY);
            int bottomLeftCB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, lerpX, 1.0 - lerpY);
            int bottomLeftCA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, lerpX, 1.0 - lerpY);
            
            int bottomRightCR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0 - lerpX, 1.0 - lerpY);
            int bottomRightCG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0 - lerpX, 1.0 - lerpY);
            int bottomRightCB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0 - lerpX, 1.0 - lerpY);
            int bottomRightCA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0 - lerpX, 1.0 - lerpY);
            
            int leftTR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 0.0, lerpY);
            int leftTG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 0.0, lerpY);
            int leftTB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 0.0, lerpY);
            int leftTA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 0.0, lerpY);
            
            int leftBR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 0.0, 1.0 - lerpY);
            int leftBG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 0.0, 1.0 - lerpY);
            int leftBB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 0.0, 1.0 - lerpY);
            int leftBA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 0.0, 1.0 - lerpY);
            
            int topLR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, lerpX, 0.0);
            int topLG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, lerpX, 0.0);
            int topLB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, lerpX, 0.0);
            int topLA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, lerpX, 0.0);
            
            int topRR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0 - lerpX, 0.0);
            int topRG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0 - lerpX, 0.0);
            int topRB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0 - lerpX, 0.0);
            int topRA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0 - lerpX, 0.0);
            
            int rightTR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0, lerpY);
            int rightTG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0, lerpY);
            int rightTB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0, lerpY);
            int rightTA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0, lerpY);
            
            int rightBR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0, 1.0 - lerpY);
            int rightBG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0, 1.0 - lerpY);
            int rightBB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0, 1.0 - lerpY);
            int rightBA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0, 1.0 - lerpY);
            
            int bottomLR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, lerpX, 1.0);
            int bottomLG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, lerpX, 1.0);
            int bottomLB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, lerpX, 1.0);
            int bottomLA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, lerpX, 1.0);
            
            int bottomRR = lerp(topLeftR, topRightR, bottomLeftR, bottomRightR, 1.0 - lerpX, 1.0);
            int bottomRG = lerp(topLeftG, topRightG, bottomLeftG, bottomRightG, 1.0 - lerpX, 1.0);
            int bottomRB = lerp(topLeftB, topRightB, bottomLeftB, bottomRightB, 1.0 - lerpX, 1.0);
            int bottomRA = lerp(topLeftA, topRightA, bottomLeftA, bottomRightA, 1.0 - lerpX, 1.0);
            
            // Center Quad
            fillQuad(topLeftCX, topLeftCY,
                     topRightCX, topRightCY,
                     bottomRightCX, bottomRightCY,
                     bottomLeftCX, bottomLeftCY,
                     topLeftCR, topLeftCG, topLeftCB, topLeftCA,
                     topRightCR, topRightCG, topRightCB, topRightCA,
                     bottomRightCR, bottomRightCG, bottomRightCB, bottomRightCA,
                     bottomLeftCR, bottomLeftCG, bottomLeftCB, bottomLeftCA);
            
            // Left Quad
            fillQuad(leftTX, leftTY,
                     topLeftCX, topLeftCY,
                     bottomLeftCX, bottomLeftCY,
                     leftBX, leftBY,
                     leftTR, leftTG, leftTB, leftTA,
                     topLeftCR, topLeftCG, topLeftCB, topLeftCA,
                     bottomLeftCR, bottomLeftCG, bottomLeftCB, bottomLeftCA,
                     leftBR, leftBG, leftBB, leftBA);
            
            // Top Quad
            fillQuad(topLX, topLY,
                     topRX, topRY,
                     topRightCX, topRightCY,
                     topLeftCX, topLeftCY,
                     topLR, topLG, topLB, topLA,
                     topRR, topRG, topRB, topRA,
                     topRightCR, topRightCG, topRightCB, topRightCA,
                     topLeftCR, topLeftCG, topLeftCB, topLeftCA);
            
            // Right Quad
            fillQuad(topRightCX, topRightCY,
                     rightTX, rightTY,
                     rightBX, rightBY,
                     bottomRightCX, bottomRightCY,
                     topRightCR, topRightCG, topRightCB, topRightCA,
                     rightTR, rightTG, rightTB, rightTA,
                     rightBR, rightBG, rightBB, rightBA,
                     bottomRightCR, bottomRightCG, bottomRightCB, bottomRightCA);
            
            // Bottom Quad
            fillQuad(bottomLeftCX, bottomLeftCY,
                     bottomRightCX, bottomRightCY,
                     bottomRX, bottomRY,
                     bottomLX, bottomLY,
                     bottomLeftCR, bottomLeftCG, bottomLeftCB, bottomLeftCA,
                     bottomRightCR, bottomRightCG, bottomRightCB, bottomRightCA,
                     bottomRR, bottomRG, bottomRB, bottomRA,
                     bottomLR, bottomLG, bottomLB, bottomLA);
            
            int segments = segments(cornerRadius, cornerRadius, 0, Math.PI_2);
            
            double topLeftStart     = -Math.PI + rotationAngle;
            double topLeftStop      = -Math.PI_2 + rotationAngle;
            double topRightStart    = -Math.PI_2 + rotationAngle;
            double topRightStop     = rotationAngle;
            double bottomLeftStart  = Math.PI_2 + rotationAngle;
            double bottomLeftStop   = Math.PI + rotationAngle;
            double bottomRightStart = rotationAngle;
            double bottomRightStop  = Math.PI_2 + rotationAngle;
            
            if (halfW < 0.0)
            {
                double temp;
                
                temp         = topLeftStart;
                topLeftStart = topRightStop;
                topRightStop = temp;
                
                temp          = topLeftStop;
                topLeftStop   = topRightStart;
                topRightStart = temp;
                
                temp            = bottomLeftStart;
                bottomLeftStart = bottomRightStop;
                bottomRightStop = temp;
                
                temp             = bottomLeftStop;
                bottomLeftStop   = bottomRightStart;
                bottomRightStart = temp;
            }
            if (halfH < 0.0)
            {
                double temp;
                
                temp           = topLeftStart;
                topLeftStart   = bottomLeftStop;
                bottomLeftStop = temp;
                
                temp            = topLeftStop;
                topLeftStop     = bottomLeftStart;
                bottomLeftStart = temp;
                
                temp            = topRightStart;
                topRightStart   = bottomRightStop;
                bottomRightStop = temp;
                
                temp             = topRightStop;
                topRightStop     = bottomRightStart;
                bottomRightStart = temp;
            }
            
            // Top Left Corner
            fillEllipse(topLeftCX, topLeftCY,
                        cornerRadius, cornerRadius,
                        topLeftStart, topLeftStop,
                        0, 0, 0,
                        segments,
                        topLeftCR, topLeftCG, topLeftCB, topLeftCA,
                        topLeftCR, topLeftCG, topLeftCB, topLeftCA,
                        leftTR, leftTG, leftTB, leftTA,
                        topLR, topLG, topLB, topLA);
            
            // Top Right Corner
            fillEllipse(topRightCX, topRightCY,
                        cornerRadius, cornerRadius,
                        topRightStart, topRightStop,
                        0, 0, 0,
                        segments,
                        topRightCR, topRightCG, topRightCB, topRightCA,
                        topRightCR, topRightCG, topRightCB, topRightCA,
                        topRR, topRG, topRB, topRA,
                        rightTR, rightTG, rightTB, rightTA);
            
            // Bottom Left Corner
            fillEllipse(bottomLeftCX, bottomLeftCY,
                        cornerRadius, cornerRadius,
                        bottomLeftStart, bottomLeftStop,
                        0, 0, 0,
                        segments,
                        bottomLeftCR, bottomLeftCG, bottomLeftCB, bottomLeftCA,
                        bottomLeftCR, bottomLeftCG, bottomLeftCB, bottomLeftCA,
                        bottomLR, bottomLG, bottomLB, bottomLA,
                        leftBR, leftBG, leftBB, leftBA);
            
            // Bottom Right Corner
            fillEllipse(bottomRightCX, bottomRightCY,
                        cornerRadius, cornerRadius,
                        bottomRightStart, bottomRightStop,
                        0, 0, 0,
                        segments,
                        bottomRightCR, bottomRightCG, bottomRightCB, bottomRightCA,
                        bottomRightCR, bottomRightCG, bottomRightCB, bottomRightCA,
                        rightBR, rightBG, rightBB, rightBA,
                        bottomRR, bottomRG, bottomRB, bottomRA);
        }
    }
    
    protected static void drawEllipse(double x, double y,
                                      double rx, double ry,
                                      double thickness,
                                      double startAngle, double stopAngle,
                                      double rotationOriginX, double rotationOriginY, double rotationAngle,
                                      int segments,
                                      int r, int g, int b, int a)
    {
        while (startAngle > Math.PI2 + 1e-6) startAngle -= Math.PI2;
        while (startAngle < -Math.PI2 - 1e-6) startAngle += Math.PI2;
        
        while (stopAngle > Math.PI2 + 1e-6) stopAngle -= Math.PI2;
        while (stopAngle < -Math.PI2 - 1e-6) stopAngle += Math.PI2;
        
        if (Math.equals(startAngle, stopAngle, 1e-6)) return;
        
        if (segments < 3) segments = segments(rx, ry, startAngle, stopAngle);
        
        int      pointCount = (segments + 3) << 1;
        double[] points     = new double[pointCount];
        
        boolean shouldRotate = !Math.equals(rotationAngle, 0.0, 1e-6);
        double  tempX, tempY;
        
        double s = shouldRotate ? Math.sin(rotationAngle) : 0.0;
        double c = shouldRotate ? Math.cos(rotationAngle) : 0.0;
        
        double theta;
        double px, py;
        
        // Segments + 2 to make sure that the end segments are beveled
        for (int i = -1, index = 0; i < segments + 2; i++)
        {
            theta = Math.map(i, 0, segments, startAngle, stopAngle);
            px    = Math.cos(theta) * rx;
            py    = Math.sin(theta) * ry;
            if (shouldRotate)
            {
                tempX = px - rotationOriginX;
                tempY = py - rotationOriginY;
                px    = tempX * c - tempY * s + rotationOriginX;
                py    = tempX * s + tempY * c + rotationOriginY;
            }
            px += x;
            py += y;
            
            // Add Generated points to array.
            points[index++] = px;
            points[index++] = py;
        }
        
        drawLines(points, thickness, r, g, b, a, r, g, b, a);
    }
    
    protected static void fillEllipse(double x, double y,
                                      double rx, double ry,
                                      double startAngle, double stopAngle,
                                      double rotationOriginX, double rotationOriginY, double rotationAngle,
                                      int segments,
                                      int innerStartR, int innerStartG, int innerStartB, int innerStartA,
                                      int innerStopR, int innerStopG, int innerStopB, int innerStopA,
                                      int outerStartR, int outerStartG, int outerStartB, int outerStartA,
                                      int outerStopR, int outerStopG, int outerStopB, int outerStopA)
    {
        while (startAngle > Math.PI2 + 1e-6) startAngle -= Math.PI2;
        while (startAngle < -Math.PI2 - 1e-6) startAngle += Math.PI2;
        
        while (stopAngle > Math.PI2 + 1e-6) stopAngle -= Math.PI2;
        while (stopAngle < -Math.PI2 - 1e-6) stopAngle += Math.PI2;
        
        if (Math.equals(startAngle, stopAngle, 1e-6)) return;
        
        if (segments < 3) segments = segments(rx, ry, startAngle, stopAngle);
        
        double  tempX, tempY;
        boolean shouldRotate = !Math.equals(rotationAngle, 0.0, 1e-6);
        
        double s = shouldRotate ? Math.sin(rotationAngle) : 0;
        double c = shouldRotate ? Math.cos(rotationAngle) : 0;
        
        double theta = startAngle;
        double cx, cy;
        double p0x, p0y, p1x, p1y;
        
        cx  = 0.0;
        cy  = 0.0;
        p0x = Math.cos(theta) * rx;
        p0y = Math.sin(theta) * ry;
        if (shouldRotate)
        {
            tempX = cx - rotationOriginX;
            tempY = cy - rotationOriginY;
            cx    = tempX * c - tempY * s + rotationOriginX;
            cy    = tempX * s + tempY * c + rotationOriginY;
            tempX = p0x - rotationOriginX;
            tempY = p0y - rotationOriginY;
            p0x   = tempX * c - tempY * s + rotationOriginX;
            p0y   = tempX * s + tempY * c + rotationOriginY;
        }
        cx += x;
        cy += y;
        p0x += x;
        p0y += y;
        
        int ri, gi, bi, ai;
        
        int ro0 = outerStartR;
        int go0 = outerStartG;
        int bo0 = outerStartB;
        int ao0 = outerStartA;
        int ro1, go1, bo1, ao1;
        
        double lerp;
        
        GLBatch.checkBuffer(segments * 3);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        for (int i = 0; i < segments; i++)
        {
            theta = Math.map(i + 1, 0, segments, startAngle, stopAngle);
            p1x   = Math.cos(theta) * rx;
            p1y   = Math.sin(theta) * ry;
            if (shouldRotate)
            {
                tempX = p1x - rotationOriginX;
                tempY = p1y - rotationOriginY;
                p1x   = tempX * c - tempY * s + rotationOriginX;
                p1y   = tempX * s + tempY * c + rotationOriginY;
            }
            p1x += x;
            p1y += y;
            
            lerp = (double) i / (double) segments;
            
            ri = lerp(innerStartR, innerStopR, lerp);
            gi = lerp(innerStartG, innerStopG, lerp);
            bi = lerp(innerStartB, innerStopB, lerp);
            ai = lerp(innerStartA, innerStopA, lerp);
            
            lerp = (double) (i + 1) / (double) segments;
            
            ro1 = lerp(outerStartR, outerStopR, lerp);
            go1 = lerp(outerStartG, outerStopG, lerp);
            bo1 = lerp(outerStartB, outerStopB, lerp);
            ao1 = lerp(outerStartA, outerStopA, lerp);
            
            Draw2D.VERTEX0.clear().pos(cx, cy).texCoord(Draw2D.u1, Draw2D.v1).color(ri, gi, bi, ai);
            Draw2D.VERTEX1.clear().pos(p0x, p0y).texCoord(Draw2D.u0, Draw2D.v0).color(ro0, go0, bo0, ao0);
            Draw2D.VERTEX2.clear().pos(p1x, p1y).texCoord(Draw2D.u1, Draw2D.v0).color(ro1, go1, bo1, ao1);
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            
            p0x = p1x;
            p0y = p1y;
            
            ro0 = ro1;
            go0 = go1;
            bo0 = bo1;
            ao0 = ao1;
        }
        GLBatch.end();
    }
    
    protected static void fillRing(double x, double y,
                                   double innerRX, double innerRY,
                                   double outerRX, double outerRY,
                                   double startAngle, double stopAngle,
                                   double rotationOriginX, double rotationOriginY, double rotationAngle,
                                   int segments,
                                   int innerStartR, int innerStartG, int innerStartB, int innerStartA,
                                   int innerStopR, int innerStopG, int innerStopB, int innerStopA,
                                   int outerStartR, int outerStartG, int outerStartB, int outerStartA,
                                   int outerStopR, int outerStopG, int outerStopB, int outerStopA)
    {
        while (startAngle > Math.PI2 + 1e-6) startAngle -= Math.PI2;
        while (startAngle < -Math.PI2 - 1e-6) startAngle += Math.PI2;
        
        while (stopAngle > Math.PI2 + 1e-6) stopAngle -= Math.PI2;
        while (stopAngle < -Math.PI2 - 1e-6) stopAngle += Math.PI2;
        
        if (Math.equals(startAngle, stopAngle, 1e-6)) return;
        
        if (segments < 3) segments = segments(Math.max(Math.abs(innerRX), Math.abs(outerRX)), Math.max(Math.abs(innerRY), Math.abs(outerRY)), startAngle, stopAngle);
        
        double  tempX, tempY;
        boolean shouldRotate = !Math.equals(rotationAngle, 0.0, 1e-6);
        
        double s = shouldRotate ? Math.sin(rotationAngle) : 0;
        double c = shouldRotate ? Math.cos(rotationAngle) : 0;
        
        double theta = startAngle;
        double cos, sin, x0i, y0i, x0o, y0o, x1i, y1i, x1o, y1o;
        
        cos = Math.cos(theta);
        sin = Math.sin(theta);
        x0i = cos * innerRX;
        y0i = sin * innerRY;
        x0o = cos * outerRX;
        y0o = sin * outerRY;
        if (shouldRotate)
        {
            tempX = x0i - rotationOriginX;
            tempY = y0i - rotationOriginY;
            x0i   = tempX * c - tempY * s + rotationOriginX;
            y0i   = tempX * s + tempY * c + rotationOriginY;
            tempX = x0o - rotationOriginX;
            tempY = y0o - rotationOriginY;
            x0o   = tempX * c - tempY * s + rotationOriginX;
            y0o   = tempX * s + tempY * c + rotationOriginY;
        }
        x0i += x;
        y0i += y;
        x0o += x;
        y0o += y;
        
        double lerp;
        
        int ri0 = innerStartR;
        int gi0 = innerStartG;
        int bi0 = innerStartB;
        int ai0 = innerStartA;
        int ri1, gi1, bi1, ai1;
        
        int ro0 = outerStartR;
        int go0 = outerStartG;
        int bo0 = outerStartB;
        int ao0 = outerStartA;
        int ro1, go1, bo1, ao1;
        
        GLBatch.checkBuffer(segments * 4);
        
        GLBatch.setTexture(Draw2D.texture);
        
        GLBatch.begin(DrawMode.TRIANGLES);
        
        for (int i = 0; i < segments; i++)
        {
            theta = Math.map(i + 1, 0, segments, startAngle, stopAngle);
            cos   = Math.cos(theta);
            sin   = Math.sin(theta);
            x1i   = cos * innerRX;
            y1i   = sin * innerRY;
            x1o   = cos * outerRX;
            y1o   = sin * outerRY;
            if (shouldRotate)
            {
                tempX = x1i - rotationOriginX;
                tempY = y1i - rotationOriginY;
                x1i   = tempX * c - tempY * s + rotationOriginX;
                y1i   = tempX * s + tempY * c + rotationOriginY;
                tempX = x1o - rotationOriginX;
                tempY = y1o - rotationOriginY;
                x1o   = tempX * c - tempY * s + rotationOriginX;
                y1o   = tempX * s + tempY * c + rotationOriginY;
            }
            x1i += x;
            y1i += y;
            x1o += x;
            y1o += y;
            
            lerp = (double) (i + 1) / (double) segments;
            
            ri1 = lerp(innerStartR, innerStopR, lerp);
            gi1 = lerp(innerStartG, innerStopG, lerp);
            bi1 = lerp(innerStartB, innerStopB, lerp);
            ai1 = lerp(innerStartA, innerStopA, lerp);
            
            ro1 = lerp(outerStartR, outerStopR, lerp);
            go1 = lerp(outerStartG, outerStopG, lerp);
            bo1 = lerp(outerStartB, outerStopB, lerp);
            ao1 = lerp(outerStartA, outerStopA, lerp);
            
            Draw2D.VERTEX0.clear().pos(x0o, y0o).texCoord(Draw2D.u0, Draw2D.v0).color(ro0, go0, bo0, ao0);
            Draw2D.VERTEX1.clear().pos(x0i, y0i).texCoord(Draw2D.u0, Draw2D.v1).color(ri0, gi0, bi0, ai0);
            Draw2D.VERTEX2.clear().pos(x1i, y1i).texCoord(Draw2D.u1, Draw2D.v1).color(ri1, gi1, bi1, ai1);
            Draw2D.VERTEX3.clear().pos(x1o, y1o).texCoord(Draw2D.u1, Draw2D.v0).color(ro1, go1, bo1, ao1);
            
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX1, Draw2D.VERTEX2);
            windTriangle(Draw2D.VERTEX0, Draw2D.VERTEX2, Draw2D.VERTEX3);
            
            x0i = x1i;
            y0i = y1i;
            x0o = x1o;
            y0o = y1o;
            
            ri0 = ri1;
            gi0 = gi1;
            bi0 = bi1;
            ai0 = ai1;
            
            ro0 = ro1;
            go0 = go1;
            bo0 = bo1;
            ao0 = ao1;
        }
        GLBatch.end();
    }
    
    protected static void drawTexture(@NotNull GLTexture texture,
                                      double x0, double y0,
                                      double x1, double y1,
                                      double x2, double y2,
                                      double x3, double y3,
                                      double u0, double v0,
                                      double u1, double v1,
                                      double u2, double v2,
                                      double u3, double v3,
                                      int r, int g, int b, int a)
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
    
    protected static void drawTexture(@NotNull GLTexture texture,
                                      double srcX, double srcY, double srcW, double srcH,
                                      double dstX, double dstY, double dstW, double dstH,
                                      double rotationOriginX, double rotationOriginY, double rotationAngle,
                                      int r, int g, int b, int a)
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
        if (Math.equals(rotationAngle, 0.0, 1e-6))
        {
            double dx = dstX - rotationOriginX;
            double dy = dstY - rotationOriginY;
            
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
            double sin = Math.sin(rotationAngle);
            double cos = Math.cos(rotationAngle);
            double dx  = -rotationOriginX;
            double dy  = -rotationOriginY;
            
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
        return Math.clamp((int) (Math.max(Math.abs(rx), Math.abs(ry)) * Math.abs(stop - start) / Math.PI2), 4, 48);
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
    
    private static int lerp(int a, int b, double x)
    {
        return a == b ? a : Math.lerp(a, b, x);
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    private static int lerp(int tl, int tr, int bl, int br, double x, double y)
    {
        return lerp(lerp(tl, tr, x), lerp(bl, br, x), y);
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
