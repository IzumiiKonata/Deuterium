package tritium.rendering.shader;

import tritium.rendering.shader.impl.*;
import tritium.rendering.shader.impl.*;
import tritium.rendering.shader.impl.StencilShader;

public class Shaders {
    public static Shader POST_BLOOM_SHADER = new BloomShader();
    public static Shader UI_BLOOM_SHADER = POST_BLOOM_SHADER;
    public static Shader UI_POST_BLOOM_SHADER = POST_BLOOM_SHADER;
    public static GaussianBlurShader GAUSSIAN_BLUR_SHADER = new GaussianBlurShader();
    public static Shader UI_GAUSSIAN_BLUR_SHADER = GAUSSIAN_BLUR_SHADER;
    public static BlendShader BLEND = new BlendShader();
    public static ColorShader COLOR = new ColorShader();

    public static MotionShader MOTION = new MotionShader();

    public static ROQShader ROQ_SHADER = new ROQShader();
    public static ROGQShader ROGQ_SHADER = new ROGQShader();
    public static RQShader RQ_SHADER = new RQShader();
    public static RQTShader RQT_SHADER = new RQTShader();
    public static RQGShader RQG_SHADER = new RQGShader();

    public static Deconverge DECONVERGE = new Deconverge();

    public static StencilShader STENCIL = new StencilShader();
}
