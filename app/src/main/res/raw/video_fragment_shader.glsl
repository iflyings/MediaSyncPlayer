#version 300 es
#extension GL_OES_EGL_image_external_essl3 : require

precision highp float;

out vec4 vColor;
in vec2 vTexCoord;
uniform samplerExternalOES uTexSampler;

