varying vec2 fp;
uniform vec2 xUnit;
uniform vec4 coeffs;

// refer to org.webrtc.YuvConverter
void main() {
  gl_FragColor.r = coeffs.a + dot(coeffs.rgb, sampler(fp - 1.5 * xUnit).rgb);
  gl_FragColor.g = coeffs.a + dot(coeffs.rgb, sampler(fp - 0.5 * xUnit).rgb);
  gl_FragColor.b = coeffs.a + dot(coeffs.rgb, sampler(fp + 0.5 * xUnit).rgb);
  gl_FragColor.a = coeffs.a + dot(coeffs.rgb, sampler(fp + 1.5 * xUnit).rgb);
}
