#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec4 aTexCoord;

out vec2 vTexCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexCoord = (uTexMatrix * aTexCoord).xy;
}