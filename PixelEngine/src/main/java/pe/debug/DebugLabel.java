package pe.debug;

import pe.Debug2;
import pe.color.Color;
import pe.color.Colorc;

public class DebugLabel extends Element
{
    public static final Colorc TEXT_COLOR       = Color.WHITE;
    public static final Colorc BACKGROUND_COLOR = Color.GRAY;
    
    public String text;
    
    public DebugLabel(String text)
    {
        this.text = text;
    }
    
    @Override
    protected void layout(int contentX, int contentY, int contentW, int contentH)
    {
        if (this.text == null) this.text = "";
        
        int textW = Debug2.textWidth(this.text);
        int textH = Debug2.textHeight(this.text);
        
        this.rect.pos.set(contentX, contentY);
        if (this.rect.size.x < textW) this.rect.size.x = textW;
        if (this.rect.size.y < textH) this.rect.size.y = textH;
    }
    
    @Override
    protected void draw()
    {
        Debug2.drawTextWithBackground(this.rect.x(), this.rect.y(), this.text, DebugLabel.TEXT_COLOR, DebugLabel.BACKGROUND_COLOR);
    }
}
