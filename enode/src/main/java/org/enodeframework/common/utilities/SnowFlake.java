package org.enodeframework.common.utilities;

import com.google.common.base.Strings;
import org.enodeframework.common.exception.IdGenerateException;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;

public class SnowFlake {

    /**
     * 起始的时间戳
     */
    private final static long START_STAMP = 1480166465631L;

    /**
     * 每一部分占用的位数
     */
    private final static long SEQUENCE_BIT = 12; //序列号占用的位数
    private final static long MACHINE_BIT = 5;   //机器标识占用的位数
    private final static long DATACENTER_BIT = 5;//数据中心占用的位数

    /**
     * 每一部分的最大值
     */
    private final static long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private final static long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private final static long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    /**
     * 每一部分向左的位移
     */
    private final static long MACHINE_LEFT = SEQUENCE_BIT;
    private final static long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private final static long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;  //数据中心
    private final long machineId;     //机器标识
    private long sequence = 0L; //序列号
    private long lastStamp = -1L;//上一次时间戳

    public SnowFlake() {
        this.datacenterId = getDatacenterId();
        this.machineId = getMachineId(datacenterId);
    }

    public SnowFlake(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }


    /**
     * 获取 maxWorkerId
     */
    private long getMachineId(long datacenterId) {
        StringBuilder machineId = new StringBuilder();
        machineId.append(datacenterId);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        if (!Strings.isNullOrEmpty(name)) {
            /*
             *  jvm pid
             */
            machineId.append(name.split("@")[0]);
        }
        /*
         * mac + pid 的 hashcode 获取16个低位
         */
        return (machineId.toString().hashCode() & 0xffff) % (MAX_MACHINE_NUM + 1);
    }

    /**
     * 数据标识id部分
     */
    private long getDatacenterId() {
        long id = 0L;
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            if (network == null) {
                id = 1L;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (null != mac) {
                    id = ((0x000000FF & (long) mac[mac.length - 1]) | (0x0000FF00 & (((long) mac[mac.length - 2]) << 8))) >> 6;
                    id = id % (MAX_DATACENTER_NUM + 1);
                }
            }
        } catch (Exception ignored) {
        }
        return id;
    }


    /**
     * 产生下一个ID
     */
    public synchronized long nextId() {
        long currStamp = getStamp();
        if (currStamp < lastStamp) {
            throw new IdGenerateException("Clock moved backwards. Refusing to generate id");
        }
        if (currStamp == lastStamp) {
            //相同毫秒内，序列号自增
            sequence = (sequence + 1) & MAX_SEQUENCE;
            //同一毫秒的序列数已经达到最大
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            //不同毫秒内，序列号置为0
            sequence = 0L;
        }

        lastStamp = currStamp;

        return (currStamp - START_STAMP) << TIMESTAMP_LEFT //时间戳部分
                | datacenterId << DATACENTER_LEFT       //数据中心部分
                | machineId << MACHINE_LEFT             //机器标识部分
                | sequence;                             //序列号部分
    }

    private long getNextMill() {
        long mill = getStamp();
        while (mill <= lastStamp) {
            mill = getStamp();
        }
        return mill;
    }

    private long getStamp() {
        return System.currentTimeMillis();
    }
}