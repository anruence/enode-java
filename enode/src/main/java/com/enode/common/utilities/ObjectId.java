package com.enode.common.utilities;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ObjectId {
    private static final int __staticMachine;
    private static final short __staticPid;
    private static final AtomicInteger __staticIncrement;

    private long _timestamp;
    private int _machine;
    private short _pid;
    private int _increment;

    private static String[] _lookup32 = new String[256];

    static {
        __staticMachine = getMachineHash();
        __staticIncrement = new AtomicInteger(new Random().nextInt());
        __staticPid = (short) getCurrentProcessId();
        for (int i = 0; i < 256; i++) {
            _lookup32[i] = String.format("%02x", i);
        }
    }

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

        _timestamp = timestamp;
        _machine = machine;
        _pid = pid;
        _increment = increment;
    }

    public long getTimestamp() {
        return _timestamp;
    }

    public Date getCreationTime() {
        return new Date(_timestamp * 1000);
    }

    public int getMachine() {
        return _machine;
    }

    public short getPid() {
        return _pid;
    }

    public int getIncrement() {
        return _increment;
    }

    public static ObjectId generateNewId() {
        return generateNewId(getTimestampFromDateTime(new Date()));
    }

    public static ObjectId generateNewId(Date timestamp) {
        return generateNewId(getTimestampFromDateTime(timestamp));
    }

    public static ObjectId generateNewId(long timestamp) {
        int increment = __staticIncrement.incrementAndGet() & 0x00ffffff;// only use low order 3 bytes
        return new ObjectId(timestamp, __staticMachine, __staticPid, increment);
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

    public void unpack(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        if (bytes.length != 12) {
            throw new IllegalArgumentException("Byte array must be 12 bytes long.");
        }
        _timestamp = ((bytes[0] & 0xff) << 24) | ((bytes[1] & 0xff) << 16) | ((bytes[2] & 0xff) << 8) | (bytes[3] & 0xff);
        _machine = ((bytes[4] & 0xff) << 16) | ((bytes[5] & 0xff) << 8) | (bytes[6] & 0xff);
        _pid = (short) (((bytes[7] & 0xff) << 8) | (bytes[8] & 0xff));
        _increment = ((bytes[9] & 0xff) << 16) | ((bytes[10] & 0xff) << 8) | (bytes[11] & 0xff);
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new NullPointerException("bytes");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            String val = _lookup32[bytes[i] & 0xff];
            result.append(val);
        }
        return result.toString();
    }

    public byte[] toByteArray() {
        return pack(_timestamp, _machine, _pid, _increment);
    }

    @Override
    public String toString() {
        return toHexString(toByteArray());
    }

    private static int getMachineHash() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] hash = md5.digest(hostName.getBytes());

            return ((hash[0] & 0xff) << 16) | ((hash[1] & 0xff) << 8) | hash[2] & 0xff; // use first 3 bytes of hash
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static long getTimestampFromDateTime(Date timestamp) {
        return timestamp.getTime() / 1000;
    }

    public static int getCurrentProcessId() {
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        String name = runtime.getName(); // format: "pid@hostname"
        try {
            return Integer.parseInt(name.substring(0, name.indexOf('@')));
        } catch (Exception e) {
            return -1;
        }
    }
}
