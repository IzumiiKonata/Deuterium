#version 130

uniform sampler2D textureIn;
uniform sampler2D stencilTex;

void main() {

    vec2 uv = gl_TexCoord[0].st;

    vec4 stColor = texture2D(stencilTex, uv);

    if (stColor.a == 0.0) {
        discard;
    }

    vec4 texColor = texture2D(textureIn, uv);

    gl_FragColor = vec4(texColor.rgb, stColor.a * texColor.a);

}