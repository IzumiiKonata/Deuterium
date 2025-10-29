package tritium.rendering.states;

/**
 * Properties class to hold OpenGL state values.
 */
class Properties {
    // Texture and program states
    public final int[] lastActiveTexture = new int[1];
    public final int[] lastProgram = new int[1];
    public final int[] lastTexture = new int[1];
    public final int[] lastSampler = new int[1];
    public final int[] lastArrayBuffer = new int[1];
    public final int[] lastVertexArrayObject = new int[1];
    public final int[] lastPolygonMode = new int[2];
    
    // Viewport and scissor
    public final int[] lastViewport = new int[4];
    public final int[] lastScissorBox = new int[4];
    
    // Blend states
    public final int[] lastBlendSrcRgb = new int[1];
    public final int[] lastBlendDstRgb = new int[1];
    public final int[] lastBlendSrcAlpha = new int[1];
    public final int[] lastBlendDstAlpha = new int[1];
    public final int[] lastBlendEquationRgb = new int[1];
    public final int[] lastBlendEquationAlpha = new int[1];
    
    // Enable states
    public boolean lastEnableBlend;
    public boolean lastEnableCullFace;
    public boolean lastEnableDepthTest;
    public boolean lastEnableStencilTest;
    public boolean lastEnableScissorTest;
    public boolean lastEnablePrimitiveRestart;
    
    // Depth mask state
    public boolean lastDepthMask;
    
    // Pixel buffer states
    public final int[] lastPixelUnpackBufferBinding = new int[1];
    
    // Pack states
    public final int[] lastPackSwapBytes = new int[1];
    public final int[] lastPackLsbFirst = new int[1];
    public final int[] lastPackRowLength = new int[1];
    public final int[] lastPackSkipPixels = new int[1];
    public final int[] lastPackSkipRows = new int[1];
    public final int[] lastPackAlignment = new int[1];
    public final int[] lastPackImageHeight = new int[1];
    public final int[] lastPackSkipImages = new int[1];
    
    // Unpack states
    public final int[] lastUnpackSwapBytes = new int[1];
    public final int[] lastUnpackLsbFirst = new int[1];
    public final int[] lastUnpackAlignment = new int[1];
    public final int[] lastUnpackRowLength = new int[1];
    public final int[] lastUnpackSkipPixels = new int[1];
    public final int[] lastUnpackSkipRows = new int[1];
    public final int[] lastUnpackImageHeight = new int[1];
    public final int[] lastUnpackSkipImages = new int[1];
}