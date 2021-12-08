package pe.texture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.render.GLTexture;
import rutils.Logger;

public class TextureDepthStencil extends Texture
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    public static @NotNull TextureDepthStencil load(int width, int height)
    {
        TextureDepthStencil texture = new TextureDepthStencil();
        
        texture.width  = width;
        texture.height = height;
        
        texture.format = ColorFormat.RGBA;
        
        GLTexture.bind(texture);
        
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_DEPTH24_STENCIL8, width, height, 0, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8, MemoryUtil.NULL);
        
        // Default Texture Parameters
        texture.wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        texture.filter(TextureFilter.DEFAULT, TextureFilter.DEFAULT);
        
        TextureDepthStencil.LOGGER.fine("Created", texture);
        
        return texture;
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    @Override
    public @Nullable Color.Buffer getPixelData()
    {
        GLTexture.bind(this);
        
        Color.Buffer pixels = Color.malloc(this.format, this.width * this.height * this.format.sizeof);
        
        GL33.glGetTexImage(this.type, 0, GL33.GL_DEPTH_STENCIL, GL33.GL_UNSIGNED_INT_24_8, pixels.address());
        
        return pixels;
    }
    
    @Override
    public void update(Color.@NotNull Buffer data, int x, int y, int width, int height)
    {
        throw new UnsupportedOperationException("Not supported for TextureDepthStencil");
    }
    
    @Override
    public void genMipmaps()
    {
        throw new UnsupportedOperationException("Not supported for TextureDepthStencil");
    }
}
