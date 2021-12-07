package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.ColorFormat;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.util.Objects;

public class GLTexture
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    private static GLTexture defaultTexture;
    private static GLTexture current;
    
    static void setup()
    {
        GLTexture.LOGGER.fine("Setup");
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            // 1 pixel RGBA (4 bytes)
            ByteBuffer pixels = stack.bytes((byte) 255, (byte) 255, (byte) 255, (byte) 255);
            GLTexture.defaultTexture        = new GLTexture(GL33.GL_TEXTURE_2D);
            GLTexture.defaultTexture.width  = 1;
            GLTexture.defaultTexture.height = 1;
            GL33.glBindTexture(GL33.GL_TEXTURE_2D, GLTexture.defaultTexture.id);
            GL33.glTexImage2D(GLTexture.defaultTexture.type, 0, GL33.GL_RGBA, 1, 1, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(pixels));
        }
    }
    
    static void destroy()
    {
        GLTexture.LOGGER.fine("Destroy");
        
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GLTexture.current = null;
        
        GLTexture texture = GLTexture.defaultTexture;
        GLTexture.defaultTexture = null;
        texture.delete();
    }
    
    public static @NotNull GLTexture getDefault()
    {
        return GLTexture.defaultTexture;
    }
    
    public static void bind(@Nullable GLTexture texture)
    {
        if (texture == null) texture = GLTexture.defaultTexture;
        
        if (!Objects.equals(GLTexture.current, texture))
        {
            GLTexture.LOGGER.finest("Binding Texture:", texture);
            
            GLTexture.current = texture;
            
            GL33.glActiveTexture(GL33.GL_TEXTURE0);
            GL33.glBindTexture(texture.type, texture.id);
        }
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
    
    // /**
    //  * Read texture pixel data.
    //  * <p>
    //  * Make sure that it is bound first.
    //  */
    // public @Nullable Color.Buffer getPixelData()
    // {
    //     int bufferSize = 0;
    //
    //     int mipWidth  = this.width;
    //     int mipHeight = this.height;
    //
    //     // Load the different mipmap levels
    //     for (int i = 0; i < this.mipmaps; i++)
    //     {
    //         bufferSize += mipWidth * mipHeight * this.format.sizeOf();
    //
    //         mipWidth >>= 1;
    //         mipHeight >>= 1;
    //
    //         if (mipWidth < 1) mipWidth = 1;
    //         if (mipHeight < 1) mipHeight = 1;
    //     }
    //
    //     GL33.glPixelStorei(GL33.GL_PACK_ALIGNMENT, 1);
    //
    //     int glInternalFormat = this.format.internalFormat();
    //     int glFormat         = this.format.format();
    //     int glType           = this.format.type();
    //
    //     Color.Buffer pixels = Color.malloc(this.format, bufferSize);
    //
    //     long ptr = pixels.address();
    //
    //     mipWidth  = this.width;
    //     mipHeight = this.height;
    //
    //     // Load the different mipmap levels
    //     for (int i = 0; i < this.mipmaps; i++)
    //     {
    //         int mipSize = mipWidth * mipHeight * this.format.sizeOf();
    //
    //         GL33.glGetTexImage(getTextureTypeInt(this.type), i, glFormat, glType, ptr);
    //
    //         mipWidth >>= 1;
    //         mipHeight >>= 1;
    //         ptr += mipSize;
    //
    //         if (mipWidth < 1) mipWidth = 1;
    //         if (mipHeight < 1) mipHeight = 1;
    //     }
    //
    //     return pixels;
    // }
    
    public enum Type
    {
        // TEXTURE_1D,
        TEXTURE_2D,
        // TEXTURE_3D,
        
        // ARRAY_1D,
        // ARRAY_2D,
        
        // BUFFER,
        // RECTANGLE,
        
        // MULTI_SAMPLE_2D,
        // MULTI_SAMPLE_ARRAY_2D,
        
        CUBE_MAP,
        // CUBE_MAP_POSITIVE_X,
        // CUBE_MAP_NEGATIVE_X,
        // CUBE_MAP_POSITIVE_Y,
        // CUBE_MAP_NEGATIVE_Y,
        // CUBE_MAP_POSITIVE_Z,
        // CUBE_MAP_NEGATIVE_Z,
        // CUBE_MAP_ARRAY,
        
        // RENDERBUFFER,
        
        // TEXTURE_1D
        // TEXTURE_2D
        // TEXTURE_3D
        // TEXTURE_1D_ARRAY
        // TEXTURE_2D_ARRAY
        // TEXTURE_BUFFER
        // TEXTURE_RECTANGLE
        // TEXTURE_CUBE_MAP
        // TEXTURE_CUBE_MAP_ARRAY
        // TEXTURE_2D_MULTISAMPLE
        // TEXTURE_2D_MULTISAMPLE_ARRAY
    }
}
