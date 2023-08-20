precision mediump float;

varying vec2 fp;
uniform sampler2D tx;

void main() {
    gl_FragColor = texture2D(tx, fp);
}