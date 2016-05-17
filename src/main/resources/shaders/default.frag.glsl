#version 300 es

precision mediump float;

in vec4 sh_Colour;
in vec2 sh_TexCoords;

out vec4 out_Colour;

uniform sampler2D u_Texture;

void main() {
    vec4 texel = texture(u_Texture, sh_TexCoords);
    out_Colour = texel * sh_Colour;
}