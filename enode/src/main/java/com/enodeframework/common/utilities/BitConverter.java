package com.enodeframework.common.utilities;

import java.nio.charset.Charset;

/**
 * @author anruence@gmail.com
 */
public class BitConverter {
    private static final Charset UTF8 = Charset.forName("utf-8");

    public static byte[] getBytes(short v) {
        return new byte[]{(byte) v, (byte) (v >> 8)};
    }

    public static byte[] getBytes(int v) {
        return new byte[]{(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24)};
    }

    public static byte[] getBytes(long v) {
        return new byte[]{(byte) v, (byte) (v >> 8), (byte) (v >> 16), (byte) (v >> 24), (byte) (v >> 32), (byte) (v >> 40), (byte) (v >> 48), (byte) (v >> 56)};
    }

    public static byte[] getBytes(String v) {
        return v.getBytes(UTF8);
    }

    public static short toShort(byte[] b) {
        return (short) (b[0] & 0xff | (b[1] & 0xff) << 8);
    }

    public static int toInt(byte[] b) {
        return b[0] & 0xff | (b[1] & 0xff) << 8 | (b[2] & 0xff) << 16 | (b[3] & 0xff) << 24;
    }

    public static long toLong(byte[] b) {
        return b[0] & (long) 255 | (b[1] & (long) 255) << 8 | (b[2] & (long) 255) << 16 | (b[3] & (long) 255) << 24 | (b[4] & (long) 255) << 32
                | (b[5] & (long) 255) << 40 | (b[6] & (long) 255) << 48 | (b[7] & (long) 255) << 56;
    }

    public static String toString(byte[] b) {
        return new String(b, UTF8);
    }
}
