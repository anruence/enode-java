package com.qianzhui.enode.rocketmq.client.ons;

public class ONSUtil {

    public static byte[] combineBytes(byte[] b1, byte[] b2) {
        int size = (null != b1 ? b1.length : 0) + (null != b2 ? b2.length : 0);
        byte[] total = new byte[size];
        if (null != b1) {
            System.arraycopy(b1, 0, total, 0, b1.length);
        }
        if (null != b2) {
            System.arraycopy(b2, 0, total, b1.length, b2.length);
        }

        return total;
    }
}
