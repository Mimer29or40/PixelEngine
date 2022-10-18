package pe.debug;

import pe.Debug2;
import pe.color.Colorc;

public class DebugLabel extends DebugElement
{
    public String text;
    public Colorc textColor;
    public Colorc backgroundColor;
    
    public DebugLabel(String text)
    {
        this.text = text;
    }
    
    @Override
    public void handleEvents()
    {
    
    }
    
    @Override
    public void draw(int contentX, int contentY, int contentW, int contentH)
    {
        if (this.text == null) this.text = "";
        
        int textW = Debug2.textWidth(this.text);
        int textH = Debug2.textHeight(this.text);
        
        if (this.rect.size.x < textW) this.rect.size.x = textW;
        if (this.rect.size.y < textH) this.rect.size.y = textH;
        
        Debug2.drawTextWithBackground(contentX + this.rect.x(), contentY + this.rect.y(), this.text, this.textColor, this.backgroundColor);
    }
}
