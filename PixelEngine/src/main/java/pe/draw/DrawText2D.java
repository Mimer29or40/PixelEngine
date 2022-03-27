package pe.draw;

import org.jetbrains.annotations.NotNull;
import pe.color.Colorc;
import pe.font.*;
import rutils.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DrawText2D extends Draw2D implements Point<DrawText2D>,
                                                  Size<DrawText2D>,
                                                  Color<DrawText2D>
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
    
    private boolean underline;
    private boolean strike;
    
    private int textR, textG, textB, textA;
    private int backR, backG, backB, backA;
    
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
        
        this.underline = false;
        this.strike    = false;
        
        this.textR = 255;
        this.textG = 255;
        this.textB = 255;
        this.textA = 255;
        
        this.backR = 0;
        this.backG = 0;
        this.backB = 0;
        this.backA = 0;
    }
    
    @Override
    protected void check()
    {
        if (!this.hasPoint) throw new IllegalStateException("Must provide point");
        if (this.w < 0) throw new IllegalStateException("Width must be >= 0");
        if (this.h < 0) throw new IllegalStateException("Height must be >= 0");
        if (this.font == null) this.font = Font.get(this.name, this.weight, this.italicized);
        if (this.size < 0) throw new IllegalStateException("Text Size must be >= 0");
    }
    
    @Override
    protected void drawImpl()
    {
        DrawText2D.LOGGER.finest("Drawing text=\"%s\" anchor=(%s, %s) bounds=(%s, %s) font=%s size=%s align=%s ignoreFormatting=%s color=(%s, %s, %s, %s)",
                                 this.text,
                                 this.x, this.y, this.w, this.h,
                                 this.font, this.size, this.align, this.ignoreFormatting,
                                 this.textR, this.textG, this.textB, this.textA);
        
        List<String> lines;
        if (this.w > 0 && this.h > 0)
        {
            lines = new ArrayList<>();
            
            TextState state = new TextState(this.font, this.weight, this.italicized, this.size);
            state.underline     = this.underline;
            state.strike        = this.strike;
            state.textR         = this.textR;
            state.textG         = this.textG;
            state.textB         = this.textB;
            state.textA         = this.textA;
            state.backR         = this.backR;
            state.backG         = this.backG;
            state.backB         = this.backB;
            state.backA         = this.backA;
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
        state.underline     = this.underline;
        state.strike        = this.strike;
        state.textR         = this.textR;
        state.textG         = this.textG;
        state.textB         = this.textB;
        state.textA         = this.textA;
        state.backR         = this.backR;
        state.backG         = this.backG;
        state.backB         = this.backB;
        state.backA         = this.backA;
        state.ignoreChanges = this.ignoreFormatting;
        
        TextState other = new TextState(this.font, this.weight, this.italicized, this.size);
        
        double actualHeight = this.font.getTextHeight(this.text, this.size);
        
        int hPos = this.align.getH(), vPos = this.align.getV();
        
        double yOffset = vPos == -1 ? 0 : vPos == 0 ? 0.5 * (this.h - actualHeight) : this.h - actualHeight;
        for (String line : lines)
        {
            double lineWidth  = this.font.getTextWidthImpl(line, other.set(state));
            double lineHeight = this.font.getTextHeightImpl(line, other.set(state));
            
            double xOffset = hPos == -1 ? 0 : hPos == 0 ? 0.5 * (this.w - lineWidth) : this.w - lineWidth;
            
            drawText(state, line, this.x + xOffset, this.y + yOffset);
            
            yOffset += lineHeight;
        }
    }
    
    public DrawText2D text(String text)
    {
        this.text = text;
        return this;
    }
    
    @Override
    public DrawText2D point(double x, double y)
    {
        this.x        = x;
        this.y        = y;
        this.hasPoint = true;
        return this;
    }
    
    @Override
    public DrawText2D size(double width, double height)
    {
        this.w = width;
        this.h = height;
        return this;
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
    
    public DrawText2D underline(boolean underline)
    {
        this.underline = false;
        return this;
    }
    
    public DrawText2D strike(boolean strike)
    {
        this.strike = false;
        return this;
    }
    
    @Override
    public DrawText2D color(int r, int g, int b, int a)
    {
        this.textR = r;
        this.textG = g;
        this.textB = b;
        this.textA = a;
        return this;
    }
    
    public DrawText2D backgroundColor(int r, int g, int b, int a)
    {
        this.backR = r;
        this.backG = g;
        this.backB = b;
        this.backA = a;
        return this;
    }
    
    public DrawText2D backgroundColor(@NotNull Colorc color)
    {
        return backgroundColor(color.r(), color.g(), color.b(), color.a());
    }
}
