package com.enodeframework.common.utilities;

import com.enodeframework.common.exception.EnodeRuntimeException;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author anruence@gmail.com
 */
public class ObjectId {
    private static final int STATIC_MACHINE;
    private static final short STATIC_PID;
    private static final AtomicInteger STATIC_INCREMENT;
    private static String[] lookup32 = new String[256];

    static {
        STATIC_MACHINE = getMachineHash();
        STATIC_INCREMENT = new AtomicInteger(new Random().nextInt());
        STATIC_PID = (short) getCurrentProcessId();
        for (int i = 0; i < 256; i++) {
            lookup32[i] = String.format("%02x", i);
        }
    }

    private long timestamp;
    private int machine;
    private short pid;
    private int increment;

    public ObjectId(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        unpack(bytes);
    }

    public ObjectId(Date timestamp, int machine, short pid, int increment) {
        this(getTimestampFromDateTime(timestamp), machine, pid, increment);
    }

    public ObjectId(long timestamp, int machine, short pid, int increment) {
        if ((machine & 0xff000000) != 0) {
            throw new IllegalArgumentException("The machine value must be between 0 and 16777215 (it must fit in 3 bytes).");
        }
        if ((increment & 0xff000000) != 0) {
            throw new IllegalArgumentException("The increment value must be between 0 and 16777215 (it must fit in 3 bytes).");
        }

        this.timestamp = timestamp;
        this.machine = machine;
        this.pid = pid;
        this.increment = increment;
    }

    public static ObjectId generateNewId() {
        return generateNewId(getTimestampFromDateTime(new Date()));
    }

    public static ObjectId generateNewId(Date timestamp) {
        return generateNewId(getTimestampFromDateTime(timestamp));
    }

    public static ObjectId generateNewId(long timestamp) {
        // only use low order 3 bytes
        int increment = STATIC_INCREMENT.incrementAndGet() & 0x00ffffff;
        return new ObjectId(timestamp, STATIC_MACHINE, STATIC_PID, increment);
    }

    public static String generateNewStringId() {
        return generateNewId().toString();
    }

    public static byte[] pack(long timestamp, int machine, short pid, int increment) {
        if ((machine & 0xff000000) != 0) {
            throw new IllegalArgumentException("The machine value must be between 0 and 16777215 (it must fit in 3 bytes).");
        }
        if ((increment & 0xff000000) != 0) {
            throw new IllegalArgumentException("The increment value must be between 0 and 16777215 (it must fit in 3 bytes).");
        }

        byte[] bytes = new byte[12];
        bytes[0] = (byte) (timestamp >> 24);
        bytes[1] = (byte) (timestamp >> 16);
        bytes[2] = (byte) (timestamp >> 8);
        bytes[3] = (byte) (timestamp);
        bytes[4] = (byte) (machine >> 16);
        bytes[5] = (byte) (machine >> 8);
        bytes[6] = (byte) (machine);
        bytes[7] = (byte) (pid >> 8);
        bytes[8] = (byte) (pid);
        bytes[9] = (byte) (increment >> 16);
        bytes[10] = (byte) (increment >> 8);
        bytes[11] = (byte) (increment);
        return bytes;
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String val = lookup32[bytes[i] & 0xff];
            result.append(val);
        }
        return result.toString();
    }

    private static int getMachineHash() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] hash = md5.digest(hostName.getBytes());

            // use first 3 bytes of hash
            return ((hash[0] & 0xff) << 16) | ((hash[1] & 0xff) << 8) | hash[2] & 0xff;
        } catch (Exception ex) {
            throw new EnodeRuntimeException(ex);
        }
    }

    private static long getTimestampFromDateTime(Date timestamp) {
        return timestamp.getTime() / 1000;
    }

    public static int getCurrentProcessId() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        // format: "pid@hostname"
        String name = runtime.getName();
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Date getCreationTime() {
        return new Date(timestamp * 1000);
    }

    public int getMachine() {
        return machine;
    }

    public short getPid() {
        return pid;
    }

    public int getIncrement() {
        return increment;
    }

    public void unpack(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        if (bytes.length != 12) {
            throw new IllegalArgumentException("Byte array must be 12 bytes long.");
        }
        timestamp = ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
        machine = ((bytes[4] & 0xff) << 16) | ((bytes[5] & 0xff) << 8) | (bytes[6] & 0xff);
        pid = (short) (((bytes[7] & 0xff) << 8) | (bytes[8] & 0xff));
        increment = ((bytes[9] & 0xff) << 16) | ((bytes[10] & 0xff) << 8) | (bytes[11] & 0xff);
    }

    public byte[] toByteArray() {
        return pack(timestamp, machine, pid, increment);
    }

    @Override
    public String toString() {
        return toHexString(toByteArray());
    }
}
