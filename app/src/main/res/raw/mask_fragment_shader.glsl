#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision highp float;

out vec4 vColor;

in vec2 vTexCoord;
in vec2 vMaskCoord;
uniform samplerExternalOES uTexSampler;
uniform sampler2D uMaskSampler;

void main() {
    vec4 color = texture(uMaskSampler, vMaskCoord);
    if (color.r <= 0.5 && color.g <= 0.5 && color.b <= 0.5) {
        discard;
    }
    vColor = texture(uTexSampler, vTexCoord);
}
