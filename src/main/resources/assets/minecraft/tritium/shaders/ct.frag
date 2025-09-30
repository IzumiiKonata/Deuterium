#version 120

// colored texture
uniform sampler2D texture;
uniform vec4 color;

void main() {
    vec2 uv = gl_TexCoord[0].st;
    vec4 texColor = texture2D(texture, uv);

    if (texColor.a == 0.0f)
            discard;

    gl_FragColor = vec4(color.rgb, color.a * texColor.a);
}