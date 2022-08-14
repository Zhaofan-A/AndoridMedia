#include <stdio.h>
#include <stdlib.h>
#include "watermark.h"
#include "log.h"

WatermarkTool::WatermarkTool()
{
    LOGD("%s: constructor",__FUNCTION__);
}


WatermarkTool::~WatermarkTool()
{
    LOGD("%s: destructor",__FUNCTION__);
}


void WatermarkTool::calculateWaterXY(int x, int y, int w, int h, int width, int height,float* outVertexData)
{
    LOGD("%s: calculateWaterXY  start",__FUNCTION__);

    float pW = width / 2.0f, pH = height / 2.0f;
    float num_x,num_y,num_w,num_h;

    if (x > pW) {
        num_x = 1.0f / pW * (x - pW);
    } else {
        num_x = -1 * (1.0f - 1.0f / pW * x);
    }
    if (y > pH) {
        num_y = -1.0f / pH * (y - pH);
    } else {
        num_y = 1 - 1.0f / pH * y;
    }

    num_w = 1.0f / pW * w;
    num_h = 1.0f / pH * h;

    outVertexData[0] = num_x;
    outVertexData[1] = num_y;
    outVertexData[2] = 0;

    outVertexData[3] = num_x + num_w;
    outVertexData[4] = num_y;
    outVertexData[5] = 0;

    outVertexData[6] = num_x;
    outVertexData[7] = num_y + num_h;
    outVertexData[8] = 0;

    outVertexData[9] = num_x + num_w;
    outVertexData[10] = num_y + num_h;
    outVertexData[11] = 0;

    LOGD("%s: calculateWaterXY  end",__FUNCTION__);
}