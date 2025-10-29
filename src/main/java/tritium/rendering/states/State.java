package tritium.rendering.states;

import org.lwjgl.opengl.*;

/**
 * Represents the OpenGL state.
 */
public class State {
    private final int glVersion;
    
    /**
     * The properties of the OpenGL state.
     */
    private final Properties props = new Properties();
    
    public State(int glVersion) {
        this.glVersion = glVersion;
    }
    
    /**
     * Saves the current OpenGL state.
     *
     * This code was inspired by <a href="https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L398-L425">imgui-java</a>
     * and modified to fit the project's codebase.
     *
     * @see #pop()
     */
    public State push() {
        GL11.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, props.lastActiveTexture);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, props.lastProgram);
        GL11.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, props.lastTexture);
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            GL11.glGetIntegerv(GL33.GL_SAMPLER_BINDING, props.lastSampler);
        }
        GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, props.lastArrayBuffer);
        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, props.lastVertexArrayObject);
        if (glVersion >= 200) {
            GL11.glGetIntegerv(GL11.GL_POLYGON_MODE, props.lastPolygonMode);
        }
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, props.lastViewport);
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, props.lastScissorBox);
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_RGB, props.lastBlendSrcRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_RGB, props.lastBlendDstRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_ALPHA, props.lastBlendSrcAlpha);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_ALPHA, props.lastBlendDstAlpha);
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_RGB, props.lastBlendEquationRgb);
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_ALPHA, props.lastBlendEquationAlpha);
        props.lastEnableBlend = GL11.glIsEnabled(GL11.GL_BLEND);
        props.lastEnableCullFace = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        props.lastEnableDepthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        props.lastEnableStencilTest = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        props.lastEnableScissorTest = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        if (glVersion >= 310) {
            props.lastEnablePrimitiveRestart = GL11.glIsEnabled(GL31.GL_PRIMITIVE_RESTART);
        }

        // This state is not saved in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        props.lastDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);

        GL11.glGetIntegerv(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING, props.lastPixelUnpackBufferBinding);
        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

        GL11.glGetIntegerv(GL11.GL_PACK_SWAP_BYTES, props.lastPackSwapBytes);
        GL11.glGetIntegerv(GL11.GL_PACK_LSB_FIRST, props.lastPackLsbFirst);
        GL11.glGetIntegerv(GL11.GL_PACK_ROW_LENGTH, props.lastPackRowLength);
        GL11.glGetIntegerv(GL11.GL_PACK_SKIP_PIXELS, props.lastPackSkipPixels);
        GL11.glGetIntegerv(GL11.GL_PACK_SKIP_ROWS, props.lastPackSkipRows);
        GL11.glGetIntegerv(GL11.GL_PACK_ALIGNMENT, props.lastPackAlignment);

        GL11.glGetIntegerv(GL11.GL_UNPACK_SWAP_BYTES, props.lastUnpackSwapBytes);
        GL11.glGetIntegerv(GL11.GL_UNPACK_LSB_FIRST, props.lastUnpackLsbFirst);
        GL11.glGetIntegerv(GL11.GL_UNPACK_ALIGNMENT, props.lastUnpackAlignment);
        GL11.glGetIntegerv(GL11.GL_UNPACK_ROW_LENGTH, props.lastUnpackRowLength);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_PIXELS, props.lastUnpackSkipPixels);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_ROWS, props.lastUnpackSkipRows);

        if (glVersion >= 120) {
            GL11.glGetIntegerv(GL12.GL_PACK_IMAGE_HEIGHT, props.lastPackImageHeight);
            GL11.glGetIntegerv(GL12.GL_PACK_SKIP_IMAGES, props.lastPackSkipImages);
            GL11.glGetIntegerv(GL12.GL_UNPACK_IMAGE_HEIGHT, props.lastUnpackImageHeight);
            GL11.glGetIntegerv(GL12.GL_UNPACK_SKIP_IMAGES, props.lastUnpackSkipImages);
        }

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        
        return this;
    }

    /**
     * Restores the state that was saved with {@link #push()}.
     *
     * This code was inspired by <a href="https://github.com/SpaiR/imgui-java/blob/2a605f0d8500f27e13fa1d2b4cf8cadd822789f4/imgui-lwjgl3/src/main/java/imgui/gl3/ImGuiImplGl3.java#L500-L532">imgui-java</a>
     * and modified to fit the project's codebase.
     *
     * @see #push()
     */
    public State pop() {
        GL20.glUseProgram(props.lastProgram[0]);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, props.lastTexture[0]);
        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            GL33.glBindSampler(0, props.lastSampler[0]);
        }
        GL13.glActiveTexture(props.lastActiveTexture[0]);
        GL30.glBindVertexArray(props.lastVertexArrayObject[0]);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, props.lastArrayBuffer[0]);
        GL20.glBlendEquationSeparate(props.lastBlendEquationRgb[0], props.lastBlendEquationAlpha[0]);
        GL14.glBlendFuncSeparate(
            props.lastBlendSrcRgb[0],
            props.lastBlendDstRgb[0],
            props.lastBlendSrcAlpha[0],
            props.lastBlendDstAlpha[0]
        );
        if (props.lastEnableBlend) GL11.glEnable(GL11.GL_BLEND);
        else GL11.glDisable(GL11.GL_BLEND);
        if (props.lastEnableCullFace) GL11.glEnable(GL11.GL_CULL_FACE);
        else GL11.glDisable(GL11.GL_CULL_FACE);
        if (props.lastEnableDepthTest) GL11.glEnable(GL11.GL_DEPTH_TEST);
        else GL11.glDisable(GL11.GL_DEPTH_TEST);
        if (props.lastEnableStencilTest) GL11.glEnable(GL11.GL_STENCIL_TEST);
        else GL11.glDisable(GL11.GL_STENCIL_TEST);
        if (props.lastEnableScissorTest) GL11.glEnable(GL11.GL_SCISSOR_TEST);
        else GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (glVersion >= 310) {
            if (props.lastEnablePrimitiveRestart) GL11.glEnable(GL31.GL_PRIMITIVE_RESTART);
            else GL11.glDisable(GL31.GL_PRIMITIVE_RESTART);
        }
        if (glVersion >= 200) {
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, props.lastPolygonMode[0]);
        }
        GL11.glViewport(props.lastViewport[0], props.lastViewport[1], props.lastViewport[2], props.lastViewport[3]);
        GL11.glScissor(
            props.lastScissorBox[0],
            props.lastScissorBox[1],
            props.lastScissorBox[2],
            props.lastScissorBox[3]
        );

        GL11.glPixelStorei(GL11.GL_PACK_SWAP_BYTES, props.lastPackSwapBytes[0]);
        GL11.glPixelStorei(GL11.GL_PACK_LSB_FIRST, props.lastPackLsbFirst[0]);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, props.lastPackRowLength[0]);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, props.lastPackSkipPixels[0]);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, props.lastPackSkipRows[0]);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, props.lastPackAlignment[0]);

        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, props.lastPixelUnpackBufferBinding[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, props.lastUnpackSwapBytes[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, props.lastUnpackLsbFirst[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, props.lastUnpackAlignment[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, props.lastUnpackRowLength[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, props.lastUnpackSkipPixels[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, props.lastUnpackSkipRows[0]);

        if (glVersion >= 120) {
            GL11.glPixelStorei(GL12.GL_PACK_IMAGE_HEIGHT, props.lastPackImageHeight[0]);
            GL11.glPixelStorei(GL12.GL_PACK_SKIP_IMAGES, props.lastPackSkipImages[0]);
            GL11.glPixelStorei(GL12.GL_UNPACK_IMAGE_HEIGHT, props.lastUnpackImageHeight[0]);
            GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_IMAGES, props.lastUnpackSkipImages[0]);
        }

        // This state is not restored in the original imgui-java project but is included to address bugs encountered when drawing with Skija.
        GL11.glDepthMask(props.lastDepthMask); // This is a workaround for a bug where the text renderer of Minecraft would not render text properly (flickering text). This also fixes the issue that resizing the window would cause the buttons and more to disappear.
        
        return this;
    }
}
