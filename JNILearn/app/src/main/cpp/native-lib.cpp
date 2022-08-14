#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include "motion_detection.h"
#include <opencv2/opencv.hpp>
using namespace cv;
using namespace std;



using namespace cv;
using namespace std;
motion_detection *pmotion_detection = nullptr;
Mat imgY;
float scale;
extern "C"
JNIEXPORT void JNICALL
Java_com_example_jnilearn2_NativeLibrary_init(JNIEnv *env, jobject thiz, jint w, jint h,jfloat oversize_scale) {
    // TODO: implement init()
    scale = oversize_scale;
    pmotion_detection = new motion_detection(w,h,scale,500,50000,20);
    imgY = Mat::zeros(Size(w*scale,h*scale),CV_8UC4);
}extern "C"
JNIEXPORT void JNICALL
Java_com_example_jnilearn2_NativeLibrary_destroy(JNIEnv *env, jobject thiz) {
    // TODO: implement destory()
    if(pmotion_detection!= nullptr)
        delete pmotion_detection;

    pmotion_detection = nullptr;
}extern "C"
JNIEXPORT jintArray JNICALL
Java_com_example_jnilearn2_NativeLibrary_detection(JNIEnv *env, jobject thiz, jbyteArray buf, jint w,
                                                  jint h) {
    // TODO: implement detection()
    // TODO: implement gray()
    jbyte *cbuf;
    cbuf = env->GetByteArrayElements(buf, JNI_FALSE );
    if (cbuf == NULL) {
        return 0;
    }
    uchar* ptr = imgY.ptr(0);
    uchar* buf_start = (uchar*)cbuf;
    if(pmotion_detection->detection(buf_start,ptr))
    {
        LOGD("warning!!!");
    } else
    {
        LOGD("pass!!!!!");
    }
    int size = w * h*scale *scale;
    jintArray result = env->NewIntArray(size);
    env->SetIntArrayRegion(result, 0, size, (int*)ptr);
    env->ReleaseByteArrayElements(buf, cbuf, 0);
    return result;
}extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_jnilearn2_NativeLibrary_detection2(JNIEnv *env, jobject thiz, jbyteArray buf,
                                                   jint w, jint h) {
    // TODO: implement detection2()
    jbyte *cbuf;
    cbuf = env->GetByteArrayElements(buf, JNI_FALSE );
    if (cbuf == NULL) {
        return 0;
    }
    uchar* ptr = imgY.ptr(0);
    uchar* buf_start = (uchar*)cbuf;
    if(pmotion_detection->detection(buf_start,ptr))
    {
        LOGD("warning!!!");
        return true;
    } else
    {
        LOGD("pass!!!!!");
        return false;
    }

}