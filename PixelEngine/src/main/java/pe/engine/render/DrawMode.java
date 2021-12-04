package pe.engine.render;

public enum DrawMode
{
    POINTS,
    LINE_STRIP,
    LINE_STRIP_ADJACENCY,
    LINE_LOOP,
    LINES,
    LINES_ADJACENCY,
    TRIANGLE_STRIP,
    TRIANGLE_STRIP_ADJACENCY,
    TRIANGLE_FAN,
    TRIANGLES,
    TRIANGLES_ADJACENCY,
    // PATCHES,
    QUADS,
    ;
    
    public static final DrawMode DEFAULT = QUADS;
}
