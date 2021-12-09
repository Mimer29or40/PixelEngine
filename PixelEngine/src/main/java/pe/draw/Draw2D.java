package pe.draw;

import org.jetbrains.annotations.Nullable;
import pe.render.DrawMode;
import pe.render.GLBatch;
import pe.render.GLTexture;
import rutils.Logger;
import rutils.Math;

public abstract class Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    protected static GLTexture texture;
    
    protected static double u0, v0, u1, v1;
    
    static
    {
        Draw2D.texture = null;
        
        Draw2D.u0 = 0.0;
        Draw2D.v0 = 0.0;
        Draw2D.u1 = 1.0;
        Draw2D.v1 = 1.0;
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
        GLBatch batch = GLBatch.get();
        
        if (thickness <= 1.0)
        {
            batch.checkBuffer(2);
            
            batch.begin(DrawMode.LINES);
            
            batch.vertex(x, y);
            batch.color(r, g, b, a);
            
            batch.vertex(x + 1, y + 1);
            batch.color(r, g, b, a);
            
            batch.end();
        }
        else
        {
            fillEllipse(x, y, thickness, thickness, 0, Math.PI2, 0.0, 0.0, 0.0, segments(thickness, thickness), r, g, b, a, r, g, b, a);
        }
    }
    
    protected static void drawLine(double x0, double y0, double x1, double y1, double thickness, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1)
    {
        GLBatch batch = GLBatch.get();
        
        double dx = x1 - x0;
        double dy = y1 - y0;
        
        if (thickness <= 1.0)
        {
            batch.checkBuffer(2);
            
            batch.begin(DrawMode.LINES);
            
            batch.vertex(x0, y0);
            batch.color(r0, g0, b0, a0);
            
            batch.vertex(x1, y1);
            batch.color(r1, g1, b1, a1);
            
            batch.end();
        }
        else if (dx != 0.0 || dy != 0.0)
        {
            double l  = Math.sqrt(dx * dx + dy * dy);
            double s  = thickness / (2 * l);
            double nx = -dy * s;
            double ny = dx * s;
            
            batch.checkBuffer(6);
            
            batch.begin(DrawMode.TRIANGLES);
            
            batch.color(r0, g0, b0, a0);
            batch.vertex(x0 - nx, y0 - ny);
            batch.color(r0, g0, b0, a0);
            batch.vertex(x0 + nx, y0 + ny);
            batch.color(r1, g1, b1, a1);
            batch.vertex(x1 - nx, y1 - ny);
            
            batch.color(r0, g0, b0, a0);
            batch.vertex(x0 + nx, y0 + ny);
            batch.color(r1, g1, b1, a1);
            batch.vertex(x1 + nx, y1 + ny);
            batch.color(r1, g1, b1, a1);
            batch.vertex(x1 - nx, y1 - ny);
            
            batch.end();
        }
    }
    
    protected static void drawLines(double[] points, double thickness, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1)
    {
        int pointsCount = points.length >> 1;
        
        if (pointsCount < 4)
        {
            Draw2D.LOGGER.warning("DrawLines: Invalid points array:", points);
            return;
        }
        
        GLBatch batch = GLBatch.get();
        
        int _r0 = r0;
        int _g0 = g0;
        int _b0 = b0;
        int _a0 = a0;
        int _r1, _g1, _b1, _a1;
        
        if (thickness <= 1)
        {
            batch.checkBuffer(pointsCount);
            
            batch.begin(DrawMode.LINES);
            for (int i = 1; i < pointsCount - 2; i++)
            {
                int p0 = i << 1;
                int p1 = (i + 1) << 1;
                
                double lerp = (double) i / (pointsCount - 2);
                
                _r1 = Math.lerp(r0, r1, lerp);
                _g1 = Math.lerp(g0, g1, lerp);
                _b1 = Math.lerp(b0, b1, lerp);
                _a1 = Math.lerp(a0, a1, lerp);
                
                batch.vertex(points[p0], points[p0 + 1]);
                batch.color(_r0, _g0, _b0, _a0);
                
                batch.vertex(points[p1], points[p1 + 1]);
                batch.color(_r1, _g1, _b1, _a1);
                
                _r0 = _r1;
                _g0 = _g1;
                _b0 = _b1;
                _a0 = _a1;
            }
        }
        else
        {
            thickness *= 0.5;
            
            batch.checkBuffer((pointsCount - 2) * 9);
            
            batch.setTexture(Draw2D.texture);
            
            batch.begin(DrawMode.TRIANGLES);
            
            int     p0, p1, p2, p3;
            double  p1x, p1y, p2x, p2y;
            double  v0x, v0y, v1x, v1y, v2x, v2y;
            double  v0ux, v0uy, v1ux, v1uy, v2ux, v2uy;
            double  n0x, n0y, n1x, n1y, n2x, n2y;
            double  o0x, o0y, o1x, o1y, o2x, o2y, o3x, o3y, o4x, o4y;
            double  lerp, temp;
            boolean buttStart, buttEnd, drawBevel;
            for (int i = 1; i < pointsCount - 2; i++)
            {
                p0 = (i - 1) << 1;
                p1 = i << 1;
                p2 = (i + 1) << 1;
                p3 = (i + 2) << 1;
                
                lerp = (double) i / (pointsCount - 2);
                
                _r1 = Math.lerp(r0, r1, lerp);
                _g1 = Math.lerp(g0, g1, lerp);
                _b1 = Math.lerp(b0, b1, lerp);
                _a1 = Math.lerp(a0, a1, lerp);
                
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
                    batch.vertex(o0x, o0y);
                    batch.texCoord(Draw2D.u0, Draw2D.v0);
                    batch.color(_r0, _g0, _b0, _a0);
                    
                    batch.vertex(o1x, o1y);
                    batch.texCoord(Draw2D.u0, Draw2D.v1);
                    batch.color(_r0, _g0, _b0, _a0);
                    
                    batch.vertex(o2x, o2y);
                    batch.texCoord(Draw2D.u1, Draw2D.v1);
                    batch.color(_r0, _g0, _b0, _a0);
                }
                
                // Generates Line Strip
                batch.vertex(o2x, o2y);
                batch.texCoord(Draw2D.u0, Draw2D.v0);
                batch.color(_r0, _g0, _b0, _a0);
                
                batch.vertex(o1x, o1y);
                batch.texCoord(Draw2D.u0, Draw2D.v1);
                batch.color(_r0, _g0, _b0, _a0);
                
                batch.vertex(o3x, o3y);
                batch.texCoord(Draw2D.u1, Draw2D.v1);
                batch.color(_r1, _g1, _b1, _a1);
                
                batch.vertex(o2x, o2y);
                batch.texCoord(Draw2D.u0, Draw2D.v0);
                batch.color(_r0, _g0, _b0, _a0);
                
                batch.vertex(o3x, o3y);
                batch.texCoord(Draw2D.u0, Draw2D.v1);
                batch.color(_r1, _g1, _b1, _a1);
                
                batch.vertex(o4x, o4y);
                batch.texCoord(Draw2D.u1, Draw2D.v1);
                batch.color(_r1, _g1, _b1, _a1);
                
                _r0 = _r1;
                _g0 = _g1;
                _b0 = _b1;
                _a0 = _a1;
            }
        }
        batch.end();
    }
    
    protected static void fillTriangle(double x0, double y0, double x1, double y1, double x2, double y2, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2)
    {
        double cross = (x1 - x0) * (y2 - y1) - (y1 - y0) * (x2 - x1);
        
        GLBatch batch = GLBatch.get();
        
        batch.checkBuffer(3);
        
        batch.setTexture(Draw2D.texture);
        
        batch.begin(DrawMode.TRIANGLES);
        
        batch.vertex(x0, y0);
        batch.texCoord(Draw2D.u0, Draw2D.v0);
        batch.color(r0, g0, b0, a0);
        
        if (cross > 0.0)
        {
            batch.vertex(x2, y2);
            batch.texCoord(Draw2D.u1, Draw2D.v1);
            batch.color(r2, g2, b2, a2);
            
            batch.vertex(x1, y1);
            batch.texCoord(Draw2D.u0, Draw2D.v1);
            batch.color(r1, g1, b1, a1);
        }
        else
        {
            batch.vertex(x1, y1);
            batch.texCoord(Draw2D.u0, Draw2D.v1);
            batch.color(r1, g1, b1, a1);
            
            batch.vertex(x2, y2);
            batch.texCoord(Draw2D.u1, Draw2D.v1);
            batch.color(r2, g2, b2, a2);
        }
        
        batch.end();
    }
    
    protected static void fillQuad(double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1, int r2, int g2, int b2, int a2, int r3, int g3, int b3, int a3)
    {
        // TODO - Rearrange indices
        
        double cx = (x0 + x1 + x2 + x3) * 0.25;
        double cy = (y0 + y1 + y2 + y3) * 0.25;
        
        double d0 = Math.sqrt((x0 - cx) * (x0 - cx) + (y0 - cy) * (y0 - cy));
        double d1 = Math.sqrt((x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy));
        double d2 = Math.sqrt((x2 - cx) * (x2 - cx) + (y2 - cy) * (y2 - cy));
        double d3 = Math.sqrt((x3 - cx) * (x3 - cx) + (y3 - cy) * (y3 - cy));
        
        double q0 = (d0 + d2) / d2;
        double q1 = (d1 + d3) / d3;
        double q2 = (d2 + d0) / d0;
        double q3 = (d3 + d1) / d1;
        
        GLBatch batch = GLBatch.get();
        
        batch.checkBuffer(4);
        
        batch.setTexture(Draw2D.texture);
        
        batch.begin(DrawMode.QUADS);
        
        batch.vertex(x0, y0);
        batch.texCoord(Draw2D.u0 * q0, Draw2D.v0 * q0, q0);
        batch.color(r0, g0, b0, a0);
        
        batch.vertex(x1, y1);
        batch.texCoord(Draw2D.u0 * q1, Draw2D.v1 * q1, q1);
        batch.color(r1, g1, b1, a1);
        
        batch.vertex(x2, y2);
        batch.texCoord(Draw2D.u1 * q2, Draw2D.v1 * q2, q2);
        batch.color(r2, g2, b2, a2);
        
        batch.vertex(x3, y3);
        batch.texCoord(Draw2D.u1 * q3, Draw2D.v0 * q3, q3);
        batch.color(r3, g3, b3, a3);
        
        batch.end();
    }
    
    protected static void fillEllipse(double x, double y, double rx, double ry, double start, double stop, double offsetX, double offsetY, double rotation, int segments, int ri, int gi, int bi, int ai, int ro, int go, int bo, int ao)
    {
        if (start > stop)
        {
            double temp = start;
            start = stop;
            stop  = temp;
        }
        
        double inc   = (stop - start) / segments;
        double theta = start;
        
        double  tempX, tempY;
        boolean shouldRotate = rotation != 0.0;
        
        double s = shouldRotate ? Math.sin(Math.toRadians(rotation)) : 0;
        double c = shouldRotate ? Math.cos(Math.toRadians(rotation)) : 0;
        
        double cx, cy;
        
        double p0x, p0y, p1x, p1y;
        
        cx  = 0.0;
        cy  = 0.0;
        p0x = Math.cos(theta) * rx;
        p0y = Math.sin(theta) * ry;
        if (shouldRotate)
        {
            tempX = cx - offsetX;
            tempY = cy - offsetY;
            cx    = tempX * c - tempY * s + offsetX;
            cy    = tempX * s + tempY * c + offsetY;
            tempX = p0x - offsetX;
            tempY = p0y - offsetY;
            p0x   = tempX * c - tempY * s + offsetX;
            p0y   = tempX * s + tempY * c + offsetY;
        }
        cx += x;
        cy += y;
        p0x += x;
        p0y += y;
        
        GLBatch batch = GLBatch.get();
        
        batch.checkBuffer(segments * 3);
        
        batch.setTexture(Draw2D.texture);
        
        batch.begin(DrawMode.TRIANGLES);
        for (int i = 0; i < segments; i++)
        {
            theta = start + (i + 1) * inc;
            p1x   = Math.cos(theta) * rx;
            p1y   = Math.sin(theta) * ry;
            if (shouldRotate)
            {
                tempX = p1x - offsetX;
                tempY = p1y - offsetY;
                p1x   = tempX * c - tempY * s + offsetX;
                p1y   = tempX * s + tempY * c + offsetY;
            }
            p1x += x;
            p1y += y;
            
            batch.vertex(cx, cy);
            batch.texCoord(Draw2D.u0, Draw2D.v0);
            batch.color(ri, gi, bi, ai);
            
            batch.vertex(p1x, p1y);
            batch.texCoord(Draw2D.u1, Draw2D.v1);
            batch.color(ro, go, bo, ao);
            
            batch.vertex(p0x, p0y);
            batch.texCoord(Draw2D.u0, Draw2D.v1);
            batch.color(ro, go, bo, ao);
            
            p0x = p1x;
            p0y = p1y;
        }
        batch.end();
    }
    
    protected static void drawTexture(GLTexture texture, double x0, double y0, double x1, double y1, double x2, double y2, double x3, double y3, double u0, double v0, double u1, double v1, double u2, double v2, double u3, double v3, int r, int g, int b, int a)
    {
        // Check if texture is valid
        if (texture.id() <= 0) return;
        
        double rd = (x2 - x0) * (y3 - y1) - (x3 - x1) * (y2 - y0);
        
        if (rd == 0.0) return;
        
        rd = 1.0 / rd;
        
        double cx = 0.0, cy = 0.0;
        
        double rn = ((x3 - x1) * (y0 - y1) - (y3 - y1) * (x0 - x1)) * rd;
        double sn = ((x2 - x0) * (y0 - y1) - (y2 - y0) * (x0 - x1)) * rd;
    
        if (!(rn < 0.0 || rn > 1.0 || sn < 0.0 || sn > 1.0))
        {
            cx = x0 + rn * (x2 - x0);
            cy = y0 + rn * (y2 - y0);
        }
        
        double d0 = Math.sqrt((x0 - cx) * (x0 - cx) + (y0 - cy) * (y0 - cy));
        double d1 = Math.sqrt((x1 - cx) * (x1 - cx) + (y1 - cy) * (y1 - cy));
        double d2 = Math.sqrt((x2 - cx) * (x2 - cx) + (y2 - cy) * (y2 - cy));
        double d3 = Math.sqrt((x3 - cx) * (x3 - cx) + (y3 - cy) * (y3 - cy));
        
        double q0 = d0 == 0.0 ? 1.0 : (d0 + d2) / d2;
        double q1 = d0 == 0.0 ? 1.0 : (d1 + d3) / d3;
        double q2 = d0 == 0.0 ? 1.0 : (d2 + d0) / d0;
        double q3 = d0 == 0.0 ? 1.0 : (d3 + d1) / d1;
        
        GLBatch batch = GLBatch.get();
        
        batch.checkBuffer(4); // Make sure there is enough free space on the batch buffer
        
        batch.setTexture(texture);
        
        batch.begin(DrawMode.QUADS);
        
        // Top-left corner for texture and quad
        batch.vertex(x0, y0);
        batch.texCoord(u0 * q0, v0 * q0, q0);
        batch.color(r, g, b, a);
        
        // Bottom-left corner for texture and quad
        batch.vertex(x1, y1);
        batch.texCoord(u1 * q1, v1 * q1, q1);
        batch.color(r, g, b, a);
        
        // Bottom-right corner for texture and quad
        batch.vertex(x2, y2);
        batch.texCoord(u2 * q2, v2 * q2, q2);
        batch.color(r, g, b, a);
        
        // Top-right corner for texture and quad
        batch.vertex(x3, y3);
        batch.texCoord(u3 * q3, v3 * q3, q3);
        batch.color(r, g, b, a);
        
        batch.end();
    }
    
    protected static void drawTexture(GLTexture texture, double srcX, double srcY, double srcW, double srcH, double dstX, double dstY, double dstW, double dstH, double originX, double originY, double angle, int r, int g, int b, int a)
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
        if (Double.compare(angle, 0.0) == 0)
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
        
        // GLBatch batch = GLBatch.get();
        //
        // batch.checkBuffer(4); // Make sure there is enough free space on the batch buffer
        //
        // batch.setTexture(texture);
        //
        // batch.begin(DrawMode.QUADS);
        //
        // // Top-left corner for texture and quad
        // batch.vertex(x0, y0);
        // batch.texCoord(u0, v0);
        // batch.color(r, g, b, a);
        //
        // // Bottom-left corner for texture and quad
        // batch.vertex(x1, y1);
        // batch.texCoord(u0, v1);
        // batch.color(r, g, b, a);
        //
        // // Bottom-right corner for texture and quad
        // batch.vertex(x2, y2);
        // batch.texCoord(u1, v1);
        // batch.color(r, g, b, a);
        //
        // // Top-right corner for texture and quad
        // batch.vertex(x3, y3);
        // batch.texCoord(u1, v0);
        // batch.color(r, g, b, a);
        //
        // batch.end();
    }
    
    private static int segments(double rx, double ry)
    {
        return Math.clamp((int) Math.max(rx, ry), 8, 48);
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
        check();
        
        drawImpl();
        
        reset();
    }
}
