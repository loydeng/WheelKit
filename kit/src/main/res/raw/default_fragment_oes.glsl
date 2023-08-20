#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 fp;
uniform samplerExternalOES tx;

void main() {
    gl_FragColor = texture2D(tx, fp);
}