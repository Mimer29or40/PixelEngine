package pe;

import org.lwjgl.system.MemoryStack;
import pe.color.Color;
import pe.render.*;
import rutils.Logger;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;

public class EngineTest extends Engine
{
    Window.Builder builder;
    Window         window;
    
    static GLProgram     program;
    static GLVertexArray vertexArray;
    
    
    @Override
    protected void setup()
    {
        size(100, 100, 4, 4);
        
        builder = new Window.Builder();
        
        builder.name("Test");
        builder.size(200, 200);
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        if (Keyboard.down(Keyboard.Key.SPACE) && this.window == null)
        {
            this.window = builder.build();
            Window.makeCurrent(this.window);
            
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
            program = GLProgram.loadFromCode(vert, null, frag);
            
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
                vertexArray = GLVertexArray.builder()
                                           .buffer(vertices, Usage.STATIC_DRAW,
                                                   new GLAttribute(GLType.FLOAT, 1, false),
                                                   new GLAttribute(GLType.FLOAT, 2, false))
                                           .indexBuffer(indices, Usage.STATIC_DRAW)
                                           .build();
            }
        }
        
        if (this.window != null && this.window.isOpen())
        {
            Window.makeCurrent(this.window);
            
            Draw.clearBackground(Color.BACKGROUND_GRAY);
            
            GLFramebuffer.bind(null);
            GLProgram.bind(program);
            
            int fbWidth  = this.window.framebufferWidth();
            int fbHeight = this.window.framebufferHeight();
            
            GLProgram.Uniform.vec2("framebuffer", fbWidth, fbHeight);
            
            GLState.defaultState();
            GLState.viewport(0, 0, fbWidth, fbHeight);
            GLState.depthMode(DepthMode.NONE);
            
            GLState.clearScreenBuffers(ScreenBuffer.COLOR);
            
            for (Layer layer : Layer.layers.values())
            {
                if (layer == null) continue;
                
                GLProgram.Uniform.float1("aspect", layer.aspectRatio());
                
                GLTexture.bind(layer.framebuffer.color());
                vertexArray.drawElements(DrawMode.TRIANGLES, 6);
            }
            
            this.window.swap();
            
            Window.makeCurrent(null);
            
            GLFramebuffer.bind(Layer.primary.framebuffer);
            GLProgram.bind(null);
            
            GLState.defaultState();
            GLState.wireframe(Engine.wireframe);
            
            GLBatch.bind(null);
        }
        
        Draw.clearBackground(Color.BACKGROUND_GRAY);
        
        // double x = Mouse.x();
        // double y = Mouse.y();
        //
        // GLBatch.get().matrix.mode(MatrixMode.MODEL);
        // GLBatch.get().matrix.translate(x, y, 0.0);
        
        Draw.fillTriangle2D()
            .point0(50.0, 25.0)
            .color0(255, 0, 0, 255)
            .point1(25.0, 50.0)
            .color1(0, 255, 0, 255)
            .point2(75.0, 50.0)
            .color2(0, 0, 255, 255)
            .draw();
        
        Draw.drawTriangle2D()
            .point0(25.0, 25.0)
            .point1(50.0, 50.0)
            .point2(75.0, 25.0)
            .thickness(1.0)
            .draw();
        
        if (Keyboard.down(Keyboard.Key.R)) Mouse.pos(10, 10);
        if (Keyboard.down(Keyboard.Key.A)) Mouse.pos(10, 10);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        Logger.addLogFile("out/console.log");
        start(new EngineTest());
    }
}
