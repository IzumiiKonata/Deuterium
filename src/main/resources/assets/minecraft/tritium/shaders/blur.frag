#version 130

uniform sampler2D u_diffuse_sampler;
uniform sampler2D u_other_sampler;
uniform vec2 u_texel_size;
uniform vec2 u_direction;
uniform float u_kernel[128];

void main()
{
    vec2 uv = gl_TexCoord[0].st;

    // Early discard if alpha is 0 and direction is horizontal
    float base_alpha = texture(u_other_sampler, uv).a;
    if (u_direction.x == 0.0 && base_alpha == 0.0) {
        discard;
    }

    // Main blur computation
    vec4 pixel_color = texture(u_diffuse_sampler, uv) * u_kernel[0];

    // Precompute direction * texel_size to avoid redundant calculations
    vec2 step = u_direction * u_texel_size;

    // Fully unrolled loop for radius = 5
    vec2 offset1 = step;
    pixel_color += (texture(u_diffuse_sampler, uv - offset1) + texture(u_diffuse_sampler, uv + offset1)) * u_kernel[1];

    vec2 offset2 = 2.0 * step;
    pixel_color += (texture(u_diffuse_sampler, uv - offset2) + texture(u_diffuse_sampler, uv + offset2)) * u_kernel[2];

    vec2 offset3 = 3.0 * step;
    pixel_color += (texture(u_diffuse_sampler, uv - offset3) + texture(u_diffuse_sampler, uv + offset3)) * u_kernel[3];

    vec2 offset4 = 4.0 * step;
    pixel_color += (texture(u_diffuse_sampler, uv - offset4) + texture(u_diffuse_sampler, uv + offset4)) * u_kernel[4];

    vec2 offset5 = 5.0 * step;
    pixel_color += (texture(u_diffuse_sampler, uv - offset5) + texture(u_diffuse_sampler, uv + offset5)) * u_kernel[5];

    // Output final color - use base_alpha directly from inputFramebuffer
    float final_alpha = u_direction.x == 0.0 ? base_alpha : 1.0;
    gl_FragColor = vec4(pixel_color.rgb, final_alpha);
}