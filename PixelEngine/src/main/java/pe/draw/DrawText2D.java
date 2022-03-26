package pe.draw;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2dc;
import org.joml.Vector2fc;
import org.joml.Vector2ic;
import pe.color.Colorc;
import pe.font.*;
import rutils.Logger;

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
    private boolean italicized;
    
    private int size;
    
    private TextAlign align;
    
    private boolean ignoreFormatting;
    
    private int r, g, b, a;
    
    @Override
    protected void reset()
    {
        this.text = null;
        
        this.hasPoint = false;
        
        this.w = 0;
        this.h = 0;
        
        this.font = null;
        
        this.name       = FontSingle.DEFAULT_FAMILY;
        this.weight     = FontSingle.DEFAULT_WEIGHT;
        this.italicized = FontSingle.DEFAULT_ITALICS;
        
        this.size = FontSingle.DEFAULT_SIZE;
        
        this.align = TextAlign.TOP_LEFT;
        
        this.ignoreFormatting = false;
        
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
        if (this.font == null) this.font = Font.get(this.name, this.weight, this.italicized);
        
        DrawText2D.LOGGER.finest("Drawing text=\"%s\" anchor=(%s, %s) bounds=(%s, %s) font=%s size=%s align=%s ignoreFormatting=%s color=(%s, %s, %s, %s)",
                                 this.text,
                                 this.x, this.y, this.w, this.h,
                                 this.font, this.size, this.align, this.ignoreFormatting,
                                 this.r, this.g, this.b, this.a);
        
        List<String> lines;
        if (this.w > 0 && this.h > 0)
        {
            lines = new ArrayList<>();
            
            TextState state = new TextState(this.font, this.weight, this.italicized, this.size);
            state.textR = this.r;
            state.textG = this.g;
            state.textB = this.b;
            state.textA = this.a;
            
            state.ignoreChanges = this.ignoreFormatting;
            
            TextState lineState       = new TextState(this.font, this.weight, this.italicized, this.size);
            TextState subLineState    = new TextState(this.font, this.weight, this.italicized, this.size);
            TextState subSubLineState = new TextState(this.font, this.weight, this.italicized, this.size);
            
            for (String line : this.text.split("\n"))
            {
                lineState.set(state);
                if (this.font.getTextWidthImpl(line, state) > this.w)
                {
                    String[]      subLines = line.split(" ");
                    StringBuilder builder  = new StringBuilder(subLines[0]);
                    for (int j = 1, n = subLines.length; j < n; j++)
                    {
                        subLineState.set(lineState);
                        if (this.font.getTextWidthImpl(builder + " " + subLines[j], subSubLineState.set(subLineState)) > this.w)
                        {
                            lines.add(builder.toString());
                            builder.setLength(0);
                            builder.append(subLines[j]);
                            lineState.set(subLineState);
                            continue;
                        }
                        builder.append(" ").append(subLines[j]);
                    }
                    lines.add(builder.toString());
                }
                else
                {
                    lines.add(line);
                }
            }
        }
        else
        {
            lines = Arrays.asList(this.text.split("\n"));
        }
        
        TextState state = new TextState(this.font, this.weight, this.italicized, this.size);
        
        double actualHeight = this.font.getTextHeight(this.text, this.size);
        
        int hPos = this.align.getH(), vPos = this.align.getV();
        
        double yOffset = vPos == -1 ? 0 : vPos == 0 ? 0.5 * (this.h - actualHeight) : this.h - actualHeight;
        for (String line : lines)
        {
            state.ignoreChanges = true;
            double lineWidth  = this.font.getTextWidthImpl(line, state);
            double lineHeight = this.font.getTextHeightImpl(line, state);
            state.ignoreChanges = this.ignoreFormatting;
            
            double xOffset = hPos == -1 ? 0 : hPos == 0 ? 0.5 * (this.w - lineWidth) : this.w - lineWidth;
            
            this.font.drawTextImpl(line, x + xOffset, y + yOffset, state);
            
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
    
    public DrawText2D italicized(boolean italicized)
    {
        this.italicized = italicized;
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
    
    public DrawText2D ignoreFormatting(boolean ignoreFormatting)
    {
        this.ignoreFormatting = ignoreFormatting;
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
