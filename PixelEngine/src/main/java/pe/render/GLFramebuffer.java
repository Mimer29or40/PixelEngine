package pe.render;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import pe.Engine;
import rutils.Logger;

import java.util.Objects;

public class GLFramebuffer
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    private static GLFramebuffer current;
    
    static void setup()
    {
        GLFramebuffer.LOGGER.fine("Setup");
    }
    
    static void destroy()
    {
        GLFramebuffer.LOGGER.fine("Destroy");
        
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GLFramebuffer.current = null;
    }
    
    public static void bind(@Nullable GLFramebuffer framebuffer)
    {
        if (!Objects.equals(GLFramebuffer.current, framebuffer))
        {
            GLFramebuffer.LOGGER.finest("Binding Framebuffer:", framebuffer);
            
            GLFramebuffer.current = framebuffer;
            
            if (framebuffer == null)
            {
                GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
                GL33.glViewport(Engine.Viewport.x(), Engine.Viewport.y(), Engine.Viewport.width(), Engine.Viewport.height());
            }
            else
            {
                GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, framebuffer.id());
                GL33.glViewport(0, 0, framebuffer.width(), framebuffer.height());
            }
        }
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected final int width;
    protected final int height;
    
    public GLFramebuffer(int width, int height)
    {
        this.id = GL33.glGenFramebuffers();
        
        this.width  = width;
        this.height = height;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof GLFramebuffer other)) return false;
        return this.id == other.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + '}';
    }
    
    public int id()
    {
        return this.id;
    }
    
    public int width()
    {
        return this.width;
    }
    
    public int height()
    {
        return this.height;
    }
}
