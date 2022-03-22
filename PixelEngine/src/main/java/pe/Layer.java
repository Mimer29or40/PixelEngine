package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.lwjgl.system.MemoryStack;
import pe.render.*;
import pe.shape.AABBi;
import pe.shape.AABBic;
import rutils.Logger;
import rutils.Math;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public final class Layer extends GLFramebuffer
{
    private static final Logger LOGGER = new Logger();
    
    static GLProgram     program;
    static GLVertexArray vertexArray;
    
    static final Map<Index, Layer> layers = new EnumMap<>(Index.class);
    static       Layer             primary;
    
    static void setup(int width, int height)
    {
        Layer.LOGGER.fine("Setup");
        
        String vert = """
                      #version 330
                      layout(location = 0) in float aIndex;
                      layout(location = 1) in vec2 aTexCoord;
                      uniform vec2 framebuffer;
                      uniform float aspect;
                      out vec2 texCoord;
                      
                      vec2 toViewport(float x, float y)
                      {
                          x = (2.0 * x / framebuffer.x) - 1;
                          y = (2.0 * y / framebuffer.y) - 1;
                          return vec2(x, y);
                      }
                      
                      void main(void)
                      {
                          float width = framebuffer.x;
                          float height = round(framebuffer.x / aspect);
                          if (height > framebuffer.y)
                          {
                              width = round(framebuffer.y * aspect);
                              height = framebuffer.y;
                          }
                          float x = (framebuffer.x - width) * 0.5;
                          float y = (framebuffer.y - height) * 0.5;
                          
                          vec2 pos = vec2(0, 0);
                          if (aIndex == 0.0)
                          {
                              pos = toViewport(x, y);
                          }
                          else if (aIndex == 1.0)
                          {
                              pos = toViewport(x, y + height);
                          }
                          else if (aIndex == 2.0)
                          {
                              pos = toViewport(x + width, y);
                          }
                          else if (aIndex == 3.0)
                          {
                              pos = toViewport(x + width, y + height);
                          }
                          
                          texCoord = aTexCoord;
                          gl_Position = vec4(pos, 0.0, 1.0);
                      }
                      """;
        String frag = """
                      #version 330
                      uniform sampler2D tex;
                      in vec2 texCoord;
                      out vec4 FragColor;
                      void main(void)
                      {
                          FragColor = texture(tex, texCoord);
                      }
                      """;
        Layer.program = GLProgram.loadFromCode(vert, null, frag);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            @SuppressWarnings("RedundantArrayCreation")
            FloatBuffer vertices = stack.floats(new float[] {
                    1F, 0F, 1F,
                    0F, 0F, 0F,
                    2F, 1F, 0F,
                    3F, 1F, 1F
            });
            IntBuffer indices = stack.ints(0, 1, 2, 0, 2, 3);
            Layer.vertexArray = GLVertexArray.builder()
                                             .buffer(vertices, Usage.STATIC_DRAW,
                                                     new GLAttribute(GLType.FLOAT, 1, false),
                                                     new GLAttribute(GLType.FLOAT, 2, false))
                                             .indexBuffer(indices, Usage.STATIC_DRAW)
                                             .build();
        }
        
        Layer.primary = Layer.create(Index.ZERO, width, height);
    }
    
    static void destroy()
    {
        Layer.LOGGER.fine("Destroy");
        
        Layer.program.delete();
        Layer.vertexArray.delete();
        
        for (Layer layer : Layer.layers.values())
        {
            if (layer == null) continue;
            
            layer.delete();
        }
        Layer.layers.clear();
    }
    
    static void events()
    {
        int fbWidth  = Window.framebufferWidth();
        int fbHeight = Window.framebufferHeight();
        
        for (Layer layer : Layer.layers.values())
        {
            if (layer == null) continue;
            
            AABBi bounds = layer.bounds;
            
            double aspect = layer.aspectRatio();
            
            bounds.size.set(fbWidth, (int) (fbWidth / aspect));
            if (bounds.height() > fbHeight) bounds.size.set((int) (fbHeight * aspect), fbHeight);
            bounds.pos.set((fbWidth - bounds.width()) >> 1, (fbHeight - bounds.height()) >> 1);
            
            layer.pixelSize.x = Math.max(bounds.width() / (double) layer.width(), 1.0);
            layer.pixelSize.y = Math.max(bounds.height() / (double) layer.height(), 1.0);
        }
    }
    
    static void draw()
    {
        GLFramebuffer.bind(null);
        GLProgram.bind(Layer.program);
        
        int fbWidth  = GLFramebuffer.currentWidth();
        int fbHeight = GLFramebuffer.currentHeight();
        
        GLProgram.Uniform.vec2("framebuffer", fbWidth, fbHeight);
        
        GLState.defaultState();
        GLState.depthMode(DepthMode.NONE);
        
        GLState.clearScreenBuffers(ScreenBuffer.COLOR);
        
        for (Layer layer : Layer.layers.values())
        {
            if (layer == null) continue;
            
            GLProgram.Uniform.float1("aspect", layer.aspectRatio());
            
            GLTexture.bind(layer.color());
            Layer.vertexArray.drawElements(DrawMode.TRIANGLES, 6);
        }
    }
    
    @NotNull
    public static Layer primary()
    {
        return Layer.primary;
    }
    
    @Nullable
    public static Layer get(Index index)
    {
        return Layer.layers.get(index);
    }
    
    @NotNull
    public static Layer create(@NotNull Index index, int width, int height)
    {
        if (Layer.layers.get(index) != null) throw new IllegalArgumentException(String.format("Layer at %s already exists", index));
        Layer layer = new Layer(width, height);
        Layer.layers.put(index, layer);
        return layer;
    }
    
    @NotNull
    public static Layer create(@NotNull Index index, @NotNull Vector2ic size)
    {
        return create(index, size.x(), size.y());
    }
    
    @NotNull
    public static Layer create(int width, int height)
    {
        Index index = null;
        for (Index idx : Layer.layers.keySet())
        {
            Layer layer = Layer.layers.get(idx);
            if (layer == null)
            {
                index = idx;
                break;
            }
        }
        if (index == null) throw new IllegalArgumentException("No available Layers remain.");
        Layer layer = new Layer(width, height);
        Layer.layers.put(index, layer);
        return layer;
    }
    
    @NotNull
    public static Layer create(@NotNull Vector2ic size)
    {
        return create(size.x(), size.y());
    }
    
    // -------------------- Instance -------------------- //
    
    final Vector2i size;
    final AABBi    bounds;
    final Vector2d pixelSize;
    
    private Layer(int width, int height)
    {
        super(width, height);
        
        this.size      = new Vector2i(width, height);
        this.bounds    = new AABBi();
        this.pixelSize = new Vector2d(1.0);
    }
    
    @NotNull
    public Vector2ic size()
    {
        return this.size;
    }
    
    @Override
    public int width()
    {
        return this.size.x;
    }
    
    @Override
    public int height()
    {
        return this.size.y;
    }
    
    @NotNull
    public AABBic bounds()
    {
        return this.bounds;
    }
    
    @NotNull
    public Vector2dc pixelSize()
    {
        return this.pixelSize;
    }
    
    public double pixelWidth()
    {
        return this.pixelSize.x;
    }
    
    public double pixelHeight()
    {
        return this.pixelSize.y;
    }
    
    public double aspectRatio()
    {
        return (width() * pixelWidth()) / (height() * pixelHeight());
    }
    
    public enum Index
    {
        ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN;
        
        public static final Index LAST = SEVEN;
        
        private static final Map<Integer, Index> LOOKUP = new HashMap<>();
        
        public static Index valueOf(int value)
        {
            return Index.LOOKUP.getOrDefault(value, Index.ZERO);
        }
        
        static
        {
            for (Index index : values())
            {
                Index.LOOKUP.put(index.ordinal(), index);
            }
        }
    }
}
