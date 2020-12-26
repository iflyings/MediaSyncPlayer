#version 300 es

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec4 aTexCoord;
layout(location = 2) in vec4 aMaskCoord;

out vec2 vTexCoord;
out vec2 vMaskCoord;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexCoord = (uTexMatrix * aTexCoord).xy;
    vMaskCoord = aMaskCoord.xy;
}