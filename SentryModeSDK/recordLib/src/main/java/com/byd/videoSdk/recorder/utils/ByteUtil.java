package com.byd.videoSdk.recorder.utils;

import android.util.Base64;

import java.nio.charset.Charset;

/**
 * Byte utils.
 * big endian.
 *
 * @author bai.yu1
 */
public class ByteUtil {
    /**
     * Big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (data & 0xff);
        bytes[0] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[1] = (byte) (data);
        bytes[0] = (byte) (data >> 8);
        return bytes;
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(int data) {
        byte[] bytes = new byte[4];
        bytes[3] = (byte) (data & 0xff);
        bytes[2] = (byte) ((data & 0xff00) >> 8);
        bytes[1] = (byte) ((data & 0xff0000) >> 16);
        bytes[0] = (byte) ((data & 0xff000000) >> 24);
        return bytes;
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[7] = (byte) (data & 0xff);
        bytes[6] = (byte) ((data >> 8) & 0xff);
        bytes[5] = (byte) ((data >> 16) & 0xff);
        bytes[4] = (byte) ((data >> 24) & 0xff);
        bytes[3] = (byte) ((data >> 32) & 0xff);
        bytes[2] = (byte) ((data >> 40) & 0xff);
        bytes[1] = (byte) ((data >> 48) & 0xff);
        bytes[0] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(float data) {
        int intBits = Float.floatToIntBits(data);
        return getBytes(intBits);
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return getBytes(intBits);
    }

    /**
     * @param data
     * @param charsetName
     * @return
     */
    public static byte[] getBytes(String data, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        return data.getBytes(charset);
    }

    /**
     * big endian
     *
     * @param data
     * @return
     */
    public static byte[] getBytes(String data) {
        return getBytes(data, "GBK");
    }

    /**
     * big endian
     *
     * @param bytes
     * @return
     */
    public static short getShort(byte[] bytes) {
        return (short) ((0xff & bytes[1]) | (0xff00 & (bytes[0] << 8)));
    }

    /**
     * big endian
     *
     * @param bytes
     * @return
     */
    public static char getChar(byte[] bytes) {
        return (char) ((0xff & bytes[1]) | (0xff00 & (bytes[0] << 8)));
    }

    /**
     * @param bytes
     * @return
     */
    public static int getInt(byte[] bytes) {
        return (0xff & bytes[3]) | (0xff00 & (bytes[2] << 8))
                | (0xff0000 & (bytes[1] << 16))
                | (0xff000000 & (bytes[0] << 24));
    }

    /**
     * big endian
     *
     * @param bytes
     * @return
     */
    public static long getLong(byte[] bytes) {
        return (0xffL & (long) bytes[7]) | (0xff00L & ((long) bytes[6] << 8))
                | (0xff0000L & ((long) bytes[5] << 16))
                | (0xff000000L & ((long) bytes[4] << 24))
                | (0xff00000000L & ((long) bytes[3] << 32))
                | (0xff0000000000L & ((long) bytes[2] << 40))
                | (0xff000000000000L & ((long) bytes[1] << 48))
                | (0xff00000000000000L & ((long) bytes[0] << 56));
    }

    /**
     * big endian
     *
     * @param bytes
     * @return
     */
    public static float getFloat(byte[] bytes) {
        return Float.intBitsToFloat(getInt(bytes));
    }

    /**
     * big endian
     *
     * @param bytes
     * @return
     */
    public static double getDouble(byte[] bytes) {
        long l = getLong(bytes);
        return Double.longBitsToDouble(l);
    }

    /**
     * big endian
     *
     * @param bytes
     * @param charsetName
     * @return
     */
    public static String getString(byte[] bytes, String charsetName) {
        return new String(bytes, Charset.forName(charsetName));
    }

    /**
     * @param bytes
     * @return
     */
    public static String getStringGBK(byte[] bytes) {
        return getString(bytes, "GBK");
    }


    public static String bs2hexStr(byte[] bytes) {
        if (bytes == null) return null;
        StringBuilder hexStr = new StringBuilder();
        for (byte b: bytes) {
            hexStr.append(b2hexStr(b));
        }
        return hexStr.toString();
    }


    public static String b2hexStr(byte b) {
        return String.format("%02x", b);
    }

    public static String b64Encode(byte[] data) {
        if (data == null) return "";
        return Base64.encodeToString(data, Base64.NO_WRAP | Base64.NO_PADDING);
    }

    public static byte[] b64Decode(String text) {
        if (text == null) return new byte[0];
        return Base64.decode(text, Base64.NO_WRAP | Base64.NO_PADDING);
    }
}