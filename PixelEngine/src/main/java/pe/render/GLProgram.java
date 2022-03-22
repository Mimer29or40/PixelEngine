package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import pe.color.Colorc;
import rutils.Logger;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("UnusedReturnValue")
public class GLProgram
{
    private static final Logger LOGGER = new Logger();
    
    public static final String ATTRIBUTE_POSITION  = "POSITION";
    public static final String ATTRIBUTE_TEXCOORD  = "TEXCOORD";
    public static final String ATTRIBUTE_NORMAL    = "NORMAL";
    public static final String ATTRIBUTE_TANGENT   = "TANGENT";
    public static final String ATTRIBUTE_COLOR     = "COLOR";
    public static final String ATTRIBUTE_TEXCOORD2 = "TEXCOORD2";
    
    public static final List<String> DEFAULT_ATTRIBUTES = List.of(ATTRIBUTE_POSITION,
                                                                  ATTRIBUTE_TEXCOORD,
                                                                  ATTRIBUTE_NORMAL,
                                                                  ATTRIBUTE_TANGENT,
                                                                  ATTRIBUTE_COLOR,
                                                                  ATTRIBUTE_TEXCOORD2);
    
    public static final String UNIFORM_MATRIX_PROJECTION = "MATRIX_PROJECTION";
    public static final String UNIFORM_MATRIX_VIEW       = "MATRIX_VIEW";
    public static final String UNIFORM_MATRIX_MODEL      = "MATRIX_MODEL";
    public static final String UNIFORM_MATRIX_MVP        = "MATRIX_MVP";
    public static final String UNIFORM_MATRIX_NORMAL     = "MATRIX_NORMAL";
    public static final String UNIFORM_VECTOR_VIEW       = "VECTOR_VIEW";
    public static final String UNIFORM_COLOR_DIFFUSE     = "COLOR_DIFFUSE";
    public static final String UNIFORM_COLOR_SPECULAR    = "COLOR_SPECULAR";
    public static final String UNIFORM_COLOR_AMBIENT     = "COLOR_AMBIENT";
    
    public static final List<String> DEFAULT_UNIFORMS = List.of(UNIFORM_MATRIX_PROJECTION,
                                                                UNIFORM_MATRIX_VIEW,
                                                                UNIFORM_MATRIX_MODEL,
                                                                UNIFORM_MATRIX_MVP,
                                                                UNIFORM_MATRIX_NORMAL,
                                                                UNIFORM_VECTOR_VIEW,
                                                                UNIFORM_COLOR_DIFFUSE,
                                                                UNIFORM_COLOR_SPECULAR,
                                                                UNIFORM_COLOR_AMBIENT);
    
    public static final String MAP_ALBEDO     = "texture0";
    public static final String MAP_METALNESS  = "texture1";
    public static final String MAP_NORMAL     = "texture2";
    public static final String MAP_ROUGHNESS  = "texture3";
    public static final String MAP_OCCLUSION  = "texture4";
    public static final String MAP_EMISSION   = "texture5";
    public static final String MAP_HEIGHT     = "texture6";
    public static final String MAP_CUBEMAP    = "texture7";
    public static final String MAP_IRRADIANCE = "texture8";
    public static final String MAP_PREFILTER  = "texture9";
    public static final String MAP_BRDF       = "texture10";
    
    public static final String MAP_DIFFUSE  = MAP_ALBEDO;
    public static final String MAP_SPECULAR = MAP_METALNESS;
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    static void setup()
    {
        GLProgram.LOGGER.fine("Setup");
        
        GL.defaultProgram = new GLProgram(GL.defaultVertShader, null, GL.defaultFragShader);
    }
    
    static void destroy()
    {
        GLProgram.LOGGER.fine("Destroy");
        
        GL33.glUseProgram(0);
        GL.currentProgram = null;
        
        GLProgram program = GL.defaultProgram;
        GL.defaultProgram = null;
        program.delete();
    }
    
    /**
     * Sets the current shader program.
     *
     * @param program the new shader program
     */
    public static void bind(@Nullable GLProgram program)
    {
        if (program == null) program = GL.defaultProgram;
        
        GLProgram.LOGGER.finest("Binding Program:", program);
        
        GL.currentProgram = program;
        
        GL33.glUseProgram(program.id);
    }
    
    @SuppressWarnings("unused")
    public static final class Attribute
    {
        /**
         * Sets a short attribute in the shader program.
         *
         * @param name  The attribute name.
         * @param value The value.
         */
        public static void short1(@NotNull String name, short value)
        {
            GLProgram.LOGGER.finest("%s: Setting short Attribute: %s=%s", GL.currentProgram, name, value);
            
            GL33.glVertexAttrib1s(GL.currentProgram.getAttribute(name), value);
        }
        
        /**
         * Sets an int attribute in the shader program.
         *
         * @param name  The attribute name.
         * @param value The value.
         */
        public static void int1(@NotNull String name, int value)
        {
            GLProgram.LOGGER.finest("%s: Setting int Attribute: %s=%s", GL.currentProgram, name, value);
            
            GL33.glVertexAttribI1i(GL.currentProgram.getAttribute(name), value);
        }
        
        /**
         * Sets an uint attribute in the shader program.
         *
         * @param name  The attribute name.
         * @param value The value.
         */
        public static void uint1(@NotNull String name, long value)
        {
            GLProgram.LOGGER.finest("%s: Setting uint Attribute: %s=%s", GL.currentProgram, name, value);
            
            GL33.glVertexAttribI1ui(GL.currentProgram.getAttribute(name), (int) (value & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a float attribute in the shader program.
         *
         * @param name  The attribute name.
         * @param value The value.
         */
        public static void float1(@NotNull String name, double value)
        {
            GLProgram.LOGGER.finest("%s: Setting float Attribute: %s=%s", GL.currentProgram, name, value);
            
            GL33.glVertexAttrib1f(GL.currentProgram.getAttribute(name), (float) value);
        }
        
        /**
         * Sets a svec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void svec2(@NotNull String name, short x, short y)
        {
            GLProgram.LOGGER.finest("%s: Setting svec2 Attribute: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glVertexAttrib2s(GL.currentProgram.getAttribute(name), x, y);
        }
        
        /**
         * Sets an ivec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void ivec2(@NotNull String name, int x, int y)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec2 Attribute: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glVertexAttribI2i(GL.currentProgram.getAttribute(name), x, y);
        }
        
        /**
         * Sets an uvec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void uvec2(@NotNull String name, long x, long y)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec2 Attribute: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glVertexAttribI2ui(GL.currentProgram.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void vec2(@NotNull String name, double x, double y)
        {
            GLProgram.LOGGER.finest("%s: Setting vec2 Attribute: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glVertexAttrib2f(GL.currentProgram.getAttribute(name), (float) x, (float) y);
        }
        
        /**
         * Sets an ivec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void ivec2(@NotNull String name, @NotNull Vector2ic vec)
        {
            ivec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets an uvec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void uvec2(@NotNull String name, @NotNull Vector2ic vec)
        {
            uvec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a vec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec2(@NotNull String name, @NotNull Vector2fc vec)
        {
            vec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a vec2 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec2(@NotNull String name, @NotNull Vector2dc vec)
        {
            vec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a svec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void svec3(@NotNull String name, short x, short y, short z)
        {
            GLProgram.LOGGER.finest("%s: Setting svec3 Attribute: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glVertexAttrib3s(GL.currentProgram.getAttribute(name), x, y, z);
        }
        
        /**
         * Sets an ivec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void ivec3(@NotNull String name, int x, int y, int z)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec3 Attribute: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glVertexAttribI3i(GL.currentProgram.getAttribute(name), x, y, z);
        }
        
        /**
         * Sets an uvec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void uvec3(@NotNull String name, long x, long y, long z)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec3 Attribute: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glVertexAttribI3ui(GL.currentProgram.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void vec3(@NotNull String name, double x, double y, double z)
        {
            GLProgram.LOGGER.finest("%s: Setting vec3 Attribute: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glVertexAttrib3f(GL.currentProgram.getAttribute(name), (float) x, (float) y, (float) z);
        }
        
        /**
         * Sets an ivec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void ivec3(@NotNull String name, @NotNull Vector3ic vec)
        {
            ivec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets an uvec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void uvec3(@NotNull String name, @NotNull Vector3ic vec)
        {
            uvec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets a vec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec3(@NotNull String name, @NotNull Vector3fc vec)
        {
            vec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets a vec3 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec3(@NotNull String name, @NotNull Vector3dc vec)
        {
            vec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets an ubvec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void ubvec4(@NotNull String name, int x, int y, int z, int w)
        {
            GLProgram.LOGGER.finest("%s: Setting ubvec4 Attribute: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glVertexAttrib4Nub(GL.currentProgram.getAttribute(name), (byte) (x & 0xFF), (byte) (y & 0xFF), (byte) (z & 0xFF), (byte) (w & 0xFF));
        }
        
        /**
         * Sets a svec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void svec4(@NotNull String name, short x, short y, short z, short w)
        {
            GLProgram.LOGGER.finest("%s: Setting svec4 Attribute: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glVertexAttrib4s(GL.currentProgram.getAttribute(name), x, y, z, w);
        }
        
        /**
         * Sets an ivec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void ivec4(@NotNull String name, int x, int y, int z, int w)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec4 Attribute: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glVertexAttribI4i(GL.currentProgram.getAttribute(name), x, y, z, w);
        }
        
        /**
         * Sets an uvec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void uvec4(@NotNull String name, long x, long y, long z, long w)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec4 Attribute: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glVertexAttribI4ui(GL.currentProgram.getAttribute(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void vec4(@NotNull String name, double x, double y, double z, double w)
        {
            GLProgram.LOGGER.finest("%s: Setting vec4 Attribute: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glVertexAttrib4f(GL.currentProgram.getAttribute(name), (float) x, (float) y, (float) z, (float) w);
        }
        
        /**
         * Sets an ivec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void ivec4(@NotNull String name, @NotNull Vector4ic vec)
        {
            ivec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets an uvec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void uvec4(@NotNull String name, @NotNull Vector4ic vec)
        {
            uvec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets a vec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec4(@NotNull String name, @NotNull Vector4fc vec)
        {
            vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets a vec4 attribute in the shader program.
         *
         * @param name The attribute name.
         * @param vec  The value.
         */
        public static void vec4(@NotNull String name, @NotNull Vector4dc vec)
        {
            vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
    }
    
    @SuppressWarnings("unused")
    public static final class Uniform
    {
        /**
         * Sets a bool uniform in the shader program.
         *
         * @param name  The uniform name.
         * @param value The value.
         */
        public static void bool1(@NotNull String name, boolean value)
        {
            GLProgram.LOGGER.finest("%s: Setting bool Uniform: %s=%s", GL.currentProgram, name, value);
            
            GL33.glUniform1i(GL.currentProgram.getUniform(name), value ? 1 : 0);
        }
        
        /**
         * Sets an uint uniform in the shader program.
         *
         * @param name  The uniform name.
         * @param value The value.
         */
        public static void uint1(@NotNull String name, long value)
        {
            GLProgram.LOGGER.finest("%s: Setting uint Uniform: %s=%s", GL.currentProgram, name, value);
            
            GL33.glUniform1ui(GL.currentProgram.getUniform(name), (int) (value & 0xFFFFFFFFL));
        }
        
        /**
         * Sets an int uniform in the shader program.
         *
         * @param name  The uniform name.
         * @param value The value.
         */
        public static void int1(@NotNull String name, int value)
        {
            GLProgram.LOGGER.finest("%s: Setting int Uniform: %s=%s", GL.currentProgram, name, value);
            
            GL33.glUniform1i(GL.currentProgram.getUniform(name), value);
        }
        
        /**
         * Sets a float uniform in the shader program.
         *
         * @param name  The uniform name.
         * @param value The value.
         */
        public static void float1(@NotNull String name, double value)
        {
            GLProgram.LOGGER.finest("%s: Setting float Uniform: %s=%s", GL.currentProgram, name, value);
            
            GL33.glUniform1f(GL.currentProgram.getUniform(name), (float) value);
        }
        
        /**
         * Sets a bvec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void bvec2(@NotNull String name, boolean x, boolean y)
        {
            GLProgram.LOGGER.finest("%s: Setting bvec2 Uniform: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glUniform2i(GL.currentProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0);
        }
        
        /**
         * Sets an ivec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void ivec2(@NotNull String name, int x, int y)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec2 Uniform: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glUniform2i(GL.currentProgram.getUniform(name), x, y);
        }
        
        /**
         * Sets an uvec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void uvec2(@NotNull String name, long x, long y)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec2 Uniform: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glUniform2ui(GL.currentProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         */
        public static void vec2(@NotNull String name, double x, double y)
        {
            GLProgram.LOGGER.finest("%s: Setting vec2 Uniform: %s=(%s, %s)", GL.currentProgram, name, x, y);
            
            GL33.glUniform2f(GL.currentProgram.getUniform(name), (float) x, (float) y);
        }
        
        /**
         * Sets an ivec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void ivec2(@NotNull String name, @NotNull Vector2ic vec)
        {
            ivec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets an uvec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void uvec2(@NotNull String name, @NotNull Vector2ic vec)
        {
            uvec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a vec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec2(@NotNull String name, @NotNull Vector2fc vec)
        {
            vec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a vec2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec2(@NotNull String name, @NotNull Vector2dc vec)
        {
            vec2(name, vec.x(), vec.y());
        }
        
        /**
         * Sets a bvec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void bvec3(@NotNull String name, boolean x, boolean y, boolean z)
        {
            GLProgram.LOGGER.finest("%s: Setting bvec3 Uniform: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glUniform3i(GL.currentProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0);
        }
        
        /**
         * Sets an ivec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void ivec3(@NotNull String name, int x, int y, int z)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec3 Uniform: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glUniform3i(GL.currentProgram.getUniform(name), x, y, z);
        }
        
        /**
         * Sets an uvec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void uvec3(@NotNull String name, long x, long y, long z)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec3 Uniform: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glUniform3ui(GL.currentProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         */
        public static void vec3(@NotNull String name, double x, double y, double z)
        {
            GLProgram.LOGGER.finest("%s: Setting vec3 Uniform: %s=(%s, %s, %s)", GL.currentProgram, name, x, y, z);
            
            GL33.glUniform3f(GL.currentProgram.getUniform(name), (float) x, (float) y, (float) z);
        }
        
        /**
         * Sets an ivec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void ivec3(@NotNull String name, @NotNull Vector3ic vec)
        {
            ivec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets an uvec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void uvec3(@NotNull String name, @NotNull Vector3ic vec)
        {
            uvec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets a vec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec3(@NotNull String name, @NotNull Vector3fc vec)
        {
            vec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets a vec3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec3(@NotNull String name, @NotNull Vector3dc vec)
        {
            vec3(name, vec.x(), vec.y(), vec.z());
        }
        
        /**
         * Sets a bvec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void bvec4(@NotNull String name, boolean x, boolean y, boolean z, boolean w)
        {
            GLProgram.LOGGER.finest("%s: Setting bvec4 Uniform: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glUniform4i(GL.currentProgram.getUniform(name), x ? 1 : 0, y ? 1 : 0, z ? 1 : 0, w ? 1 : 0);
        }
        
        /**
         * Sets an ivec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void ivec4(@NotNull String name, int x, int y, int z, int w)
        {
            GLProgram.LOGGER.finest("%s: Setting ivec4 Uniform: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glUniform4i(GL.currentProgram.getUniform(name), x, y, z, w);
        }
        
        /**
         * Sets an uvec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void uvec4(@NotNull String name, long x, long y, long z, long w)
        {
            GLProgram.LOGGER.finest("%s: Setting uvec4 Uniform: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glUniform4ui(GL.currentProgram.getUniform(name), (int) (x & 0xFFFFFFFFL), (int) (y & 0xFFFFFFFFL), (int) (z & 0xFFFFFFFFL), (int) (w & 0xFFFFFFFFL));
        }
        
        /**
         * Sets a vec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param x    The x value.
         * @param y    The y value.
         * @param z    The z value.
         * @param w    The w value.
         */
        public static void vec4(@NotNull String name, double x, double y, double z, double w)
        {
            GLProgram.LOGGER.finest("%s: Setting vec3 Uniform: %s=(%s, %s, %s, %s)", GL.currentProgram, name, x, y, z, w);
            
            GL33.glUniform4f(GL.currentProgram.getUniform(name), (float) x, (float) y, (float) z, (float) w);
        }
        
        /**
         * Sets an ivec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void ivec4(@NotNull String name, @NotNull Vector4ic vec)
        {
            ivec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets an uvec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void uvec4(@NotNull String name, @NotNull Vector4ic vec)
        {
            uvec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets a vec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec4(@NotNull String name, @NotNull Vector4fc vec)
        {
            vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets a vec4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param vec  The value.
         */
        public static void vec4(@NotNull String name, @NotNull Vector4dc vec)
        {
            vec4(name, vec.x(), vec.y(), vec.z(), vec.w());
        }
        
        /**
         * Sets a mat2 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat2(@NotNull String name, boolean transpose, @NotNull Matrix2fc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat2 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix2fv(GL.currentProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(4)));
            }
        }
        
        /**
         * Sets a mat2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat2(@NotNull String name, @NotNull Matrix2fc mat)
        {
            mat2(name, false, mat);
        }
        
        /**
         * Sets a mat2 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat2(@NotNull String name, boolean transpose, @NotNull Matrix2dc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat2 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix2fv(GL.currentProgram.getUniform(name), transpose, stack.floats((float) mat.m00(), (float) mat.m01(), (float) mat.m10(), (float) mat.m11()));
            }
        }
        
        /**
         * Sets a mat2 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat2(@NotNull String name, @NotNull Matrix2dc mat)
        {
            mat2(name, false, mat);
        }
        
        /**
         * Sets a mat3 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat3(@NotNull String name, boolean transpose, @NotNull Matrix3fc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat3 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix3fv(GL.currentProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
            }
        }
        
        /**
         * Sets a mat3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat3(@NotNull String name, @NotNull Matrix3fc mat)
        {
            mat3(name, false, mat);
        }
        
        /**
         * Sets a mat3 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat3(@NotNull String name, boolean transpose, @NotNull Matrix3dc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat3 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix3fv(GL.currentProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(9)));
            }
        }
        
        /**
         * Sets a mat3 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat3(@NotNull String name, @NotNull Matrix3dc mat)
        {
            mat3(name, false, mat);
        }
        
        /**
         * Sets a mat4 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat4(@NotNull String name, boolean transpose, @NotNull Matrix4fc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat4 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix4fv(GL.currentProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
            }
        }
        
        /**
         * Sets a mat4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat4(@NotNull String name, @NotNull Matrix4fc mat)
        {
            mat4(name, false, mat);
        }
        
        /**
         * Sets a mat4 uniform in the shader program.
         *
         * @param name      The uniform name.
         * @param transpose If the matrix is transposed
         * @param mat       The matrix value.
         */
        public static void mat4(@NotNull String name, boolean transpose, @NotNull Matrix4dc mat)
        {
            GLProgram.LOGGER.finest("%s: Setting mat4 Uniform: %s=%n%s", GL.currentProgram, name, mat);
            
            try (MemoryStack stack = MemoryStack.stackPush())
            {
                GL33.glUniformMatrix4fv(GL.currentProgram.getUniform(name), transpose, mat.get(stack.mallocFloat(16)));
            }
        }
        
        /**
         * Sets a mat4 uniform in the shader program.
         *
         * @param name The uniform name.
         * @param mat  The matrix value.
         */
        public static void mat4(@NotNull String name, @NotNull Matrix4dc mat)
        {
            mat4(name, false, mat);
        }
        
        /**
         * Sets a vec4 uniform that represents a color in the shader program.
         *
         * @param name  The uniform name.
         * @param color The color value.
         */
        public static void color(@NotNull String name, @NotNull Colorc color)
        {
            GLProgram.LOGGER.finest("%s: Setting Color (vec4) Uniform: %s=%s", GL.currentProgram, name, color);
            
            GL33.glUniform4f(GL.currentProgram.getUniform(name), color.rf(), color.gf(), color.bf(), color.af());
        }
    }
    
    /**
     * Loads a shader program with the provided shaders. If the provided
     * shaders are the same as the default shaders, then the default shader
     * program is returned.
     * <p>
     * All shaders loaded this way with have default attributes bound to the
     * first indexes.
     *
     * @param vert The vertex shader
     * @param geom The geometry shader
     * @param frag The fragment shader
     * @return The new shader program, or the default shader program
     */
    public static @NotNull GLProgram load(@Nullable GLShader vert, @Nullable GLShader geom, @Nullable GLShader frag)
    {
        if (vert == null) vert = GL.defaultVertShader;
        if (frag == null) frag = GL.defaultFragShader;
        
        if (vert == GL.defaultVertShader && frag == GL.defaultFragShader)
        {
            return GL.defaultProgram;
        }
        
        return new GLProgram(vert, geom, frag);
    }
    
    /**
     * Loads a shader program with the provided shaders code strings. If the
     * code strings are null, then the default shader is used. If all code
     * strings are null, then the default shader program is returned.
     * <p>
     * All shaders loaded this way with have default attributes bound to the
     * first indexes.
     *
     * @param vertCode The vertex shader code string, or null
     * @param geomCode The geometry shader code string, or null
     * @param fragCode The fragment shader code string, or null
     * @return The new shader program, or the default shader program
     */
    public static @NotNull GLProgram loadFromCode(@Nullable String vertCode, @Nullable String geomCode, @Nullable String fragCode)
    {
        GLShader vertShader = vertCode != null ? GLShader.loadFromCode(ShaderType.VERTEX, vertCode) : null;
        GLShader geomShader = geomCode != null ? GLShader.loadFromCode(ShaderType.GEOMETRY, geomCode) : null;
        GLShader fragShader = fragCode != null ? GLShader.loadFromCode(ShaderType.FRAGMENT, fragCode) : null;
        
        return load(vertShader, geomShader, fragShader);
    }
    
    /**
     * Loads a shader program with the provided shaders files. If the
     * file are null, or could not be read, then the default shader is used. If
     * all files are null, or could not be read, then the default shader
     * program is returned.
     * <p>
     * All shaders loaded this way with have default attributes bound to the
     * first indexes.
     *
     * @param vertFile The vertex shader file, or null
     * @param geomFile The geometry shader file, or null
     * @param fragFile The fragment shader file, or null
     * @return The new shader program, or the default shader program
     */
    public static @NotNull GLProgram loadFromFile(@Nullable String vertFile, @Nullable String geomFile, @Nullable String fragFile)
    {
        GLShader vertShader = vertFile != null ? GLShader.loadFromFile(vertFile) : null;
        GLShader geomShader = geomFile != null ? GLShader.loadFromFile(geomFile) : null;
        GLShader fragShader = fragFile != null ? GLShader.loadFromFile(fragFile) : null;
        
        return load(vertShader, geomShader, fragShader);
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected int id;
    
    protected final Map<ShaderType, GLShader> shaders = new HashMap<>();
    
    protected final Map<String, Integer> attributes = new HashMap<>();
    protected final Map<String, Integer> uniforms   = new HashMap<>();
    
    protected GLProgram(@NotNull GLShader vert, @Nullable GLShader geom, @NotNull GLShader frag)
    {
        this.id = GL33.glCreateProgram();
        
        GL33.glAttachShader(this.id, vert.id());
        if (geom != null) GL33.glAttachShader(this.id, geom.id());
        GL33.glAttachShader(this.id, frag.id());
        
        this.shaders.put(vert.type(), vert);
        if (geom != null) this.shaders.put(geom.type(), geom);
        this.shaders.put(frag.type(), frag);
        
        // NOTE: Default attribute program locations must be bound before linking
        for (int i = 0, n = GLProgram.DEFAULT_ATTRIBUTES.size(); i < n; i++)
        {
            String name = GLProgram.DEFAULT_ATTRIBUTES.get(i);
            
            GLProgram.LOGGER.finest("Binding Default Attribute (%s) at Location (%s) for %s", name, i, this);
            
            GL33.glBindAttribLocation(this.id, i, name);
        }
        
        GL33.glLinkProgram(this.id);
        if (GL33.glGetProgrami(this.id, GL33.GL_LINK_STATUS) != GL33.GL_TRUE) throw new IllegalStateException("Link failure: " + this + '\n' + GL33.glGetProgramInfoLog(this.id));
        
        GL33.glValidateProgram(this.id);
        if (GL33.glGetProgrami(this.id, GL33.GL_VALIDATE_STATUS) != GL33.GL_TRUE) throw new IllegalStateException("Validation failure: " + this + '\n' + GL33.glGetProgramInfoLog(this.id));
        
        GLProgram.LOGGER.fine("Created", this);
        
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            String    name;
            IntBuffer size = stack.callocInt(1);
            IntBuffer type = stack.callocInt(1);
            
            for (int i = 0, n = GL33.glGetProgrami(this.id, GL33.GL_ACTIVE_ATTRIBUTES); i < n; i++)
            {
                name = GL33.glGetActiveAttrib(this.id, i, size, type);
                
                getAttribute(name);
            }
            
            for (int i = 0, n = GL33.glGetProgrami(this.id, GL33.GL_ACTIVE_UNIFORMS); i < n; i++)
            {
                name = GL33.glGetActiveUniform(this.id, i, size, type);
                
                getUniform(name);
            }
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLProgram program = (GLProgram) o;
        return this.id == program.id;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id);
    }
    
    @Override
    public String toString()
    {
        return "GLProgram{" + "id=" + this.id + '}';
    }
    
    /**
     * @return The shader program handle
     */
    public int id()
    {
        return this.id;
    }
    
    /**
     * Unload this shader program from VRAM (GPU)
     * <p>
     * NOTE: When the application is shutdown, shader programs are
     * automatically unloaded.
     */
    public void delete()
    {
        if (!equals(GL.defaultProgram) && this.id > 0)
        {
            GLProgram.LOGGER.fine("Deleting", this);
            
            for (GLShader shader : this.shaders.values())
            {
                GL33.glDetachShader(this.id, shader.id());
                shader.delete();
            }
            
            GL33.glDeleteProgram(this.id);
            
            this.id = 0;
            
            this.shaders.clear();
            
            this.uniforms.clear();
            this.attributes.clear();
        }
    }
    
    private int _getAttribute(String attribute)
    {
        int location = GL33.glGetAttribLocation(this.id, attribute);
        if (location == -1)
        {
            GLProgram.LOGGER.warning("Failed to find Attribute (%s) for %s", attribute, this);
        }
        else
        {
            GLProgram.LOGGER.finer("Attribute (%s) Set at Location (%s) for %s", attribute, location, this);
        }
        return location;
    }
    
    /**
     * Gets the location of the specified attribute, or -1 if the attribute is
     * not present.
     *
     * @param attribute The specified attribute name
     * @return The attribute location, or -1
     */
    public int getAttribute(@NotNull String attribute)
    {
        return this.attributes.computeIfAbsent(attribute, this::_getAttribute);
    }
    
    private int _getUniform(String uniform)
    {
        int location = GL33.glGetUniformLocation(this.id, uniform);
        if (location == -1)
        {
            GLProgram.LOGGER.warning("Failed to find Uniform (%s) for %s", uniform, this);
        }
        else
        {
            GLProgram.LOGGER.finer("Uniform (%s) Set at Location (%s) for %s", uniform, location, this);
        }
        return location;
    }
    
    /**
     * Gets the location of the specified uniform, or -1 if the uniform is not
     * present.
     *
     * @param uniform The specified uniform name
     * @return The uniform location, or -1
     */
    public int getUniform(@NotNull String uniform)
    {
        return this.uniforms.computeIfAbsent(uniform, this::_getUniform);
    }
}
