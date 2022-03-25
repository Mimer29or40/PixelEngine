package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import pe.font.Font;
import pe.font.Weight;
import rutils.Logger;
import rutils.Math;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawText2D extends Draw2D
{
    private static final Logger LOGGER = new Logger();
    
    private String text;
    
    private double x, y;
    private boolean hasPoint;
    
    private double w, h;
    
    private Font font;
    
    private String  name;
    private Weight  weight;
    private boolean italics;
    private int     size;
    
    private TextAlign align;
    
    private int r, g, b, a;
    
    @Override
    protected void reset()
    {
        this.text = null;
        
        this.hasPoint = false;
        
        this.w = 0;
        this.h = 0;
        
        this.font = null;
        
        this.name    = Font.DEFAULT_NAME;
        this.weight  = Font.DEFAULT_WEIGHT;
        this.italics = Font.DEFAULT_ITALICS;
        
        this.size = Font.DEFAULT_SIZE;
        
        this.align = TextAlign.TOP_LEFT;
        
        this.r = 255;
        this.g = 255;
        this.b = 255;
        this.a = 255;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint) throw new IllegalStateException("Must provide point");
        if (this.w < 0) throw new IllegalStateException("Width must be >= 0");
        if (this.h < 0) throw new IllegalStateException("Height must be >= 0");
        if (this.size < 0) throw new IllegalStateException("Text Size must be >= 0");
    }
    
    @Override
    protected void drawImpl()
    {
        if (this.font == null) this.font = Font.get(this.name, this.weight, this.italics);
        
        DrawText2D.LOGGER.finest("Drawing text=\"%s\" anchor=(%s, %s) bounds=(%s, %s) font=%s size=%s align=%s color=(%s, %s, %s, %s)",
                                 this.text,
                                 this.x, this.y, this.w, this.h,
                                 this.font, this.size, this.align,
                                 this.r, this.g, this.b, this.a);
        
        List<String> lines;
        if (this.w > 0 && this.h > 0)
        {
            lines = new ArrayList<>();
            for (String line : this.text.split("\n"))
            {
                if (this.font.getTextWidth(line, this.size) > w)
                {
                    String[]      subLines = line.split(" ");
                    StringBuilder builder  = new StringBuilder(subLines[0]);
                    for (int j = 1, n = subLines.length; j < n; j++)
                    {
                        if (this.font.getTextWidth(builder + " " + subLines[j], this.size) > this.w)
                        {
                            if (this.font.getTextWidth(builder.toString(), this.size) > this.w) break;
                            if ((lines.size() + 1) * this.size > this.h) break;
                            lines.add(builder.toString());
                            builder.setLength(0);
                            builder.append(subLines[j]);
                            continue;
                        }
                        builder.append(" ").append(subLines[j]);
                    }
                    if (this.font.getTextWidth(builder.toString(), this.size) > this.w) break;
                    if ((lines.size() + 1) * this.size > this.h) break;
                    lines.add(builder.toString());
                }
                else
                {
                    if ((lines.size() + 1) * this.size > this.h) break;
                    lines.add(line);
                }
            }
        }
        else
        {
            lines = Arrays.asList(this.text.split("\n"));
        }
        
        double actualHeight = this.font.getTextHeight(this.text, this.size);
        
        int hPos = this.align.getH(), vPos = this.align.getV();
        
        double yOffset = vPos == -1 ? 0 : vPos == 0 ? 0.5 * (this.h - actualHeight) : this.h - actualHeight;
        for (String line : lines)
        {
            double lineWidth  = Math.ceil(this.font.getTextWidth(line, this.size));
            double lineHeight = Math.ceil(this.font.getTextHeight(line, this.size));
            
            double xOffset = hPos == -1 ? 0 : hPos == 0 ? 0.5 * (this.w - lineWidth) : this.w - lineWidth;
            
            drawText(this.font, this.size, line,
                     this.x + xOffset, this.y + yOffset,
                     this.r, this.g, this.b, this.a);
            
            yOffset += lineHeight;
        }
    }
    
    public DrawText2D text(String text)
    {
        this.text = text;
        return this;
    }
    
    public DrawText2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    public DrawText2D point(@NotNull Vector2ic vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawText2D point(@NotNull Vector2fc vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawText2D point(@NotNull Vector2dc vec)
    {
        return point(vec.x(), vec.y());
    }
    
    public DrawText2D bounds(double width, double height)
    {
        this.w = width;
        this.h = height;
        return this;
    }
    
    public DrawText2D bounds(@NotNull Vector2ic bounds)
    {
        return bounds(bounds.x(), bounds.y());
    }
    
    public DrawText2D bounds(@NotNull Vector2fc bounds)
    {
        return bounds(bounds.x(), bounds.y());
    }
    
    public DrawText2D bounds(@NotNull Vector2dc bounds)
    {
        return bounds(bounds.x(), bounds.y());
    }
    
    public DrawText2D font(@NotNull Font font)
    {
        this.font = font;
        return this;
    }
    
    public DrawText2D name(String name)
    {
        this.name = name;
        return this;
    }
    
    public DrawText2D weight(Weight weight)
    {
        this.weight = weight;
        return this;
    }
    
    public DrawText2D italics(boolean italics)
    {
        this.italics = italics;
        return this;
    }
    
    public DrawText2D size(int size)
    {
        this.size = size;
        return this;
    }
    
    public DrawText2D align(@NotNull TextAlign align)
    {
        this.align = align;
        return this;
    }
    
    public DrawText2D color(int r, int g, int b, int a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return this;
    }
    
    public DrawText2D color(@NotNull Colorc color)
    {
        return color(color.r(), color.g(), color.b(), color.a());
    }
}
