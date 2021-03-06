package pe.gui;

import pe.Keyboard;
import pe.gui.event.IModalClosed;
import pe.gui.event.IModalOpened;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

import static pe.PixelEngine.*;

public abstract class Modal extends Window
{
    protected final Label title;
    protected final Label text;
    
    public Modal(String title, String text)
    {
        super(null);
        
        this.title = new Label(this, title);
        this.title.setPosition(0, 0);
        this.title.setWidth(getWidth());
        this.title.onMouseButtonDragged((button, widgetX, widgetY, dragX, dragY, relX, relY) -> {
            setPosition(getX() + relX, getY() + relY);
            return true;
        });
        this.title.onKeyboardKeyDown(key -> key != Keyboard.ESCAPE);
    
        this.text = new Label(this, text);
        this.text.setPosition(getForegroundOriginX(), getForegroundOriginY());
        this.text.setBorderSize(0);
        this.text.setMarginSize(0);
        this.text.setTextPosition(TextPosition.TOP_LEFT);
        this.text.onKeyboardKeyDown(key -> key != Keyboard.ESCAPE);
        this.text.onMouseButtonDown((button, widgetX, widgetY) -> false);
        this.text.onMouseButtonUp((button, widgetX, widgetY) -> false);
        this.text.onMouseButtonHeld((button, widgetX, widgetY) -> false);
        this.text.onMouseButtonRepeated((button, widgetX, widgetY) -> false);
        this.text.onMouseButtonDragged((button, widgetX, widgetY, dragX, dragY, relX, relY) -> false);
        this.text.onMouseButtonClicked((button, widgetX, widgetY, doubleClicked) -> false);
    
        setVisible(false);
        setMarginSize(1);
    
        initButtons();
        for (Button button : getButtons()) button.onKeyboardKeyDown(key -> key != Keyboard.ESCAPE);
    
        String linesString = join(clipTextWidth(getText(), this.text.getScale(), 150), "\n");
        setForegroundSize(textWidth(linesString, this.text.getScale()), textHeight(linesString, this.text.getScale()));
    }
    
    @Override
    public void addChild(Window child)
    {
        super.addChild(child);
        if (child instanceof Button) this.buttons.add((Button) child);
    }
    
    @Override
    protected void updatedWidth(int prev, int width)
    {
        super.updatedWidth(prev, width);
        this.title.setWidth(width);
        this.text.setForegroundWidth(getForegroundWidth());
    }
    
    @Override
    protected void updatedHeight(int prev, int height)
    {
        super.updatedHeight(prev, height);
        this.text.setForegroundHeight(getForegroundHeight());
        moveButtons();
    }
    
    @Override
    protected void updatedBorderSize(int prev, int borderSize)
    {
        super.updatedBorderSize(prev, borderSize);
        this.title.setBorderSize(borderSize);
        this.text.setSize(getForegroundWidth(), getForegroundHeight());
        moveButtons();
    }
    
    @Override
    protected void updatedMarginSize(int prev, int marginSize)
    {
        super.updatedMarginSize(prev, marginSize);
        this.text.setPosition(getForegroundOriginX(), getForegroundOriginY());
        this.text.setSize(getForegroundWidth(), getForegroundHeight());
        moveButtons();
    }
    
    @Override
    public int getForegroundOriginY()
    {
        return super.getForegroundOriginY() + this.title.getHeight() - 1;
    }
    
    @Override
    public int getForegroundHeight()
    {
        OptionalInt buttonHeight = getButtons().stream().mapToInt(Window::getHeight).max();
        return super.getForegroundHeight() - (this.title.getHeight() - 1) - getMarginSize() - (buttonHeight.isPresent() ? buttonHeight.getAsInt() : 0);
    }
    
    @Override
    public void setForegroundHeight(int height)
    {
        OptionalInt buttonHeight = getButtons().stream().mapToInt(Window::getHeight).max();
        super.setForegroundHeight(height + (this.title.getHeight() - 1) + getMarginSize() + (buttonHeight.isPresent() ? buttonHeight.getAsInt() : 0));
    }
    
    public String getTitle()
    {
        return this.title.getText();
    }
    
    public void setTitle(String title)
    {
        this.title.setText(title);
    }
    
    public String getText()
    {
        return this.text.getText();
    }
    
    public void setText(String text)
    {
        this.text.setText(text);
    }
    
    private final List<Button> buttons = new ArrayList<>();
    
    protected List<Button> getButtons()
    {
        return this.buttons;
    }
    
    protected abstract void initButtons();
    
    protected void moveButtons()
    {
        for (int i = 0; i < getButtons().size(); i++)
        {
            getButtons().get(i).setX((i > 0 ? getButtons().get(i - 1).getX() : getWidth()) - getMarginSize() - getBorderSize() - getButtons().get(i).getWidth());
            getButtons().get(i).setY(getHeight() - getMarginSize() - getBorderSize() - getButtons().get(i).getHeight());
        }
    }
    
    public void open()
    {
        if (!isVisible())
        {
            setVisible(true);
            setPosition((screenWidth() - getWidth()) / 2, (screenHeight() - getHeight()) / 2);
            
            setParent(PEX_GUI.MODAL);
            setFocused();
            
            onModalOpened();
        }
    }
    
    public void close(int result)
    {
        if (isVisible())
        {
            setVisible(false);
            setPosition(screenWidth() + 10, screenHeight() + 10);
            
            setParent(null);
            
            onModalClosed(result);
        }
    }
    
    /*
     * Events
     */
    
    @Override
    protected boolean onKeyboardKeyDown(Keyboard.Key key)
    {
        if (super.onKeyboardKeyDown(key))
        {
            if (key == Keyboard.ESCAPE) close(0);
            return true;
        }
        return false;
    }
    
    private IModalOpened modalOpened;
    
    public void onModalOpened(IModalOpened modalOpened)
    {
        this.modalOpened = modalOpened;
    }
    
    protected void onModalOpened()
    {
        if (this.modalOpened != null) this.modalOpened.fire();
    }
    
    private IModalClosed modalClosed;
    
    public void onModalClosed(IModalClosed modalClosed)
    {
        this.modalClosed = modalClosed;
    }
    
    protected void onModalClosed(int result)
    {
        if (this.modalClosed != null) this.modalClosed.fire(result);
    }
}
