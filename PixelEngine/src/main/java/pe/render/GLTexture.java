package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import pe.color.ColorFormat;
import pe.texture.Texture;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.util.Objects;

public class GLTexture
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    protected static GLTexture defaultTexture;
    
    static void setup()
    {
        GLTexture.LOGGER.fine("Setup");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            ByteBuffer pixels = stack.bytes((byte) 255, (byte) 255, (byte) 255, (byte) 255);
            GLTexture.defaultTexture = Texture.load(pixels, 1, 1, 1, ColorFormat.RGBA);
        }
    }
    
    static void destroy()
    {
        GLTexture.LOGGER.fine("Destroy");
        
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        
        GLTexture texture = GLTexture.defaultTexture;
        GLTexture.defaultTexture = null;
        texture.delete();
    }
    
    public static @NotNull GLTexture getDefault()
    {
        return GLTexture.defaultTexture;
    }
    
    public static void bindRaw(int textureType, int textureID, int index)
    {
        GLTexture.LOGGER.finest("Binding textureID=%s to index=%s", textureID, index);
        
        GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
        GL33.glBindTexture(textureType, textureID);
    }
    
    public static void bind(@Nullable GLTexture texture, int index)
    {
        if (texture == null) texture = GLTexture.defaultTexture;
        
        GLTexture.LOGGER.finest("Binding %s to index=%s", texture, index);
        
        GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
        GL33.glBindTexture(texture.type, texture.id);
    }
    
    public static void bind(@Nullable GLTexture texture)
    {
        bind(texture, 0);
    }
    
    public static void unbind(@Nullable GLTexture texture, int index)
    {
        if (texture == null) texture = GLTexture.defaultTexture;
        
        GLTexture.LOGGER.finest("Unbinding %s from index=%s", texture, index);
        
        GL33.glActiveTexture(GL33.GL_TEXTURE0 + index);
        GL33.glBindTexture(texture.type, 0);
    }
    
    public static void unbind(@Nullable GLTexture texture)
    {
        unbind(texture, 0);
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected final int type;
    
    protected int width;
    protected int height;
    
    protected int mipmaps = 1;
    
    protected ColorFormat format = ColorFormat.RGBA;
    
    protected GLTexture(int type)
    {
        this.id = GL33.glGenTextures();
        
        this.type = type;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof GLTexture other)) return false;
        return this.id == other.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.type, this.format);
    }
    
    @Override
    public @NotNull String toString()
    {
        return getClass().getSimpleName() + "{" + "id=" + this.id + ", width=" + this.width + ", height=" + this.height + ", format=" + this.format + '}';
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
    
    public int mipmaps()
    {
        return this.mipmaps;
    }
    
    public @NotNull ColorFormat format()
    {
        return this.format;
    }
    
    /**
     * Unload texture from GPU memory
     */
    public void delete()
    {
        if (!equals(GLTexture.defaultTexture) && this.id > 0)
        {
            GLTexture.LOGGER.fine("Deleting", this);
            
            GL33.glDeleteTextures(this.id);
            
            this.id = 0;
            
            this.width  = 0;
            this.height = 0;
            
            this.mipmaps = 1;
        }
    }
}
