package pe.engine.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.CustomBuffer;
import rutils.Logger;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GLVertexArray
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    private static final Builder BUILDER = new Builder();
    
    private static GLVertexArray current;
    
    static void setup()
    {
        GLVertexArray.LOGGER.fine("Setup");
    }
    
    static void destroy()
    {
        GLVertexArray.LOGGER.fine("Destroy");
        
        GL33.glBindVertexArray(0);
        GLVertexArray.current = null;
    }
    
    public static Builder builder()
    {
        return GLVertexArray.BUILDER.reset();
    }
    
    public static void bind(@Nullable GLVertexArray vertexArray)
    {
        if (!Objects.equals(GLVertexArray.current, vertexArray))
        {
            GLVertexArray.LOGGER.finest("Binding Vertex Array:", vertexArray);
            
            GLVertexArray.current = vertexArray;
            
            GL33.glBindVertexArray(vertexArray != null ? vertexArray.id : 0);
        }
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected final GLBufferElementArray indexBuffer;
    
    protected final List<GLBufferArray> vertexBuffers;
    protected final List<GLAttribute>   vertexAttributes;
    
    protected int vertexCount;
    
    /**
     * Creates a new GLVertexArray.
     */
    private GLVertexArray(GLBufferElementArray indexBuffer, List<GLBufferArray> vertexBuffers, List<GLAttribute[]> vertexAttributes)
    {
        this.id = GL33.glGenVertexArrays();
        
        GL33.glBindVertexArray(this.id);
        
        this.indexBuffer = indexBuffer;
        
        this.vertexBuffers    = new ArrayList<>();
        this.vertexAttributes = new ArrayList<>();
        
        for (int i = 0, n = vertexBuffers.size(); i < n; i++)
        {
            GLBufferArray buffer     = vertexBuffers.get(i);
            GLAttribute[] attributes = vertexAttributes.get(i);
            
            int stride = 0;
            for (GLAttribute attribute : attributes)
            {
                stride += attribute.size();
            }
            
            GLVertexArray.LOGGER.finest("Adding VBO %s of structure %s to", buffer, attributes, this);
            
            this.vertexCount = Math.min(this.vertexCount > 0 ? this.vertexCount : Integer.MAX_VALUE, (int) (buffer.size() / stride));
            
            GLBuffer.bind(buffer);
            this.vertexBuffers.add(buffer);
            for (int j = 0, m = attributes.length, attributeCount = attributeCount(), offset = 0; j < m; j++)
            {
                GLAttribute attribute = attributes[j];
                
                int typeInt = switch (attribute.type())
                        {
                            case BYTE -> GL33.GL_BYTE;
                            case UNSIGNED_BYTE -> GL33.GL_UNSIGNED_BYTE;
                            case SHORT -> GL33.GL_SHORT;
                            case UNSIGNED_SHORT -> GL33.GL_UNSIGNED_SHORT;
                            case INT -> GL33.GL_INT;
                            case UNSIGNED_INT -> GL33.GL_UNSIGNED_INT;
                            case FLOAT -> GL33.GL_FLOAT;
                            case DOUBLE -> GL33.GL_DOUBLE;
                        };
                
                GL33.glVertexAttribPointer(attributeCount, attribute.count(), typeInt, attribute.normalized(), stride, offset);
                GL33.glEnableVertexAttribArray(attributeCount++);
                offset += attribute.size();
                
                this.vertexAttributes.add(attribute);
            }
        }
        
        GLVertexArray.LOGGER.fine("Created", this);
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLVertexArray that = (GLVertexArray) o;
        return this.id == that.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return "GLVertexArray{" + "id=" + this.id + ", vertex=" + this.vertexAttributes + ", vertexCount=" + this.vertexCount + (indexCount() > 0 ? ", indexCount=" + indexCount() : "") + '}';
    }
    
    public String printArrays()
    {
        return "[VBOs=" + this.vertexBuffers + ", EBO=" + this.indexBuffer + ']';
    }
    
    /**
     * @return The count of elements in a vertex.
     */
    public int attributeCount()
    {
        return this.vertexAttributes.size();
    }
    
    /**
     * @return The size in bytes of a vertex.
     */
    public int vertexSize()
    {
        int size = 0;
        for (GLAttribute attribute : this.vertexAttributes) size += attribute.size();
        return size;
    }
    
    /**
     * @return The number of vertices in the vertex array.
     */
    public int vertexCount()
    {
        return this.vertexCount;
    }
    
    /**
     * @return The number of indices in the vertex array.
     */
    public int indexCount()
    {
        return this.indexBuffer != null ? (int) this.indexBuffer.size() / Integer.BYTES : 0;
    }
    
    /**
     * @return The object id.
     */
    public int id()
    {
        return this.id;
    }
    
    /**
     * Deletes the GLVertexArray and Buffers.
     */
    public void delete()
    {
        GLVertexArray.LOGGER.fine("Deleting", this);
        
        for (GLBuffer vbo : this.vertexBuffers) vbo.delete();
        this.vertexBuffers.clear();
        if (this.indexBuffer != null) this.indexBuffer.delete();
        
        int i = 0;
        for (GLAttribute ignored : this.vertexAttributes)
        {
            GL33.glDisableVertexAttribArray(i++);
        }
        this.vertexAttributes.clear();
        this.vertexCount = 0;
        
        GL33.glDeleteVertexArrays(this.id);
        
        this.id = 0;
    }
    
    //-------------------------------
    // ----- DrawCall Functions -----
    //-------------------------------
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     * <p>
     * Make sure to bind the vertex array first.
     *
     * @param mode   The primitive type.
     * @param offset The offset into the array.
     * @param count  the number of vertices to draw.
     * @return This instance for call chaining.
     */
    public GLVertexArray draw(DrawMode mode, int offset, int count)
    {
        bind(this);
        
        int modeInt = switch (mode)
                {
                    case POINTS -> GL33.GL_POINTS;
                    case LINE_STRIP -> GL33.GL_LINE_STRIP;
                    case LINE_STRIP_ADJACENCY -> GL33.GL_LINE_STRIP_ADJACENCY;
                    case LINE_LOOP -> GL33.GL_LINE_LOOP;
                    case LINES -> GL33.GL_LINES;
                    case LINES_ADJACENCY -> GL33.GL_LINES_ADJACENCY;
                    case TRIANGLE_STRIP -> GL33.GL_TRIANGLE_STRIP;
                    case TRIANGLE_STRIP_ADJACENCY -> GL33.GL_TRIANGLE_STRIP_ADJACENCY;
                    case TRIANGLE_FAN -> GL33.GL_TRIANGLE_FAN;
                    case TRIANGLES -> GL33.GL_TRIANGLES;
                    case TRIANGLES_ADJACENCY -> GL33.GL_TRIANGLES_ADJACENCY;
                    case QUADS -> throw new IllegalStateException(mode + " is not supported in a GLVertexArray");
                };
        
        if (this.indexBuffer != null)
        {
            GLVertexArray.LOGGER.finer("Drawing Elements size=%s from %s", count, this);
            
            if (indexCount() > 0) GL33.glDrawElements(modeInt, count, GL33.GL_UNSIGNED_INT, Integer.toUnsignedLong(offset));
        }
        else
        {
            GLVertexArray.LOGGER.finer("Drawing Arrays size=%s from %s", count, this);
            
            if (this.vertexCount > 0) GL33.glDrawArrays(modeInt, offset, count);
            // glDrawArraysInstanced(int mode, int first, int count, int primcount) // TODO
        }
        return this;
    }
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     * <p>
     * Make sure to bind the vertex array first.
     *
     * @param mode  The primitive type.
     * @param count The size of the buffer to draw.
     * @return This instance for call chaining.
     */
    public GLVertexArray draw(DrawMode mode, int count)
    {
        return draw(mode, 0, count);
    }
    
    /**
     * Draws the array in the specified mode. If an element buffer is available, it used it.
     * <p>
     * Make sure to bind the vertex array first.
     *
     * @param mode The primitive type.
     * @return This instance for call chaining.
     */
    public GLVertexArray draw(DrawMode mode)
    {
        return draw(mode, 0, this.indexBuffer != null ? indexCount() : this.vertexCount);
    }
    
    //-----------------------------
    // ----- Buffer Functions -----
    //-----------------------------
    
    /**
     * Gets the GLBuffer that holds the indices bound to the GLVertexArray. Can be null.
     *
     * @return The index GLBuffer
     */
    public GLBufferElementArray indexBuffer()
    {
        return this.indexBuffer;
    }
    
    /**
     * Gets the GLBuffer that has been bound to the GLVertexArray.
     *
     * @param index The index.
     * @return The GLBuffer
     */
    public GLBufferArray buffer(int index)
    {
        return this.vertexBuffers.get(index);
    }
    
    public static final class Builder
    {
        private       GLBufferElementArray indexBuffer;
        private final List<GLBufferArray>  buffers    = new ArrayList<>();
        private final List<GLAttribute[]>  attributes = new ArrayList<>();
        
        private Builder reset()
        {
            this.indexBuffer = null;
            this.buffers.clear();
            this.attributes.clear();
            return this;
        }
        
        public Builder buffer(long size, Usage usage, GLAttribute... attributes)
        {
            this.buffers.add(new GLBufferArray(size, usage));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull Buffer data, Usage usage, GLAttribute... attributes)
        {
            this.buffers.add(new GLBufferArray(data, usage));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder buffer(@NotNull CustomBuffer<?> data, Usage usage, GLAttribute... attributes)
        {
            this.buffers.add(new GLBufferArray(data, usage));
            this.attributes.add(attributes);
            return this;
        }
        
        public Builder indexBuffer(int indexCount, Usage usage)
        {
            this.indexBuffer = new GLBufferElementArray(indexCount, usage);
            return this;
        }
        
        public Builder indexBuffer(@NotNull IntBuffer data, Usage usage)
        {
            this.indexBuffer = new GLBufferElementArray(data, usage);
            return this;
        }
        
        public GLVertexArray build()
        {
            GLVertexArray vertexArray = new GLVertexArray(this.indexBuffer, this.buffers, this.attributes);
            reset();
            return vertexArray;
        }
    }
}
