package pe.render;

import java.util.regex.Pattern;

public enum ShaderType
{
    VERTEX(Pattern.compile(".*\\.(?:vert|vs)")),
    GEOMETRY(Pattern.compile(".*\\.(?:geom|gs)")),
    FRAGMENT(Pattern.compile(".*\\.(?:frag|fs)")),
    // COMPUTE(Pattern.compile(".*\\.(?:comp|cs)")),
    // TESS_CONTROL(Pattern.compile(".*\\.(?:tesc|tc)")),
    // TESS_EVALUATION(Pattern.compile(".*\\.(?:tese|te)")),
    ;
    
    private final Pattern pattern;
    
    ShaderType(Pattern pattern)
    {
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
