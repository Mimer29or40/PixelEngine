package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4d;
import org.joml.Vector3d;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.util.buffer.Byte4;
import pe.util.buffer.Float3;
import rutils.Logger;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Objects;

public class GLBatch
{
    private static final Logger LOGGER = new Logger();
    
    private static int index = 0;
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    static void setup()
    {
        GLBatch.LOGGER.fine("Setup");
        
        GL.defaultBatch = new GLBatch();
    }
    
    static void destroy()
    {
        GLBatch.LOGGER.fine("Destroy");
        
        GL.currentBatch = null;
        
        GLBatch batch = GL.defaultBatch;
        GL.defaultBatch = null;
        batch.delete();
    }
    
    public static void bind(@Nullable GLBatch batch)
    {
        if (batch == null) batch = GL.defaultBatch;
        
        GLBatch.LOGGER.finest("Binding Batch:", batch);
        
        GL.currentBatch = batch;
    }
    
    // -------------------- Public Interface -------------------- //
    
    public static BatchStats stats()
    {
        GL.currentBatch.drawInternal();
        
        GL.currentBatch.stats.set(GL.currentBatch.internalStats);
        GL.currentBatch.internalStats.reset();
        return GL.currentBatch.stats;
    }
    
    public static void push()
    {
        GL.currentBatch.drawInternal();
        
        GLBatch.LOGGER.finest("Pushing Stack");
        
        Matrix4d projection = GL.currentBatch.projection[GL.currentBatch.stackIndex];
        Matrix4d view       = GL.currentBatch.view[GL.currentBatch.stackIndex];
        Matrix4d model      = GL.currentBatch.model[GL.currentBatch.stackIndex];
        Matrix4d normal     = GL.currentBatch.normal[GL.currentBatch.stackIndex];
        
        Color diffuse  = GL.currentBatch.diffuse.get(GL.currentBatch.stackIndex);
        Color specular = GL.currentBatch.specular.get(GL.currentBatch.stackIndex);
        Color ambient  = GL.currentBatch.ambient.get(GL.currentBatch.stackIndex);
        
        ++GL.currentBatch.stackIndex;
        
        GL.currentBatch.projection[GL.currentBatch.stackIndex].set(projection);
        GL.currentBatch.view[GL.currentBatch.stackIndex].set(view);
        GL.currentBatch.model[GL.currentBatch.stackIndex].set(model);
        GL.currentBatch.normal[GL.currentBatch.stackIndex].set(normal);
        
        GL.currentBatch.diffuse.put(GL.currentBatch.stackIndex, diffuse);
        GL.currentBatch.specular.put(GL.currentBatch.stackIndex, specular);
        GL.currentBatch.ambient.put(GL.currentBatch.stackIndex, ambient);
    }
    
    public static void pop()
    {
        GLBatch.LOGGER.finest("Popping Stack");
        
        --GL.currentBatch.stackIndex;
    }
    
    public static void draw()
    {
        GL.currentBatch.drawInternal();
    }
    
    public static void begin(@NotNull DrawMode mode)
    {
        if (GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not ended: " + GL.currentBatch);
        
        GL.currentBatch.hasBegun = true;
        
        GLBatch.LOGGER.finest("Beginning Mode (%s): %s", mode, GL.currentBatch);
        
        if (GL.currentBatch.drawCalls[GL.currentBatch.currentDraw].mode != mode)
        {
            GL.currentBatch.incDrawCall();
            
            GL.currentBatch.drawCalls[GL.currentBatch.currentDraw].mode = mode;
        }
    }
    
    public static void end()
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not begun: " + GL.currentBatch);
        
        GL.currentBatch.hasBegun = false;
        
        GLBatch.LOGGER.finest("Ending", GL.currentBatch);
        
        // Make sure tex1 count match vertex count
        for (int i = 0, n = GL.currentBatch.pos.position() - GL.currentBatch.tex1.position(); i < n; i++) GL.currentBatch.tex1.put(0.0, 0.0, 1.0);
        
        // Make sure norm count match vertex count
        for (int i = 0, n = GL.currentBatch.pos.position() - GL.currentBatch.norm.position(); i < n; i++) GL.currentBatch.norm.put(0.0, 0.0, 1.0);
        
        // Make sure tan count match vertex count
        for (int i = 0, n = GL.currentBatch.pos.position() - GL.currentBatch.tan.position(); i < n; i++) GL.currentBatch.tan.put(1.0, 0.0, 0.0);
        
        // Make sure col count match vertex count
        for (int i = 0, n = GL.currentBatch.pos.position() - GL.currentBatch.col.position(); i < n; i++) GL.currentBatch.col.put(GL.currentBatch.col.get(GL.currentBatch.col.position() - 1));
        
        // Make sure tex2 count match vertex count
        for (int i = 0, n = GL.currentBatch.pos.position() - GL.currentBatch.tex2.position(); i < n; i++) GL.currentBatch.tex2.put(0.0, 0.0, 1.0);
        
        // Correct increment formula would be: depthInc = (zFar - zNear)/pow(2, bits)
        GL.currentBatch.currentDepth -= 0.00005;
    }
    
    public static void setTexture(@NotNull GLTexture texture)
    {
        GLBatch.LOGGER.finest("Setting Texture (%s) %s", texture, GL.currentBatch);
        
        if (GL.currentBatch.drawCalls[GL.currentBatch.currentDraw].texture != texture)
        {
            GL.currentBatch.incDrawCall();
            
            GL.currentBatch.drawCalls[GL.currentBatch.currentDraw].texture = texture;
        }
    }
    
    public static void addTexture(@NotNull String name, @NotNull GLTexture texture)
    {
        GLBatch.LOGGER.finest("Adding Texture to Batch: %s=%s", name, texture);
        
        if (GL.currentBatch.textureIndex + 1 > GL.currentBatch.textureNames.length) throw new IllegalStateException("Active Texture Limit Exceeded: " + GL.currentBatch.textureNames.length);
        
        GL.currentBatch.textureNames[GL.currentBatch.textureIndex]  = name;
        GL.currentBatch.textureActive[GL.currentBatch.textureIndex] = texture;
        
        GL.currentBatch.textureIndex++;
    }
    
    public static void pos(double x, double y, double z)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        // Verify that current vertex buffer elements limit has not been reached
        if (GL.currentBatch.pos.position() < GL.currentBatch.elementsCount * 4)
        {
            GLBatch.LOGGER.finest("Setting Vertex Position: [%s, %s, %s]", x, y, z);
            
            GL.currentBatch.pos.put(x, y, z);
            
            GL.currentBatch.drawCalls[GL.currentBatch.currentDraw].vertexCount++;
        }
        else
        {
            GLBatch.LOGGER.severe("Vertex Element Overflow");
        }
    }
    
    public static void pos(double x, double y)
    {
        pos(x, y, GL.currentBatch.currentDepth);
    }
    
    public static void texCoord(double u, double v, double q)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        GLBatch.LOGGER.finest("Setting Vertex Texture Coordinate: [%s, %s, %s]", u, v, q);
        
        GL.currentBatch.tex1.put(u, v, q);
    }
    
    public static void texCoord(double u, double v)
    {
        texCoord(u, v, 1.0);
    }
    
    public static void normal(double x, double y, double z)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        GLBatch.LOGGER.finest("Setting Vertex Normal: [%s, %s, %s]", x, y, z);
        
        GL.currentBatch.norm.put(x, y, z);
    }
    
    public static void tangent(double x, double y, double z)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        GLBatch.LOGGER.finest("Setting Vertex Tangent: [%s, %s, %s]", x, y, z);
        
        GL.currentBatch.tan.put(x, y, z);
    }
    
    public static void color(int r, int g, int b, int a)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        GLBatch.LOGGER.finest("Setting Vertex Color: [%s, %s, %s, %s]", r, g, b, a);
        
        GL.currentBatch.col.put(r, g, b, a);
    }
    
    public static void texCoord2(double u, double v, double q)
    {
        if (!GL.currentBatch.hasBegun) throw new IllegalStateException("Batch was not started: " + GL.currentBatch);
        
        GLBatch.LOGGER.finest("Setting Vertex Texture Coordinate 2: [%s, %s, %s]", u, v, q);
        
        GL.currentBatch.tex2.put(u, v, q);
    }
    
    public static void texCoord2(double u, double v)
    {
        texCoord2(u, v, 1.0);
    }
    
    public static void vertex(@NotNull Vertex vertex)
    {
        if (vertex.hasPos)
        {
            if (vertex.hasPosZ)
            {
                pos(vertex.x, vertex.y, vertex.z);
            }
            else
            {
                pos(vertex.x, vertex.y);
            }
        }
        if (vertex.hasTexCoord)
        {
            if (Double.compare(vertex.q, 1.0) == 0)
            {
                texCoord(vertex.u, vertex.v);
            }
            else
            {
                texCoord(vertex.u * vertex.q, vertex.v * vertex.q, vertex.q);
            }
        }
        if (vertex.hasNormal) normal(vertex.nx, vertex.ny, vertex.nz);
        if (vertex.hasTangent) tangent(vertex.tx, vertex.ty, vertex.tz);
        if (vertex.hasColor) color(vertex.r, vertex.g, vertex.b, vertex.a);
        if (vertex.hasTexCoord2)
        {
            if (Double.compare(vertex.q2, 1.0) == 0)
            {
                texCoord2(vertex.u2, vertex.v2);
            }
            else
            {
                texCoord2(vertex.u2 * vertex.q2, vertex.v2 * vertex.q2, vertex.q2);
            }
        }
    }
    
    public static void checkBuffer(int vertexCount)
    {
        if (GL.currentBatch.pos.position() + vertexCount >= GL.currentBatch.elementsCount * 4) GL.currentBatch.drawInternal();
    }
    
    /**
     * @return A view of the projection matrix
     */
    public static @NotNull Matrix4d projection()
    {
        return GL.currentBatch.projection[GL.currentBatch.stackIndex];
    }
    
    /**
     * @return A view of the view matrix
     */
    public static @NotNull Matrix4d view()
    {
        return GL.currentBatch.view[GL.currentBatch.stackIndex];
    }
    
    /**
     * @return A view of the model matrix
     */
    public static @NotNull Matrix4d model()
    {
        return GL.currentBatch.model[GL.currentBatch.stackIndex];
    }
    
    /**
     * @return A view of the normal matrix
     */
    public static @NotNull Matrix4d normal()
    {
        return GL.currentBatch.normal[GL.currentBatch.stackIndex];
    }
    
    /**
     * @return A view of the diffuse color
     */
    public static @NotNull Color diffuse()
    {
        return GL.currentBatch.diffuse.get(GL.currentBatch.stackIndex);
    }
    
    /**
     * @return A view of the specular color
     */
    public static @NotNull Color specular()
    {
        return GL.currentBatch.specular.get(GL.currentBatch.stackIndex);
    }
    
    /**
     * @return A view of the ambient color
     */
    public static @NotNull Color ambient()
    {
        return GL.currentBatch.ambient.get(GL.currentBatch.stackIndex);
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    private int id;
    
    private final int elementsCount; // Number of elements in the buffer (QUADS)
    
    private final Float3.Buffer pos;  // (XYZ)  (shader-location = 0)
    private final Float3.Buffer tex1; // (UVQ)  (shader-location = 1)
    private final Float3.Buffer norm; // (XYZ)  (shader-location = 2)
    private final Float3.Buffer tan;  // (XYZ)  (shader-location = 3)
    private final Byte4.Buffer  col;  // (RGBA) (shader-location = 4)
    private final Float3.Buffer tex2; // (UVQ)  (shader-location = 5)
    
    private final GLVertexArray vertexArray;
    
    private       int        currentDraw;
    private final DrawCall[] drawCalls;
    
    private double currentDepth;
    
    private boolean hasBegun;
    
    private       int         textureIndex;
    private final String[]    textureNames;
    private final GLTexture[] textureActive;
    
    private final Matrix4d mvp = new Matrix4d();
    
    private final Vector3d viewX = new Vector3d();
    private final Vector3d viewY = new Vector3d();
    private final Vector3d viewZ = new Vector3d();
    
    private int stackIndex;
    
    private final Matrix4d[] projection;
    private final Matrix4d[] view;
    private final Matrix4d[] model;
    private final Matrix4d[] normal;
    
    private final Color.Buffer diffuse;
    private final Color.Buffer specular;
    private final Color.Buffer ambient;
    
    private final BatchStats internalStats;
    private final BatchStats stats;
    
    public GLBatch()
    {
        this.id = ++GLBatch.index;
        
        this.elementsCount = GL.DEFAULT_BATCH_BUFFER_ELEMENTS;
        
        int capacity = this.elementsCount * 4;
        
        this.pos  = Float3.calloc(capacity); // 3 floats per position
        this.tex1 = Float3.calloc(capacity); // 2 floats per texcoord
        this.norm = Float3.calloc(capacity); // 3 floats per normal
        this.tan  = Float3.calloc(capacity); // 3 floats per tangent
        this.col  = Byte4.calloc(capacity);  // 4 bytes  per color
        this.tex2 = Float3.calloc(capacity); // 2 floats per texcoord2
        
        IntBuffer indices = MemoryUtil.memCallocInt(this.elementsCount * 6); // 6 int per quad (indices)
        for (int i = 0; i < this.elementsCount; ++i)
        {
            indices.put(4 * i);
            indices.put(4 * i + 1);
            indices.put(4 * i + 2);
            indices.put(4 * i);
            indices.put(4 * i + 2);
            indices.put(4 * i + 3);
        }
        
        this.vertexArray = GLVertexArray.builder()
                                        .buffer(this.pos, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.FLOAT, 3, false))
                                        .buffer(this.tex1, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.FLOAT, 3, false))
                                        .buffer(this.norm, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.FLOAT, 3, false))
                                        .buffer(this.tan, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.FLOAT, 3, false))
                                        .buffer(this.col, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.UNSIGNED_BYTE, 4, true))
                                        .buffer(this.tex2, Usage.DYNAMIC_DRAW, new GLAttribute(GLType.FLOAT, 3, false))
                                        .indexBuffer(indices.clear(), Usage.STATIC_DRAW)
                                        .build();
        MemoryUtil.memFree(indices);
        
        this.currentDraw = 0;
        this.drawCalls   = new DrawCall[GL.DEFAULT_BATCH_DRAWCALLS];
        for (int i = 0; i < this.drawCalls.length; i++) this.drawCalls[i] = new DrawCall();
        
        this.currentDepth = 0.99995;
        
        this.hasBegun = false;
        
        this.stackIndex = 0;
        
        this.projection = new Matrix4d[GL.MAX_MATRIX_STACK_SIZE];
        this.view       = new Matrix4d[GL.MAX_MATRIX_STACK_SIZE];
        this.model      = new Matrix4d[GL.MAX_MATRIX_STACK_SIZE];
        this.normal     = new Matrix4d[GL.MAX_MATRIX_STACK_SIZE];
        
        this.diffuse  = Color_RGBA.create(GL.MAX_MATRIX_STACK_SIZE);
        this.specular = Color_RGBA.create(GL.MAX_MATRIX_STACK_SIZE);
        this.ambient  = Color_RGBA.create(GL.MAX_MATRIX_STACK_SIZE);
        
        for (int i = 0; i < GL.MAX_MATRIX_STACK_SIZE; i++)
        {
            this.projection[i] = new Matrix4d();
            this.view[i]       = new Matrix4d();
            this.model[i]      = new Matrix4d();
            this.normal[i]     = new Matrix4d();
        }
        
        this.textureIndex  = 0;
        this.textureNames  = new String[GL.MAX_ACTIVE_TEXTURES];
        this.textureActive = new GLTexture[GL.MAX_ACTIVE_TEXTURES];
        
        this.internalStats = new BatchStats();
        this.stats         = new BatchStats();
        
        GLBatch.LOGGER.fine("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLBatch batch = (GLBatch) o;
        return this.id == batch.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return "GLBatch{" + "id=" + this.id + '}';
    }
    
    public void delete()
    {
        if (this != GL.defaultBatch && this.id > 0)
        {
            GLBatch.LOGGER.fine("Deleting", this);
            
            // Free vertex arrays memory from CPU (RAM)
            MemoryUtil.memFree(this.pos);
            MemoryUtil.memFree(this.tex1);
            MemoryUtil.memFree(this.norm);
            MemoryUtil.memFree(this.tan);
            MemoryUtil.memFree(this.col);
            MemoryUtil.memFree(this.tex2);
            
            this.vertexArray.delete();
            
            this.currentDraw = 0;
            for (DrawCall drawCall : this.drawCalls) drawCall.reset();
            Arrays.fill(this.drawCalls, null);
            
            this.id = 0;
        }
    }
    
    private void drawInternal()
    {
        // Check to see if the vertex array was updated.
        if (this.pos.position() > 0)
        {
            this.internalStats.vertices += this.pos.position();
            
            GLVertexArray.bind(this.vertexArray);
            
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_POSITION)).set(0, this.pos.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TEXCOORD)).set(0, this.tex1.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_NORMAL)).set(0, this.norm.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TANGENT)).set(0, this.tan.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_COLOR)).set(0, this.col.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TEXCOORD2)).set(0, this.tex2.flip());
            
            // Get the values at the stack location
            Matrix4d projection = this.projection[this.stackIndex];
            Matrix4d view       = this.view[this.stackIndex];
            Matrix4d model      = this.model[this.stackIndex];
            Matrix4d normal     = this.normal[this.stackIndex];
            
            Color diffuse  = this.diffuse.get(this.stackIndex);
            Color specular = this.specular.get(this.stackIndex);
            Color ambient  = this.ambient.get(this.stackIndex);
            
            // Create modelView-projection matrix
            this.mvp.set(projection);
            this.mvp.mul(view);
            this.mvp.mul(model);
            
            view.transformDirection(this.viewX.set(1, 0, 0));
            view.transformDirection(this.viewY.set(0, 1, 0));
            view.transformDirection(this.viewZ.set(0, 0, 1));
            
            // Upload to Shader
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_MVP, false, this.mvp);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_PROJECTION, false, projection);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_VIEW, false, view);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_MODEL, false, model);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_NORMAL, false, normal);
            GLProgram.Uniform.vec3(GLProgram.UNIFORM_VECTOR_VIEW_X, this.viewX);
            GLProgram.Uniform.vec3(GLProgram.UNIFORM_VECTOR_VIEW_Y, this.viewY);
            GLProgram.Uniform.vec3(GLProgram.UNIFORM_VECTOR_VIEW_Z, this.viewZ);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_DIFFUSE, diffuse);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_SPECULAR, specular);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_AMBIENT, ambient);
            
            // TODO - Is this needed?
            GLProgram.Uniform.int1(GLProgram.MAP_DIFFUSE, 0);
            GLProgram.Uniform.int1(GLProgram.MAP_SPECULAR, 1);
            GLProgram.Uniform.int1(GLProgram.MAP_NORMAL, 2);
            GLProgram.Uniform.int1(GLProgram.MAP_ROUGHNESS, 3);
            GLProgram.Uniform.int1(GLProgram.MAP_OCCLUSION, 4);
            GLProgram.Uniform.int1(GLProgram.MAP_EMISSION, 5);
            GLProgram.Uniform.int1(GLProgram.MAP_HEIGHT, 6);
            GLProgram.Uniform.int1(GLProgram.MAP_CUBEMAP, 7);
            GLProgram.Uniform.int1(GLProgram.MAP_IRRADIANCE, 8);
            GLProgram.Uniform.int1(GLProgram.MAP_PREFILTER, 9);
            GLProgram.Uniform.int1(GLProgram.MAP_BRDF, 10);
            
            for (int i = 0; i < this.textureIndex; i++)
            {
                GLTexture.bind(this.textureActive[i], i + 1);
                GLProgram.Uniform.int1(this.textureNames[i], i + 1);
            }
            
            for (int i = 0, offset = 0; i <= this.currentDraw; i++)
            {
                this.internalStats.draws++;
                
                DrawCall drawCall = this.drawCalls[i];
                
                GLTexture.bind(drawCall.texture);
                
                if (drawCall.mode == DrawMode.QUADS)
                {
                    this.vertexArray.drawElements(DrawMode.TRIANGLES, Integer.toUnsignedLong(offset / 4 * 6), drawCall.vertexCount / 4 * 6);
                }
                else
                {
                    this.vertexArray.draw(drawCall.mode, offset, drawCall.vertexCount);
                }
                
                offset += drawCall.vertexCount + drawCall.alignment;
            }
            
            for (int i = 0; i < this.textureIndex; i++)
            {
                GLTexture.unbind(this.textureActive[i], i + 1);
                
                this.textureNames[i]  = null;
                this.textureActive[i] = null;
            }
            this.textureIndex = 0;
            
            // Reset Vertex Array and increment buffer objects (in case of multi-buffering)
            this.pos.clear();
            this.tex1.clear();
            this.norm.clear();
            this.tan.clear();
            this.col.clear();
            this.tex2.clear();
            
            // Reset Draw Calls
            this.currentDraw = 0;
            // This doesn't need to happen because the draw call is reset when it is incremented.
            // This happens to get rid of the reference to the GLTexture
            for (DrawCall drawCall : this.drawCalls) drawCall.reset();
            
            // Reset Depth
            this.currentDepth = 0.99995;
        }
    }
    
    private void incDrawCall()
    {
        DrawCall drawCall = this.drawCalls[this.currentDraw];
        
        // Check to see if DrawCall is empty
        if (drawCall.vertexCount > 0)
        {
            // Make sure current this.draw.count is aligned a multiple of 4,
            // that way, following QUADS drawing will keep aligned with index processing
            // It implies adding some extra alignment vertex at the end of the draw,
            // those vertex are not processed but they are considered as an additional offset
            // for the next set of vertex to be drawn
            
            int offset = drawCall.vertexCount % 4;
            drawCall.alignment = offset != 0 ? 4 - offset : 0;
            
            if (drawCall.alignment > 0)
            {
                checkBuffer(drawCall.alignment);
                
                this.pos.position(this.pos.position() + drawCall.alignment);
                this.tex1.position(this.tex1.position() + drawCall.alignment);
                this.norm.position(this.norm.position() + drawCall.alignment);
                this.tan.position(this.tan.position() + drawCall.alignment);
                this.col.position(this.col.position() + drawCall.alignment);
                this.tex2.position(this.tex2.position() + drawCall.alignment);
            }
            
            if (++this.currentDraw >= this.drawCalls.length) drawInternal();
        }
    }
    
    private static final class DrawCall
    {
        private DrawMode mode;
        
        private int vertexCount;
        private int alignment;
        
        private GLTexture texture;
        
        private void reset()
        {
            this.mode = DrawMode.DEFAULT;
            
            this.vertexCount = 0;
            this.alignment   = 0;
            
            this.texture = null;
        }
    }
    
    public static final class BatchStats
    {
        private int vertices;
        private int draws;
        
        private BatchStats()
        {
            reset();
        }
        
        private void reset()
        {
            this.vertices = 0;
            this.draws    = 0;
        }
        
        private void set(@NotNull BatchStats other)
        {
            this.vertices = other.vertices;
            this.draws    = other.draws;
        }
        
        public int vertices()
        {
            return this.vertices;
        }
        
        public int draws()
        {
            return this.draws;
        }
    }
    
    public static class Vertex
    {
        public double x, y, z;
        public double u, v, q;
        public double nx, ny, nz;
        public double tx, ty, tz;
        public int r, g, b, a;
        public double u2, v2, q2;
        
        private boolean hasPos;
        private boolean hasPosZ;
        private boolean hasTexCoord;
        private boolean hasNormal;
        private boolean hasTangent;
        private boolean hasColor;
        private boolean hasTexCoord2;
        
        public @NotNull Vertex clear()
        {
            this.hasPos       = false;
            this.hasPosZ      = false;
            this.hasTexCoord  = false;
            this.hasNormal    = false;
            this.hasTangent   = false;
            this.hasColor     = false;
            this.hasTexCoord2 = false;
            return this;
        }
        
        public @NotNull Vertex pos(double x, double y, double z)
        {
            this.x       = x;
            this.y       = y;
            this.z       = z;
            this.hasPos  = true;
            this.hasPosZ = true;
            return this;
        }
        
        public @NotNull Vertex pos(double x, double y)
        {
            this.x       = x;
            this.y       = y;
            this.hasPos  = true;
            this.hasPosZ = false;
            return this;
        }
        
        public @NotNull Vertex texCoord(double u, double v, double q)
        {
            this.u           = u;
            this.v           = v;
            this.q           = q;
            this.hasTexCoord = true;
            return this;
        }
        
        public @NotNull Vertex texCoord(double u, double v)
        {
            this.u           = u;
            this.v           = v;
            this.q           = 1.0;
            this.hasTexCoord = true;
            return this;
        }
        
        public @NotNull Vertex normal(double x, double y, double z)
        {
            this.nx        = x;
            this.ny        = y;
            this.nz        = z;
            this.hasNormal = true;
            return this;
        }
        
        public @NotNull Vertex tangent(double x, double y, double z)
        {
            this.tx         = x;
            this.ty         = y;
            this.tz         = z;
            this.hasTangent = true;
            return this;
        }
        
        public @NotNull Vertex color(int r, int g, int b, int a)
        {
            this.r        = r;
            this.g        = g;
            this.b        = b;
            this.a        = a;
            this.hasColor = true;
            return this;
        }
        
        public @NotNull Vertex texCoord2(double u, double v, double q)
        {
            this.u2           = u;
            this.v2           = v;
            this.q2           = q;
            this.hasTexCoord2 = true;
            return this;
        }
        
        public @NotNull Vertex texCoord2(double u, double v)
        {
            this.u2           = u;
            this.v2           = v;
            this.q2           = 1.0;
            this.hasTexCoord2 = true;
            return this;
        }
    }
    
    public record VertexGroup(GLTexture texture, Vertex... points) {}
}
