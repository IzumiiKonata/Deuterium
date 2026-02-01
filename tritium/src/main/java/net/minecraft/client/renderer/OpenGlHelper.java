package net.minecraft.client.renderer;

import com.sun.jna.platform.win32.Advapi32Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import org.lwjgl.opengl.*;
import org.lwjglx.opengl.GLContext;
import tritium.screens.ConsoleScreen;
import tritium.utils.other.multithreading.MultiThreadingUtil;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

public class OpenGlHelper {
    public static boolean nvidia;
    public static boolean ati;
    public static int GL_FRAMEBUFFER;
    public static int GL_RENDERBUFFER;
    public static int GL_COLOR_ATTACHMENT0;
    public static int GL_DEPTH_ATTACHMENT;
    public static int GL_FRAMEBUFFER_COMPLETE;
    public static int GL_FB_INCOMPLETE_ATTACHMENT;
    public static int GL_FB_INCOMPLETE_MISS_ATTACH;
    public static int GL_FB_INCOMPLETE_DRAW_BUFFER;
    public static int GL_FB_INCOMPLETE_READ_BUFFER;
    private static int framebufferType;
    public static boolean framebufferSupported;
    private static boolean arbShaders;
    public static int GL_LINK_STATUS;
    public static int GL_COMPILE_STATUS;
    public static int GL_VERTEX_SHADER;
    public static int GL_FRAGMENT_SHADER;
    private static boolean arbMultitexture;

    /**
     * An OpenGL constant corresponding to GL_TEXTURE0, used when setting data pertaining to auxiliary OpenGL texture
     * units.
     */
    public static int defaultTexUnit;

    /**
     * An OpenGL constant corresponding to GL_TEXTURE1, used when setting data pertaining to auxiliary OpenGL texture
     * units.
     */
    public static int lightmapTexUnit;
    public static int GL_TEXTURE2;
    public static int GL_COMBINE;
    public static int GL_INTERPOLATE;
    public static int GL_PRIMARY_COLOR;
    public static int GL_CONSTANT;
    public static int GL_PREVIOUS;
    public static int GL_COMBINE_RGB;
    public static int GL_SOURCE0_RGB;
    public static int GL_SOURCE1_RGB;
    public static int GL_SOURCE2_RGB;
    public static int GL_OPERAND0_RGB;
    public static int GL_OPERAND1_RGB;
    public static int GL_OPERAND2_RGB;
    public static int GL_COMBINE_ALPHA;
    public static int GL_SOURCE0_ALPHA;
    public static int GL_SOURCE1_ALPHA;
    public static int GL_SOURCE2_ALPHA;
    public static int GL_OPERAND0_ALPHA;
    public static int GL_OPERAND1_ALPHA;
    public static int GL_OPERAND2_ALPHA;
    private static boolean openGL14;
    public static boolean extBlendFuncSeparate;
    public static boolean openGL21;
    public static boolean shadersSupported;
    private static String logText = "";
    private static String cpu;
    public static boolean vboSupported;
    public static boolean vboSupportedAti;
    private static boolean arbVbo;
    public static int GL_ARRAY_BUFFER;
    public static int GL_STATIC_DRAW;
    public static float lastBrightnessX = 0.0F;
    public static float lastBrightnessY = 0.0F;
    public static boolean openGL31;
    public static boolean vboRegions;
    public static int GL_COPY_READ_BUFFER;
    public static int GL_COPY_WRITE_BUFFER;
    public static final int GL_QUADS = 7;
    public static final int GL_TRIANGLES = 4;

    /**
     * Initializes the texture constants to be used when rendering lightmap values
     */
    public static void initializeTextures() {
        Config.initDisplay();
        GLCapabilities contextcapabilities = GLContext.getCapabilities();
        arbMultitexture = contextcapabilities.GL_ARB_multitexture && !contextcapabilities.OpenGL13;
        boolean arbTextureEnvCombine = contextcapabilities.GL_ARB_texture_env_combine && !contextcapabilities.OpenGL13;
        openGL31 = contextcapabilities.OpenGL31;

        GL_COPY_READ_BUFFER = 36662;
        GL_COPY_WRITE_BUFFER = 36663;

        boolean flag = openGL31 || contextcapabilities.GL_ARB_copy_buffer;
        boolean flag1 = contextcapabilities.OpenGL14;
        vboRegions = flag && flag1;

        if (!vboRegions) {
            List<String> list = new ArrayList<>();

            if (!flag) {
                list.add("OpenGL 1.3, ARB_copy_buffer");
            }

            if (!flag1) {
                list.add("OpenGL 1.4");
            }

            String s = "不支持 VboRegions, 缺失: " + Config.listToString(list);
            Config.dbg(s);
            logText = logText + s + "\n";
        }

        if (arbMultitexture) {
            logText = logText + "正在使用 ARB_multitexture.\n";
        } else {
            logText = logText + "正在使用 GL 1.3 多重纹理.\n";
        }
        defaultTexUnit = GL13.GL_TEXTURE0;
        lightmapTexUnit = 33985;
        GL_TEXTURE2 = 33986;

        if (arbTextureEnvCombine) {
            logText = logText + "正在使用 ARB_texture_env_combine.\n";
        } else {
            logText = logText + "正在使用 GL 1.3 纹理组合.\n";
        }
        GL_COMBINE = 34160;
        GL_INTERPOLATE = 34165;
        GL_PRIMARY_COLOR = 34167;
        GL_CONSTANT = 34166;
        GL_PREVIOUS = 34168;
        GL_COMBINE_RGB = 34161;
        GL_SOURCE0_RGB = 34176;
        GL_SOURCE1_RGB = 34177;
        GL_SOURCE2_RGB = 34178;
        GL_OPERAND0_RGB = 34192;
        GL_OPERAND1_RGB = 34193;
        GL_OPERAND2_RGB = 34194;
        GL_COMBINE_ALPHA = 34162;
        GL_SOURCE0_ALPHA = 34184;
        GL_SOURCE1_ALPHA = 34185;
        GL_SOURCE2_ALPHA = 34186;
        GL_OPERAND0_ALPHA = 34200;
        GL_OPERAND1_ALPHA = 34201;
        GL_OPERAND2_ALPHA = 34202;

        extBlendFuncSeparate = contextcapabilities.GL_EXT_blend_func_separate && !contextcapabilities.OpenGL14;
        openGL14 = contextcapabilities.OpenGL14 || contextcapabilities.GL_EXT_blend_func_separate;
        framebufferSupported = openGL14 && (contextcapabilities.GL_ARB_framebuffer_object || contextcapabilities.GL_EXT_framebuffer_object || contextcapabilities.OpenGL30);

        if (framebufferSupported) {
            logText = logText + "使用帧缓冲区对象, 原因: ";

            if (contextcapabilities.OpenGL30) {
                logText = logText + "支持 OpenGL 3.0, 并支持分离混合.\n";
                framebufferType = 0;
                GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
                GL_RENDERBUFFER = 36161;
                GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
                GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
                GL_FRAMEBUFFER_COMPLETE = 36053;
                GL_FB_INCOMPLETE_ATTACHMENT = 36054;
                GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
                GL_FB_INCOMPLETE_DRAW_BUFFER = 36059;
                GL_FB_INCOMPLETE_READ_BUFFER = 36060;
            } else if (contextcapabilities.GL_ARB_framebuffer_object) {
                logText = logText + "支持 ARB_framebuffer_object, 并支持分离混合.\n";
                framebufferType = 1;
                GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
                GL_RENDERBUFFER = 36161;
                GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
                GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
                GL_FRAMEBUFFER_COMPLETE = 36053;
                GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
                GL_FB_INCOMPLETE_ATTACHMENT = 36054;
                GL_FB_INCOMPLETE_DRAW_BUFFER = 36059;
                GL_FB_INCOMPLETE_READ_BUFFER = 36060;
            } else if (contextcapabilities.GL_EXT_framebuffer_object) {
                logText = logText + "支持 EXT_framebuffer_object.\n";
                framebufferType = 2;
                GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER;
                GL_RENDERBUFFER = 36161;
                GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0;
                GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT;
                GL_FRAMEBUFFER_COMPLETE = 36053;
                GL_FB_INCOMPLETE_MISS_ATTACH = 36055;
                GL_FB_INCOMPLETE_ATTACHMENT = 36054;
                GL_FB_INCOMPLETE_DRAW_BUFFER = 36059;
                GL_FB_INCOMPLETE_READ_BUFFER = 36060;
            }
        } else {
            logText = logText + "不使用帧缓冲区对象, 因为 ";
            logText = logText + "OpenGL 1.4 是" + (contextcapabilities.OpenGL14 ? "" : "不被") + "支持的, ";
            logText = logText + "EXT_blend_func_separate 是" + (contextcapabilities.GL_EXT_blend_func_separate ? "" : "不被") + "支持的, ";
            logText = logText + "OpenGL 3.0 是" + (contextcapabilities.OpenGL30 ? "" : "不被") + "支持的, ";
            logText = logText + "ARB_framebuffer_object 是" + (contextcapabilities.GL_ARB_framebuffer_object ? "" : "不被") + "支持的, 并且 ";
            logText = logText + "EXT_framebuffer_object 是" + (contextcapabilities.GL_EXT_framebuffer_object ? "" : "不被") + "支持的.\n";
        }

        openGL21 = contextcapabilities.OpenGL21;
        boolean shadersAvailable = openGL21 || contextcapabilities.GL_ARB_vertex_shader && contextcapabilities.GL_ARB_fragment_shader && contextcapabilities.GL_ARB_shader_objects;
        logText = logText + "着色器是" + (shadersAvailable ? "" : "不被") + "支持的 因为 ";

        if (shadersAvailable) {
            if (contextcapabilities.OpenGL21) {
                logText = logText + "支持OpenGL 2.1.\n";
                arbShaders = false;
            } else {
                logText = logText + "支持ARB_shader_objects, ARB_vertex_shader, 和 ARB_fragment_shader.\n";
                arbShaders = true;
            }
            GL_LINK_STATUS = 35714;
            GL_COMPILE_STATUS = 35713;
            GL_VERTEX_SHADER = 35633;
            GL_FRAGMENT_SHADER = 35632;
        } else {
            logText = logText + "OpenGL 2.1 是" + (contextcapabilities.OpenGL21 ? "" : "不被") + "支持的, ";
            logText = logText + "ARB_shader_objects 是" + (contextcapabilities.GL_ARB_shader_objects ? "" : "不被") + "支持的, ";
            logText = logText + "ARB_vertex_shader 是" + (contextcapabilities.GL_ARB_vertex_shader ? "" : "不被") + "支持的, 并且 ";
            logText = logText + "ARB_fragment_shader 是" + (contextcapabilities.GL_ARB_fragment_shader ? "" : "不被") + "支持的.\n";
        }

        shadersSupported = framebufferSupported && shadersAvailable;
        String s1 = GL11.glGetString(GL11.GL_VENDOR).toLowerCase();
        nvidia = s1.contains("nvidia");
        arbVbo = !contextcapabilities.OpenGL15 && contextcapabilities.GL_ARB_vertex_buffer_object;
        vboSupported = contextcapabilities.OpenGL15 || arbVbo;
        logText = logText + "VBO是" + (vboSupported ? "" : "不被") + "支持的 因为 ";

        if (vboSupported) {
            if (arbVbo) {
                logText = logText + "支持ARB_vertex_buffer_object.\n";
            } else {
                logText = logText + "支持OpenGL 1.5.\n";
            }
            GL_STATIC_DRAW = 35044;
            GL_ARRAY_BUFFER = 34962;
        }

        ati = s1.contains("ati");

        if (ati) {
            if (vboSupported) {
                vboSupportedAti = true;
            } else {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
            }
        }

        MultiThreadingUtil.runAsync(() -> {
            try {
                cpu = Advapi32Util.registryGetStringValue
                        (HKEY_LOCAL_MACHINE,
                                "HARDWARE\\DESCRIPTION\\System\\CentralProcessor\\0\\",
                                "ProcessorNameString");
            } catch (Throwable var5) {
            }
        });

        for (String s : getLogText().split("\n")) {
            ConsoleScreen.log("[GL] {}", s);
        }

    }

    public static boolean areShadersSupported() {
        return shadersSupported;
    }

    public static String getLogText() {
        return logText;
    }

    public static int glGetProgrami(int program, int pname) {
        return arbShaders ? ARBShaderObjects.glGetObjectParameteriARB(program, pname) : GL20.glGetProgrami(program, pname);
    }

    public static void glAttachShader(int program, int shaderIn) {
        if (arbShaders) {
            ARBShaderObjects.glAttachObjectARB(program, shaderIn);
        } else {
            GL20.glAttachShader(program, shaderIn);
        }
    }

    public static void glDeleteShader(int p_153180_0_) {
        if (arbShaders) {
            ARBShaderObjects.glDeleteObjectARB(p_153180_0_);
        } else {
            GL20.glDeleteShader(p_153180_0_);
        }
    }

    /**
     * creates a shader with the given mode and returns the GL id. params: mode
     */
    public static int glCreateShader(int type) {
        return arbShaders ? ARBShaderObjects.glCreateShaderObjectARB(type) : GL20.glCreateShader(type);
    }

    public static void glShaderSource(int shaderIn, String string) {
        if (arbShaders) {
            ARBShaderObjects.glShaderSourceARB(shaderIn, string);
        } else {
            GL20.glShaderSource(shaderIn, string);
        }
    }

    public static void glCompileShader(int shaderIn) {
        if (arbShaders) {
            ARBShaderObjects.glCompileShaderARB(shaderIn);
        } else {
            GL20.glCompileShader(shaderIn);
        }
    }

    public static int glGetShaderi(int shaderIn, int pname) {
        return arbShaders ? ARBShaderObjects.glGetObjectParameteriARB(shaderIn, pname) : GL20.glGetShaderi(shaderIn, pname);
    }

    public static String glGetShaderInfoLog(int shaderIn, int maxLength) {
        return arbShaders ? ARBShaderObjects.glGetInfoLogARB(shaderIn, maxLength) : GL20.glGetShaderInfoLog(shaderIn, maxLength);
    }

    public static String glGetProgramInfoLog(int program, int maxLength) {
        return arbShaders ? ARBShaderObjects.glGetInfoLogARB(program, maxLength) : GL20.glGetProgramInfoLog(program, maxLength);
    }

    public static void glUseProgram(int program) {
        if (arbShaders) {
            ARBShaderObjects.glUseProgramObjectARB(program);
        } else {
            GL20.glUseProgram(program);
        }
    }

    public static int glCreateProgram() {
        return arbShaders ? ARBShaderObjects.glCreateProgramObjectARB() : GL20.glCreateProgram();
    }

    public static void glDeleteProgram(int program) {
        if (arbShaders) {
            ARBShaderObjects.glDeleteObjectARB(program);
        } else {
            GL20.glDeleteProgram(program);
        }
    }

    public static void glLinkProgram(int program) {
        if (arbShaders) {
            ARBShaderObjects.glLinkProgramARB(program);
        } else {
            GL20.glLinkProgram(program);
        }
    }

    public static int glGetUniformLocation(int programObj, CharSequence name) {
        return arbShaders ? ARBShaderObjects.glGetUniformLocationARB(programObj, name) : GL20.glGetUniformLocation(programObj, name);
    }

    public static void glUniform1(int location, IntBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform1ivARB(location, values);
        } else {
            GL20.glUniform1iv(location, values);
        }
    }

    public static void glUniform1i(int location, int v0) {
        if (arbShaders) {
            ARBShaderObjects.glUniform1iARB(location, v0);
        } else {
            GL20.glUniform1i(location, v0);
        }
    }

    public static void glUniform1(int location, FloatBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform1fvARB(location, values);
        } else {
            GL20.glUniform1fv(location, values);
        }
    }

    public static void glUniform2(int location, IntBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform2ivARB(location, values);
        } else {
            GL20.glUniform2iv(location, values);
        }
    }

    public static void glUniform2(int location, FloatBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform2fvARB(location, values);
        } else {
            GL20.glUniform2fv(location, values);
        }
    }

    public static void glUniform3(int location, IntBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform3ivARB(location, values);
        } else {
            GL20.glUniform3iv(location, values);
        }
    }

    public static void glUniform3(int location, FloatBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform3fvARB(location, values);
        } else {
            GL20.glUniform3fv(location, values);
        }
    }

    public static void glUniform4(int location, IntBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform4ivARB(location, values);
        } else {
            GL20.glUniform4iv(location, values);
        }
    }

    public static void glUniform4(int location, FloatBuffer values) {
        if (arbShaders) {
            ARBShaderObjects.glUniform4fvARB(location, values);
        } else {
            GL20.glUniform4fv(location, values);
        }
    }

    public static void glUniformMatrix2(int location, boolean transpose, FloatBuffer matrices) {
        if (arbShaders) {
            ARBShaderObjects.glUniformMatrix2fvARB(location, transpose, matrices);
        } else {
            GL20.glUniformMatrix2fv(location, transpose, matrices);
        }
    }

    public static void glUniformMatrix3(int location, boolean transpose, FloatBuffer matrices) {
        if (arbShaders) {
            ARBShaderObjects.glUniformMatrix3fvARB(location, transpose, matrices);
        } else {
            GL20.glUniformMatrix3fv(location, transpose, matrices);
        }
    }

    public static void glUniformMatrix4(int location, boolean transpose, FloatBuffer matrices) {
        if (arbShaders) {
            ARBShaderObjects.glUniformMatrix4fvARB(location, transpose, matrices);
        } else {
            GL20.glUniformMatrix4fv(location, transpose, matrices);
        }
    }

    public static int glGetAttribLocation(int p_153164_0_, CharSequence p_153164_1_) {
        return arbShaders ? ARBVertexShader.glGetAttribLocationARB(p_153164_0_, p_153164_1_) : GL20.glGetAttribLocation(p_153164_0_, p_153164_1_);
    }

    public static int glGenBuffers() {
        return arbVbo ? ARBVertexBufferObject.glGenBuffersARB() : GL15.glGenBuffers();
    }

    public static void glBindBuffer(int target, int buffer) {
        if (arbVbo) {
            ARBVertexBufferObject.glBindBufferARB(target, buffer);
        } else {
            GL15.glBindBuffer(target, buffer);
        }
    }

    public static void glBufferData(int target, ByteBuffer data, int usage) {
        if (arbVbo) {
            ARBVertexBufferObject.glBufferDataARB(target, data, usage);
        } else {
            GL15.glBufferData(target, data, usage);
        }
    }

    public static void glDeleteBuffers(int buffer) {
        if (arbVbo) {
            ARBVertexBufferObject.glDeleteBuffersARB(buffer);
        } else {
            GL15.glDeleteBuffers(buffer);
        }
    }

    public static boolean useVbo() {
        return !Config.isMultiTexture() && ((!Config.isRenderRegions() || vboRegions) && vboSupported && Minecraft.getMinecraft().gameSettings.useVbo);
    }

    public static void glBindFramebuffer(int target, int framebufferIn) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glBindFramebuffer(target, framebufferIn);
                    break;

                case 1:
                    ARBFramebufferObject.glBindFramebuffer(target, framebufferIn);
                    break;

                case 2:
                    EXTFramebufferObject.glBindFramebufferEXT(target, framebufferIn);
            }
        }
    }

    public static void glBindRenderbuffer(int target, int renderbuffer) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glBindRenderbuffer(target, renderbuffer);
                    break;

                case 1:
                    ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
                    break;

                case 2:
                    EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
            }
        }
    }

    public static void glDeleteRenderbuffers(int renderbuffer) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glDeleteRenderbuffers(renderbuffer);
                    break;

                case 1:
                    ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
                    break;

                case 2:
                    EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
            }
        }
    }

    public static void glDeleteFramebuffers(int framebufferIn) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glDeleteFramebuffers(framebufferIn);
                    break;

                case 1:
                    ARBFramebufferObject.glDeleteFramebuffers(framebufferIn);
                    break;

                case 2:
                    EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferIn);
            }
        }
    }

    /**
     * Calls the appropriate glGenFramebuffers method and returns the newly created fbo, or returns -1 if not supported.
     */
    public static int glGenFramebuffers() {
        if (!framebufferSupported) {
            return -1;
        } else {
            return switch (framebufferType) {
                case 0 -> GL30.glGenFramebuffers();
                case 1 -> ARBFramebufferObject.glGenFramebuffers();
                case 2 -> EXTFramebufferObject.glGenFramebuffersEXT();
                default -> -1;
            };
        }
    }

    public static int glGenRenderbuffers() {
        if (!framebufferSupported) {
            return -1;
        } else {
            return switch (framebufferType) {
                case 0 -> GL30.glGenRenderbuffers();
                case 1 -> ARBFramebufferObject.glGenRenderbuffers();
                case 2 -> EXTFramebufferObject.glGenRenderbuffersEXT();
                default -> -1;
            };
        }
    }

    public static void glRenderbufferStorage(int target, int internalFormat, int width, int height) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glRenderbufferStorage(target, internalFormat, width, height);
                    break;

                case 1:
                    ARBFramebufferObject.glRenderbufferStorage(target, internalFormat, width, height);
                    break;

                case 2:
                    EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
            }
        }
    }

    public static void glFramebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
                    break;

                case 1:
                    ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
                    break;

                case 2:
                    EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderBufferTarget, renderBuffer);
            }
        }
    }

    public static int glCheckFramebufferStatus(int target) {
        if (!framebufferSupported) {
            return -1;
        } else {
            return switch (framebufferType) {
                case 0 -> GL30.glCheckFramebufferStatus(target);
                case 1 -> ARBFramebufferObject.glCheckFramebufferStatus(target);
                case 2 -> EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
                default -> -1;
            };
        }
    }

    public static void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        if (framebufferSupported) {
            switch (framebufferType) {
                case 0:
                    GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
                    break;

                case 1:
                    ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
                    break;

                case 2:
                    EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
            }
        }
    }

    /**
     * Sets the current lightmap texture to the specified OpenGL constant
     */
    public static void setActiveTexture(int texture) {
        if (arbMultitexture) {
            ARBMultitexture.glActiveTextureARB(texture);
        } else {
            GL13.glActiveTexture(texture);
        }
    }

    /**
     * Sets the current lightmap texture to the specified OpenGL constant
     */
    public static void setClientActiveTexture(int texture) {
        if (arbMultitexture) {
            ARBMultitexture.glClientActiveTextureARB(texture);
        } else {
            GL13.glClientActiveTexture(texture);
        }
    }

    /**
     * Sets the current coordinates of the given lightmap texture
     */
    public static void setLightmapTextureCoords(int target, float p_77475_1_, float p_77475_2_) {
        if (arbMultitexture) {
            ARBMultitexture.glMultiTexCoord2fARB(target, p_77475_1_, p_77475_2_);
        } else {
            GL13.glMultiTexCoord2f(target, p_77475_1_, p_77475_2_);
        }

        if (target == lightmapTexUnit) {
            lastBrightnessX = p_77475_1_;
            lastBrightnessY = p_77475_2_;
        }
    }

    public static void glBlendFunc(int sFactorRGB, int dFactorRGB, int sfactorAlpha, int dfactorAlpha) {
        if (openGL14) {
            if (extBlendFuncSeparate) {
                EXTBlendFuncSeparate.glBlendFuncSeparateEXT(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
            } else {
                GL14.glBlendFuncSeparate(sFactorRGB, dFactorRGB, sfactorAlpha, dfactorAlpha);
            }
        } else {
            GL11.glBlendFunc(sFactorRGB, dFactorRGB);
        }
    }

    public static boolean isFramebufferEnabled() {
        return (!Config.isAntialiasing() && framebufferSupported && Minecraft.getMinecraft().gameSettings.fboEnable);
    }

    public static void glBufferData(int p_glBufferData_0_, long p_glBufferData_1_, int p_glBufferData_3_) {
        if (arbVbo) {
            ARBVertexBufferObject.glBufferDataARB(p_glBufferData_0_, p_glBufferData_1_, p_glBufferData_3_);
        } else {
            GL15.glBufferData(p_glBufferData_0_, p_glBufferData_1_, p_glBufferData_3_);
        }
    }

    public static void glBufferSubData(int p_glBufferSubData_0_, long p_glBufferSubData_1_, ByteBuffer p_glBufferSubData_3_) {
        if (arbVbo) {
            ARBVertexBufferObject.glBufferSubDataARB(p_glBufferSubData_0_, p_glBufferSubData_1_, p_glBufferSubData_3_);
        } else {
            GL15.glBufferSubData(p_glBufferSubData_0_, p_glBufferSubData_1_, p_glBufferSubData_3_);
        }
    }

    public static void glCopyBufferSubData(int p_glCopyBufferSubData_0_, int p_glCopyBufferSubData_1_, long p_glCopyBufferSubData_2_, long p_glCopyBufferSubData_4_, long p_glCopyBufferSubData_6_) {
        if (openGL31) {
            GL31.glCopyBufferSubData(p_glCopyBufferSubData_0_, p_glCopyBufferSubData_1_, p_glCopyBufferSubData_2_, p_glCopyBufferSubData_4_, p_glCopyBufferSubData_6_);
        } else {
            ARBCopyBuffer.glCopyBufferSubData(p_glCopyBufferSubData_0_, p_glCopyBufferSubData_1_, p_glCopyBufferSubData_2_, p_glCopyBufferSubData_4_, p_glCopyBufferSubData_6_);
        }
    }

    public static String getCpu() {
        return cpu == null ? "<unknown>" : cpu;
    }
}
