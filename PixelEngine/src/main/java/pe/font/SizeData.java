package pe.font;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import pe.color.ColorFormat;
import pe.texture.Texture;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.memAlloc;
import static org.lwjgl.system.MemoryUtil.memFree;

public final class SizeData
{
    private final Font font;
    
    public final int    size;
    public final double scale;
    
    public final double ascent;
    public final double descent;
    public final double lineGap;
    
    final Texture texture;
    
    final PackedQuad[] packedQuads = new PackedQuad[0xFFFF];
    
    SizeData(@NotNull Font font, int size)
    {
        this.font = font;
        
        this.size  = size;
        this.scale = stbtt_ScaleForPixelHeight(font.info, this.size);
        
        this.ascent  = font.ascentUnscaled * this.scale;
        this.descent = font.descentUnscaled * this.scale;
        this.lineGap = font.lineGapUnscaled * this.scale;
        
        STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(0xFFFF);
        
        int width  = 0;
        int height = 0;
        
        ByteBuffer buffer = null;
        
        boolean success = false;
        
        int textureSize = 32;
        int samples     = 2;
        while (!success && textureSize < 1000)
        {
            width  = this.size * textureSize;
            height = this.size * (textureSize >> 1);
            
            buffer = BufferUtils.createByteBuffer(width * height);
            
            charData.position(32);
            try (STBTTPackContext pc = STBTTPackContext.malloc())
            {
                stbtt_PackBegin(pc, buffer, width, height, 0, 2, MemoryUtil.NULL);
                stbtt_PackSetOversampling(pc, samples, samples);
                success = stbtt_PackFontRange(pc, font.fileData, 0, this.size, charData.position(), charData);
                stbtt_PackEnd(pc);
            }
            charData.clear();
            buffer.clear();
            
            textureSize <<= 1;
        }
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            FloatBuffer x = stack.mallocFloat(1);
            FloatBuffer y = stack.mallocFloat(1);
            
            STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);
            
            for (int i = 0; i < 0xFFFF; i++)
            {
                stbtt_GetPackedQuad(charData, width, height, i, x.put(0, 0), y.put(0, 0), q, false);
                
                this.packedQuads[i] = new PackedQuad(q, this.ascent);
            }
        }
        charData.free();
        
        // Converts GL_RED to GL_RGBA
        ByteBuffer data = memAlloc(width * height * 4);
        for (int i = 0; i < buffer.capacity(); i++) data.putInt((buffer.get(i) << 24) | 0x00FFFFFF);
        data.flip();
        
        this.texture = Texture.load(data, width, height, 1, ColorFormat.RGBA);
        
        memFree(data);
    }
    
    @Override
    public String toString()
    {
        return "SizeData{" + this.size + ", font=" + this.font + "}";
    }
    
    public @NotNull PackedQuad getPackedQuad(int character)
    {
        return this.packedQuads[character];
    }
}
