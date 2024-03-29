package pe.render;

import org.lwjgl.opengl.GL33;

public enum DrawMode
{
    POINTS(GL33.GL_POINTS),
    LINE_STRIP(GL33.GL_LINE_STRIP),
    LINE_STRIP_ADJACENCY(GL33.GL_LINE_STRIP_ADJACENCY),
    LINE_LOOP(GL33.GL_LINE_LOOP),
    LINES(GL33.GL_LINES),
    LINES_ADJACENCY(GL33.GL_LINES_ADJACENCY),
    TRIANGLE_STRIP(GL33.GL_TRIANGLE_STRIP),
    TRIANGLE_STRIP_ADJACENCY(GL33.GL_TRIANGLE_STRIP_ADJACENCY),
    TRIANGLE_FAN(GL33.GL_TRIANGLE_FAN),
    TRIANGLES(GL33.GL_TRIANGLES),
    TRIANGLES_ADJACENCY(GL33.GL_TRIANGLES_ADJACENCY),
    QUADS(GL33.GL_QUADS),
    // PATCHES(GL33.GL_PATCHES),
    ;
    
    public static final DrawMode DEFAULT = TRIANGLES;
    
    public final int ref;
    
    DrawMode(int ref)
    {
        this.ref = ref;
    }
}
