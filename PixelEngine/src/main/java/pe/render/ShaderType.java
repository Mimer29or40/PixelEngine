package pe.render;

import org.lwjgl.opengl.GL33;

import java.util.regex.Pattern;

public enum ShaderType
{
    VERTEX(GL33.GL_VERTEX_SHADER, Pattern.compile(".*\\.(?:vert|vs)")),
    GEOMETRY(GL33.GL_GEOMETRY_SHADER, Pattern.compile(".*\\.(?:geom|gs)")),
    FRAGMENT(GL33.GL_FRAGMENT_SHADER, Pattern.compile(".*\\.(?:frag|fs)")),
    // COMPUTE(GL33.GL_COMPUTE_SHADER, Pattern.compile(".*\\.(?:comp|cs)")),
    // TESS_CONTROL(GL33.GL_TESS_CONTROL_SHADER, Pattern.compile(".*\\.(?:tesc|tc)")),
    // TESS_EVALUATION(GL33.GL_TESS_EVALUATION_SHADER, Pattern.compile(".*\\.(?:tese|te)")),
    ;
    
    public final  int     ref;
    private final Pattern pattern;
    
    ShaderType(int ref, Pattern pattern)
    {
        this.ref     = ref;
        this.pattern = pattern;
    }
    
    public static ShaderType getFromFileName(String fileName)
    {
        for (ShaderType type : ShaderType.values())
        {
            if (type.pattern.matcher(fileName).matches())
            {
                return type;
            }
        }
        throw new RuntimeException("Could not identify shader type from file's name: " + fileName);
    }
}
