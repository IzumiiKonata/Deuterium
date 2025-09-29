#version 120

uniform sampler2D currentTexture;
uniform sampler2D previousTexture;
uniform vec2 resolution;

void main(void)
{
    vec2 uv = gl_TexCoord[0].st;

    // 获取当前帧和前一帧的像素颜色
    vec4 current = texture2D(currentTexture, uv);
    vec4 previous = texture2D(previousTexture, uv);

    gl_FragColor = vec4((current.rgb + previous.rgb) / 2, 1.0);
}