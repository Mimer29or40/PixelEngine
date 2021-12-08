package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import rutils.IOUtil;
import rutils.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class GLShader
{
    private static final Logger LOGGER = new Logger();
    
    // ------------------
    // ----- Static -----
    // ------------------
    
    static GLShader defaultVert;
    static GLShader defaultFrag;
    
    static void setup()
    {
        GLShader.LOGGER.fine("Setup");
        
        String vertCode =
                """
                #version 330
                in vec3 POSITION;
                in vec2 TEXCOORD;
                in vec4 COLOR;
                out vec2 fragTexCoord;
                out vec4 fragColor;
                uniform mat4 MATRIX_MVP;
                void main()
                {
                    fragTexCoord = TEXCOORD;
                    fragColor = COLOR;
                    gl_Position = MATRIX_MVP * vec4(POSITION, 1.0);
                }
                """;
        String fragCode =
                """
                #version 330
                in vec2 fragTexCoord;
                in vec4 fragColor;
                out vec4 finalColor;
                uniform sampler2D texture0;
                void main()
                {
                    vec4 texelColor = texture(texture0, fragTexCoord);
                    finalColor = texelColor * fragColor;
                }
                """;
        
        GLShader.defaultVert = new GLShader(ShaderType.VERTEX, vertCode);
        GLShader.defaultFrag = new GLShader(ShaderType.FRAGMENT, fragCode);
    }
    
    static void destroy()
    {
        GLShader.LOGGER.fine("Destroy");
        
        GLShader vert = GLShader.defaultVert;
        GLShader.defaultVert = null;
        vert.delete();
        
        GLShader frag = GLShader.defaultFrag;
        GLShader.defaultFrag = null;
        frag.delete();
    }
    
    public static @NotNull GLShader getDefaultVert()
    {
        return GLShader.defaultVert;
    }
    
    public static @NotNull GLShader getDefaultFrag()
    {
        return GLShader.defaultFrag;
    }
    
    /**
     * Loads a shader from a code string with a specified {@code type}.
     *
     * @param type The shader type. One of: <ul>
     *             <li>{@link ShaderType#VERTEX VERTEX}</li>
     *             <li>{@link ShaderType#GEOMETRY GEOMETRY}</li>
     *             <li>{@link ShaderType#FRAGMENT FRAGMENT}</li>
     *             </ul>
     * @param code The path to the shader file
     * @return The new GLShader object or null if it could not be loaded
     */
    public static @NotNull GLShader loadFromCode(@NotNull ShaderType type, @NotNull String code)
    {
        return new GLShader(type, code);
    }
    
    /**
     * Loads a shader from a specified file path. The {@code type} of the
     * shader is determined by the file extension.
     *
     * @param filePath The path to the shader file
     * @return The new GLShader object or null if it could not be loaded
     */
    public static @Nullable GLShader loadFromFile(@NotNull String filePath)
    {
        try
        {
            ShaderType type = ShaderType.getFromFileName(filePath);
            String     code = Files.readString(IOUtil.getPath(filePath));
            
            return loadFromCode(type, code);
        }
        catch (IOException e)
        {
            GLShader.LOGGER.warning("Could not read from file: " + filePath);
        }
        return null;
    }
    
    // --------------------
    // ----- Instance -----
    // --------------------
    
    protected       int        id;
    protected final ShaderType type;
    
    /**
     * Creates and compiles shader.
     *
     * @param type The shader type
     * @param code The code of the shader
     * @throws IllegalStateException Program could not be Compiled.
     */
    private GLShader(@NotNull ShaderType type, @NotNull String code)
    {
        this.id   = GL33.glCreateShader(type.ref);
        this.type = type;
        
        GL33.glShaderSource(this.id, code);
        GL33.glCompileShader(this.id);
        
        if (GL33.glGetShaderi(this.id, GL33.GL_COMPILE_STATUS) == GL33.GL_TRUE)
        {
            GLShader.LOGGER.fine("Created", this);
        }
        else
        {
            throw new IllegalStateException("Failed to Compile: " + this + '\n' + GL33.glGetShaderInfoLog(this.id));
        }
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GLShader shader = (GLShader) o;
        return this.id == shader.id && this.type == shader.type;
    }
    
    @Override
    public int hashCode()
    {
        return Objects.hash(this.id, this.type);
    }
    
    @Override
    public String toString()
    {
        return "GLShader{" + "id=" + this.id + ", type=" + this.type + '}';
    }
    
    /**
     * @return The shader handle
     */
    public int id()
    {
        return id;
    }
    
    /**
     * @return The shader type
     */
    public @NotNull ShaderType type()
    {
        return type;
    }
    
    /**
     * Unload this shader program from VRAM (GPU)
     * <p>
     * NOTE: When the application is shutdown, shader programs are
     * automatically unloaded.
     */
    public void delete()
    {
        if (!(equals(GLShader.defaultVert) || equals(GLShader.defaultFrag)) && this.id > 0)
        {
            GLShader.LOGGER.fine("Deleting", this);
            
            GL33.glDeleteShader(this.id);
            
            this.id = 0;
        }
    }
}
