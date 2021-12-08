package pe.texture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.render.GLTexture;
import rutils.Logger;

import java.nio.Buffer;

public class Texture extends GLTexture
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    private static @NotNull Texture load(long data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        Texture texture = new Texture();
        
        texture.width  = width;
        texture.height = height;
        
        texture.mipmaps = mipmaps;
        
        texture.format = format;
        
        GLTexture.bind(texture);
        
        int mipWidth  = width;
        int mipHeight = height;
        int mipOffset = 0; // Mipmap data offset
        
        if (format.format < 0) throw new IllegalArgumentException("Invalid Format: " + format);
        
        // Load the different mipmap levels
        for (int i = 0; i < mipmaps; i++)
        {
            int mipSize = mipWidth * mipHeight * format.sizeof;
            
            Texture.LOGGER.finer("Load mipmap level %s (%s x %s), size: %s, offset: %s", i, mipWidth, mipHeight, mipSize, mipOffset);
            
            GL33.glTexImage2D(GL33.GL_TEXTURE_2D, i, format.internalFormat, mipWidth, mipHeight, 0, format.format, GL33.GL_UNSIGNED_BYTE, data + mipOffset);
            
            mipWidth >>= 1;
            mipHeight >>= 1;
            mipOffset += mipSize;
            
            // Security check for NPOT textures
            if (mipWidth < 1) mipWidth = 1;
            if (mipHeight < 1) mipHeight = 1;
        }
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            switch (format)
            {
                case GRAY -> GL33.glTexParameteriv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL33.GL_RED, GL33.GL_RED, GL33.GL_RED, GL33.GL_ONE));
                case GRAY_ALPHA -> GL33.glTexParameteriv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL33.GL_RED, GL33.GL_RED, GL33.GL_RED, GL33.GL_GREEN));
                case RGB -> GL33.glTexParameteriv(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA, stack.ints(GL33.GL_RED, GL33.GL_GREEN, GL33.GL_BLUE, GL33.GL_ONE));
            }
        }
        
        // Default Texture Parameters
        texture.wrap(TextureWrap.DEFAULT, TextureWrap.DEFAULT);
        
        // Magnification and Minification filters
        // Activate Tri-Linear filtering if mipmaps are available
        texture.filter(TextureFilter.DEFAULT, mipmaps > 1 ? TextureFilter.NEAREST_MIPMAP_LINEAR : TextureFilter.DEFAULT);
        
        Texture.LOGGER.fine("Created", texture);
        
        return texture;
    }
    
    public static @NotNull Texture load(@Nullable Buffer data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        return load(data == null ? MemoryUtil.NULL : MemoryUtil.memAddress(data), width, height, mipmaps, format);
    }
    
    public static @NotNull Texture load(@Nullable CustomBuffer<?> data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        return load(MemoryUtil.memAddressSafe(data), width, height, mipmaps, format);
    }
    
    public static @NotNull Texture load(int width, int height, @NotNull ColorFormat format)
    {
        return load(MemoryUtil.NULL, width, height, 1, format);
    }
    
    public static @NotNull Texture load(@NotNull Image image)
    {
        return load(MemoryUtil.memAddressSafe(image.data()), image.width(), image.height(), image.mipmaps(), image.format());
    }
    
    public static @NotNull Texture load(@NotNull String filePath)
    {
        Image image = Image.loadFromFile(filePath);
        
        Texture texture = load(MemoryUtil.memAddressSafe(image.data()), image.width(), image.height(), image.mipmaps(), image.format());
        
        image.delete();
        
        return texture;
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected Texture()
    {
        super(GL33.GL_TEXTURE_2D);
    }
    
    public void wrap(@NotNull TextureWrap s, @NotNull TextureWrap t)
    {
        GLTexture.bind(this);
        
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, s.ref);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, t.ref);
    }
    
    public void filter(@NotNull TextureFilter min, @NotNull TextureFilter mag)
    {
        GLTexture.bind(this);
        
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, min.ref);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, mag.ref);
    }
    
    /**
     * Read texture pixel data.
     * <p>
     * Make sure that it is bound first.
     */
    public @Nullable Color.Buffer getPixelData()
    {
        GLTexture.bind(this);
        
        int bufferSize = 0;
        
        int mipWidth  = this.width;
        int mipHeight = this.height;
        
        // Load the different mipmap levels
        for (int i = 0; i < this.mipmaps; i++)
        {
            bufferSize += mipWidth * mipHeight * this.format.sizeof;
            
            mipWidth >>= 1;
            mipHeight >>= 1;
            
            if (mipWidth < 1) mipWidth = 1;
            if (mipHeight < 1) mipHeight = 1;
        }
        
        Color.Buffer pixels = Color.malloc(this.format, bufferSize);
        
        long ptr = pixels.address();
        
        mipWidth  = this.width;
        mipHeight = this.height;
        
        // Load the different mipmap levels
        for (int i = 0; i < this.mipmaps; i++)
        {
            int mipSize = mipWidth * mipHeight * this.format.sizeof;
            
            GL33.glGetTexImage(this.type, i, this.format.format, GL33.GL_UNSIGNED_BYTE, ptr);
            
            mipWidth >>= 1;
            mipHeight >>= 1;
            ptr += mipSize;
            
            if (mipWidth < 1) mipWidth = 1;
            if (mipHeight < 1) mipHeight = 1;
        }
        
        return pixels;
    }
    
    /**
     * Update GPU texture rectangle with new data
     * <p>
     * NOTE: pixels data must match texture.format
     */
    public void update(@NotNull Color.Buffer data, int x, int y, int width, int height)
    {
        if (this.format != data.format())
        {
            Texture.LOGGER.warning("Data format (%s) does not match texture (%s)", data.format(), this);
            return;
        }
        
        long pixels = MemoryUtil.memAddressSafe(data);
        
        GL33.glTexSubImage2D(this.type, 0, x, y, width, height, this.format.format, GL33.GL_UNSIGNED_BYTE, pixels);
    }
    
    /**
     * Update GPU texture with new data
     * <p>
     * NOTE: pixels data must match texture.format
     */
    public void update(@NotNull Color.Buffer data)
    {
        update(data, 0, 0, this.width, this.height);
    }
    
    /**
     * Get pixel data from GPU texture and return an Image
     * <p>
     * NOTE: Compressed texture formats not supported
     */
    public @NotNull Image toImage()
    {
        return Image.load(getPixelData(), this.width, this.height, this.mipmaps, this.format);
    }
    
    /**
     * Generate mipmap data for selected texture
     */
    public void genMipmaps()
    {
        // NOTE: NPOT textures support check inside function
        // On WebGL (OpenGL ES 2.0) NPOT textures support is limited
        GLTexture.bind(this);
        
        // Check if texture is power-of-two (POT)
        if (this.width > 0 && (this.width & this.width - 1) == 0 &&
            this.height > 0 && (this.height & this.height - 1) == 0)
        {
            // Hint for mipmaps generation algorithm: GL.FASTEST, GL.NICEST, GL.DONT_CARE
            GL33.glHint(GL33.GL_GENERATE_MIPMAP_HINT, GL33.GL_DONT_CARE);
            GL33.glGenerateMipmap(this.type); // Generate mipmaps automatically
            
            filter(TextureFilter.LINEAR, TextureFilter.LINEAR_MIPMAP_LINEAR); // Activate Tri-Linear filtering for mipmaps
            
            this.mipmaps = 1 + (int) Math.floor(Math.log(Math.max(this.width, this.height)) / Math.log(2));
            Texture.LOGGER.info("Mipmaps Generated (%s): %s", this.mipmaps, this);
        }
        else
        {
            Texture.LOGGER.warning("Failed to Generate Mipmaps:", this);
        }
    }
}
