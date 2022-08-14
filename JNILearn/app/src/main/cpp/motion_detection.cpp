#include "motion_detection.h"
motion_detection::motion_detection(const int width, const int height, const float scale,  int min_area,
                                  int max_area ,  int min_side )
{
    width_ = width;
    height_ = height;
    scale_ = scale;
    min_area_ = min_area * scale * scale;
    max_area_ = max_area* scale * scale;
    min_side_ = min_side* scale ;
    kernel = getStructuringElement(MORPH_RECT, Size(3, 3), Point(-1, -1));
    pMOG2 = createBackgroundSubtractorKNN(1000, 400.0, false);
    //pMOG2 = createBackgroundSubtractorMOG2(1000, 16.0, false);
    mogMask_out = cv::Mat::zeros(cv::Size(width_*scale, height_*scale), CV_8UC4);
    resize_bgr = cv::Mat::zeros(cv::Size(width_*scale, height_*scale), CV_8UC3);
    frame_bgr = cv::Mat::zeros(cv::Size(width_, height_), CV_8UC3);
    frame_yuv = cv::Mat::zeros(cv::Size(width_, height_ * 3 / 2), CV_8UC1);
}

bool motion_detection::detection(const unsigned char *const src, unsigned char *const dst)
{
    memcpy(frame_yuv.data, src, sizeof(unsigned char) * width_ * height_ * 3 / 2);
    cvtColor(frame_yuv, frame_bgr, COLOR_YUV2BGR_NV21);
    // memcpy(frame_bgr.data, src, sizeof(unsigned char) * width_ * height_ * 3);
    resize(frame_bgr, resize_bgr, Size(width_*scale_, height_*scale_), INTER_AREA);
    pMOG2->apply(resize_bgr, mogMask, 0.7); //数值越大，背景更新越快
    morphologyEx(mogMask, mogMask, MORPH_ERODE, kernel, Point(-1, -1));
    findContours(mogMask, contours, hireachy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, Point(0, 0));
    int count = 0;
    cvtColor(mogMask, mogMask, COLOR_GRAY2BGR);
    for (size_t t = 0; t < contours.size(); t++)
    {
        double area = contourArea(contours[t]);
        if (area < min_area_ || area > max_area_)
        {
            continue;
        }
        Rect selection = boundingRect(contours[t]);
        if (selection.width < min_side_ || selection.height < min_side_)
        {
            continue;
        }
        count++;
        rectangle(mogMask, selection, Scalar(0, 0, 255), 2, 8);
    }

    cvtColor(mogMask, mogMask_out, COLOR_BGR2RGBA);
    //cvtColor(frame_bgr, mogMask_out, COLOR_BGR2RGBA);
    memcpy(dst, mogMask_out.data, sizeof(unsigned char) * width_ *scale_* height_*scale_*4);
    if(count > 0)
    {
        return true;
    }
    else
    {
        return false;
    }
}