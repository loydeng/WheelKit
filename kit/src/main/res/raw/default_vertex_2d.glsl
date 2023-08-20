varying vec2 fp;
attribute vec4 vc;
attribute vec4 fc;
uniform mat4 fm;

void main() {
    gl_Position = vc;
    fp = (fm * fc).xy;
}
