#version 120

attribute vec4 Position;
uniform mat4 gbufferProjectionInverse;
uniform mat4 gbufferModelViewInverse;

void main() {
    gl_Position = ftransform();
}