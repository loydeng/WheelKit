//
// Created by loyde on 2023/2/22.
//

#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_JavaI420Buffer_nativeCropAndScaleI420(JNIEnv *env, jclass clazz,
                                                              jobject src_y, jint src_stride_y,
                                                              jobject src_u, jint src_stride_u,
                                                              jobject src_v, jint src_stride_v,
                                                              jint crop_x, jint crop_y,
                                                              jint crop_width, jint crop_height,
                                                              jobject dst_y, jint dst_stride_y,
                                                              jobject dst_u, jint dst_stride_u,
                                                              jobject dst_v, jint dst_stride_v,
                                                              jint scale_width, jint scale_height) {
}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_capture_NV21Buffer_nativeCropAndScale(JNIEnv *env, jclass clazz, jint crop_x,
                                                       jint crop_y, jint crop_width,
                                                       jint crop_height, jint scale_width,
                                                       jint scale_height, jbyteArray src,
                                                       jint src_width, jint src_height,
                                                       jobject dst_y, jint dst_stride_y,
                                                       jobject dst_u, jint dst_stride_u,
                                                       jobject dst_v, jint dst_stride_v) {

}

extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_JniCommon_nativeAddRef(JNIEnv *env, jclass clazz,
                                               jlong ref_counted_pointer) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_JniCommon_nativeReleaseRef(JNIEnv *env, jclass clazz,
                                                   jlong ref_counted_pointer) {

}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_loy_kit_render_JniCommon_nativeAllocateByteBuffer(JNIEnv *env, jclass clazz, jint size) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_JniCommon_nativeFreeByteBuffer(JNIEnv *env, jclass clazz, jobject buffer) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_render_TimestampAligner_nativeRtcTimeNanos(JNIEnv *env, jclass clazz) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_render_TimestampAligner_nativeCreateTimestampAligner(JNIEnv *env, jclass clazz) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_TimestampAligner_nativeReleaseTimestampAligner(JNIEnv *env, jclass clazz,
                                                                       jlong timestamp_aligner) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_render_TimestampAligner_nativeTranslateTimestamp(JNIEnv *env, jclass clazz,
                                                                  jlong timestamp_aligner,
                                                                  jlong camera_time_ns) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_YuvHelper_nativeCopyPlane(JNIEnv *env, jclass clazz, jobject src,
                                                  jint src_stride, jobject dst, jint dst_stride,
                                                  jint width, jint height) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_YuvHelper_nativeI420Copy(JNIEnv *env, jclass clazz, jobject src_y,
                                                 jint src_stride_y, jobject src_u,
                                                 jint src_stride_u, jobject src_v,
                                                 jint src_stride_v, jobject dst_y,
                                                 jint dst_stride_y, jobject dst_u,
                                                 jint dst_stride_u, jobject dst_v,
                                                 jint dst_stride_v, jint width, jint height) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_YuvHelper_nativeI420ToNV12(JNIEnv *env, jclass clazz, jobject src_y,
                                                   jint src_stride_y, jobject src_u,
                                                   jint src_stride_u, jobject src_v,
                                                   jint src_stride_v, jobject dst_y,
                                                   jint dst_stride_y, jobject dst_uv,
                                                   jint dst_stride_uv, jint width, jint height) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_YuvHelper_nativeI420Rotate(JNIEnv *env, jclass clazz, jobject src_y,
                                                   jint src_stride_y, jobject src_u,
                                                   jint src_stride_u, jobject src_v,
                                                   jint src_stride_v, jobject dst_y,
                                                   jint dst_stride_y, jobject dst_u,
                                                   jint dst_stride_u, jobject dst_v,
                                                   jint dst_stride_v, jint src_width,
                                                   jint src_height, jint rotation_mode) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_render_YuvHelper_nativeABGRToI420(JNIEnv *env, jclass clazz, jobject src,
                                                   jint src_stride, jobject dst_y,
                                                   jint dst_stride_y, jobject dst_u,
                                                   jint dst_stride_u, jobject dst_v,
                                                   jint dst_stride_v, jint width, jint height) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_render_EglBase10Impl_nativeGetCurrentNativeEGLContext(JNIEnv *env, jclass clazz) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_capture_Histogram_nativeCreateCounts(JNIEnv *env, jclass clazz, jstring name,
                                                      jint min, jint max, jint bucket_count) {

}
extern "C"
JNIEXPORT jlong JNICALL
Java_com_loy_kit_capture_Histogram_nativeCreateEnumeration(JNIEnv *env, jclass clazz, jstring name,
                                                           jint max) {

}
extern "C"
JNIEXPORT void JNICALL
Java_com_loy_kit_capture_Histogram_nativeAddSample(JNIEnv *env, jclass clazz, jlong handle,
                                                   jint sample) {

}
