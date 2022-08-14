package com.byd.videoSdk.recorder.utils;

public class RecordConfig {

    private int WIDTH = Constants.IMAGE_WIDTH_1280;
    private int HEIGHT =  Constants.IMAGE_HEIGHT_720;
    private int FRAME_RATE = 25;      //帧率
    private int BIT_RATE = 16;        //比特率，码率 编码器每秒编出的数据大小
    private int I_FRAME_INTERVAL = 1; //I帧将要出现的时间间隔

    public int getWIDTH() {
        return WIDTH;
    }

    public void setWIDTH(int WIDTH) {
        this.WIDTH = WIDTH;
    }

    public int getHEIGHT() {
        return HEIGHT;
    }

    public void setHEIGHT(int HEIGHT) {
        this.HEIGHT = HEIGHT;
    }

    public int getFRAME_RATE() {
        return FRAME_RATE;
    }

    public void setFRAME_RATE(int FRAME_RATE) {
        this.FRAME_RATE = FRAME_RATE;
    }

    public int getBIT_RATE() {
        return BIT_RATE;
    }

    public void setBIT_RATE(int BIT_RATE) {
        this.BIT_RATE = BIT_RATE;
    }

    public int getI_FRAME_INTERVAL() {
        return I_FRAME_INTERVAL;
    }

    public void setI_FRAME_INTERVAL(int i_FRAME_INTERVAL) {
        I_FRAME_INTERVAL = i_FRAME_INTERVAL;
    }


    public RecordConfig(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }

    public RecordConfig(int FRAME_RATE, int BIT_RATE, int i_FRAME_INTERVAL) {
        this.FRAME_RATE = FRAME_RATE;
        this.BIT_RATE = BIT_RATE;
        I_FRAME_INTERVAL = i_FRAME_INTERVAL;
    }

    public RecordConfig(int WIDTH, int HEIGHT, int FRAME_RATE, int BIT_RATE, int i_FRAME_INTERVAL) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
        this.FRAME_RATE = FRAME_RATE;
        this.BIT_RATE = BIT_RATE;
        I_FRAME_INTERVAL = i_FRAME_INTERVAL;
    }

    @Override
    public String toString() {
        return "RecordConfig{" +
                "WIDTH=" + WIDTH +
                ", HEIGHT=" + HEIGHT +
                ", FRAME_RATE=" + FRAME_RATE +
                ", BIT_RATE=" + BIT_RATE +
                ", I_FRAME_INTERVAL=" + I_FRAME_INTERVAL +
                '}';
    }
}
