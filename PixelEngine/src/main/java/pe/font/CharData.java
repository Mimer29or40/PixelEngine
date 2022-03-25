package pe.font;

import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.*;

public final class CharData
{
    private final Font font;
    
    public final char character;
    public final int  index;
    
    public final int advanceWidthUnscaled;
    public final int leftSideBearingUnscaled;
    
    public final int x0Unscaled;
    public final int y0Unscaled;
    public final int x1Unscaled;
    public final int y1Unscaled;
    
    CharData(Font font, int character, IntBuffer advanceWidth, IntBuffer leftSideBearing, IntBuffer x0, IntBuffer y0, IntBuffer x1, IntBuffer y1)
    {
        this.font = font;
        
        this.character = (char) character;
        this.index     = stbtt_FindGlyphIndex(font.info, this.character);
        
        stbtt_GetGlyphHMetrics(font.info, this.index, advanceWidth, leftSideBearing);
        
        this.advanceWidthUnscaled    = advanceWidth.get(0);
        this.leftSideBearingUnscaled = leftSideBearing.get(0);
        
        stbtt_GetGlyphBox(font.info, this.index, x0, y0, x1, y1);
        
        this.x0Unscaled = x0.get(0);
        this.y0Unscaled = y0.get(0);
        this.x1Unscaled = x1.get(0);
        this.y1Unscaled = y1.get(0);
    }
    
    @Override
    public String toString()
    {
        return "CharData{" + this.character + ", font=" + this.font + "}";
    }
}
