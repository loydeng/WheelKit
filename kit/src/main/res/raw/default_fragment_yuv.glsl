precision mediump float;

varying vec2 fp;

uniform sampler2D y_tex;
uniform sampler2D u_tex;
uniform sampler2D v_tex;

vec4 yuv2rgb(vec2 p) {
    // 其中 *1.16438,是将 Y 分量范围 (16 - 235,根据 ITU-R BT.601 标准) 映射至 RGB范围(0 - 255), 即 1.16438 ≈ 255 / (235-16)
    //
    float y = texture2D(y_tex, p).r * 1.16438;
    float u = texture2D(u_tex, p).r;
    float v = texture2D(v_tex, p).r;
    return vec4(y + 1.59603 * v - 0.874202, y - 0.391762 * u - 0.812968 * v + 0.531668, y + 2.01723 * u - 1.08563, 1);
}

void main() {
    gl_FragColor = yuv2rgb(fp);
}
