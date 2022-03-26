package pe.font;

import org.jetbrains.annotations.NotNull;

public class TextState
{
    public Weight  weight;
    public boolean italicized;
    
    public Font       baseFont;
    public FontSingle prevFont;
    public FontSingle currFont;
    
    public int size;
    
    public boolean underline = false;
    public boolean strike    = false;
    
    public int textR = 255;
    public int textG = 255;
    public int textB = 255;
    public int textA = 255;
    
    public int backgroundR = 0;
    public int backgroundG = 0;
    public int backgroundB = 0;
    public int backgroundA = 0;
    
    public boolean ignoreChanges = false;
    
    private boolean changeFont = false;
    private boolean inModifier = false;
    
    private final StringBuilder modifier = new StringBuilder();
    
    public TextState(@NotNull Font font, Weight weight, boolean italicized, int size)
    {
        this.weight     = weight;
        this.italicized = italicized;
        
        this.baseFont = font;
        this.prevFont = null;
        this.currFont = this.baseFont.withProperties(this.weight, this.italicized);
        
        this.size = size;
    }
    
    public @NotNull TextState set(@NotNull TextState state)
    {
        this.weight     = state.weight;
        this.italicized = state.italicized;
        
        this.baseFont = state.baseFont;
        this.prevFont = state.prevFont;
        this.currFont = state.currFont;
        
        this.size = state.size;
        
        this.underline = state.underline;
        this.strike    = state.strike;
        
        this.textR = state.textR;
        this.textG = state.textG;
        this.textB = state.textB;
        this.textA = state.textA;
        
        this.backgroundR = state.backgroundR;
        this.backgroundG = state.backgroundG;
        this.backgroundB = state.backgroundB;
        this.backgroundA = state.backgroundA;
        
        this.ignoreChanges = state.ignoreChanges;
        this.changeFont    = state.changeFont;
        this.inModifier    = state.inModifier;
        
        this.modifier.setLength(0);
        this.modifier.append(state.modifier);
        
        return this;
    }
    
    boolean handleModifier(char character)
    {
        if (character == TextFormat.MODIFIER)
        {
            if (!this.inModifier)
            {
                this.modifier.setLength(0);
            }
            else if (!this.ignoreChanges)
            {
                changeState(this.modifier.toString());
            }
            this.inModifier = !this.inModifier;
            return true;
        }
        if (this.inModifier)
        {
            this.modifier.append(character);
            return true;
        }
        return false;
    }
    
    void changeState(@NotNull String modifierTag)
    {
        String[] mod = modifierTag.split("" + TextFormat.SEPARATOR);
        switch (mod[0])
        {
            case TextFormat.RESET -> {
                this.weight     = Font.DEFAULT_WEIGHT;
                this.italicized = Font.DEFAULT_ITALICS;
                
                this.underline = false;
                this.strike    = false;
                
                this.textR = 255;
                this.textG = 255;
                this.textB = 255;
                this.textA = 255;
                
                this.backgroundR = 0;
                this.backgroundG = 0;
                this.backgroundB = 0;
                this.backgroundA = 0;
            }
            case TextFormat.WEIGHT -> this.weight = Weight.get(mod[1]);
            case TextFormat.ITALICS -> this.italicized = Boolean.parseBoolean(mod[1]);
            case TextFormat.UNDERLINE -> this.underline = Boolean.parseBoolean(mod[1]);
            case TextFormat.STRIKE -> this.strike = Boolean.parseBoolean(mod[1]);
            case TextFormat.COLOR -> {
                int colorNumber = Integer.parseInt(mod[1], 16);
                this.textR = (colorNumber >> 16) & 0xFF;
                this.textG = (colorNumber >> 8) & 0xFF;
                this.textB = colorNumber & 0xFF;
            }
            case TextFormat.COLOR_ALPHA -> {
                int colorNumber = Integer.parseInt(mod[1], 16);
                this.textA = colorNumber & 0xFF;
            }
            case TextFormat.BACKGROUND -> {
                int colorNumber = Integer.parseInt(mod[1], 16);
                this.backgroundR = (colorNumber >> 16) & 0xFF;
                this.backgroundG = (colorNumber >> 8) & 0xFF;
                this.backgroundB = colorNumber & 0xFF;
            }
            case TextFormat.BACKGROUND_ALPHA -> {
                int colorNumber = Integer.parseInt(mod[1], 16);
                this.backgroundA = colorNumber & 0xFF;
            }
        }
        
        this.changeFont = true;
    }
    
    void changeFont()
    {
        if (this.changeFont)
        {
            this.prevFont = this.currFont;
            this.currFont = this.baseFont.withProperties(this.weight, this.italicized);
            
            this.changeFont = false;
        }
    }
}
