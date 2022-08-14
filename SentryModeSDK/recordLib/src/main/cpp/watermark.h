#include <jni.h>
#include <string>

class WatermarkTool
{
public:
    WatermarkTool();
    ~WatermarkTool();

     void calculateWaterXY(int x, int y, int w, int h, int width, int height ,float* outVertexData);
};
