#version 130

uniform sampler2D u_diffuse_sampler;
uniform sampler2D u_other_sampler;
uniform vec2 u_texel_size;
uniform vec2 u_direction;

highp float gaussian(float x, float sigma) {
    return exp(-0.5 * (x * x) / (sigma * sigma));
}

void main() {
    highp vec2 uv = gl_TexCoord[0].st;

    // 从 u_other_sampler 读取 alpha 值
    highp float alpha = texture2D(u_other_sampler, uv).a;

    // 丢弃
    if (u_direction.x == 0.0 && alpha == 0.0) {
        discard;
    }

    highp float rad = 1.0 + 10 * alpha;

    // 标准差
    highp float sigma = rad / 2.0;

    highp vec3 pixel_color = vec3(0.0);
    highp float kernelSum = 0.0;

    // 高斯模糊采样
    for (highp float i = -rad; i <= rad; i++) {
        highp vec2 offset = i * u_texel_size * u_direction;
        highp vec4 color = texture2D(u_diffuse_sampler, uv + offset) + texture2D(u_diffuse_sampler, uv - offset);

        // 计算权重
        highp float weight = gaussian(i, sigma);

        // 累加颜色和权重
        pixel_color += color.rgb * weight;
        kernelSum += weight * 2;
    }

    // 归一
    pixel_color /= kernelSum;

    gl_FragColor = vec4(pixel_color, 1.0);
}