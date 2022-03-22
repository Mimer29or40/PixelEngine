package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import pe.Window;
import pe.color.ColorFormat;
import pe.texture.Texture;
import pe.texture.TextureDepthStencil;
import rutils.Logger;

import java.util.Objects;

public class GLFramebuffer
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    static GLFramebuffer defaultFramebuffer;
    static GLFramebuffer current;
    
    static void setup()
    {
        GLFramebuffer.LOGGER.fine("Setup");
        
        GLFramebuffer.defaultFramebuffer = new DefaultFramebuffer();
    }
    
    static void destroy()
    {
        GLFramebuffer.LOGGER.fine("Destroy");
        
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
    }
    
    public static void bind(@Nullable GLFramebuffer framebuffer)
    {
        if (framebuffer == null) framebuffer = GLFramebuffer.defaultFramebuffer;
        
        GLFramebuffer.LOGGER.finest("Binding Framebuffer:", framebuffer);
        
        if (GLFramebuffer.current != framebuffer || GLFramebuffer.defaultFramebuffer == framebuffer)
        {
            GLFramebuffer.current = framebuffer;
            
            GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, framebuffer.id());
            GLState.viewport(0, 0, framebuffer.width(), framebuffer.height());
        }
    }
    
    public static @NotNull GLFramebuffer load(int width, int height)
    {
        return new GLFramebuffer(width, height);
    }
    
    /**
     * @return The width of the currently bound framebuffer.
     */
    public static int currentWidth()
    {
        return GLFramebuffer.current.width();
    }
    
    /**
     * @return The height of the currently bound framebuffer.
     */
    public static int currentHeight()
    {
        return GLFramebuffer.current.height();
    }
    
    // TODO
    // public static @NotNull GLFramebuffer load(int width, int height, int colorAttachments)
    // {
    //     return new GLFramebuffer(width, height, colorAttachments);
    // }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected int width;
    protected int height;
    
    protected final Texture color0;
    protected final Texture depthStencil;
    
    private GLFramebuffer()
    {
        this.color0       = null;
        this.depthStencil = null;
    }
    
    protected GLFramebuffer(int width, int height)
    {
        this.id = GL33.glGenFramebuffers();
        
        this.width  = width;
        this.height = height;
        
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.id);
        
        this.color0 = Texture.load(width, height, ColorFormat.RGBA);
        // TODO - GL_COLOR_ATTACHMENT0
        // TODO - GL_COLOR_ATTACHMENT1
        // TODO - GL_COLOR_ATTACHMENT2
        // TODO - GL_COLOR_ATTACHMENT3
        // TODO - GL_COLOR_ATTACHMENT4
        // TODO - GL_COLOR_ATTACHMENT5
        // TODO - GL_COLOR_ATTACHMENT6
        // TODO - GL_COLOR_ATTACHMENT7
        // TODO - GL_COLOR_ATTACHMENT31
        this.depthStencil = TextureDepthStencil.load(width, height);
        
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_COLOR_ATTACHMENT0, GL33.GL_TEXTURE_2D, this.color0.id(), 0);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_STENCIL_ATTACHMENT, GL33.GL_TEXTURE_2D, this.depthStencil.id(), 0);
        
        int status = GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER);
        
        if (status != GL33.GL_FRAMEBUFFER_COMPLETE)
        {
            String message = switch (status)
                    {
                        case GL33.GL_FRAMEBUFFER_UNDEFINED -> "GL_FRAMEBUFFER_UNDEFINED";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> "GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> "GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER";
                        case GL33.GL_FRAMEBUFFER_UNSUPPORTED -> "GL_FRAMEBUFFER_UNSUPPORTED";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE -> "GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE";
                        case GL33.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS -> "GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS";
                        default -> "" + status;
                    };
            throw new IllegalStateException(String.format("%s: Framebuffer Error: %s", this, message));
        }
        
        GLFramebuffer.LOGGER.fine("Created", this);
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
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", width=" + this.width + ", height=" + this.height + '}';
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
    
    public @Nullable Texture color()
    {
        return this.color0;
    }
    
    public @Nullable Texture depthStencil()
    {
        return this.depthStencil;
    }
    
    /**
     * Delete framebuffer from GPU
     */
    public void delete()
    {
        GL33.glDeleteFramebuffers(this.id);
        
        this.color0.delete();
        this.depthStencil.delete();
        
        this.id = 0;
        
        GLFramebuffer.LOGGER.fine("Unloaded", this);
    }
    
    private static class DefaultFramebuffer extends GLFramebuffer
    {
        private DefaultFramebuffer()
        {
            this.id = 0;
        }
        
        @Override
        public boolean equals(Object o)
        {
            return this == o;
        }
        
        @Override
        public int hashCode()
        {
            return Objects.hash(-1);
        }
        
        @Override
        public String toString()
        {
            return "DefaultFramebuffer{}";
        }
        
        @Override
        public int width()
        {
            return Window.framebufferWidth();
        }
        
        @Override
        public int height()
        {
            return Window.framebufferHeight();
        }
        
        @Override
        public void delete()
        {
            GLFramebuffer.LOGGER.warning("Cannot delete Default Framebuffer");
        }
    }
}
