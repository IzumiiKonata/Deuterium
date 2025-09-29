#version 120

uniform float u_radius;
uniform vec4 u_color;
uniform float u_angle;
uniform vec2 u_texel_size;

void main() {
    float width = u_radius * 2;
    float height = width;

    // 计算相对于圆心的位置
    vec2 center = gl_TexCoord[0].st * vec2(width, height) - vec2(u_radius, u_radius);

    vec2 cCenter = vec2(u_radius, u_radius);
    vec2 curPoint = gl_TexCoord[0].st * vec2(width, height);

    float dx = curPoint.x - cCenter.x;
    float dy = curPoint.y - cCenter.y;

    const float PI2 = 6.2831853071795864769252867665590; // 2π
    float ang = mod(atan(dy, dx) + PI2, PI2); // 计算角度并调整范围
    float angDeg = degrees(ang); // 转换为角度

    // 计算到圆心的距离
    float distance = length(center);

    // 渐变边缘的透明度计算
    float alpha = 1.0 - smoothstep(u_radius - 1.0, u_radius, distance);

    const float u_edge_width = 10;

    // 如果超出扇形角度范围，但还在渐变边缘范围内
    if (angDeg > u_angle && angDeg <= u_angle + u_edge_width) {
        // 计算渐变透明度
        float edgeAlpha = 1.0 - smoothstep(u_angle, u_angle + u_edge_width, angDeg);
        gl_FragColor = vec4(u_color.rgb, u_color.a * alpha * edgeAlpha);
    } else if (angDeg <= u_angle) {
        // 在扇形角度范围内
        gl_FragColor = vec4(u_color.rgb, u_color.a * alpha);
    } else {
        // 超出渐变范围，完全透明
        gl_FragColor = vec4(0.0, 0.0, 0.0, 0.0);
    }
}