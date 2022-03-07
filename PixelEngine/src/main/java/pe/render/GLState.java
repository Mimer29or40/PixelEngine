package pe.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL33;
import pe.Engine;
import pe.color.BlendMode;
import rutils.Logger;

import java.util.Objects;

public class GLState
{
    // TODO - Move these to another class
    
    public static final int DEFAULT_BATCH_BUFFER_ELEMENTS = 8192; // Default internal render batch limits
    
    public static final int DEFAULT_BATCH_BUFFERS   = 1;   // Default number of batch buffers (multi-buffering)
    public static final int DEFAULT_BATCH_DRAWCALLS = 256; // Default number of batch draw calls (by state changes: mode, texture)
    public static final int MAX_ACTIVE_TEXTURES     = 16;  // Maximum number of additional textures that can be activated on batch drawing (SetShaderValueTexture())
    
    public static final int MAX_MATRIX_STACK_SIZE   = 32; // Maximum size of internal MatrixStack stack
    public static final int MAX_MESH_VERTEX_BUFFERS = 7;  // Maximum vertex buffers (VBO) per mesh
    public static final int MAX_SHADER_LOCATIONS    = 32; // Maximum number of shader locations supported
    public static final int MAX_MATERIAL_MAPS       = 12; // Maximum number of shader maps supported
    
    public static final float DEFAULT_CULL_DISTANCE_NEAR = 0.01f;  // Default projection matrix near cull distance
    public static final float DEFAULT_CULL_DISTANCE_FAR  = 1000.0f; // Default projection matrix far cull distance
    
    private static final Logger LOGGER = new Logger();
    
    private static boolean depthClamp;
    private static boolean lineSmooth;
    private static boolean textureCubeMapSeamless;
    
    private static boolean wireframe;
    
    private static BlendMode   blendMode;
    private static DepthMode   depthMode;
    private static StencilMode stencilMode;
    private static ScissorMode scissorMode;
    
    private static int colorMask;
    private static int depthMask;
    private static int stencilMask;
    
    private static int clearColor;
    private static int clearDepth;
    private static int clearStencil;
    
    private static CullFace cullFace;
    private static Winding  winding;
    
    private static final ScissorMode scissorModeCustom = new ScissorMode(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    
    public static void setup()
    {
        GLState.LOGGER.fine("Setup");
        
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
        
        defaultState();
        
        // TODO - Setup Uniform Buffer for default things
        // TODO - Setup Default Uniform values
    }
    
    public static void destroy()
    {
        GLState.LOGGER.fine("Destroy");
        
        GLProgram.destroy();
        GLShader.destroy();
        
        GLTexture.destroy();
        
        GLBuffer.destroy();
        
        GLVertexArray.destroy();
        
        GLFramebuffer.destroy();
        
        GLBatch.destroy();
    }
    
    public static void defaultState()
    {
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
        
        int r = Engine.screenWidth() >> 1;
        int l = -r;
        int b = Engine.screenHeight() >> 1;
        int t = -b;
        
        GLBatch.get().matrixMode(MatrixMode.PROJECTION);
        GLBatch.get().loadIdentity();
        GLBatch.get().ortho(l, r, b, t, 1.0, -1.0);
        
        GLBatch.get().matrixMode(MatrixMode.VIEW);
        GLBatch.get().loadIdentity();
        GLBatch.get().translate(l, t, 0.0);
        
        GLBatch.get().matrixMode(MatrixMode.MODEL);
        GLBatch.get().loadIdentity();
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
        GLState.LOGGER.finest("Setting Viewport: [%s, %s, %s, %s]", x, y, w, h);
        
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
        GLState.LOGGER.finest("Setting Depth Clamp Flag:", depthClamp);
        
        if (GLState.depthClamp != depthClamp)
        {
            GLState.depthClamp = depthClamp;
            
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
        GLState.LOGGER.finest("Setting Line Smooth Flag:", lineSmooth);
        
        if (GLState.lineSmooth != lineSmooth)
        {
            GLState.lineSmooth = lineSmooth;
            
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
        GLState.LOGGER.finest("Setting Texture Cube Map Seamless Flag:", textureCubeMapSeamless);
        
        if (GLState.textureCubeMapSeamless != textureCubeMapSeamless)
        {
            GLState.textureCubeMapSeamless = textureCubeMapSeamless;
            
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
        GLState.LOGGER.finest("Setting Wireframe Flag:", wireframe);
        
        if (GLState.wireframe != wireframe)
        {
            GLState.wireframe = wireframe;
            
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
        
        GLState.LOGGER.finest("Setting Blend Mode:", mode);
        
        if (!Objects.equals(GLState.blendMode, mode))
        {
            GLState.blendMode = mode;
            
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
        
        GLState.LOGGER.finest("Setting Depth Mode:", mode);
        
        if (!Objects.equals(GLState.depthMode, mode))
        {
            GLState.depthMode = mode;
            
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
        
        GLState.LOGGER.finest("Setting Stencil Mode:", mode);
        
        if (!Objects.equals(GLState.stencilMode, mode))
        {
            GLState.stencilMode = mode;
            
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
        
        GLState.LOGGER.finest("Setting ScissorMode:", mode);
        
        if (!Objects.equals(GLState.scissorMode, mode))
        {
            GLState.scissorMode = mode;
            
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
        GLState.LOGGER.finest("Setting Custom Scissor: [%s, %s, %s, %s]", x, y, width, height);
    
        GLState.scissorMode = GLState.scissorModeCustom;
        
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
        GLState.LOGGER.finest("Setting Color Mask: r=%s g=%s b=%s a=%s", r, g, b, a);
        
        int mask = (r ? 8 : 0) | (g ? 4 : 0) | (b ? 2 : 0) | (a ? 1 : 0);
        
        if (GLState.colorMask != mask)
        {
            GLState.colorMask = mask;
            
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
        GLState.LOGGER.finest("Setting Depth Mask:", flag);
        
        int mask = flag ? 1 : 0;
        
        if (GLState.depthMask != mask)
        {
            GLState.depthMask = mask;
            
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
        GLState.LOGGER.finest("Setting Stencil Mask: 0x%02X", mask);
        
        if (GLState.stencilMask != mask)
        {
            GLState.stencilMask = mask;
            
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
        GLState.LOGGER.finest("Setting Clear Color: (%.3f, %.3f, %.3f, %.3f)", r, g, b, a);
        
        int hash = Objects.hash(r, g, b, a);
        
        if (GLState.clearColor != hash)
        {
            GLState.clearColor = hash;
            
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
        GLState.LOGGER.finest("Setting Clear Depth: %.3f", depth);
        
        int hash = Double.hashCode(depth);
        
        if (GLState.clearDepth != hash)
        {
            GLState.clearDepth = hash;
            
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
        GLState.LOGGER.finest("Setting Clear Stencil: 0x%02X", stencil);
        
        if (GLState.clearStencil != stencil)
        {
            GLState.clearStencil = stencil;
            
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
        GLState.LOGGER.finest("Clearing All Buffers");
        
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
        GLState.LOGGER.finest("Clearing Buffers:", buffers);
        
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
        
        GLState.LOGGER.finest("Setting Cull Face:", cullFace);
        
        if (!Objects.equals(GLState.cullFace, cullFace))
        {
            GLState.cullFace = cullFace;
            
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
        
        GLState.LOGGER.finest("Setting Winding:", winding);
        
        if (!Objects.equals(GLState.winding, winding))
        {
            GLState.winding = winding;
            
            GL33.glFrontFace(winding.ref);
        }
    }
    
}
