#version 300 es

precision mediump float;

in vec4 sh_Colour;

void main() {
    gl_FragColor = sh_Colour;
}