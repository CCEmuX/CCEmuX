#version 300 es

// per-vertex
layout(location = 0) in vec3 in_Position;
layout(location = 1) in vec2 in_TexCoords;

// per-instance
layout(location = 2) in mat4 in_InstTransform;

uniform mat4 u_MVPMatrix;

void main() {
    gl_Position = u_MVPMatrix * in_InstTransform * vec4(in_Position, 1.0f);
}
