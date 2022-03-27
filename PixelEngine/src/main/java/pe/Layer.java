package pe;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector2ic;
import org.lwjgl.system.MemoryStack;
import pe.render.*;
import pe.shape.AABB2i;
import pe.shape.AABB2ic;
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
                      uniform vec4 layer;
                      out vec2 texCoord;
                      
                      vec2 toViewport(float x, float y)
                      {
                          x = (2.0 * x / framebuffer.x) - 1.0;
                          y = (2.0 * y / framebuffer.y) - 1.0;
                          return vec2(x, y);
                      }
                      
                      void main(void)
                      {
                          float x = layer.x;
                          float y = layer.y;
                          float w = layer.z;
                          float h = layer.w;
                          
                          vec2 pos = vec2(0, 0);
                          if (aIndex == 0.0)
                          {
                              pos = toViewport(x, y);
                          }
                          else if (aIndex == 1.0)
                          {
                              pos = toViewport(x, y + h);
                          }
                          else if (aIndex == 2.0)
                          {
                              pos = toViewport(x + w, y);
                          }
                          else if (aIndex == 3.0)
                          {
                              pos = toViewport(x + w, y + h);
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
            
            AABB2i bounds = layer.bounds;
            
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
        
        GL.defaultState();
        GL.depthMode(DepthMode.NONE);
        
        GL.clearScreenBuffers(ScreenBuffer.COLOR);
        
        for (Layer layer : Layer.layers.values())
        {
            if (layer == null) continue;
            
            GLProgram.Uniform.vec4("layer", layer.bounds.x(), layer.bounds.y(), layer.bounds.width(), layer.bounds.height());
            
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
    
    final AABB2i   bounds;
    final Vector2d pixelSize;
    
    private Layer(int width, int height)
    {
        super(width, height);
        
        this.bounds    = new AABB2i();
        this.pixelSize = new Vector2d(1.0);
    }
    
    @NotNull
    public AABB2ic bounds()
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
