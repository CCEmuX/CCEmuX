#version 300 es

precision mediump float;

in vec4 sh_Colour;

out vec4 out_Colour;

void main() {
    out_Colour = sh_Colour;
}