package pe.texture;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.BlendMode;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.color.Colorc;
import pe.util.Random;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;

import static org.lwjgl.stb.STBImage.stbi_load_from_memory;
import static org.lwjgl.stb.STBImage.stbi_load_gif_from_memory;
import static org.lwjgl.stb.STBImageResize.stbir_resize_uint8;
import static org.lwjgl.stb.STBImageWrite.*;
import static org.lwjgl.stb.STBPerlin.stb_perlin_fbm_noise3;
import static rutils.IOUtil.*;

public class Image
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    public static @NotNull Image load(@Nullable Color.Buffer data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        return new Image(data, width, height, mipmaps, format);
    }
    
    public static @NotNull Image load(@Nullable ByteBuffer data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        return new Image(Color.wrapSafe(format, data), width, height, mipmaps, format);
    }
    
    public static @NotNull Image loadFromFile(@NotNull String filePath)
    {
        ByteBuffer fileData = readFromFile(filePath, new int[1], MemoryUtil::memAlloc);
        
        if (fileData == null) return new Image(null, 0, 0, 1, ColorFormat.RGBA);
        
        Color.Buffer colorData;
        int          width;
        int          height;
        int          mipmaps;
        ColorFormat  format;
        
        switch (getExtension(filePath))
        {
            case ".png",
                    ".bmp",
                    ".tga",
                    ".jpg",
                    ".jpeg",
                    ".gif",
                    ".pic",
                    ".psd",
                    ".pnm" -> {
                try (MemoryStack stack = MemoryStack.stackPush())
                {
                    IntBuffer w = stack.mallocInt(1);
                    IntBuffer h = stack.mallocInt(1);
                    IntBuffer c = stack.mallocInt(1);
                    
                    ByteBuffer _data = stbi_load_from_memory(fileData, w, h, c, 0);
                    
                    format = ColorFormat.get(c.get());
                    
                    colorData = Color.wrapSafe(format, _data);
                    
                    width  = w.get();
                    height = h.get();
                    
                    mipmaps = 1;
                }
            }
            case ".hdr" -> {
                try (MemoryStack stack = MemoryStack.stackPush())
                {
                    IntBuffer w = stack.mallocInt(1);
                    IntBuffer h = stack.mallocInt(1);
                    IntBuffer c = stack.mallocInt(1);
                    
                    ByteBuffer _data = stbi_load_from_memory(fileData, w, h, c, 0);
                    
                    format = switch (c.get())
                            {
                                case 2 -> throw new RuntimeException("HDR file format not supported");
                                case 3 -> ColorFormat.RGB;
                                case 4 -> ColorFormat.RGBA;
                                default -> ColorFormat.GRAY;
                            };
                    
                    colorData = Color.wrapSafe(format, _data);
                    
                    width  = w.get();
                    height = h.get();
                    
                    mipmaps = 1;
                }
            }
            case ".engine_img" -> {
                width  = fileData.getInt();
                height = fileData.getInt();
                
                mipmaps = fileData.getInt();
                
                format = ColorFormat.values()[fileData.getInt()];
                
                colorData = Color.malloc(format, width * height);
                
                MemoryUtil.memCopy(MemoryUtil.memAddress(fileData), colorData.address(), fileData.remaining());
            }
            // case ".dds" -> {}
            // case ".pkm" -> {}
            // case ".ktx" -> {}
            // case ".pvr" -> {}
            // case ".astc" -> {}
            default -> throw new RuntimeException("File format not supported");
        }
        MemoryUtil.memFree(fileData);
        return new Image(colorData, width, height, mipmaps, format);
    }
    
    public static @NotNull Image loadAnimFromFile(@NotNull String fileName, int[] frameCount)
    {
        ByteBuffer fileData = readFromFile(fileName, new int[1], MemoryUtil::memAlloc);
        
        if (fileData == null) return new Image(null, 0, 0, 1, ColorFormat.RGBA);
        
        switch (getExtension(fileName))
        {
            case ".png", "gif" -> {
                try (MemoryStack stack = MemoryStack.stackPush())
                {
                    // NOTE: Frames delays are discarded
                    PointerBuffer delays = stack.mallocPointer(16);
                    
                    IntBuffer width  = stack.mallocInt(1);
                    IntBuffer height = stack.mallocInt(1);
                    IntBuffer frames = stack.mallocInt(1);
                    
                    IntBuffer channels = stack.mallocInt(1);
                    
                    ByteBuffer colorData = stbi_load_gif_from_memory(fileData, delays, width, height, frames, channels, 0);
                    
                    ColorFormat format = ColorFormat.get(channels.get());
                    
                    MemoryUtil.memFree(fileData);
                    
                    frameCount[0] = frames.get();
                    return new Image(Color.wrapSafe(format, colorData), width.get(), height.get(), 1, format);
                }
            }
            default -> {
                MemoryUtil.memFree(fileData);
                
                frameCount[0] = 1;
                return loadFromFile(fileName);
            }
        }
    }
    
    /**
     * Generates an {@link Image} filled with the specified {@link Colorc}.
     *
     * @param width  The width in pixels
     * @param height The height in pixels
     * @param color  The color value to set the {@link Image}
     * @return The new {@link Image}
     */
    public static @NotNull Image genColor(int width, int height, @NotNull Colorc color)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        for (int i = 0, n = width * height; i < n; i++) pixels.put(i, color);
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    /**
     * Generates an {@link Image} with a color gradient specified by the
     * provided {@link Colorc}.
     * <p>
     * The color of each pixel will be a linear interpolation of the colors at
     * the corner of the image.
     *
     * @param width       The width in pixels
     * @param height      The height in pixels
     * @param topLeft     The {@link Colorc} at the top left of the image.
     * @param topRight    The {@link Colorc} at the top right of the image.
     * @param bottomRight The {@link Colorc} at the bottom left of the image.
     * @param bottomLeft  The {@link Colorc} at the bottom right of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradient(int width, int height, @NotNull Colorc topLeft, @NotNull Colorc topRight, @NotNull Colorc bottomRight, @NotNull Colorc bottomLeft)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        Color colorL = Color.malloc(ColorFormat.RGBA);
        Color colorR = Color.malloc(ColorFormat.RGBA);
        
        int vFactor, hFactor;
        for (int j = 0; j < height; j++)
        {
            vFactor = (int) ((float) j / (height - 1) * 255);
            
            colorL.set((bottomLeft.r() * vFactor + topLeft.r() * (255 - vFactor)) / 255,
                       (bottomLeft.g() * vFactor + topLeft.g() * (255 - vFactor)) / 255,
                       (bottomLeft.b() * vFactor + topLeft.b() * (255 - vFactor)) / 255,
                       (bottomLeft.a() * vFactor + topLeft.a() * (255 - vFactor)) / 255);
            colorR.set((bottomRight.r() * vFactor + topRight.r() * (255 - vFactor)) / 255,
                       (bottomRight.g() * vFactor + topRight.g() * (255 - vFactor)) / 255,
                       (bottomRight.b() * vFactor + topRight.b() * (255 - vFactor)) / 255,
                       (bottomRight.a() * vFactor + topRight.a() * (255 - vFactor)) / 255);
            for (int i = 0; i < width; i++)
            {
                hFactor = (int) ((float) i / (width - 1) * 255);
                
                pixels.put(j * width + i,
                           (colorR.r() * hFactor + colorL.r() * (255 - hFactor)) / 255,
                           (colorR.g() * hFactor + colorL.g() * (255 - hFactor)) / 255,
                           (colorR.b() * hFactor + colorL.b() * (255 - hFactor)) / 255,
                           (colorR.a() * hFactor + colorL.a() * (255 - hFactor)) / 255);
            }
        }
        
        colorL.free();
        colorR.free();
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    /**
     * Generates an {@link Image} with a vertical color gradient specified by
     * the provided {@link Colorc}.
     * <p>
     * The each individual pixel will be linear interpolated from each color.
     *
     * @param width  The width in pixels
     * @param height The height in pixels
     * @param top    The {@link Colorc} at the top of the image.
     * @param bottom The {@link Colorc} at the bottom of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradientV(int width, int height, @NotNull Colorc top, @NotNull Colorc bottom)
    {
        return genColorGradient(width, height, top, top, bottom, bottom);
    }
    
    /**
     * Generates an {@link Image} with a horizontal color gradient specified by
     * the provided {@link Colorc}.
     * <p>
     * The each individual pixel will be linear interpolated from each color.
     *
     * @param width  The width in pixels
     * @param height The height in pixels
     * @param left   The {@link Colorc} at the left of the image.
     * @param right  The {@link Colorc} at the right of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradientH(int width, int height, @NotNull Colorc left, @NotNull Colorc right)
    {
        return genColorGradient(width, height, left, right, right, left);
    }
    
    /**
     * Generates an {@link Image} with a diagonal color gradient specified by
     * the provided {@link Colorc}. The gradient will move from the top left to
     * to the bottom right of the image.
     * <p>
     * The each individual pixel will be linear interpolated from each color.
     *
     * @param width       The width in pixels
     * @param height      The height in pixels
     * @param topLeft     The {@link Colorc} at the top left of the image.
     * @param bottomRight The {@link Colorc} at the bottom right of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradientDiagonalTLBR(int width, int height, @NotNull Colorc topLeft, @NotNull Colorc bottomRight)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            Color mid = Color.malloc(ColorFormat.RGBA, stack);
            mid.set((bottomRight.r() * 127 + topLeft.r() * 128) / 255,
                    (bottomRight.g() * 127 + topLeft.g() * 128) / 255,
                    (bottomRight.b() * 127 + topLeft.b() * 128) / 255,
                    (bottomRight.a() * 127 + topLeft.a() * 128) / 255);
            
            return genColorGradient(width, height, topLeft, mid, bottomRight, mid);
        }
    }
    
    /**
     * Generates an {@link Image} with a diagonal color gradient specified by
     * the provided {@link Colorc}. The gradient will move from the top right to
     * to the bottom left of the image.
     * <p>
     * The each individual pixel will be linear interpolated from each color.
     *
     * @param width      The width in pixels
     * @param height     The height in pixels
     * @param topRight   The {@link Colorc} at the top right of the image.
     * @param bottomLeft The {@link Colorc} at the bottom left of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradientDiagonalTRBL(int width, int height, @NotNull Colorc topRight, @NotNull Colorc bottomLeft)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            Color mid = Color.malloc(ColorFormat.RGBA, stack);
            mid.set((bottomLeft.r() * 127 + topRight.r() * 128) / 255,
                    (bottomLeft.g() * 127 + topRight.g() * 128) / 255,
                    (bottomLeft.b() * 127 + topRight.b() * 128) / 255,
                    (bottomLeft.a() * 127 + topRight.a() * 128) / 255);
            
            return genColorGradient(width, height, mid, topRight, mid, bottomLeft);
        }
    }
    
    /**
     * Generates an {@link Image} with a gradient from the center to the edges
     * specified by the provided {@link Colorc}.
     *
     * @param width   The width in pixels
     * @param height  The height in pixels
     * @param density The intensity of the gradient.
     * @param inner   The color at the center of the image.
     * @param outer   The color at the edges of the image.
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorGradientRadial(int width, int height, double density, @NotNull Colorc inner, @NotNull Colorc outer)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        double radius  = width < height ? width * 0.5 : height * 0.5;
        double centerX = (width - 1) * 0.5;
        double centerY = (height - 1) * 0.5;
        
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                double dist   = Math.hypot(i - centerX, j - centerY);
                double factor = (dist - radius * density) / (radius * (1.0f - density));
                
                factor = Math.max(0.0, Math.min(factor, 1.0));
                
                int f = (int) Math.round(255 * factor);
                
                pixels.put(j * width + i,
                           (outer.r() * f + inner.r() * (255 - f)) / 255,
                           (outer.g() * f + inner.g() * (255 - f)) / 255,
                           (outer.b() * f + inner.b() * (255 - f)) / 255,
                           (outer.a() * f + inner.a() * (255 - f)) / 255);
            }
        }
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    /**
     * Generates an {@link Image} with a checker board pattern with the
     * specified colors.
     * <p>
     * The size of the pattern is in pixels.
     *
     * @param width   The width in pixels
     * @param height  The height in pixels
     * @param checksX The width of each tile in pixels
     * @param checksY The height of each tile in pixels
     * @param col1    The first color
     * @param col2    The second color
     * @return The new {@link Image}
     */
    public static @NotNull Image genColorCheckered(int width, int height, int checksX, int checksY, @NotNull Colorc col1, @NotNull Colorc col2)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                pixels.put(j * width + i, (i / checksX + j / checksY) % 2 == 0 ? col1 : col2);
            }
        }
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    // TODO - Use my noise
    public static @NotNull Image genNoiseWhite(int width, int height, double factor)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        Random random = new Random();
        
        for (int i = 0, n = width * height; i < n; i++) pixels.put(i, random.nextDouble() < factor ? Color.WHITE : Color.BLACK);
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    // TODO - Use my noise
    public static @NotNull Image genNoisePerlin(int width, int height, int offsetX, int offsetY, double scale)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                float nx = (i + offsetX) * (float) scale / width;
                float ny = (j + offsetY) * (float) scale / height;
                
                // Typical values to start playing with:
                //   lacunarity = ~2.0   -- spacing between successive octaves (use exactly 2.0 for wrapping output)
                //   gain       =  0.5   -- relative weighting applied to each successive octave
                //   octaves    =  6     -- number of "octaves" of noise3() to sum
                
                // NOTE: We need to translate the data from [-1..1] to [0..1]
                float p = (stb_perlin_fbm_noise3(nx, ny, 1.0F, 2.0F, 0.5F, 6) + 1.0F) / 2.0F;
                
                pixels.put(j * width + i, (int) (p * 255), 255);
            }
        }
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    // TODO - Use my noise
    public static @NotNull Image genNoiseCellular(int width, int height, int tileSize)
    {
        Color.Buffer pixels = Color.malloc(ColorFormat.RGBA, width * height);
        
        Random random = new Random();
        
        int seedsPerRow = width / tileSize;
        int seedsPerCol = height / tileSize;
        int seedsCount  = seedsPerRow * seedsPerCol;
        
        int[] seeds = new int[seedsCount << 1];
        
        for (int i = 0; i < seedsCount; i++)
        {
            seeds[2 * i]     = i % seedsPerRow * tileSize + random.nextInt(tileSize);
            seeds[2 * i + 1] = i / seedsPerRow * tileSize + random.nextInt(tileSize);
        }
        
        for (int j = 0; j < height; j++)
        {
            int tileY = j / tileSize;
            
            for (int i = 0; i < width; i++)
            {
                int tileX = i / tileSize;
                
                double minDistance = Double.MAX_VALUE;
                
                // Check all adjacent tiles
                for (int x = -1; x < 2; x++)
                {
                    if (tileX + x < 0 || tileX + x >= seedsPerRow) continue;
                    
                    for (int y = -1; y < 2; y++)
                    {
                        if (tileY + y < 0 || tileY + y >= seedsPerCol) continue;
                        
                        int index = (tileY + y) * seedsPerRow + tileX + x;
                        
                        int seedX = seeds[2 * index];
                        int seedY = seeds[2 * index + 1];
                        
                        double dist = Math.hypot(i - seedX, j - seedY);
                        
                        minDistance = Math.min(minDistance, dist);
                    }
                }
                
                // I made this up but it seems to give good results at all tile sizes
                int intensity = (int) Math.round(minDistance * 256 / tileSize);
                
                pixels.put(j * width + i, Math.min(intensity, 255), 255);
            }
        }
        
        return new Image(pixels, width, height, 1, ColorFormat.RGBA);
    }
    
    // TODO
    // public static @NotNull Image genText(@NotNull String text, int fontSize, Colorc color)
    // {
    //     int defaultFontSize = 10;   // Default Font chars height in pixel
    //     if (fontSize < defaultFontSize) fontSize = defaultFontSize;
    //     int spacing = fontSize / defaultFontSize;
    //
    //     return genTextEx(getFontDefault(), text, fontSize, spacing, color);
    // }
    //
    // public static @NotNull Image genTextEx(@NotNull Font font, @NotNull String text, double fontSize, double spacing, Colorc tint)
    // {
    //     int length = text.length();
    //
    //     int textOffsetX = 0;            // Image drawing position X
    //     int textOffsetY = 0;            // Offset between lines (on line break '\n')
    //
    //     // NOTE: Text image is generated at font base size, later scaled to desired font size
    //     Vector2 imSize = MeasureTextEx(font, text, (float) font.baseSize, spacing);
    //
    //     // Create image to store _text
    //     Image imText = GenImageColor((int) imSize.x, (int) imSize.y, BLANK);
    //
    //     for (int i = 0; i < length; i++)
    //     {
    //         // Get next codepoint from byte string and glyph index in font
    //         int[] codepointByteCount = {0};
    //         int   codepoint          = GetNextCodepoint("" + text.charAt(i), codepointByteCount);
    //         int   index              = GetGlyphIndex(font, codepoint);
    //
    //         // NOTE: Normally we exit the decoding sequence as soon as a bad byte is found (and return 0x3f)
    //         // but we need to draw all of the bad bytes using the '?' symbol moving one byte
    //         if (codepoint == 0x3f) codepointByteCount[0] = 1;
    //
    //         if (codepoint == '\n')
    //         {
    //             // NOTE: Fixed line spacing of 1.5 line-height
    //             // TODO: Support custom line spacing defined by user
    //             textOffsetY += (font.baseSize + font.baseSize / 2);
    //             textOffsetX = 0;
    //         }
    //         else
    //         {
    //             if ((codepoint != ' ') && (codepoint != '\t'))
    //             {
    //                 Rectangle rec = new Rectangle((float) (textOffsetX + font.chars[index].offsetX), (float) (textOffsetY + font.chars[index].offsetY), font.recs[index].width, font.recs[index].height);
    //                 ImageDraw(imText, font.chars[index].image, new Rectangle(0, 0, (float) font.chars[index].image.width, (float) font.chars[index].image.height), rec, tint);
    //             }
    //
    //             if (font.chars[index].advanceX == 0) { textOffsetX += (int) (font.recs[index].width + spacing); }
    //             else { textOffsetX += font.chars[index].advanceX + (int) spacing; }
    //         }
    //
    //         i += (codepointByteCount[0] - 1);   // Move _text bytes counter to next codepoint
    //     }
    //
    //     // Scale image depending on _text size
    //     if (fontSize > imSize.y)
    //     {
    //         float scaleFactor = fontSize / imSize.y;
    //         TRACELOG(LOG_INFO, "IMAGE: Text scaled by factor: %f", scaleFactor);
    //
    //         // Using nearest-neighbor scaling algorithm for default font
    //         if (font.texture.id == GetFontDefault().texture.id)
    //         {
    //             ImageResizeNN(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
    //         }
    //         else
    //         {
    //             ImageResize(imText, (int) (imSize.x * scaleFactor), (int) (imSize.y * scaleFactor));
    //         }
    //     }
    //
    //     return imText;
    // }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    private @Nullable Color.Buffer data;
    
    private int width;
    private int height;
    
    private int mipmaps;
    
    private ColorFormat format;
    
    private Image(@Nullable Color.Buffer data, int width, int height, int mipmaps, @NotNull ColorFormat format)
    {
        this.data = data;
        
        this.width  = width;
        this.height = height;
        
        this.mipmaps = mipmaps;
        
        this.format = format;
    }
    
    @Override
    public @NotNull String toString()
    {
        return "Image{" + "width=" + this.width + ", height=" + this.height + ", mipmaps=" + this.mipmaps + ", format=" + this.format + '}';
    }
    
    public @Nullable Color.Buffer data()
    {
        return this.data;
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
     * Unloads the data in this image.
     */
    public void delete()
    {
        if (this.data != null)
        {
            this.data.free();
            this.data = null;
        }
        
        this.width  = 0;
        this.height = 0;
        
        this.mipmaps = 1;
    }
    
    /**
     * Exports this image to file. If specified, then the mipmaps will be
     * exported to separate files if the image has them.
     *
     * @param fileName    The path to the file.
     * @param withMipmaps If mipmaps should be exported
     * @return If everything was a success.
     */
    public boolean export(@NotNull String fileName, boolean withMipmaps)
    {
        if (this.data == null) return false;
        
        String extension = getExtension(fileName);
        
        ByteBuffer buffer = this.data.toBuffer();
        buffer.limit(this.width * this.height * this.format.sizeof);
        
        boolean success = export(extension, fileName, this.width, this.height, this.format.sizeof, this.mipmaps, this.format, buffer);
        
        if (withMipmaps)
        {
            int nextMip = this.width * this.height * this.format.sizeof;
            
            int mipWidth  = this.width >> 1;
            int mipHeight = this.height >> 1;
            int mipSize   = mipWidth * mipHeight * this.format.sizeof;
            
            String mipFileBase = fileName.substring(0, fileName.length() - ".png".length());
            
            for (int i = 1; i < this.mipmaps; i++)
            {
                String mipFileName = mipFileBase + "_" + i + extension;
                buffer.position(nextMip);
                buffer.limit(nextMip + mipSize);
                
                boolean mipSuccess = export(extension, mipFileName, mipWidth, mipHeight, this.format.sizeof, 1, this.format, buffer);
                
                success = mipSuccess && success;
                nextMip += mipSize;
                
                if ((mipWidth >>= 1) < 1) mipWidth = 1;
                if ((mipHeight >>= 1) < 1) mipHeight = 1;
                
                mipSize = mipWidth * mipHeight * this.format.sizeof;
            }
        }
        
        return success;
    }
    
    /**
     * Exports this image to file.
     *
     * @param fileName The path to the file.
     * @return If everything was a success.
     */
    public boolean export(@NotNull String fileName)
    {
        return export(fileName, false);
    }
    
    private static boolean export(String extension, String fileName, int width, int height, int channels, int mipmaps, ColorFormat format, ByteBuffer data)
    {
        boolean success = false;
        switch (extension)
        {
            case ".png" -> success = stbi_write_png(fileName, width, height, channels, data, width * channels);
            case ".bmp" -> success = stbi_write_bmp(fileName, width, height, channels, data);
            case ".tga" -> success = stbi_write_tga(fileName, width, height, channels, data);
            case ".jpg" -> success = stbi_write_jpg(fileName, width, height, channels, data, 90); // JPG quality: between 1 and 100
            // case ".ktx" -> SaveKTX(image, fileName);
            case ".raw" -> success = writeToFile(fileName, data);
            case ".engine_img" -> {
                ByteBuffer newBuffer = MemoryUtil.memAlloc(data.remaining() + Integer.BYTES * 4);
                newBuffer.putInt(width);
                newBuffer.putInt(height);
                newBuffer.putInt(mipmaps);
                newBuffer.putInt(format.ordinal());
                newBuffer.put(data);
                newBuffer.flip();
                
                success = writeToFile(fileName, newBuffer);
                
                MemoryUtil.memFree(newBuffer);
            }
        }
        if (success)
        {
            Image.LOGGER.info("Image exported successfully:", fileName);
        }
        else
        {
            Image.LOGGER.warning("Failed to export image:", fileName);
        }
        return success;
    }
    
    //---------------------------
    // ----- Copy Functions -----
    //---------------------------
    
    /**
     * @return A copy of this {@link Image}
     */
    public Image copy()
    {
        Color.Buffer data = this.data != null ? this.data.copy(this.format) : null;
        return new Image(data, this.width, this.height, this.mipmaps, this.format);
    }
    
    /**
     * Creates a copy of a region of this image.
     *
     * @param x      The top left x coordinate of the sub region
     * @param y      The top left y coordinate of the sub region
     * @param width  The width of the sub region
     * @param height The height of the sub region
     * @return The new {@link Image}
     * @throws IllegalArgumentException if rectangle exceeds image bounds
     */
    public Image subImage(int x, int y, int width, int height)
    {
        validateRect(x, y, width, height);
        
        Color.Buffer newData = null;
        
        if (this.data != null)
        {
            newData = Color.malloc(this.format, width * height);
            
            long srcPtr = this.data.address0();
            long dstPtr = newData.address0();
            
            long bytesPerLine = Integer.toUnsignedLong(width * this.format.sizeof);
            for (int j = 0; j < height; j++)
            {
                int srcIdx = (j + y) * this.width + x;
                int dstIdx = j * width;
                
                long src = srcPtr + Integer.toUnsignedLong(srcIdx * this.format.sizeof);
                long dst = dstPtr + Integer.toUnsignedLong(dstIdx * this.format.sizeof);
                
                MemoryUtil.memCopy(src, dst, bytesPerLine);
            }
        }
        
        return new Image(newData, width, height, 1, this.format);
    }
    
    //------------------------------
    // ----- Utility Functions -----
    //------------------------------
    
    /**
     * Convert image data to desired format
     * <p>
     * Mipmap data will be lost.
     *
     * @param format The format to change the image to
     * @return this
     */
    public @NotNull Image reformat(ColorFormat format)
    {
        if (format != ColorFormat.GRAY &&
            format != ColorFormat.GRAY_ALPHA &&
            format != ColorFormat.RGB &&
            format != ColorFormat.RGBA) {throw new UnsupportedOperationException("invalid format: " + format);}
        
        if (this.data != null && this.format != format)
        {
            Color.Buffer output = this.data.copy(format);
            
            this.data.free();
            
            this.data    = output;
            this.format  = format;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Generate all mipmap levels for a provided image
     * <p>
     * NOTE 1: Supports POT and NPOT images
     * NOTE 2: image.data is scaled to include mipmap levels
     * NOTE 3: Mipmaps format is the same as base image
     *
     * @return this
     */
    public @NotNull Image genMipmaps()
    {
        if (this.data != null)
        {
            // TODO - LWJGL-ify
            int mipCount  = 1;           // Required mipmap levels count (including base level)
            int mipWidth  = this.width;  // Base image width
            int mipHeight = this.height; // Base image height
            
            int mipSize = mipWidth * mipHeight * this.format.sizeof; // Image data size (in bytes)
            
            // Count mipmap levels required
            while (mipWidth != 1 || mipHeight != 1)
            {
                if (mipWidth != 1) mipWidth >>= 1;
                if (mipHeight != 1) mipHeight >>= 1;
                
                // Security check for NPOT textures
                if (mipWidth < 1) mipWidth = 1;
                if (mipHeight < 1) mipHeight = 1;
                
                Image.LOGGER.fine("Next mipmap level: %s x %s - current size %s", mipWidth, mipHeight, mipSize);
                
                mipCount++;
                mipSize += mipWidth * mipHeight * this.format.sizeof; // Add mipmap size (in bytes)
            }
            
            if (this.mipmaps < mipCount)
            {
                Color.Buffer temp = Color.realloc(this.data, mipSize);
                
                if (temp.address() != 0)
                {
                    this.data = temp; // Assign new pointer (new size) to store mipmaps data
                }
                else
                {
                    Image.LOGGER.warning("IMAGE: Mipmaps required memory could not be allocated");
                }
                
                // Pointer to allocated memory point where store next mipmap level data
                long nextMip = this.data.address() + (long) this.width * this.height * this.format.sizeof;
                
                mipWidth  = this.width >> 1;
                mipHeight = this.height >> 1;
                mipSize   = mipWidth * mipHeight * this.format.sizeof;
                Image imCopy = copy();
                
                for (int i = 1; i < mipCount; i++)
                {
                    Image.LOGGER.fine("IMAGE: Generating mipmap level: %s (%s x %s) - size: %s - offset: 0x%x", i, mipWidth, mipHeight, mipSize, nextMip);
                    
                    imCopy.resize(mipWidth, mipHeight); // Uses internally Mitchell cubic downscale filter
                    
                    if (imCopy.data != null) MemoryUtil.memCopy(imCopy.data.address(), nextMip, mipSize);
                    nextMip += mipSize;
                    this.mipmaps++;
                    
                    // Security check for NPOT textures
                    if ((mipWidth >>= 1) < 1) mipWidth = 1;
                    if ((mipHeight >>= 1) < 1) mipHeight = 1;
                    
                    mipSize = mipWidth * mipHeight * this.format.sizeof;
                }
                
                imCopy.delete();
            }
            else
            {
                Image.LOGGER.warning("Mipmaps already available");
            }
        }
        return this;
    }
    
    /**
     * Gets the image data formatted as {@link ColorFormat#RGBA RGBA}
     *
     * @return The color data.
     */
    public @Nullable Color.Buffer getColorData()
    {
        return this.data != null ? this.data.copy(ColorFormat.RGBA) : null;
    }
    
    /**
     * Generates a {@link Color.Buffer} filled with one of every color in the image.
     *
     * @param maxPaletteSize The maximum size of the buffer.
     * @return The pallet data.
     */
    public @NotNull Color.Buffer getPalette(int maxPaletteSize)
    {
        int count = 0;
        
        Color.Buffer palette = Color.calloc(ColorFormat.RGBA, maxPaletteSize);
        Color.Buffer pixels  = Objects.requireNonNull(getColorData());
        for (int i = 0, n = this.width * this.height; i < n; i++)
        {
            Color pixel = pixels.get(i);
            
            if (pixel.a() != 0)
            {
                // Check if the color is already on palette
                boolean present = false;
                for (int j = 0; j < count; j++)
                {
                    if (pixel.equals(palette.get(j)))
                    {
                        present = true;
                        break;
                    }
                }
                
                // Store color if not on the palette
                if (!present)
                {
                    // Add pixels[i] to palette
                    palette.get(count).set(pixel);
                    count++;
                    
                    // We reached the limit of colors supported by palette
                    if (count >= maxPaletteSize)
                    {
                        Image.LOGGER.warning("Palette is greater than %s colors", maxPaletteSize);
                        break;
                    }
                }
            }
        }
        pixels.free();
        return palette.limit(count);
    }
    
    private void validateRect(int x, int y, int width, int height)
    {
        if (x < 0) throw new IllegalArgumentException("subregion x exceeds image bounds");
        if (y < 0) throw new IllegalArgumentException("subregion y exceeds image bounds");
        if (x + width - 1 >= this.width) throw new IllegalArgumentException("subregion width exceeds image bounds");
        if (y + height - 1 >= this.height) throw new IllegalArgumentException("subregion height exceeds image bounds");
    }
    
    //--------------------------------------------
    // ----- Instance Manipulation Functions -----
    //--------------------------------------------
    
    /**
     * Resizes the image to a new size.
     * <p>
     * NOTE: Uses stb default scaling filters (both bicubic):
     * STBIR_DEFAULT_FILTER_UPSAMPLE    STBIR_FILTER_CATMULLROM
     * STBIR_DEFAULT_FILTER_DOWNSAMPLE  STBIR_FILTER_MITCHELL   (high-quality Catmull-Rom)
     * <p>
     * Mipmap data will be lost.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param width  The new width
     * @param height The new height
     * @return this
     */
    public @NotNull Image resize(int width, int height)
    {
        if (this.data != null)
        {
            Color.Buffer output = Color.malloc(this.format, width * height);
            
            int channels = switch (this.format)
                    {
                        case GRAY_ALPHA -> 2;
                        case RGB -> 3;
                        case RGBA -> 4;
                        default -> 1;
                    };
            stbir_resize_uint8(this.data.toBuffer(), this.width, this.height, 0, output.toBuffer(), width, height, 0, channels);
            
            this.data.free();
            this.data    = output;
            this.width   = width;
            this.height  = height;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Resizes the image to a new size.
     * <p>
     * NOTE: Uses Nearest-Neighbor scaling algorithm
     * <p>
     * Mipmap data will be lost.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param width  The new width
     * @param height The new height
     * @return this
     */
    public @NotNull Image resizeNN(int width, int height)
    {
        if (this.data != null)
        {
            Color.Buffer pixels = Objects.requireNonNull(getColorData());
            Color.Buffer output = Color.malloc(ColorFormat.RGBA, width * height);
            
            // EDIT: added +1 to account for an early rounding problem
            int xRatio = (this.width << 16) / width + 1;
            int yRatio = (this.height << 16) / height + 1;
            
            long srcPtr = pixels.address();
            long dstPtr = output.address();
            
            int sizeof = ColorFormat.RGBA.sizeof;
            for (int y = 0; y < height; y++)
            {
                for (int x = 0; x < width; x++)
                {
                    int x2 = x * xRatio >> 16;
                    int y2 = y * yRatio >> 16;
                    
                    long src = Integer.toUnsignedLong(y2 * this.width + x2) * sizeof;
                    long dst = Integer.toUnsignedLong(y * width + x) * sizeof;
                    
                    MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, sizeof);
                }
            }
            
            this.data.free();
            pixels.free();
            
            this.data    = output;
            this.width   = width;
            this.height  = height;
            this.format  = ColorFormat.RGBA;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Resizes the image to a new size and fills empty space with color.
     * <p>
     * Mipmap data will be lost.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param x      The offset x coordinate of the region
     * @param y      The offset y coordinate of the region
     * @param width  The width of the region
     * @param height The height of the region
     * @param fill   The color to fill the empty space with.
     * @return this
     */
    public @NotNull Image resizeCanvas(int x, int y, int width, int height, @NotNull Colorc fill)
    {
        if (this.data != null)
        {
            int intersectX = Math.max(0, x);
            int intersectY = Math.max(0, y);
            int intersectW = Math.min(this.width - 1, x + width - 1);
            int intersectH = Math.min(this.height - 1, y + height - 1);
            
            if (intersectW <= intersectX || intersectH <= intersectY)
            {
                Image.LOGGER.warning("Resized image does not overlap existing image.");
                return this;
            }
            intersectW -= intersectX;
            intersectH -= intersectY;
            
            Color.Buffer pixels = Objects.requireNonNull(getColorData());
            Color.Buffer output = Color.malloc(ColorFormat.RGBA, width * height);
            output.fill(fill);
            
            long srcPtr = pixels.address();
            long dstPtr = output.address();
            
            //noinspection UnnecessaryLocalVariable
            int srcX = intersectX, srcY = intersectY;
            
            int dstX = Math.max(0, intersectX - x);
            int dstY = Math.max(0, intersectY - y);
            
            int  sizeof  = ColorFormat.RGBA.sizeof;
            long rowSize = Integer.toUnsignedLong(intersectW) * sizeof;
            for (int j = 0; j < intersectH; j++)
            {
                long src = Integer.toUnsignedLong((srcY + j) * this.width + srcX) * sizeof;
                long dst = Integer.toUnsignedLong((dstY + j) * width + dstX) * sizeof;
                
                MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, rowSize);
            }
            
            pixels.free();
            this.data.free();
            
            this.data    = output;
            this.width   = width;
            this.height  = height;
            this.format  = ColorFormat.RGBA;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Crop an image to area defined by a rectangle
     * <p>
     * Mipmap data will be lost.
     *
     * @param x      The offset x coordinate of the sub region
     * @param y      The offset y coordinate of the sub region
     * @param width  The width of the sub region
     * @param height The height of the sub region
     * @return this
     */
    public @NotNull Image crop(int x, int y, int width, int height)
    {
        if (this.data != null)
        {
            validateRect(x, y, width, height);
            
            Color.Buffer output = Color.malloc(this.format, width * height);
            
            long srcPtr = this.data.address();
            long dstPtr = output.address();
            
            long bytesPerLine = Integer.toUnsignedLong(width) * this.format.sizeof;
            for (int j = 0; j < height; j++)
            {
                long src = srcPtr + Integer.toUnsignedLong((j + y) * this.width + x) * this.format.sizeof;
                long dst = dstPtr + Integer.toUnsignedLong(j * width) * this.format.sizeof;
                MemoryUtil.memCopy(src, dst, bytesPerLine);
            }
            
            this.data.free();
            
            this.data    = output;
            this.width   = width;
            this.height  = height;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Convert image to POT (power-of-two)
     *
     * @param fill The color to fill the empty space with
     * @return this
     */
    public @NotNull Image toPOT(@NotNull Colorc fill)
    {
        // Calculate next power-of-two values
        // NOTE: Just add the required amount of pixels at the right and bottom sides of image...
        int potWidth  = (int) Math.pow(2, Math.ceil(Math.log(this.width) / Math.log(2)));
        int potHeight = (int) Math.pow(2, Math.ceil(Math.log(this.height) / Math.log(2)));
        
        // Check if POT texture generation is required (if texture is not already POT)
        return resizeCanvas(0, 0, potWidth, potHeight, fill);
    }
    
    /**
     * Quantize the image by decreasing the bits per pixels to the values
     * specified.
     *
     * @param rBpp The number of bits in the r channel [0-8]
     * @param gBpp The number of bits in the g channel [0-8]
     * @param bBpp The number of bits in the b channel [0-8]
     * @param aBpp The number of bits in the a channel [0-8]
     * @return This
     */
    public @NotNull Image quantize(int rBpp, int gBpp, int bBpp, int aBpp)
    {
        if (this.data != null)
        {
            Color.Buffer pixels = Objects.requireNonNull(getColorData());
            
            this.data.free(); // free old image data
            
            this.format  = ColorFormat.RGBA;
            this.data    = Color.malloc(this.format, this.width * this.height);
            this.mipmaps = 1;
            
            Color color;
            
            int r, g, b, a;
            
            int rs = (1 << rBpp) - 1;
            int gs = (1 << gBpp) - 1;
            int bs = (1 << bBpp) - 1;
            int as = (1 << aBpp) - 1;
            
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    color = pixels.get(y * this.width + x);
                    
                    // This "CAN" be simplified, but we need the precision to be lost.
                    r = rs > 0 ? ((((color.r() + (127 / rs)) * rs) / 255) * 255) / rs : 0;
                    g = gs > 0 ? ((((color.g() + (127 / gs)) * gs) / 255) * 255) / gs : 0;
                    b = bs > 0 ? ((((color.b() + (127 / bs)) * bs) / 255) * 255) / bs : 0;
                    a = as > 0 ? ((((color.a() + (127 / as)) * as) / 255) * 255) / as : 255;
                    
                    this.data.put(y * this.width + x, r, g, b, a);
                }
            }
            
            pixels.free();
        }
        return this;
    }
    
    /**
     * Quantize the image to only 256 colors.
     *
     * @param sampleFactor Sampling Factor {@code [1..30]}
     * @return This
     */
    public @NotNull Image neuQuantize(int sampleFactor)
    {
        if (this.data != null)
        {
            int sizeof = this.data.sizeof();
            
            final int NET_SIZE = 256;
            final int
                    PRIME1 = 499,
                    PRIME2 = 491,
                    PRIME3 = 487,
                    PRIME4 = 503;
            final int MIN_PICTURE_BYTES = sizeof * PRIME4;
            final int
                    MAX_NET_POS = NET_SIZE - 1,
                    NET_BIAS_SHIFT = 4,
                    N_CYCLES = 100;
            @SuppressWarnings("unused")
            final int
                    INT_BIAS_SHIFT = 16,
                    INT_BIAS = 1 << INT_BIAS_SHIFT,
                    GAMMA_SHIFT = 10,
                    GAMMA = 1 << GAMMA_SHIFT,
                    BETA_SHIFT = 10,
                    BETA = INT_BIAS >> BETA_SHIFT,
                    BETA_GAMMA = INT_BIAS << (GAMMA_SHIFT - BETA_SHIFT);
            final int
                    INIT_RAD = NET_SIZE >> 3,
                    RADIUS_BIAS_SHIFT = 6,
                    RADIUS_BIAS = 1 << RADIUS_BIAS_SHIFT,
                    INIT_RADIUS = INIT_RAD * RADIUS_BIAS,
                    RADIUS_DEC = 30;
            final int
                    ALPHA_BIAS_SHIFT = 10,
                    INIT_ALPHA = 1 << ALPHA_BIAS_SHIFT;
            final int
                    RAD_BIAS_SHIFT = 8,
                    RAD_BIAS = 1 << RAD_BIAS_SHIFT,
                    ALPHA_RAD_BIAS_SHIFT = ALPHA_BIAS_SHIFT + RAD_BIAS_SHIFT,
                    ALPHA_RAD_BIAS = 1 << ALPHA_RAD_BIAS_SHIFT;
            
            int dataSize = this.data.capacity() * sizeof;
            
            sampleFactor = dataSize < MIN_PICTURE_BYTES ? 1 : sampleFactor;
            
            int[][] network  = new int[NET_SIZE][];
            int[]   netIndex = new int[256];
            int[]   bias     = new int[NET_SIZE];
            int[]   freq     = new int[NET_SIZE];
            int[]   radPower = new int[INIT_RAD];
            
            int alphaDec = 30 + ((sampleFactor - 1) / sizeof);
            
            for (int i = 0; i < NET_SIZE; i++)
            {
                int initial = (i << (NET_BIAS_SHIFT + 8)) / NET_SIZE;
                
                network[i] = new int[] {initial, initial, initial, initial, 0};
            }
            Arrays.fill(netIndex, 0);
            Arrays.fill(bias, 0);
            Arrays.fill(freq, INT_BIAS / NET_SIZE); // 1/NET_SIZE
            Arrays.fill(radPower, 0);
            
            int samplePixels = dataSize / (sizeof * sampleFactor);
            int alpha        = INIT_ALPHA;
            int radius       = INIT_RADIUS;
            int rad          = radius >> RADIUS_BIAS_SHIFT;
            
            // if (rad <= 1) rad = 0;
            
            for (int i = 0, rad2 = rad * rad; i < rad; i++) radPower[i] = alpha * (((rad2 - i * i) * RAD_BIAS) / rad2);
            
            int step;
            if (dataSize < MIN_PICTURE_BYTES)
            {
                step = 1;
            }
            else if ((dataSize % PRIME1) != 0)
            {
                step = PRIME1;
            }
            else if ((dataSize % PRIME2) != 0)
            {
                step = PRIME2;
            }
            else if ((dataSize % PRIME3) != 0)
            {
                step = PRIME3;
            }
            else
            {
                step = PRIME4;
            }
            
            int delta = samplePixels / N_CYCLES;
            if (delta == 0) delta = 1;
            for (int i = 0, pix = 0; i < samplePixels; )
            {
                Color pixel = this.data.get(pix);
                
                int r = pixel.r() << NET_BIAS_SHIFT;
                int g = pixel.g() << NET_BIAS_SHIFT;
                int b = pixel.b() << NET_BIAS_SHIFT;
                int a = pixel.a() << NET_BIAS_SHIFT;
                
                int bestDist     = Integer.MAX_VALUE;
                int bestBiasDist = Integer.MAX_VALUE;
                int bestPos      = -1;
                int bestBiasPos  = -1;
                
                for (int j = 0; j < NET_SIZE; j++)
                {
                    int[] n = network[j];
                    
                    int dist = Math.abs(n[0] - r) +
                               Math.abs(n[1] - g) +
                               Math.abs(n[2] - b) +
                               Math.abs(n[3] - a);
                    
                    if (dist < bestDist)
                    {
                        bestDist = dist;
                        bestPos  = j;
                    }
                    
                    int biasDist = dist - (bias[j] >> (INT_BIAS_SHIFT - NET_BIAS_SHIFT));
                    if (biasDist < bestBiasDist)
                    {
                        bestBiasDist = biasDist;
                        bestBiasPos  = j;
                    }
                    
                    int betaFreq = freq[j] >> BETA_SHIFT;
                    bias[j] += betaFreq << GAMMA_SHIFT;
                    freq[j] -= betaFreq;
                }
                bias[bestPos] -= BETA_GAMMA;
                freq[bestPos] += BETA;
                
                // alter hit neuron
                int[] n = network[bestBiasPos];
                n[0] -= (alpha * (n[0] - r)) / INIT_ALPHA;
                n[1] -= (alpha * (n[1] - g)) / INIT_ALPHA;
                n[2] -= (alpha * (n[2] - b)) / INIT_ALPHA;
                n[3] -= (alpha * (n[3] - a)) / INIT_ALPHA;
                if (rad != 0)
                {
                    // alter neighbours
                    int lo = Math.max(bestBiasPos - rad, -1);
                    int hi = Math.min(bestBiasPos + rad, NET_SIZE);
                    
                    int j = bestBiasPos + 1;
                    int k = bestBiasPos - 1;
                    int m = 1;
                    while (j < hi || k > lo)
                    {
                        int radP = radPower[m++];
                        if (j < hi)
                        {
                            int[] p = network[j++];
                            try
                            {
                                p[0] -= (radP * (p[0] - r)) / ALPHA_RAD_BIAS;
                                p[1] -= (radP * (p[1] - g)) / ALPHA_RAD_BIAS;
                                p[2] -= (radP * (p[2] - b)) / ALPHA_RAD_BIAS;
                                p[3] -= (radP * (p[3] - a)) / ALPHA_RAD_BIAS;
                            }
                            catch (Exception ignored) {}
                        }
                        if (k > lo)
                        {
                            int[] p = network[k--];
                            try
                            {
                                p[0] -= (radP * (p[0] - r)) / ALPHA_RAD_BIAS;
                                p[1] -= (radP * (p[1] - g)) / ALPHA_RAD_BIAS;
                                p[2] -= (radP * (p[2] - b)) / ALPHA_RAD_BIAS;
                                p[3] -= (radP * (p[3] - a)) / ALPHA_RAD_BIAS;
                            }
                            catch (Exception ignored) {}
                        }
                    }
                }
                
                pix += step;
                if (pix >= this.data.capacity()) pix -= this.data.capacity();
                
                i++;
                
                if (i % delta == 0)
                {
                    alpha -= alpha / alphaDec;
                    radius -= radius / RADIUS_DEC;
                    rad = radius >> RADIUS_BIAS_SHIFT;
                    if (rad <= 1) rad = 0;
                    for (bestBiasPos = 0; bestBiasPos < rad; bestBiasPos++)
                    {
                        radPower[bestBiasPos] = alpha * (((rad * rad - bestBiasPos * bestBiasPos) * RAD_BIAS) / (rad * rad));
                    }
                }
            }
            
            // Unbias network to give byte values 0..255 and record position i to prepare for sort
            for (int i = 0; i < NET_SIZE; i++)
            {
                network[i][0] >>= NET_BIAS_SHIFT;
                network[i][1] >>= NET_BIAS_SHIFT;
                network[i][2] >>= NET_BIAS_SHIFT;
                network[i][3] >>= NET_BIAS_SHIFT;
                network[i][4] =   i; /* record color no */
            }
            
            int prevCol  = 0;
            int startPos = 0;
            for (int i = 0; i < NET_SIZE; i++)
            {
                int[] p        = network[i];
                int   smallPos = i;
                int   smallVal = p[1]; /* index on g */
                /* find smallest in [i..NET_SIZE-1] */
                for (int j = i + 1; j < NET_SIZE; j++)
                {
                    int[] q = network[j];
                    if (q[1] < smallVal)
                    { /* index on g */
                        smallPos = j;
                        smallVal = q[1]; /* index on g */
                    }
                }
                int[] q = network[smallPos];
                /* swap p(i) and q(smallPos) entries */
                if (i != smallPos)
                {
                    int temp;
                    
                    temp = q[0];
                    q[0] = p[0];
                    p[0] = temp;
                    temp = q[1];
                    q[1] = p[1];
                    p[1] = temp;
                    temp = q[2];
                    q[2] = p[2];
                    p[2] = temp;
                    temp = q[3];
                    q[3] = p[3];
                    p[3] = temp;
                }
                /* smallVal entry is now in position i */
                if (smallVal != prevCol)
                {
                    netIndex[prevCol] = (startPos + i) >> 1;
                    for (int j = prevCol + 1; j < smallVal; j++) netIndex[j] = i;
                    prevCol  = smallVal;
                    startPos = i;
                }
            }
            
            netIndex[prevCol] = (startPos + MAX_NET_POS) >> 1;
            Arrays.fill(netIndex, prevCol + 1, netIndex.length, MAX_NET_POS); // really 256
            
            int[] index = new int[NET_SIZE];
            for (int i = 0; i < NET_SIZE; i++) index[network[i][4]] = i;
            
            byte[] colorTable = new byte[sizeof * NET_SIZE];
            for (int i = 0, k = 0; i < NET_SIZE; i++)
            {
                int j = index[i];
                colorTable[k++] = (byte) network[j][0];
                colorTable[k++] = (byte) network[j][1];
                colorTable[k++] = (byte) network[j][2];
                colorTable[k++] = (byte) network[j][3];
            }
            
            this.data.clear();
            for (Color color = this.data.get(); this.data.hasRemaining(); color = this.data.get())
            {
                int r = color.r();
                int g = color.g();
                int b = color.b();
                int a = color.a();
                
                int bestDist = 1000; /* biggest possible dist is 256*3 */
                int best     = -1;
                
                // i: index on g
                // j: start at netIndex[g] and work outwards
                for (int i = netIndex[g], j = i - 1; i < NET_SIZE || j >= 0; )
                {
                    if (i < NET_SIZE)
                    {
                        int[] p    = network[i];
                        int   dist = p[1] - g; /* inx key */
                        if (dist >= bestDist)
                        {
                            i = NET_SIZE; /* stop iter */
                        }
                        else
                        {
                            i++;
                            dist = Math.abs(dist) + Math.abs(p[0] - r);
                            if (dist < bestDist)
                            {
                                dist += Math.abs(p[2] - b);
                                if (dist < bestDist)
                                {
                                    dist += Math.abs(p[3] - a);
                                    if (dist < bestDist)
                                    {
                                        bestDist = dist;
                                        best     = p[4];
                                    }
                                }
                            }
                        }
                    }
                    if (j >= 0)
                    {
                        int[] p    = network[j];
                        int   dist = g - p[1]; /* inx key - reverse dif */
                        if (dist >= bestDist)
                        {
                            j = -1; /* stop iter */
                        }
                        else
                        {
                            j--;
                            dist = Math.abs(dist) + Math.abs(p[0] - r);
                            if (dist < bestDist)
                            {
                                dist += Math.abs(p[2] - b);
                                if (dist < bestDist)
                                {
                                    dist += Math.abs(p[3] - a);
                                    if (dist < bestDist)
                                    {
                                        bestDist = dist;
                                        best     = p[4];
                                    }
                                }
                            }
                        }
                    }
                }
                
                color.r(colorTable[(4 * best)] & 0xFF);
                color.g(colorTable[(4 * best) + 1] & 0xFF);
                color.b(colorTable[(4 * best) + 2] & 0xFF);
                color.a(colorTable[(4 * best) + 3] & 0xFF);
            }
            this.data.clear();
        }
        return this;
    }
    
    /**
     * Dither image data (Floyd-Steinberg dithering)
     * <p>
     * Mipmap data will be lost.
     *
     * @param rBpp The number of bits in the r channel
     * @param gBpp The number of bits in the g channel
     * @param bBpp The number of bits in the b channel
     * @param aBpp The number of bits in the a channel
     * @return This
     */
    public @NotNull Image dither(int rBpp, int gBpp, int bBpp, int aBpp)
    {
        if (this.data != null)
        {
            Color.Buffer pixels = Objects.requireNonNull(getColorData());
            
            this.data.free(); // free old image data
            
            this.format  = ColorFormat.RGBA;
            this.data    = Color.malloc(this.format, this.width * this.height);
            this.mipmaps = 1;
            
            Color color;
            
            int r, g, b, a;
            int rErr, gErr, bErr, aErr;
            int rNew, gNew, bNew, aNew;
            
            int rs = (1 << rBpp) - 1;
            int gs = (1 << gBpp) - 1;
            int bs = (1 << bBpp) - 1;
            int as = (1 << aBpp) - 1;
            
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    color = pixels.get(y * this.width + x);
                    
                    r = color.r();
                    g = color.g();
                    b = color.b();
                    a = color.a();
                    
                    // NOTE: New pixel obtained by bits truncate, it would be better to round values (check ImageFormat())
                    rNew = rs > 0 ? ((((r + (127 / rs)) * rs) / 255) * 255) / rs : 0;
                    gNew = gs > 0 ? ((((g + (127 / gs)) * gs) / 255) * 255) / gs : 0;
                    bNew = bs > 0 ? ((((b + (127 / bs)) * bs) / 255) * 255) / bs : 0;
                    aNew = as > 0 ? ((((a + (127 / as)) * as) / 255) * 255) / as : 255;
                    
                    // NOTE: Error must be computed between new and old pixel but using same number of bits!
                    // We want to know how much color precision we have lost...
                    rErr = r - rNew;
                    gErr = g - gNew;
                    bErr = b - bNew;
                    aErr = a - aNew;
                    
                    this.data.put(y * this.width + x, rNew, gNew, bNew, aNew);
                    
                    // NOTE: Some cases are out of the array and should be ignored
                    if (x < this.width - 1)
                    {
                        color = pixels.get(y * this.width + x + 1);
                        color.set(color.r() + (int) (rErr * 7F / 16F),
                                  color.g() + (int) (gErr * 7F / 16F),
                                  color.b() + (int) (bErr * 7F / 16F),
                                  color.a() + (int) (aErr * 7F / 16F));
                    }
                    
                    if (x > 0 && y < this.height - 1)
                    {
                        color = pixels.get((y + 1) * this.width + x - 1);
                        color.set(color.r() + (int) (rErr * 3F / 16F),
                                  color.g() + (int) (gErr * 3F / 16F),
                                  color.b() + (int) (bErr * 3F / 16F),
                                  color.a() + (int) (aErr * 3F / 16F));
                    }
                    
                    if (y < this.height - 1)
                    {
                        color = pixels.get((y + 1) * this.width + x);
                        color.set(color.r() + (int) (rErr * 5F / 16F),
                                  color.g() + (int) (gErr * 5F / 16F),
                                  color.b() + (int) (bErr * 5F / 16F),
                                  color.a() + (int) (aErr * 5F / 16F));
                    }
                    
                    if (x < this.width - 1 && y < this.height - 1)
                    {
                        color = pixels.get((y + 1) * this.width + x + 1);
                        color.set(color.r() + (int) (rErr * 1F / 16F),
                                  color.g() + (int) (gErr * 1F / 16F),
                                  color.b() + (int) (bErr * 1F / 16F),
                                  color.a() + (int) (aErr * 1F / 16F));
                    }
                }
            }
        }
        return this;
    }
    
    /**
     * Flips this image vertically.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    public @NotNull Image flipV()
    {
        if (this.data != null)
        {
            if (this.mipmaps > 1) Image.LOGGER.warning("Image manipulation only applied to base mipmap level");
            
            Color.Buffer output = Color.malloc(this.format, this.width * this.height);
            
            long srcPtr = this.data.address();
            long dstPtr = output.address();
            
            long bytesPerLine = Integer.toUnsignedLong(this.width) * this.format.sizeof;
            for (int i = this.height - 1, offsetSize = 0; i >= 0; i--)
            {
                long src = srcPtr + Integer.toUnsignedLong(i * this.width) * this.format.sizeof;
                
                MemoryUtil.memCopy(src, dstPtr + offsetSize, bytesPerLine);
                offsetSize += bytesPerLine;
            }
            
            this.data.free();
            this.data    = output;
            this.mipmaps = 1;
        }
        return this;
    }
    
    /**
     * Flips this image horizontally.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    public @NotNull Image flipH()
    {
        if (this.data != null)
        {
            if (this.mipmaps > 1) Image.LOGGER.warning("Image manipulation only applied to base mipmap level");
            
            if (this.format != ColorFormat.RGBA)
            {
                Color.Buffer output = Color.malloc(this.format, this.width * this.height);
                
                long srcPtr = this.data.address();
                long dstPtr = output.address();
                
                long bytesPerLine = Integer.toUnsignedLong(this.width) * this.format.sizeof;
                for (int y = 0; y < this.height; y++)
                {
                    for (int x = 0; x < this.width; x++)
                    {
                        // OPTION 1: Move pixels with memCopy()
                        long src = Integer.toUnsignedLong(y * this.width + this.width - 1 - x) * this.format.sizeof;
                        long dst = Integer.toUnsignedLong(y * this.width + x) * this.format.sizeof;
                        
                        MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, bytesPerLine);
                        
                        // OPTION 2: Just copy data pixel by pixel
                        // output.put(y * this.width + x, this.data.getBytes(y * this.width + (this.width - 1 - x)));
                    }
                }
                
                this.data.free();
                
                this.data = output;
            }
            else
            {
                // OPTION 3: Faster implementation (specific for 32bit pixels)
                // NOTE: It does not require additional allocations
                IntBuffer ptr = this.data.toBuffer().asIntBuffer();
                for (int y = 0; y < this.height; y++)
                {
                    for (int x = 0; x < this.width / 2; x++)
                    {
                        int backup = ptr.get(y * this.width + x);
                        ptr.put(y * this.width + x, ptr.get(y * this.width + this.width - 1 - x));
                        ptr.put(y * this.width + this.width - 1 - x, backup);
                    }
                }
            }
            this.mipmaps = 1;
        }
        
        return this;
    }
    
    /**
     * Rotates this image 90 degrees clockwise.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    public @NotNull Image rotateCW()
    {
        if (this.data != null)
        {
            if (this.mipmaps > 1) Image.LOGGER.warning("Image manipulation only applied to base mipmap level");
            
            Color.Buffer output = Color.malloc(this.format, this.width * this.height);
            
            long srcPtr = this.data.address();
            long dstPtr = output.address();
            
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    long src = Integer.toUnsignedLong(y * this.width + x) * this.format.sizeof;
                    long dst = Integer.toUnsignedLong(x * this.height + this.height - y - 1) * this.format.sizeof;
                    
                    MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, this.format.sizeof);
                }
            }
            
            this.data.free();
            this.data = output;
        }
        return this;
    }
    
    /**
     * Rotates this image 90 degrees counter-clockwise.
     * <p>
     * Mipmap data will be lost.
     *
     * @return this
     */
    public @NotNull Image rotateCCW()
    {
        if (this.data != null)
        {
            if (this.mipmaps > 1) Image.LOGGER.warning("Image manipulation only applied to base mipmap level");
            
            Color.Buffer output = Color.malloc(this.format, this.width * this.height);
            
            long srcPtr = this.data.address();
            long dstPtr = output.address();
            
            for (int y = 0; y < this.height; y++)
            {
                for (int x = 0; x < this.width; x++)
                {
                    long src = Integer.toUnsignedLong(y * this.width + this.width - x - 1) * this.format.sizeof;
                    long dst = Integer.toUnsignedLong(x * this.height + y) * this.format.sizeof;
                    
                    MemoryUtil.memCopy(srcPtr + src, dstPtr + dst, this.format.sizeof);
                }
            }
            
            this.data.free();
            this.data = output;
        }
        return this;
    }
    
    //----------------------------
    // ----- Color Functions -----
    //----------------------------
    
    /**
     * Tints this image using the specified {@link Colorc}.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param color The color to tint by.
     * @return this;
     */
    public @NotNull Image colorTint(@NotNull Colorc color)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.tint(color));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Converts the image to grayscale.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @return this;
     */
    public @NotNull Image colorGrayscale()
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(Color::grayscale);
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Changes the brightness of this image by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param brightness The amount to change the brightness by [{@code -255 - +255}]
     * @return this;
     */
    public @NotNull Image colorBrightness(int brightness)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.brightness(brightness));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Changes the brightness of this image by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param brightness The amount to change the brightness by [{@code -1.0 - +1.0}]
     * @return this;
     */
    public @NotNull Image colorBrightness(double brightness)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.brightness(brightness));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Changes the contrast of this image by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param contrast The amount to change the contrast by [{@code -255 - +255}]
     * @return this;
     */
    public @NotNull Image colorContrast(int contrast)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.contrast(contrast));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Changes the contrast of this image by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param contrast The amount to change the contrast by [{@code -1.0 - +1.0}]
     * @return this;
     */
    public @NotNull Image colorContrast(double contrast)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.contrast(contrast));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Changes the gamma of this image by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param gamma The amount to change the gamma
     * @return this;
     */
    public @NotNull Image colorGamma(double gamma)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.gamma(gamma));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Inverts the color of this image.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @return this;
     */
    public @NotNull Image colorInvert()
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(Color::invert);
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Makes the image brighter by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param percentage the percentage to make the color brighter [{@code 0.0 - 1.0}]
     * @return this;
     */
    public @NotNull Image colorBrighter(double percentage)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.brighter(percentage));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Makes the image darker by a specified amount.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param percentage the percentage to make the color darker [{@code 0.0 - 1.0}]
     * @return this;
     */
    public @NotNull Image colorDarker(double percentage)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            pixels.forEach(c -> c.darker(percentage));
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Replaces all pixels that are close enough to {@code color} with {@code replace}
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param color    The color to find.
     * @param distance The distance to the color.
     * @param replace  The color to replace.
     * @return this;
     */
    public @NotNull Image colorReplace(@NotNull Colorc color, @NotNull Colorc replace, double distance)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            if (distance <= 0.0)
            {
                pixels.forEach(c -> {if (c.equals(color)) c.set(replace);});
            }
            else
            {
                int r = color.r();
                int g = color.g();
                int b = color.b();
                
                pixels.forEach(c -> {
                    double rc   = r > 0 ? (double) Math.abs(c.r() - r) / r : c.rf();
                    double gc   = g > 0 ? (double) Math.abs(c.g() - g) / g : c.gf();
                    double bc   = b > 0 ? (double) Math.abs(c.b() - b) / b : c.bf();
                    double dist = rc * rc + gc * gc + bc * bc;
                    if (dist <= distance) c.set(replace);
                });
            }
            
            this.data = pixels;
        }
        return this;
    }
    
    //----------------------------
    // ----- Alpha Functions -----
    //----------------------------
    
    /**
     * Replaces any pixel whose alpha value is within the threshold with the
     * provided color.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param color     The color to use.
     * @param threshold The threshold.
     * @return this
     */
    public @NotNull Image alphaClear(@NotNull Colorc color, double threshold)
    {
        if (this.data != null)
        {
            Color.Buffer pixels;
            
            if (this.format != ColorFormat.RGBA)
            {
                pixels = Objects.requireNonNull(getColorData());
                this.data.free();
                this.format = ColorFormat.RGBA;
            }
            else
            {
                pixels = this.data;
            }
            
            int _threshold = (int) (threshold * 255);
            
            pixels.forEach(c -> {if (c.a() <= _threshold) c.set(color);});
            
            this.data = pixels;
        }
        return this;
    }
    
    /**
     * Apply alpha mask to image
     * <p>
     * NOTE 1: Returned image is GRAY_ALPHA (16bit) or R
     * NOTE 2: alphaMask should be same size as image
     * <p>
     * Mipmap data will be lost.
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @param alphaMask The mask image
     * @return this
     */
    public @NotNull Image alphaMask(@NotNull Image alphaMask)
    {
        if (this.width != alphaMask.width || this.height != alphaMask.height)
        {
            Image.LOGGER.warning("Alpha mask must be same size as image");
        }
        else if (this.data != null)
        {
            // Force mask to be Grayscale
            Image mask = alphaMask.copy();
            if (mask.data != null)
            {
                mask.reformat(ColorFormat.GRAY);
                
                // Convert image to RGBA
                Color.Buffer pixels = Objects.requireNonNull(getColorData());
                Color.Buffer output = Color.malloc(ColorFormat.RGBA, this.width * this.height);
                
                long bytes = Integer.toUnsignedLong(this.width * this.height) * ColorFormat.RGBA.sizeof;
                MemoryUtil.memCopy(pixels.address(), output.address(), bytes);
                
                // Apply alpha mask to alpha channel
                output.forEach(c -> c.a(mask.data.get().r()));
                
                pixels.free();
                this.data.free();
                
                this.data    = output;
                this.format  = ColorFormat.RGBA;
                this.mipmaps = 1;
            }
            mask.delete();
        }
        return this;
    }
    
    /**
     * Pre-multiply alpha channel
     * <p>
     * The image will be converted to {@link ColorFormat#RGBA RGBA}.
     *
     * @return this
     */
    public @NotNull Image alphaPreMultiply()
    {
        if (this.data != null)
        {
            Color.Buffer pixels = Objects.requireNonNull(getColorData());
            
            pixels.forEach(c -> {
                int a = c.a();
                if (a == 0)
                {
                    c.set(0, 0, 0, 0);
                }
                else
                {
                    c.set(c.r() * a / 255,
                          c.g() * a / 255,
                          c.b() * a / 255,
                          a);
                }
            });
            
            this.data.free();
            
            this.data   = pixels;
            this.format = ColorFormat.RGBA;
        }
        return this;
    }
    
    //------------------------------
    // ----- Drawing Functions -----
    //------------------------------
    
    // TODO - Create Rectangle/Shape2d Versions
    
    public void clear(Colorc color)
    {
        if (this.data != null) this.data.forEach(c -> c.set(color));
    }
    
    public void drawPixel(int x, int y, Colorc color, BlendMode blendMode)
    {
        if (x < 0 || this.width <= x) return;
        if (y < 0 || this.height <= y) return;
        if (this.data == null) return;
        if (blendMode == BlendMode.NONE)
        {
            this.data.put(y * this.width + x, color);
            return;
        }
        this.data.get(y * this.width + x).blend(color, blendMode);
    }
    
    public void drawPixel(int x, int y, Colorc color)
    {
        drawPixel(x, y, color, BlendMode.NONE);
    }
    
    public void drawLine(int startX, int startY, int endX, int endY, Colorc color, BlendMode blendMode)
    {
        int x, y, xe, ye, temp;
        int dx = endX - startX;
        int dy = endY - startY;
        
        // straight lines idea by gurkanctn
        if (dx == 0) // Line is vertical
        {
            if (endY < startY)
            {
                temp   = startY;
                startY = endY;
                endY   = temp;
            }
            for (y = startY; y <= endY; y++) drawPixel(startX, y, color, blendMode);
            return;
        }
        
        if (dy == 0) // Line is horizontal
        {
            if (endX < startX)
            {
                temp   = startX;
                startX = endX;
                endX   = temp;
            }
            for (x = startX; x <= endX; x++) drawPixel(x, startY, color, blendMode);
            return;
        }
        
        // Line is Funk-aye
        int dx1 = Math.abs(dx);
        int dy1 = Math.abs(dy);
        int px  = 2 * dy1 - dx1;
        int py  = 2 * dx1 - dy1;
        if (dy1 <= dx1)
        {
            if (dx >= 0)
            {
                x  = startX;
                y  = startY;
                xe = endX;
            }
            else
            {
                x  = endX;
                y  = endY;
                xe = startX;
            }
            
            drawPixel(x, y, color, blendMode);
            
            while (x < xe)
            {
                x = x + 1;
                if (px < 0)
                {
                    px = px + 2 * dy1;
                }
                else
                {
                    y  = dx < 0 && dy < 0 || dx > 0 && dy > 0 ? y + 1 : y - 1;
                    px = px + 2 * (dy1 - dx1);
                }
                drawPixel(x, y, color, blendMode);
            }
        }
        else
        {
            if (dy >= 0)
            {
                x  = startX;
                y  = startY;
                ye = endY;
            }
            else
            {
                x  = endX;
                y  = endY;
                ye = startY;
            }
            
            drawPixel(x, y, color, blendMode);
            
            while (y < ye)
            {
                y = y + 1;
                if (py <= 0)
                {
                    py = py + 2 * dx1;
                }
                else
                {
                    x  = dx < 0 && dy < 0 || dx > 0 && dy > 0 ? x + 1 : x - 1;
                    py = py + 2 * (dx1 - dy1);
                }
                drawPixel(x, y, color, blendMode);
            }
        }
    }
    
    public void drawLine(int startX, int startY, int endX, int endY, Colorc color)
    {
        drawLine(startX, startY, endX, endY, color, BlendMode.NONE);
    }
    
    public void drawRectangle(int x, int y, int width, int height, int thickness, Colorc color, BlendMode blendMode)
    {
        fillRectangle(x, y, width, thickness, color, blendMode);
        fillRectangle(x, y + thickness, thickness, height - thickness * 2, color, blendMode);
        fillRectangle(x + width - thickness, y + thickness, thickness, height - thickness * 2, color, blendMode);
        fillRectangle(x, y + height - thickness, width, thickness, color, blendMode);
    }
    
    public void drawRectangle(int x, int y, int width, int height, int thickness, Colorc color)
    {
        drawRectangle(x, y, width, height, thickness, color, BlendMode.NONE);
    }
    
    public void fillRectangle(int x, int y, int width, int height, Colorc color, BlendMode blendMode)
    {
        for (int j = y; j < y + height; j++)
        {
            for (int i = x; i < x + width; i++)
            {
                drawPixel(i, j, color, blendMode);
            }
        }
    }
    
    public void fillRectangle(int x, int y, int width, int height, Colorc color)
    {
        fillRectangle(x, y, width, height, color, BlendMode.NONE);
    }
    
    public void drawCircle(int centerX, int centerY, int radius, Colorc color, BlendMode blendMode)
    {
        if (radius < 0 || centerX < -radius || centerY < -radius || centerX - this.width > radius || centerY - this.height > radius) return;
        
        if (radius > 0)
        {
            int x0 = 0;
            int y0 = radius;
            int d  = 3 - 2 * radius;
            
            while (y0 >= x0) // only formulate 1/8 of circle
            {
                // DrawCall even octants
                drawPixel(centerX + x0, centerY - y0, color, blendMode); // Q6 - upper right right
                drawPixel(centerX + y0, centerY + x0, color, blendMode); // Q4 - lower lower right
                drawPixel(centerX - x0, centerY + y0, color, blendMode); // Q2 - lower left left
                drawPixel(centerX - y0, centerY - x0, color, blendMode); // Q0 - upper upper left
                if (x0 != 0 && x0 != y0)
                {
                    drawPixel(centerX + y0, centerY - x0, color, blendMode); // Q7 - upper upper right
                    drawPixel(centerX + x0, centerY + y0, color, blendMode); // Q5 - lower right right
                    drawPixel(centerX - y0, centerY + x0, color, blendMode); // Q3 - lower lower left
                    drawPixel(centerX - x0, centerY - y0, color, blendMode); // Q1 - upper left left
                }
                
                d += d < 0 ? 4 * x0++ + 6 : 4 * (x0++ - y0--) + 10;
            }
        }
        else
        {
            drawPixel(centerX, centerY, color, blendMode);
        }
    }
    
    public void drawCircle(int centerX, int centerY, int radius, Colorc color)
    {
        drawCircle(centerX, centerY, radius, color, BlendMode.NONE);
    }
    
    public void fillCircle(int centerX, int centerY, int radius, Colorc color, BlendMode blendMode)
    {
        if (radius < 0 || centerX < -radius || centerY < -radius || centerX - this.width > radius || centerY - this.height > radius) return;
        
        if (radius > 0)
        {
            int x0 = 0;
            int y0 = radius;
            int d  = 3 - 2 * radius;
            
            while (y0 >= x0)
            {
                for (int x = centerX - y0; x <= centerX + y0; x++) drawPixel(x, centerY - x0, color, blendMode);
                if (x0 > 0) for (int x = centerX - y0; x <= centerX + y0; x++) drawPixel(x, centerY + x0, color, blendMode);
                
                if (d < 0)
                {
                    d += 4 * x0++ + 6;
                }
                else
                {
                    if (x0 != y0)
                    {
                        for (int x = centerX - x0; x <= centerX + x0; x++) drawPixel(x, centerY - y0, color, blendMode);
                        for (int x = centerX - x0; x <= centerX + x0; x++) drawPixel(x, centerY + y0, color, blendMode);
                    }
                    d += 4 * (x0++ - y0--) + 10;
                }
            }
        }
        else
        {
            drawPixel(centerX, centerY, color, blendMode);
        }
    }
    
    public void fillCircle(int centerX, int centerY, int radius, Colorc color)
    {
        fillCircle(centerX, centerY, radius, color, BlendMode.NONE);
    }
    
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color, BlendMode blendMode)
    {
        drawLine(x1, y1, x2, y2, color, blendMode);
        drawLine(x2, y2, x3, y3, color, blendMode);
        drawLine(x3, y3, x1, y1, color, blendMode);
    }
    
    public void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color)
    {
        drawTriangle(x1, y1, x2, y2, x3, y3, color, BlendMode.NONE);
    }
    
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color, BlendMode blendMode)
    {
        int     t1x, t2x, y, minx, maxx, t1xp, t2xp, temp;
        boolean changed1 = false;
        boolean changed2 = false;
        int     signx1, signx2, dx1, dy1, dx2, dy2;
        int     e1, e2;
        // Sort vertices
        if (y1 > y2)
        {
            temp = y1;
            y1   = y2;
            y2   = temp;
            
            temp = x1;
            x1   = x2;
            x2   = temp;
        }
        if (y1 > y3)
        {
            temp = y1;
            y1   = y3;
            y3   = temp;
            
            temp = x1;
            x1   = x3;
            x3   = temp;
        }
        if (y2 > y3)
        {
            temp = y2;
            y2   = y3;
            y3   = temp;
            
            temp = x2;
            x2   = x3;
            x3   = temp;
        }
        
        t1x = t2x = x1;
        y   = y1; // Starting points
        dx1 = x2 - x1;
        if (dx1 < 0)
        {
            dx1    = -dx1;
            signx1 = -1;
        }
        else
        {
            signx1 = 1;
        }
        dy1 = y2 - y1;
        
        dx2 = x3 - x1;
        if (dx2 < 0)
        {
            dx2    = -dx2;
            signx2 = -1;
        }
        else
        {
            signx2 = 1;
        }
        dy2 = y3 - y1;
        
        if (dy1 > dx1)
        {
            temp     = dx1;
            dx1      = dy1;
            dy1      = temp;
            changed1 = true;
        }
        if (dy2 > dx2)
        {
            temp     = dx2;
            dx2      = dy2;
            dy2      = temp;
            changed2 = true;
        }
        
        e2 = dx2 >> 1;
        // Flat top, just process the second half
        if (y1 != y2)
        {
            e1 = dx1 >> 1;
            
            for (int i = 0; i < dx1; )
            {
                t1xp = 0;
                t2xp = 0;
                if (t1x < t2x)
                {
                    minx = t1x;
                    maxx = t2x;
                }
                else
                {
                    minx = t2x;
                    maxx = t1x;
                }
                // process first line until y value is about to change
                next1:
                while (i < dx1)
                {
                    i++;
                    e1 += dy1;
                    while (e1 >= dx1)
                    {
                        e1 -= dx1;
                        if (changed1)
                        {
                            t1xp = signx1; //t1x += signx1;
                        }
                        else
                        {
                            break next1;
                        }
                    }
                    if (changed1)
                    {
                        break;
                    }
                    else
                    {
                        t1x += signx1;
                    }
                }
                // Move line
                // process second line until y value is about to change
                next2:
                while (true)
                {
                    e2 += dy2;
                    while (e2 >= dx2)
                    {
                        e2 -= dx2;
                        if (changed2)
                        {
                            t2xp = signx2; // t2x += signx2;
                        }
                        else
                        {
                            break next2;
                        }
                    }
                    if (changed2)
                    {
                        break;
                    }
                    else
                    {
                        t2x += signx2;
                    }
                }
                if (minx > t1x) minx = t1x;
                if (minx > t2x) minx = t2x;
                if (maxx < t1x) maxx = t1x;
                if (maxx < t2x) maxx = t2x;
                for (int j = minx; j <= maxx; j++) drawPixel(j, y, color, blendMode); // DrawCall line from min to max points found on the y
                // Now increase y
                if (!changed1) t1x += signx1;
                t1x += t1xp;
                if (!changed2) t2x += signx2;
                t2x += t2xp;
                y += 1;
                if (y == y2) break;
            }
        }
        // Second half
        dx1 = x3 - x2;
        if (dx1 < 0)
        {
            dx1    = -dx1;
            signx1 = -1;
        }
        else
        {
            signx1 = 1;
        }
        dy1 = y3 - y2;
        t1x = x2;
        
        if (dy1 > dx1)
        {   // swap values
            temp     = dx1;
            dx1      = dy1;
            dy1      = temp;
            changed1 = true;
        }
        else
        {
            changed1 = false;
        }
        
        e1 = dx1 >> 1;
        
        for (int i = 0; i <= dx1; i++)
        {
            t1xp = 0;
            t2xp = 0;
            if (t1x < t2x)
            {
                minx = t1x;
                maxx = t2x;
            }
            else
            {
                minx = t2x;
                maxx = t1x;
            }
            // process first line until y value is about to change
            next3:
            while (i < dx1)
            {
                e1 += dy1;
                while (e1 >= dx1)
                {
                    e1 -= dx1;
                    if (changed1)
                    {
                        t1xp = signx1;//t1x += signx1;
                        break;
                    }
                    else
                    {
                        break next3;
                    }
                }
                if (changed1) {break;}
                else {t1x += signx1;}
                if (i < dx1) i++;
            }
            // process second line until y value is about to change
            next4:
            while (t2x != x3)
            {
                e2 += dy2;
                while (e2 >= dx2)
                {
                    e2 -= dx2;
                    if (changed2)
                    {
                        t2xp = signx2;
                    }
                    else
                    {
                        break next4;
                    }
                }
                if (changed2) {break;}
                else {t2x += signx2;}
            }
            
            if (minx > t1x) minx = t1x;
            if (minx > t2x) minx = t2x;
            if (maxx < t1x) maxx = t1x;
            if (maxx < t2x) maxx = t2x;
            for (int j = minx; j <= maxx; j++) drawPixel(j, y, color, blendMode);
            if (!changed1) t1x += signx1;
            t1x += t1xp;
            if (!changed2) t2x += signx2;
            t2x += t2xp;
            y += 1;
            if (y > y3) return;
        }
    }
    
    public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Colorc color)
    {
        fillTriangle(x1, y1, x2, y2, x3, y3, color, BlendMode.NONE);
    }
    
    public void drawImage(Image src, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH, BlendMode blendMode)
    {
        if (this.data == null || src.data == null) return;
        
        if (this.mipmaps > 1) Image.LOGGER.warning("Image manipulation only applied to base mipmap level");
        
        Image   srcI      = src;   // Pointer to source image
        boolean copiedSrc = false; // Track source copy required
        
        // Source rectangle out-of-bounds security checks
        srcI.validateRect(srcX, srcY, srcW, srcH);
        
        // Check if source rectangle needs to be resized to destination rectangle
        // In that case, we make a copy of source and we apply all required transform
        if (srcW != dstW || srcH != dstH)
        {
            srcI = src.subImage(srcX, srcY, srcW, srcH); // Create image from another image
            srcI.resize(dstW, dstH); // Resize to destination rectangle
            srcX = 0;
            srcY = 0;
            srcW = srcI.width;
            srcH = srcI.height;
            
            copiedSrc = true;
        }
        
        if (srcI.data == null) return;
        
        // Destination rectangle out-of-bounds security checks
        validateRect(dstX, dstY, dstW, dstH);
        
        // Fast path: Avoid blendMode if source has no alpha to blendMode
        boolean blendRequired = blendMode != BlendMode.NONE && srcI.format.alpha;
        
        int srcBPP    = srcI.format.sizeof;
        int srcStride = srcI.width * srcBPP;
        
        int dstBPP    = this.format.sizeof;
        int dstStride = this.width * dstBPP;
        
        long srcPtrBase = srcI.data.address() + Integer.toUnsignedLong(srcY * srcI.width + srcX) * srcBPP;
        long dstPtrBase = this.data.address() + Integer.toUnsignedLong(dstY * this.width + dstX) * dstBPP;
        
        for (int j = 0; j < srcH; j++)
        {
            long srcPtr = srcPtrBase;
            long dstPtr = dstPtrBase;
            
            // Fast path: Avoid moving pixel by pixel if no blendMode required and same format
            if (!blendRequired && srcI.format == this.format)
            {
                MemoryUtil.memCopy(srcPtr, dstPtr, srcStride);
            }
            else
            {
                for (int i = 0; i < srcW; i++)
                {
                    Color colSrc = Color.create(srcI.format, srcPtr);
                    Color colDst = Color.create(this.format, dstPtr);
                    
                    // Fast path: Avoid blendMode if source has no alpha to blendMode
                    if (blendRequired) colDst.blend(colSrc, blendMode);
                    
                    dstPtr += dstBPP;
                    srcPtr += srcBPP;
                }
            }
            
            srcPtrBase += srcStride;
            dstPtrBase += dstStride;
        }
        
        if (copiedSrc) srcI.delete(); // Unload source modified image
    }
    
    public void drawImage(Image src, int srcX, int srcY, int srcW, int srcH, int dstX, int dstY, int dstW, int dstH)
    {
        drawImage(src, srcX, srcY, srcW, srcH, dstX, dstY, dstW, dstH, BlendMode.NONE);
    }
}
