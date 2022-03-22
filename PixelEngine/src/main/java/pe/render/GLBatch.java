package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.system.MemoryUtil;
import pe.color.Color;
import pe.color.Color_RGBA;
import pe.color.Colorc;
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
    
    static GLBatch defaultBatch;
    static GLBatch current;
    
    static void setup()
    {
        GLBatch.LOGGER.fine("Setup");
        
        GLBatch.defaultBatch = new GLBatch();
    }
    
    static void destroy()
    {
        GLBatch.LOGGER.fine("Destroy");
        
        GLBatch.current = null;
        
        GLBatch batch = GLBatch.defaultBatch;
        GLBatch.defaultBatch = null;
        batch.delete();
    }
    
    public static @NotNull GLBatch get()
    {
        return GLBatch.current;
    }
    
    public static void bind(@Nullable GLBatch batch)
    {
        if (batch == null) batch = GLBatch.defaultBatch;
        
        GLBatch.LOGGER.finest("Binding Batch:", batch);
        
        GLBatch.current = batch;
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
    
    private boolean hasStart;
    private boolean hasBegun;
    
    private       Matrix4d currentMatrix;
    private final Matrix4d projection = new Matrix4d();
    private final Matrix4d view       = new Matrix4d();
    private final Matrix4d model      = new Matrix4d();
    private final Matrix4d mvp        = new Matrix4d();
    private final Matrix4d normal     = new Matrix4d();
    
    private       int        matrixIndex;
    private final Matrix4d[] matrixStack;
    
    private final Vector3d viewVec = new Vector3d();
    
    private       Color currentColor;
    private final Color diffuse  = Color_RGBA.create();
    private final Color specular = Color_RGBA.create();
    private final Color ambient  = Color_RGBA.create();
    
    private       int          colorIndex;
    private final Color.Buffer colorStack;
    
    private       int         textureIndex;
    private final String[]    textureNames;
    private final GLTexture[] textureActive;
    
    private final BatchStats stats;
    
    public GLBatch()
    {
        this.id = ++GLBatch.index;
        
        this.elementsCount = GLState.DEFAULT_BATCH_BUFFER_ELEMENTS;
        
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
        this.drawCalls   = new DrawCall[GLState.DEFAULT_BATCH_DRAWCALLS];
        for (int i = 0; i < this.drawCalls.length; i++) this.drawCalls[i] = new DrawCall();
        
        this.currentDepth = 0.99995;
        
        this.hasBegun = false;
        
        this.matrixIndex = 0;
        this.matrixStack = new Matrix4d[GLState.MAX_MATRIX_STACK_SIZE];
        for (int i = 0; i < this.matrixStack.length; i++) this.matrixStack[i] = new Matrix4d();
        
        this.colorIndex = 0;
        this.colorStack = Color_RGBA.create(GLState.MAX_COLOR_STACK_SIZE);
        
        this.textureIndex  = 0;
        this.textureNames  = new String[GLState.MAX_ACTIVE_TEXTURES];
        this.textureActive = new GLTexture[GLState.MAX_ACTIVE_TEXTURES];
        
        this.stats = new BatchStats();
        
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
        if (!equals(GLBatch.defaultBatch) && this.id > 0)
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
    
    public void setTexture(@NotNull GLTexture texture)
    {
        GLBatch.LOGGER.finest("Setting Texture (%s) %s", texture, this);
        
        if (this.drawCalls[this.currentDraw].texture != texture)
        {
            incDrawCall();
            
            this.drawCalls[this.currentDraw].texture = texture;
        }
    }
    
    public void start()
    {
        if (this.hasStart) throw new IllegalStateException("Batch was not stopped: " + this);
        
        GLBatch.LOGGER.finest("Starting", this);
        
        this.hasStart = true;
        
        this.stats.reset();
    }
    
    public BatchStats stop()
    {
        if (!this.hasStart) throw new IllegalStateException("Batch was not stopped: " + this);
        
        GLBatch.LOGGER.finest("Stopping", this);
        
        this.hasStart = false;
        
        draw();
        
        return this.stats;
    }
    
    public void begin(@NotNull DrawMode mode)
    {
        if (this.hasBegun) throw new IllegalStateException("Batch was not ended: " + this);
        
        this.hasBegun = true;
        
        GLBatch.LOGGER.finest("Beginning Mode (%s): %s", mode, this);
        
        if (this.drawCalls[this.currentDraw].mode != mode)
        {
            incDrawCall();
            
            this.drawCalls[this.currentDraw].mode = mode;
        }
    }
    
    public void end()
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not begun: " + this);
        
        this.hasBegun = false;
        
        GLBatch.LOGGER.finest("Ending", this);
        
        // Make sure tex1 count match vertex count
        for (int i = 0, n = this.pos.position() - this.tex1.position(); i < n; i++) this.tex1.put(0.0, 0.0, 1.0);
        
        // Make sure norm count match vertex count
        for (int i = 0, n = this.pos.position() - this.norm.position(); i < n; i++) this.norm.put(0.0, 0.0, 1.0);
        
        // Make sure tan count match vertex count
        for (int i = 0, n = this.pos.position() - this.tan.position(); i < n; i++) this.tan.put(1.0, 0.0, 0.0);
        
        // Make sure col count match vertex count
        for (int i = 0, n = this.pos.position() - this.col.position(); i < n; i++) this.col.put(this.col.get(this.col.position() - 1));
        
        // Make sure tex2 count match vertex count
        for (int i = 0, n = this.pos.position() - this.tex2.position(); i < n; i++) this.tex2.put(0.0, 0.0, 1.0);
        
        // NOTE: Depth increment is dependant on rlOrtho(): z-near and z-far values,
        // as well as depth buffer bit-depth (16bit or 24bit or 32bit)
        // Correct increment formula would be: depthInc = (zFar - zNear)/pow(2, bits)
        this.currentDepth -= 0.00005;
    }
    
    public void pos(double x, double y, double z)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        // Verify that current vertex buffer elements limit has not been reached
        if (this.pos.position() < this.elementsCount * 4)
        {
            GLBatch.LOGGER.finest("Setting Vertex Position: [%s, %s, %s]", x, y, z);
            
            this.pos.put(x, y, z);
            
            this.drawCalls[this.currentDraw].vertexCount++;
        }
        else
        {
            GLBatch.LOGGER.severe("Vertex Element Overflow");
        }
    }
    
    public void pos(double x, double y)
    {
        pos(x, y, this.currentDepth);
    }
    
    public void texCoord(double u, double v, double q)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        GLBatch.LOGGER.finest("Setting Vertex Texture Coordinate: [%s, %s, %s]", u, v, q);
        
        this.tex1.put(u, v, q);
    }
    
    public void texCoord(double u, double v)
    {
        texCoord(u, v, 1.0);
    }
    
    public void normal(double x, double y, double z)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        GLBatch.LOGGER.finest("Setting Vertex Normal: [%s, %s, %s]", x, y, z);
        
        this.norm.put(x, y, z);
    }
    
    public void tangent(double x, double y, double z)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        GLBatch.LOGGER.finest("Setting Vertex Tangent: [%s, %s, %s]", x, y, z);
        
        this.tan.put(x, y, z);
    }
    
    public void color(int r, int g, int b, int a)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        GLBatch.LOGGER.finest("Setting Vertex Color: [%s, %s, %s, %s]", r, g, b, a);
        
        this.col.put(r, g, b, a);
    }
    
    public void texCoord2(double u, double v, double q)
    {
        if (!this.hasBegun) throw new IllegalStateException("Batch was not started: " + this);
        
        GLBatch.LOGGER.finest("Setting Vertex Texture Coordinate 2: [%s, %s, %s]", u, v, q);
        
        this.tex2.put(u, v, q);
    }
    
    public void texCoord2(double u, double v)
    {
        texCoord2(u, v, 1.0);
    }
    
    public void checkBuffer(int vertexCount)
    {
        if (this.pos.position() + vertexCount >= this.elementsCount * 4) draw();
    }
    
    private void draw()
    {
        // Check to see if the vertex array was updated.
        if (this.pos.position() > 0)
        {
            this.stats.vertices += this.pos.position();
            
            GLVertexArray.bind(this.vertexArray);
            
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_POSITION)).set(0, this.pos.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TEXCOORD)).set(0, this.tex1.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_NORMAL)).set(0, this.norm.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TANGENT)).set(0, this.tan.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_COLOR)).set(0, this.col.flip());
            this.vertexArray.buffer(GLProgram.DEFAULT_ATTRIBUTES.indexOf(GLProgram.ATTRIBUTE_TEXCOORD2)).set(0, this.tex2.flip());
            
            // Setup some default shader values
            
            // Create modelView-projection matrix and upload to shader
            this.mvp.set(this.projection);
            this.mvp.mul(this.view);
            this.mvp.mul(this.model);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_PROJECTION, false, this.projection);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_VIEW, false, this.view);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_MODEL, false, this.model);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_MVP, false, this.mvp);
            GLProgram.Uniform.mat4(GLProgram.UNIFORM_MATRIX_NORMAL, false, this.normal);
            GLProgram.Uniform.vec3(GLProgram.UNIFORM_VECTOR_VIEW, this.viewVec);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_DIFFUSE, this.diffuse);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_SPECULAR, this.specular);
            GLProgram.Uniform.color(GLProgram.UNIFORM_COLOR_AMBIENT, this.ambient);
            
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
            
            activate();
            
            for (int i = 0, offset = 0; i <= this.currentDraw; i++)
            {
                this.stats.draws++;
                
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
            
            deactivate();
            
            // Reset Batch to known state
            
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
            
            if (++this.currentDraw >= this.drawCalls.length) draw();
        }
    }
    
    /**
     * Set the current matrix mode.
     *
     * @param mode the matrix mode. One of:<ul>
     *             <li>{@link MatrixMode#PROJECTION PROJECTION}</li>
     *             <li>{@link MatrixMode#VIEW VIEW}</li>
     *             <li>{@link MatrixMode#MODEL MODEL}</li>
     *             <li>{@link MatrixMode#NORMAL NORMAL}</li>
     *             </ul>
     */
    public void matrixMode(@NotNull MatrixMode mode)
    {
        GLBatch.LOGGER.finest("Setting Matrix Mode:", mode);
        
        this.currentMatrix = switch (mode)
                {
                    case PROJECTION -> this.projection;
                    case VIEW -> this.view;
                    case MODEL -> this.model;
                    case NORMAL -> this.normal;
                };
    }
    
    /**
     * @return A read-only view of the currently selected matrix
     */
    public @NotNull Matrix4dc getMatrix()
    {
        return this.currentMatrix;
    }
    
    /**
     * Sets the current matrix to a 4 &times; 4 matrix in column-major order.
     * <p>
     * The matrix is stored as 16 consecutive values, i.e. as:
     * <table class=striped>
     * <tr><td>a1</td><td>a5</td><td>a9</td><td>a13</td></tr>
     * <tr><td>a2</td><td>a6</td><td>a10</td><td>a14</td></tr>
     * <tr><td>a3</td><td>a7</td><td>a11</td><td>a15</td></tr>
     * <tr><td>a4</td><td>a8</td><td>a12</td><td>a16</td></tr>
     * </table>
     * <p>
     * This differs from the standard row-major ordering for matrix elements.
     * If the standard ordering is used, all of the subsequent transformation
     * equations are transposed, and the columns representing vectors become
     * rows.
     *
     * @param mat matrix data
     */
    public void setMatrix(@NotNull Matrix4fc mat)
    {
        GLBatch.LOGGER.finest("Setting Matrix:", mat);
        
        this.currentMatrix.set(mat);
    }
    
    /**
     * Sets the current matrix to a 4 &times; 4 matrix in column-major order.
     * <p>
     * The matrix is stored as 16 consecutive values, i.e. as:
     * <table class=striped>
     * <tr><td>a1</td><td>a5</td><td>a9</td><td>a13</td></tr>
     * <tr><td>a2</td><td>a6</td><td>a10</td><td>a14</td></tr>
     * <tr><td>a3</td><td>a7</td><td>a11</td><td>a15</td></tr>
     * <tr><td>a4</td><td>a8</td><td>a12</td><td>a16</td></tr>
     * </table>
     * <p>
     * This differs from the standard row-major ordering for matrix elements.
     * If the standard ordering is used, all of the subsequent transformation
     * equations are transposed, and the columns representing vectors become
     * rows.
     *
     * @param mat matrix data
     */
    public void setMatrix(@NotNull Matrix4dc mat)
    {
        GLBatch.LOGGER.finest("Setting Matrix:", mat);
        
        this.currentMatrix.set(mat);
    }
    
    /**
     * Sets the current matrix to the identity matrix.
     * <p>
     * Calling this function is equivalent to calling {@link #setMatrix} with the following matrix:
     * <table class=striped>
     * <tr><td>1</td><td>0</td><td>0</td><td>0</td></tr>
     * <tr><td>0</td><td>1</td><td>0</td><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>1</td><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
     * </table>
     */
    public void loadIdentity()
    {
        GLBatch.LOGGER.finest("Setting Identity");
        
        this.currentMatrix.identity();
    }
    
    /**
     * Multiplies the current matrix with a 4 &times; 4 matrix in column-major
     * order. See {@link #setMatrix} for details.
     *
     * @param mat the matrix data
     */
    public void mulMatrix(@NotNull Matrix4fc mat)
    {
        GLBatch.LOGGER.finest("Multiplying:", mat);
        
        this.currentMatrix.mul((Matrix4f) mat);
    }
    
    /**
     * Multiplies the current matrix with a 4 &times; 4 matrix in column-major
     * order. See {@link #setMatrix} for details.
     *
     * @param mat the matrix data
     */
    public void mulMatrix(@NotNull Matrix4dc mat)
    {
        GLBatch.LOGGER.finest("Multiplying:", mat);
        
        this.currentMatrix.mul(mat);
    }
    
    /**
     * Manipulates the current matrix with a translation matrix along the x-,
     * y- and z- axes.
     * <p>
     * Calling this function is equivalent to calling
     * {@link #mulMatrix} with the following matrix:
     * <table class=striped>
     * <tr><td>1</td><td>0</td><td>0</td><td>x</td></tr>
     * <tr><td>0</td><td>1</td><td>0</td><td>y</td></tr>
     * <tr><td>0</td><td>0</td><td>1</td><td>z</td></tr>
     * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
     * </table>
     *
     * @param x the x-axis translation
     * @param y the y-axis translation
     * @param z the z-axis translation
     */
    public void translate(double x, double y, double z)
    {
        GLBatch.LOGGER.finest("Translating: (%s, %s, %s)", x, y, z);
        
        this.currentMatrix.translate(x, y, z);
    }
    
    /**
     * Manipulates the current matrix with a rotation matrix.
     * <p>
     * {@code angle} gives an angle of rotation; the coordinates of a vector v
     * are given by <code>v = (x y z)<sup>T</sup></code>. The computed
     * matrix is a counter-clockwise rotation about the line through the origin
     * with the specified axis when that axis is pointing up (i.e. the
     * right-hand rule determines the sense of the rotation angle). The matrix
     * is thus
     * <table class=striped>
     * <tr><td colspan=3 rowspan=3><b>R</b></td><td>0</td></tr>
     * <tr><td>0</td></tr>
     * <tr><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
     * </table>
     * <p>
     * Let <code>u = v / ||v|| = (x' y' z')<sup>T</sup></code>. If <b>S</b> =
     * <table class=striped>
     * <tr><td>0</td><td>-z'</td><td>y'</td></tr>
     * <tr><td>z'</td><td>0</td><td>-x'</td></tr>
     * <tr><td>-y'</td><td>x'</td><td>0</td></tr>
     * </table>
     * <p>
     * then <code><b>R</b> = uu<sup>T</sup> + cos(angle)(I - uu<sup>T</sup>) + sin(angle)<b>S</b></code>
     *
     * @param angle the angle of rotation
     * @param x     the x coordinate of the rotation vector
     * @param y     the y coordinate of the rotation vector
     * @param z     the z coordinate of the rotation vector
     */
    public void rotate(double angle, double x, double y, double z)
    {
        GLBatch.LOGGER.finest("Rotating: Angle=%s (%s, %s, %s)", angle, x, y, z);
        
        this.currentMatrix.rotate(angle, x, y, z);
    }
    
    /**
     * Manipulates the current matrix with a general scaling matrix along the
     * x-, y- and z- axes.
     * <p>
     * Calling this function is equivalent to calling
     * {@link #mulMatrix} with the following matrix:
     * <table class=striped>
     * <tr><td>x</td><td>0</td><td>0</td><td>0</td></tr>
     * <tr><td>0</td><td>y</td><td>0</td><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>z</td><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
     * </table>
     *
     * @param x the x-axis scaling factor
     * @param y the y-axis scaling factor
     * @param z the z-axis scaling factor
     */
    public void scale(double x, double y, double z)
    {
        GLBatch.LOGGER.finest("Scaling: (%s, %s, %s)", x, y, z);
        
        this.currentMatrix.scale(x, y, z);
    }
    
    /**
     * Manipulates the current matrix with a matrix that produces parallel
     * projection, in such a way that the coordinates
     * <code>(lb &ndash; n)<sup>T</sup></code> and
     * <code>(rt &ndash; n)<sup>T</sup></code> specify the points on the near
     * clipping plane that are mapped to the lower x and upper right corners
     * of the window, respectively (assuming that the eye is located at
     * <code>(0 0 0)<sup>T</sup></code>). {@code f} gives the distance from the
     * eye to the far clipping plane.
     * <p>
     * Calling this function is equivalent to calling
     * {@link #mulMatrix} with the following matrix:
     * <table class=striped>
     * <tr><td>2 / (r - l)</td><td>0</td><td>0</td><td>- (r + l) / (r - l)</td></tr>
     * <tr><td>0</td><td>2 / (t - b)</td><td>0</td><td>- (t + b) / (t - b)</td></tr>
     * <tr><td>0</td><td>0</td><td>- 2 / (f - n)</td><td>- (f + n) / (f - n)</td></tr>
     * <tr><td>0</td><td>0</td><td>0</td><td>1</td></tr>
     * </table>
     *
     * @param l the x frustum plane
     * @param r the right frustum plane
     * @param b the y frustum plane
     * @param t the y frustum plane
     * @param n the near frustum plane
     * @param f the far frustum plane
     */
    public void ortho(double l, double r, double b, double t, double n, double f)
    {
        GLBatch.LOGGER.finest("Ortho: l=%s, r=%s, b=%s, t=%s, n=%s, f=%s", l, r, b, t, n, f);
        
        this.currentMatrix.ortho(l, r, b, t, n, f);
    }
    
    /**
     * Manipulates the current matrix with a matrix that produces perspective
     * projection, in such a way that the coordinates
     * <code>(lb &ndash; n)<sup>T</sup></code> and
     * <code>(rt &ndash; n)<sup>T</sup></code> specify the points on the near
     * clipping plane that are mapped to the lower x and upper right corners
     * of the window, respectively (assuming that the eye is located at
     * <code>(0 0 0)<sup>T</sup></code>). {@code f} gives the distance from the
     * eye to the far clipping plane.
     * <p>
     * Calling this function is equivalent to calling
     * {@link #mulMatrix} with the following matrix:
     * <table class=striped>
     * <tr><td>2n / (r - l)</td><td>0</td><td>(r + l) / (r - l)</td><td>0</td></tr>
     * <tr><td>0</td><td>2n / (t - b)</td><td>(t + b) / (t - b)</td><td>0</td></tr>
     * <tr><td>0</td><td>0</td><td>- (f + n) / (f - n)</td><td>- (2fn) / (f - n)</td></tr>
     * <tr><td>0</td><td>0</td><td>-1</td><td>0</td></tr>
     * </table>
     *
     * @param l the x frustum plane
     * @param r the right frustum plane
     * @param b the y frustum plane
     * @param t the y frustum plane
     * @param n the near frustum plane
     * @param f the far frustum plane
     */
    public void frustum(double l, double r, double b, double t, double n, double f)
    {
        GLBatch.LOGGER.finest("Frustum: l=%s, r=%s, b=%s, t=%s, n=%s, f=%s", l, r, b, t, n, f);
        
        this.currentMatrix.frustum(l, r, b, t, n, f);
    }
    
    /**
     * Pushes the current matrix stack down by one, duplicating the current
     * matrix in both the y of the stack and the entry below it.
     */
    public void pushMatrix()
    {
        GLBatch.LOGGER.finest("Pushing Matrix Stack");
        
        this.matrixStack[this.matrixIndex++].set(this.currentMatrix);
    }
    
    /**
     * Pops the y entry off the current matrix stack, replacing the current
     * matrix with the matrix that was the second entry in the stack.
     */
    public void popMatrix()
    {
        GLBatch.LOGGER.finest("Popping Matrix Stack");
        
        this.currentMatrix.set(this.matrixStack[--this.matrixIndex]);
    }
    
    /**
     * Set the current matrix mode.
     *
     * @param mode the matrix mode. One of:<ul>
     *             <li>{@link ColorMode#DIFFUSE DIFFUSE}</li>
     *             <li>{@link ColorMode#SPECULAR SPECULAR}</li>
     *             <li>{@link ColorMode#AMBIENT AMBIENT}</li>
     *             </ul>
     */
    public void colorMode(@NotNull ColorMode mode)
    {
        GLBatch.LOGGER.finest("Setting Color Mode:", mode);
        
        this.currentColor = switch (mode)
                {
                    case DIFFUSE -> this.diffuse;
                    case SPECULAR -> this.specular;
                    case AMBIENT -> this.ambient;
                };
    }
    
    /**
     * @return A read-only view of the currently selected color
     */
    public @NotNull Colorc getColor()
    {
        return this.currentColor;
    }
    
    /**
     * Sets the current color to the specified color.
     *
     * @param color color data
     */
    public void setColor(@NotNull Colorc color)
    {
        GLBatch.LOGGER.finest("Setting Color:", color);
        
        this.currentColor.set(color);
    }
    
    /**
     * Sets the current color to black.
     * <p>
     * Calling this function is equivalent to calling {@link #setColor} with the following color:
     * <table class=striped>
     * <tr><td>0</td><td>0</td><td>0</td><td>255</td></tr>
     * </table>
     */
    public void loadBlack()
    {
        GLBatch.LOGGER.finest("Setting Color to Black");
        
        this.currentColor.set(0, 0, 0, 255);
    }
    
    /**
     * Sets the current color to white.
     * <p>
     * Calling this function is equivalent to calling {@link #setColor} with the following color:
     * <table class=striped>
     * <tr><td>255</td><td>255</td><td>255</td><td>255</td></tr>
     * </table>
     */
    public void loadWhite()
    {
        GLBatch.LOGGER.finest("Setting Color to Black");
        
        this.currentColor.set(255, 255, 255, 255);
    }
    
    /**
     * Pushes the current color stack down by one, duplicating the current
     * color in both the y of the stack and the entry below it.
     */
    public void pushColor()
    {
        GLBatch.LOGGER.finest("Pushing Color Stack");
        
        this.colorStack.put(this.colorIndex++, this.currentColor);
    }
    
    /**
     * Pops the y entry off the current color stack, replacing the current
     * color with the color that was the second entry in the stack.
     */
    public void popColor()
    {
        GLBatch.LOGGER.finest("Popping Color Stack");
        
        this.currentColor.set(this.colorStack.get(--this.colorIndex));
    }
    
    public void addTexture(@NotNull String name, @NotNull GLTexture texture)
    {
        GLBatch.LOGGER.finest("Adding Texture to Batch: %s=%s", name, texture);
        
        if (this.textureIndex + 1 > this.textureNames.length) throw new IllegalStateException("Active Texture Limit Exceeded: " + this.textureNames.length);
        
        this.textureNames[this.textureIndex]  = name;
        this.textureActive[this.textureIndex] = texture;
        
        this.textureIndex++;
    }
    
    private void activate()
    {
        GLBatch.LOGGER.finest("Activating Textures");
        
        for (int i = 0; i < this.textureIndex; i++)
        {
            GLTexture.bind(this.textureActive[i], i + 1);
            GLProgram.Uniform.int1(this.textureNames[i], i + 1);
        }
    }
    
    private void deactivate()
    {
        GLBatch.LOGGER.finest("Deactivating Textures");
        
        for (int i = 0; i < this.textureIndex; i++)
        {
            GLTexture.unbind(this.textureActive[i], i + 1);
            
            this.textureNames[i]  = null;
            this.textureActive[i] = null;
        }
        this.textureIndex = 0;
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
        
        public int vertices()
        {
            return this.vertices;
        }
        
        public int draws()
        {
            return this.draws;
        }
    }
}
