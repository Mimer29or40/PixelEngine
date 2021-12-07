package pe;

import pe.render.*;
import rutils.Logger;

import java.util.logging.Level;

public class EngineTest extends Engine
{
    GLVertexArray vao;
    
    @Override
    protected void setup()
    {
        size(100, 100, 4, 4);
        
        // try (MemoryStack stack = MemoryStack.stackPush())
        // {
        // FloatBuffer pos = stack.floats(
        //         50.0F, 25.0F, 0.0f, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 1.0F,
        //         25.0F, 50.0F, 0.0f, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.0F,
        //         75.0F, 50.0F, 0.0f, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 1.0F);
        // vao = GLVertexArray.builder().buffer(pos, Usage.STATIC_DRAW,
        //                                      new GLAttribute(GLType.FLOAT, 3),
        //                                      new GLAttribute(GLType.FLOAT, 2),
        //                                      new GLAttribute(GLType.FLOAT, 0),
        //                                      new GLAttribute(GLType.FLOAT, 0),
        //                                      new GLAttribute(GLType.FLOAT, 4, true),
        //                                      new GLAttribute(GLType.FLOAT, 0)).build();
        
        // FloatBuffer pos = stack.floats(
        //         50.0F, 25.0F, 0.0f,
        //         25.0F, 50.0F, 0.0f,
        //         75.0F, 50.0F, 0.0f);
        // FloatBuffer tex = stack.floats(
        //         0.0f, 0.0F,
        //         0.0f, 0.0F,
        //         0.0f, 0.0F);
        // FloatBuffer norm = stack.floats(
        //         0.0f, 0.0F, 1.0F,
        //         0.0f, 0.0F, 1.0F,
        //         0.0f, 0.0F, 1.0F);
        // FloatBuffer tan = stack.floats(
        //         1.0f, 0.0F, 0.0F,
        //         1.0f, 0.0F, 0.0F,
        //         1.0f, 0.0F, 0.0F);
        // ByteBuffer col = stack.bytes(
        //         (byte) 255, (byte) 0, (byte) 0, (byte) 255,
        //         (byte) 0, (byte) 0, (byte) 255, (byte) 255,
        //         (byte) 0, (byte) 255, (byte) 0, (byte) 255);
        // FloatBuffer tex2 = stack.floats(
        //         0.0f, 0.0F,
        //         0.0f, 0.0F,
        //         0.0f, 0.0F);
        // vao = GLVertexArray.builder()
        //                    .buffer(pos, Usage.STATIC_DRAW, new GLAttribute(GLType.FLOAT, 3))
        //                    .buffer(tex, Usage.STATIC_DRAW, new GLAttribute(GLType.FLOAT, 2))
        //                    .buffer(norm, Usage.STATIC_DRAW, new GLAttribute(GLType.FLOAT, 3))
        //                    .buffer(tan, Usage.STATIC_DRAW, new GLAttribute(GLType.FLOAT, 3))
        //                    .buffer(col, Usage.STATIC_DRAW, new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
        //                    .buffer(tex2, Usage.STATIC_DRAW, new GLAttribute(GLType.FLOAT, 2))
        //                    .build();
        // }
        
        GLProgram.Uniform.vec4(GLProgram.UNIFORM_COLOR_DIFFUSE, 1.0, 1.0, 1.0, 1.0);
    }
    
    @Override
    protected void draw(double elapsedTime)
    {
        GLState.clearScreenBuffers();
        
        // double x = Mouse.get().x();
        // double y = Mouse.get().y();
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
        
        GLBatch.get().draw();
        
        // vao.draw(DrawMode.TRIANGLES);
    }
    
    @Override
    protected void destroy()
    {
    
    }
    
    public static void main(String[] args)
    {
        // Logger.setLevel(Level.FINEST);
        Logger.setLevel(Level.FINE);
        start(new EngineTest());
    }
}
