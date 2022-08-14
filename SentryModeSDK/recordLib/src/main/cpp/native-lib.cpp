#include <jni.h>
#include <string>
#include "watermark.h"

WatermarkTool *watermarkTool = nullptr;

extern "C"
JNIEXPORT void JNICALL
Java_com_byd_videoSdk_watermark_NativeLibrary_initWatermark(
        JNIEnv *env,jobject thiz) {

    watermarkTool = new WatermarkTool();
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_byd_videoSdk_watermark_NativeLibrary_stringFromJNI(
        JNIEnv *env,jobject thiz) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_byd_videoSdk_watermark_NativeLibrary_initWaterVertexData(
        JNIEnv *env,jobject thiz,jint x, jint y,jint w, jint h,jint width, jint height, jfloatArray  outVertexData) {

    jfloat* outVertexDataFloat = env->GetFloatArrayElements(outVertexData, NULL);
    float* poutVertexData = (float*)outVertexDataFloat;

    watermarkTool->calculateWaterXY((int)x,(int)y,(int)w,(int)h,(int)width,(int)height,poutVertexData);

    env->ReleaseFloatArrayElements(outVertexData, outVertexDataFloat, 0);
}
