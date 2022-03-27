package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
import pe.color.BlendMode;
import pe.color.Color;
import pe.color.ColorFormat;
import pe.color.Color_RGBA;
import rutils.Logger;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class GL // TODO - Instance GL for multiple contexts
{
    // TODO - Move these to another class
    
    public static final int DEFAULT_BATCH_BUFFER_ELEMENTS = 8192; // Default internal render batch limits
    
    public static final int DEFAULT_BATCH_BUFFERS   = 1;   // Default number of batch buffers (multi-buffering)
    public static final int DEFAULT_BATCH_DRAWCALLS = 256; // Default number of batch draw calls (by state changes: mode, texture)
    public static final int MAX_ACTIVE_TEXTURES     = 16;  // Maximum number of additional textures that can be activated on batch drawing (SetShaderValueTexture())
    
    public static final int STATE_STACK_SIZE        = 32; // Initial size of GL Property stack
    public static final int BATCH_STACK_SIZE        = 32; // Initial size of Batch Property stack
    public static final int MAX_MESH_VERTEX_BUFFERS = 7;  // Maximum vertex buffers (VBO) per mesh
    public static final int MAX_SHADER_LOCATIONS    = 32; // Maximum number of shader locations supported
    public static final int MAX_MATERIAL_MAPS       = 12; // Maximum number of shader maps supported
    
    public static final float DEFAULT_CULL_DISTANCE_NEAR = 0.01f;  // Default projection matrix near cull distance
    public static final float DEFAULT_CULL_DISTANCE_FAR  = 1000.0f; // Default projection matrix far cull distance
    
    private static final Logger LOGGER = new Logger();
    
    static GLShader defaultVertShader;
    static GLShader defaultFragShader;
    
    static GLProgram defaultProgram;
    static GLProgram currentProgram;
    
    static GLTexture defaultTexture;
    
    static GLFramebuffer defaultFramebuffer;
    static GLFramebuffer currentFramebuffer;
    
    static GLBatch defaultBatch;
    static GLBatch currentBatch;
    
    static final ScissorMode scissorModeCustom = new ScissorMode(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    static int stackIndex;
    
    static boolean[] depthClamp;
    static boolean[] lineSmooth;
    static boolean[] textureCubeMapSeamless;
    
    static boolean[] wireframe;
    
    static BlendMode[]   blendMode;
    static DepthMode[]   depthMode;
    static StencilMode[] stencilMode;
    static ScissorMode[] scissorMode;
    
    static boolean[][] colorMask;
    static boolean[]   depthMask;
    static int[]       stencilMask;
    
    static double[][] clearColor;
    static double[]   clearDepth;
    static int[]      clearStencil;
    
    static CullFace[] cullFace;
    static Winding[]  winding;
    
    public static void setup()
    {
        GL.LOGGER.fine("Setup");
        
        clearScreenBuffers(ScreenBuffer.COLOR, ScreenBuffer.DEPTH, ScreenBuffer.STENCIL);
        
        GLShader.setup();
        GLProgram.setup();
        GLProgram.bind(null);
        
        GLTexture.setup();
        GLTexture.bind(null);
        
        GLBuffer.setup();
        GLBuffer.bind((GLBufferArray) null);
        GLBuffer.bind((GLBufferElementArray) null);
        GLBuffer.bind((GLBufferUniform) null);
        
        GLVertexArray.setup();
        GLVertexArray.bind(null);
        
        GLFramebuffer.setup();
        GLFramebuffer.bind(null);
        
        GLBatch.setup();
        GLBatch.bind(null);
        
        // TODO - Setup Uniform Buffer for default things
        // TODO - Setup Default Uniform values
        
        GL.stackIndex = 0;
        
        GL.depthClamp             = new boolean[GL.STATE_STACK_SIZE];
        GL.lineSmooth             = new boolean[GL.STATE_STACK_SIZE];
        GL.textureCubeMapSeamless = new boolean[GL.STATE_STACK_SIZE];
        
        GL.wireframe = new boolean[GL.STATE_STACK_SIZE];
        
        GL.blendMode   = new BlendMode[GL.STATE_STACK_SIZE];
        GL.depthMode   = new DepthMode[GL.STATE_STACK_SIZE];
        GL.stencilMode = new StencilMode[GL.STATE_STACK_SIZE];
        GL.scissorMode = new ScissorMode[GL.STATE_STACK_SIZE];
        
        GL.colorMask   = new boolean[GL.STATE_STACK_SIZE][4];
        GL.depthMask   = new boolean[GL.STATE_STACK_SIZE];
        GL.stencilMask = new int[GL.STATE_STACK_SIZE];
        
        GL.clearColor   = new double[GL.STATE_STACK_SIZE][4];
        GL.clearDepth   = new double[GL.STATE_STACK_SIZE];
        GL.clearStencil = new int[GL.STATE_STACK_SIZE];
        
        GL.cullFace = new CullFace[GL.STATE_STACK_SIZE];
        GL.winding  = new Winding[GL.STATE_STACK_SIZE];
        
        GL.defaultState();
    }
    
    public static void destroy()
    {
        GL.LOGGER.fine("Destroy");
        
        GLBatch.destroy();
        
        GLFramebuffer.destroy();
        
        GLVertexArray.destroy();
        
        GLBuffer.destroy();
        
        GLTexture.destroy();
        
        GLProgram.destroy();
        GLShader.destroy();
    }
    
    public static void defaultState()
    {
        GL.LOGGER.finest("Setting Default State");
        
        GL33.glPixelStorei(GL33.GL_PACK_ALIGNMENT, 1);
        GL33.glPixelStorei(GL33.GL_UNPACK_ALIGNMENT, 1);
        
        depthClamp(true);
        lineSmooth(false);
        textureCubeMapSeamless(true);
        
        wireframe(false);
        
        blendMode(BlendMode.DEFAULT);
        depthMode(DepthMode.DEFAULT);
        stencilMode(StencilMode.DEFAULT);
        scissorMode(ScissorMode.DEFAULT);
        
        colorMask(true, true, true, true);
        depthMask(true);
        stencilMask(0xFF);
        
        clearColor(0.0, 0.0, 0.0, 1.0);
        clearDepth(1.0);
        clearStencil(0x00);
        
        cullFace(CullFace.DEFAULT);
        winding(Winding.DEFAULT);
    }
    
    public static void pushState()
    {
        GL.LOGGER.finest("Pushing State Stack");
        
        boolean depthClamp             = GL.depthClamp[GL.stackIndex];
        boolean lineSmooth             = GL.lineSmooth[GL.stackIndex];
        boolean textureCubeMapSeamless = GL.textureCubeMapSeamless[GL.stackIndex];
        
        boolean wireframe = GL.wireframe[GL.stackIndex];
        
        BlendMode   blendMode   = GL.blendMode[GL.stackIndex];
        DepthMode   depthMode   = GL.depthMode[GL.stackIndex];
        StencilMode stencilMode = GL.stencilMode[GL.stackIndex];
        ScissorMode scissorMode = GL.scissorMode[GL.stackIndex];
        
        boolean[] colorMask   = GL.colorMask[GL.stackIndex];
        boolean   depthMask   = GL.depthMask[GL.stackIndex];
        int       stencilMask = GL.stencilMask[GL.stackIndex];
        
        double[] clearColor   = GL.clearColor[GL.stackIndex];
        double   clearDepth   = GL.clearDepth[GL.stackIndex];
        int      clearStencil = GL.clearStencil[GL.stackIndex];
        
        CullFace cullFace = GL.cullFace[GL.stackIndex];
        Winding  winding  = GL.winding[GL.stackIndex];
        
        GL.stackIndex++;
        
        GL.depthClamp[GL.stackIndex]             = depthClamp;
        GL.lineSmooth[GL.stackIndex]             = lineSmooth;
        GL.textureCubeMapSeamless[GL.stackIndex] = textureCubeMapSeamless;
        
        GL.wireframe[GL.stackIndex] = wireframe;
        
        GL.blendMode[GL.stackIndex]   = blendMode;
        GL.depthMode[GL.stackIndex]   = depthMode;
        GL.stencilMode[GL.stackIndex] = stencilMode;
        GL.scissorMode[GL.stackIndex] = scissorMode;
        
        GL.colorMask[GL.stackIndex][0] = colorMask[0];
        GL.colorMask[GL.stackIndex][1] = colorMask[1];
        GL.colorMask[GL.stackIndex][2] = colorMask[2];
        GL.colorMask[GL.stackIndex][3] = colorMask[3];
        GL.depthMask[GL.stackIndex]    = depthMask;
        GL.stencilMask[GL.stackIndex]  = stencilMask;
        
        GL.clearColor[GL.stackIndex][0] = clearColor[0];
        GL.clearColor[GL.stackIndex][1] = clearColor[1];
        GL.clearColor[GL.stackIndex][2] = clearColor[2];
        GL.clearColor[GL.stackIndex][3] = clearColor[3];
        GL.clearDepth[GL.stackIndex]    = clearDepth;
        GL.clearStencil[GL.stackIndex]  = clearStencil;
        
        GL.cullFace[GL.stackIndex] = cullFace;
        GL.winding[GL.stackIndex]  = winding;
    }
    
    public static void popState()
    {
        GL.LOGGER.finest("Popping State Stack");
        
        GL.stackIndex++;
        
        depthClamp(GL.depthClamp[GL.stackIndex]);
        lineSmooth(GL.lineSmooth[GL.stackIndex]);
        textureCubeMapSeamless(GL.textureCubeMapSeamless[GL.stackIndex]);
        
        wireframe(GL.wireframe[GL.stackIndex]);
        
        blendMode(GL.blendMode[GL.stackIndex]);
        depthMode(GL.depthMode[GL.stackIndex]);
        stencilMode(GL.stencilMode[GL.stackIndex]);
        scissorMode(GL.scissorMode[GL.stackIndex]);
        
        colorMask(GL.colorMask[GL.stackIndex][0],
                  GL.colorMask[GL.stackIndex][1],
                  GL.colorMask[GL.stackIndex][2],
                  GL.colorMask[GL.stackIndex][3]);
        depthMask(GL.depthMask[GL.stackIndex]);
        stencilMask(GL.stencilMask[GL.stackIndex]);
        
        clearColor(GL.clearColor[GL.stackIndex][0],
                   GL.clearColor[GL.stackIndex][1],
                   GL.clearColor[GL.stackIndex][2],
                   GL.clearColor[GL.stackIndex][3]);
        clearDepth(GL.clearDepth[GL.stackIndex]);
        clearStencil(GL.clearStencil[GL.stackIndex]);
        
        cullFace(GL.cullFace[GL.stackIndex]);
        winding(GL.winding[GL.stackIndex]);
    }
    
    public static @NotNull GLShader defaultVertShader()
    {
        return GL.defaultVertShader;
    }
    
    public static @NotNull GLShader defaultFragShader()
    {
        return GL.defaultFragShader;
    }
    
    public static @NotNull GLProgram defaultProgram()
    {
        return GL.defaultProgram;
    }
    
    public static @NotNull GLTexture defaultTexture()
    {
        return GL.defaultTexture;
    }
    
    public static @NotNull GLBatch currentBatch()
    {
        return GL.currentBatch;
    }
    
    /**
     * Specifies the viewport transformation parameters for all viewports.
     *
     * @param x the left viewport coordinate
     * @param y the bottom viewport coordinate
     * @param w the viewport width
     * @param h the viewport height
     */
    public static void viewport(int x, int y, int w, int h)
    {
        GL.LOGGER.finest("Setting Viewport: [%s, %s, %s, %s]", x, y, w, h);
        
        GL33.glViewport(x, y, w, h);
    }
    
    /**
     * If enabled, the -wc≤zc≤wc plane equation is ignored by view volume
     * clipping (effectively, there is not near or far plane clipping)
     *
     * @param depthClamp the new depth clamp flag
     */
    public static void depthClamp(boolean depthClamp)
    {
        GL.LOGGER.finest("Setting Depth Clamp Flag:", depthClamp);
        
        if (GL.depthClamp[GL.stackIndex] != depthClamp)
        {
            GL.depthClamp[GL.stackIndex] = depthClamp;
            
            if (depthClamp)
            {
                GL33.glEnable(GL33.GL_DEPTH_CLAMP);
            }
            else
            {
                GL33.glDisable(GL33.GL_DEPTH_CLAMP);
            }
        }
    }
    
    /**
     * If enabled, draw lines with correct filtering. Otherwise, draw aliased
     * lines.
     *
     * @param lineSmooth the new line smooth flag
     */
    public static void lineSmooth(boolean lineSmooth)
    {
        GL.LOGGER.finest("Setting Line Smooth Flag:", lineSmooth);
        
        if (GL.lineSmooth[GL.stackIndex] != lineSmooth)
        {
            GL.lineSmooth[GL.stackIndex] = lineSmooth;
            
            if (lineSmooth)
            {
                GL33.glEnable(GL33.GL_LINE_SMOOTH);
            }
            else
            {
                GL33.glDisable(GL33.GL_LINE_SMOOTH);
            }
        }
    }
    
    /**
     * If enabled, cubemap textures are sampled such that when linearly
     * sampling from the border between two adjacent faces, texels from both
     * faces are used to generate the final sample value. When disabled, texels
     * from only a single face are used to construct the final sample value.
     *
     * @param textureCubeMapSeamless the new depth clamp flag
     */
    public static void textureCubeMapSeamless(boolean textureCubeMapSeamless)
    {
        GL.LOGGER.finest("Setting Texture Cube Map Seamless Flag:", textureCubeMapSeamless);
        
        if (GL.textureCubeMapSeamless[GL.stackIndex] != textureCubeMapSeamless)
        {
            GL.textureCubeMapSeamless[GL.stackIndex] = textureCubeMapSeamless;
            
            if (textureCubeMapSeamless)
            {
                GL33.glEnable(GL33.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
            else
            {
                GL33.glDisable(GL33.GL_TEXTURE_CUBE_MAP_SEAMLESS);
            }
        }
    }
    
    /**
     * Controls the interpretation of polygons for rasterization.
     *
     * @param wireframe whether polygons are rendered as a wireframe
     */
    public static void wireframe(boolean wireframe)
    {
        GL.LOGGER.finest("Setting Wireframe Flag:", wireframe);
        
        if (GL.wireframe[GL.stackIndex] != wireframe)
        {
            GL.wireframe[GL.stackIndex] = wireframe;
            
            GL33.glPolygonMode(GL33.GL_FRONT_AND_BACK, wireframe ? GL33.GL_LINE : GL33.GL_FILL);
        }
    }
    
    /**
     * Specifies the weighting factors used by the blend equation, for both RGB
     * and alpha functions and for all draw buffers, and controls the blend
     * equations used for per-fragment blending.
     *
     * @param mode the blend mode
     */
    public static void blendMode(@Nullable BlendMode mode)
    {
        if (mode == null) mode = BlendMode.DEFAULT;
        
        GL.LOGGER.finest("Setting Blend Mode:", mode);
        
        if (!Objects.equals(GL.blendMode[GL.stackIndex], mode))
        {
            GL.blendMode[GL.stackIndex] = mode;
            
            if (mode == BlendMode.NONE)
            {
                GL33.glDisable(GL33.GL_BLEND);
            }
            else
            {
                GL33.glEnable(GL33.GL_BLEND);
                GL33.glBlendFunc(mode.srcFunc().ref, mode.dstFunc().ref);
                GL33.glBlendEquation(mode.blendEqn().ref);
            }
        }
    }
    
    /**
     * Specifies the func that takes place during the depth buffer test.
     *
     * @param mode the depth mode
     */
    public static void depthMode(@Nullable DepthMode mode)
    {
        if (mode == null) mode = DepthMode.DEFAULT;
        
        GL.LOGGER.finest("Setting Depth Mode:", mode);
        
        if (!Objects.equals(GL.depthMode[GL.stackIndex], mode))
        {
            GL.depthMode[GL.stackIndex] = mode;
            
            if (mode == DepthMode.NONE)
            {
                GL33.glDisable(GL33.GL_DEPTH_TEST);
            }
            else
            {
                GL33.glEnable(GL33.GL_DEPTH_TEST);
                GL33.glDepthFunc(mode.ref);
            }
        }
    }
    
    /**
     * Controls the stencil test.
     * <p>
     * {@code ref} is an integer reference value that is used in the unsigned
     * stencil comparison. Stencil comparison operations and queries of
     * {@code ref} clamp its value to the range [0, 2<sup>s</sup> &ndash; 1],
     * where {@code s} is the number of bits in the stencil buffer attached to
     * the draw framebuffer. The {@code s} least significant bits of
     * {@code mask} are bitwise ANDed with both the reference and the stored
     * stencil value, and the resulting masked values are those that
     * participate in the comparison controlled by {@code func}.
     *
     * @param mode the stencil mode
     */
    public static void stencilMode(@Nullable StencilMode mode)
    {
        if (mode == null) mode = StencilMode.DEFAULT;
        
        GL.LOGGER.finest("Setting Stencil Mode:", mode);
        
        if (!Objects.equals(GL.stencilMode[GL.stackIndex], mode))
        {
            GL.stencilMode[GL.stackIndex] = mode;
            
            if (mode == StencilMode.NONE)
            {
                GL33.glDisable(GL33.GL_STENCIL_TEST);
            }
            else
            {
                GL33.glEnable(GL33.GL_STENCIL_TEST);
                GL33.glStencilFunc(mode.func().ref, mode.ref(), mode.mask());
                GL33.glStencilOp(mode.sFail().ref, mode.dpFail().ref, mode.dpPass().ref);
            }
        }
    }
    
    /**
     * Defines the scissor mode for all viewports. When set to
     * {@link ScissorMode#NONE}, it is as if the scissor test always passes.
     * When set to anything else, if
     * <code>mode.x() &le; x &lt; mode.x() + mode.width()</code> and
     * <code>mode.y() &le; y &lt; mode.y() + mode.height()</code> for
     * the scissor rectangle, then the test passes. Otherwise, the test fails
     * and the fragment is discarded.
     *
     * @param mode The new scissorMode mode
     */
    public static void scissorMode(@Nullable ScissorMode mode)
    {
        if (mode == null) mode = ScissorMode.DEFAULT;
        
        GL.LOGGER.finest("Setting ScissorMode:", mode);
        
        if (!Objects.equals(GL.scissorMode[GL.stackIndex], mode))
        {
            GL.scissorMode[GL.stackIndex] = mode;
            
            if (mode == ScissorMode.NONE)
            {
                GL33.glDisable(GL33.GL_SCISSOR_TEST);
            }
            else
            {
                GL33.glEnable(GL33.GL_SCISSOR_TEST);
                GL33.glScissor(mode.x(), mode.y(), mode.width(), mode.height());
            }
        }
    }
    
    /**
     * Defines a scissor rect for all viewports. When set to anything else, if
     * <code>x &le; frag_x &lt; x) + width)</code> and
     * <code>y &le; frag_y &lt; y) + height)</code> for the scissor rectangle,
     * then the test passes. Otherwise, the test fails and the fragment is
     * discarded.
     *
     * @param x      the left scissor rectangle coordinate
     * @param y      the bottom scissor rectangle coordinate
     * @param width  the scissor rectangle width
     * @param height the scissor rectangle height
     */
    public static void scissor(int x, int y, int width, int height)
    {
        GL.LOGGER.finest("Setting Custom Scissor: [%s, %s, %s, %s]", x, y, width, height);
        
        GL.scissorMode[GL.stackIndex] = GL.scissorModeCustom;
        
        GL33.glEnable(GL33.GL_SCISSOR_TEST);
        GL33.glScissor(x, y, width, height);
    }
    
    /**
     * Masks the writing of red, green, blue and alpha values to all draw
     * buffers. In the initial state, all color values are enabled for writing
     * for all draw buffers.
     *
     * @param r whether red values are written or not
     * @param g whether green values are written or not
     * @param b whether blue values are written or not
     * @param a whether alpha values are written or not
     */
    public static void colorMask(boolean r, boolean g, boolean b, boolean a)
    {
        GL.LOGGER.finest("Setting Color Mask: r=%s g=%s b=%s a=%s", r, g, b, a);
        
        if ((Math.abs(Boolean.compare(GL.colorMask[GL.stackIndex][0], r)) +
             Math.abs(Boolean.compare(GL.colorMask[GL.stackIndex][1], g)) +
             Math.abs(Boolean.compare(GL.colorMask[GL.stackIndex][2], b)) +
             Math.abs(Boolean.compare(GL.colorMask[GL.stackIndex][3], a))) != 0)
        {
            GL.colorMask[GL.stackIndex][0] = r;
            GL.colorMask[GL.stackIndex][1] = g;
            GL.colorMask[GL.stackIndex][2] = b;
            GL.colorMask[GL.stackIndex][3] = a;
            
            GL33.glColorMask(r, g, b, a);
        }
    }
    
    /**
     * Masks the writing of depth values to the depth buffer. In the initial
     * state, the depth buffer is enabled for writing.
     *
     * @param flag whether depth values are written or not.
     */
    public static void depthMask(boolean flag)
    {
        GL.LOGGER.finest("Setting Depth Mask:", flag);
        
        if (GL.depthMask[GL.stackIndex] != flag)
        {
            GL.depthMask[GL.stackIndex] = flag;
            
            GL33.glDepthMask(flag);
        }
    }
    
    /**
     * Masks the writing of particular bits into the stencil plans.
     * <p>
     * The least significant s bits of {@code mask}, where s is the number
     * of bits in the stencil buffer, specify an integer mask. Where a 1
     * appears in this mask, the corresponding bit in the stencil buffer is
     * written; where a 0 appears, the bit is not written.
     *
     * @param mask the stencil mask
     */
    public static void stencilMask(int mask)
    {
        GL.LOGGER.finest("Setting Stencil Mask: 0x%02X", mask);
        
        if (GL.stencilMask[GL.stackIndex] != mask)
        {
            GL.stencilMask[GL.stackIndex] = mask;
            
            GL33.glStencilMask(mask);
        }
    }
    
    /**
     * Sets the clear value for fixed-point and floating-point color buffers in
     * RGBA mode. The specified components are stored as floating-point values.
     *
     * @param r the value to which to clear the red channel of the color buffer
     * @param g the value to which to clear the green channel of the color buffer
     * @param b the value to which to clear the blue channel of the color buffer
     * @param a the value to which to clear the alpha channel of the color buffer
     */
    public static void clearColor(double r, double g, double b, double a)
    {
        GL.LOGGER.finest("Setting Clear Color: (%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
        
        if ((Math.abs(Double.compare(GL.clearColor[GL.stackIndex][0], r)) +
             Math.abs(Double.compare(GL.clearColor[GL.stackIndex][1], g)) +
             Math.abs(Double.compare(GL.clearColor[GL.stackIndex][2], b)) +
             Math.abs(Double.compare(GL.clearColor[GL.stackIndex][3], a))) != 0)
        {
            GL.clearColor[GL.stackIndex][0] = r;
            GL.clearColor[GL.stackIndex][1] = g;
            GL.clearColor[GL.stackIndex][2] = b;
            GL.clearColor[GL.stackIndex][3] = a;
            
            GL33.glClearColor((float) r, (float) g, (float) b, (float) a);
        }
    }
    
    /**
     * Sets the depth value used when clearing the depth buffer. When clearing
     * a fixed-point depth buffer, {@code depth} is clamped to the range [0,1]
     * and converted to fixed-point. No conversion is applied when clearing a
     * floating-point depth buffer.
     *
     * @param depth the value to which to clear the depth buffer
     */
    public static void clearDepth(double depth)
    {
        GL.LOGGER.finest("Setting Clear Depth: %.3f", depth);
        
        int hash = Double.hashCode(depth);
        
        if (GL.clearDepth[GL.stackIndex] != hash)
        {
            GL.clearDepth[GL.stackIndex] = hash;
            
            GL33.glClearDepth(depth);
        }
    }
    
    /**
     * Sets the value to which to clear the stencil buffer. {@code s} is masked
     * to the number of bit planes in the stencil buffer.
     *
     * @param stencil the value to which to clear the stencil buffer
     */
    public static void clearStencil(int stencil)
    {
        GL.LOGGER.finest("Setting Clear Stencil: 0x%02X", stencil);
        
        if (GL.clearStencil[GL.stackIndex] != stencil)
        {
            GL.clearStencil[GL.stackIndex] = stencil;
            
            GL33.glClearStencil(stencil);
        }
    }
    
    /**
     * Sets portions of every pixel in all buffers to the same value. The value
     * to which each buffer is cleared depends on the setting of the clear
     * value for that buffer.
     */
    public static void clearScreenBuffers()
    {
        GL.LOGGER.finest("Clearing All Buffers");
        
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT | GL33.GL_STENCIL_BUFFER_BIT);
    }
    
    /**
     * Sets portions of every pixel in a particular buffer to the same value.
     * The value to which each buffer is cleared depends on the setting of the
     * clear value for that buffer.
     *
     * @param buffers The set indicating which buffers are to be cleared. One or more of:<ul>
     *                <li>{@link ScreenBuffer#COLOR COLOR}</li>
     *                <li>{@link ScreenBuffer#DEPTH DEPTH}</li>
     *                <li>{@link ScreenBuffer#STENCIL STENCIL}</li>
     *                </ul>
     */
    public static void clearScreenBuffers(@NotNull ScreenBuffer... buffers)
    {
        GL.LOGGER.finest("Clearing Buffers:", buffers);
        
        int mask = 0;
        for (ScreenBuffer buffer : buffers) mask |= buffer.ref;
        GL33.glClear(mask);
    }
    
    /**
     * Specifies which polygon faces are culled. Front-facing polygons are
     * rasterized if the CullFace mode is {@link CullFace#BACK BACK} while
     * back-facing polygons are rasterized only if the CullFace mode is
     * {@link CullFace#FRONT FRONT}. The initial setting of the CullFace mode
     * is {@link CullFace#BACK BACK}.
     *
     * @param cullFace the face to cull from rendering. One of:<ul>
     *                 <li>{@link CullFace#NONE NONE}</li>
     *                 <li>{@link CullFace#FRONT FRONT}</li>
     *                 <li>{@link CullFace#BACK BACK}</li>
     *                 <li>{@link CullFace#FRONT_AND_BACK FRONT_AND_BACK}</li>
     *                 </ul>
     */
    public static void cullFace(@Nullable CullFace cullFace)
    {
        if (cullFace == null) cullFace = CullFace.DEFAULT;
        
        GL.LOGGER.finest("Setting Cull Face:", cullFace);
        
        if (!Objects.equals(GL.cullFace[GL.stackIndex], cullFace))
        {
            GL.cullFace[GL.stackIndex] = cullFace;
            
            if (cullFace == CullFace.NONE)
            {
                GL33.glDisable(GL33.GL_CULL_FACE);
            }
            else
            {
                GL33.glEnable(GL33.GL_CULL_FACE);
                GL33.glCullFace(cullFace.ref);
            }
        }
    }
    
    /**
     * The first step of polygon rasterization is to determine if the polygon
     * is back-facing or front-facing. This determination is made based on the
     * sign of the (clipped or unclipped) polygon's area computed in window
     * coordinates. The interpretation of the sign of this value is controlled
     * with this function. In the initial state, the winding direction is set
     * to {@link Winding#CCW CCW}.
     *
     * @param winding the winding direction. One of:<ul>
     *                <li>{@link Winding#CCW CCW}</li>
     *                <li>{@link Winding#CW CW}</li>
     *                </ul>
     */
    public static void winding(@Nullable Winding winding)
    {
        if (winding == null) winding = Winding.DEFAULT;
        
        GL.LOGGER.finest("Setting Winding:", winding);
        
        if (!Objects.equals(GL.winding[GL.stackIndex], winding))
        {
            GL.winding[GL.stackIndex] = winding;
            
            GL33.glFrontFace(winding.ref);
        }
    }
    
    private static Color.@NotNull Buffer readBuffer(int buffer, int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        ByteBuffer data = MemoryUtil.memAlloc(width * height * format.sizeof);
        
        GL33.glReadBuffer(buffer);
        GL33.glReadPixels(x, y, width, height, format.format, GL33.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(data));
        
        // Flip data vertically
        int    s    = width * format.sizeof;
        byte[] tmp1 = new byte[s], tmp2 = new byte[s];
        for (int i = 0, n = height >> 1, col1, col2; i < n; i++)
        {
            col1 = i * s;
            col2 = (height - i - 1) * s;
            data.get(col1, tmp1);
            data.get(col2, tmp2);
            data.put(col1, tmp2);
            data.put(col2, tmp1);
        }
        
        return Color_RGBA.wrap(format, data);
    }
    
    /**
     * Obtains values from the front buffer from each pixel with lower
     * left-hand corner at {@code (x + i, y + j)} for {@code 0 <= i < width}
     * and {@code 0 <= j < height}; this pixel is said to be the i<sup>th</sup>
     * pixel in the j<sup>th</sup> row. If any of these pixels lies outside the
     * window allocated to the current GL context, or outside the image
     * attached to the currently bound read framebuffer object, then the values
     * obtained for those pixels are undefined.
     * <p>
     * The buffer must be explicitly freed.
     *
     * @param x      the left pixel coordinate
     * @param y      the lower pixel coordinate
     * @param width  the number of pixels to read in the x-dimension
     * @param height the number of pixels to read in the y-dimension
     * @param format the color format. One of:<br>{@link ColorFormat#GRAY GRAY}, {@link ColorFormat#GRAY_ALPHA GRAY_ALPHA}, {@link ColorFormat#RGB RGB}, {@link ColorFormat#RGBA RGBA}
     * @return The new color buffer.
     */
    @NotNull
    public static Color.Buffer readFrontBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        return readBuffer(GL33.GL_FRONT, x, y, width, height, format);
    }
    
    /**
     * Obtains values from the front buffer from each pixel with lower
     * left-hand corner at {@code (x + i, y + j)} for {@code 0 <= i < width}
     * and {@code 0 <= j < height}; this pixel is said to be the i<sup>th</sup>
     * pixel in the j<sup>th</sup> row. If any of these pixels lies outside the
     * window allocated to the current GL context, or outside the image
     * attached to the currently bound read framebuffer object, then the values
     * obtained for those pixels are undefined.
     * <p>
     * The buffer must be explicitly freed.
     *
     * @param x      the left pixel coordinate
     * @param y      the lower pixel coordinate
     * @param width  the number of pixels to read in the x-dimension
     * @param height the number of pixels to read in the y-dimension
     * @return The new color buffer.
     */
    @NotNull
    public static Color.Buffer readFrontBuffer(int x, int y, int width, int height)
    {
        return readFrontBuffer(x, y, width, height, ColorFormat.RGB);
    }
    
    /**
     * Obtains values from the back buffer from each pixel with lower
     * left-hand corner at {@code (x + i, y + j)} for {@code 0 <= i < width}
     * and {@code 0 <= j < height}; this pixel is said to be the i<sup>th</sup>
     * pixel in the j<sup>th</sup> row. If any of these pixels lies outside the
     * window allocated to the current GL context, or outside the image
     * attached to the currently bound read framebuffer object, then the values
     * obtained for those pixels are undefined.
     * <p>
     * The buffer must be explicitly freed.
     *
     * @param x      the left pixel coordinate
     * @param y      the lower pixel coordinate
     * @param width  the number of pixels to read in the x-dimension
     * @param height the number of pixels to read in the y-dimension
     * @param format the color format. One of:<br>{@link ColorFormat#GRAY GRAY}, {@link ColorFormat#GRAY_ALPHA GRAY_ALPHA}, {@link ColorFormat#RGB RGB}, {@link ColorFormat#RGBA RGBA}
     * @return The new color buffer.
     */
    @NotNull
    public static Color.Buffer readBackBuffer(int x, int y, int width, int height, @NotNull ColorFormat format)
    {
        return readBuffer(GL33.GL_BACK, x, y, width, height, format);
    }
    
    /**
     * Obtains values from the back buffer from each pixel with lower
     * left-hand corner at {@code (x + i, y + j)} for {@code 0 <= i < width}
     * and {@code 0 <= j < height}; this pixel is said to be the i<sup>th</sup>
     * pixel in the j<sup>th</sup> row. If any of these pixels lies outside the
     * window allocated to the current GL context, or outside the image
     * attached to the currently bound read framebuffer object, then the values
     * obtained for those pixels are undefined.
     * <p>
     * The buffer must be explicitly freed.
     *
     * @param x      the left pixel coordinate
     * @param y      the lower pixel coordinate
     * @param width  the number of pixels to read in the x-dimension
     * @param height the number of pixels to read in the y-dimension
     * @return The new color buffer.
     */
    @NotNull
    public static Color.Buffer readBackBuffer(int x, int y, int width, int height)
    {
        return readBackBuffer(x, y, width, height, ColorFormat.RGB);
    }
}
