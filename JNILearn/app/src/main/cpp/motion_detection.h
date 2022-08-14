//
// Created by suzhiyong on 20-10-15.
//

#ifndef JNILEARN_MOTION_DETECTION_H
#define JNILEARN_MOTION_DETECTION_H

#include <opencv2/opencv.hpp>
#include <iostream>
#include <vector>
#include <android/log.h>
#define  LOG_TAG    __FILE__
#define  LOGI(format, args...) { fprintf(stderr, format, ##args); __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, format, ##args); }
#define  LOGE(format, args...) { fprintf(stderr, format, ##args); __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, format, ##args); }
#define  LOGD(format, args...) { fprintf(stderr, format, ##args); __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, format, ##args); }
using namespace std;
using namespace cv;
class motion_detection {
public:
    motion_detection(const int width, const int height, const float scale,  int min_area,  int max_area,  int min_side);
    ~motion_detection() = default;
    bool detection(const unsigned char* const src, unsigned char* const dst);
private:
    int width_, height_, min_area_, max_area_, min_side_;
    float scale_;
    vector<vector<Point>> contours;
    vector<Vec4i> hireachy;
    Mat resize_bgr, frame_bgr, frame_yuv, mogMask, mogMask_out, kernel; // nv21
    Ptr<BackgroundSubtractor> pMOG2;
};


#endif //JNILEARN_MOTION_DETECTION_H
