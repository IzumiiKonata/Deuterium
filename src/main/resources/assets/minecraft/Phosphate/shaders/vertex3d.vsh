#version 120

uniform mat4 modelViewMatrix;
uniform mat4 ProjectionMatrix;

void main() {
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_Position = modelViewMatrix * ProjectionMatrix * gl_Vertex;
}