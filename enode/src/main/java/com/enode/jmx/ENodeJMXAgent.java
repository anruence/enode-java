package com.enode.jmx;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class ENodeJMXAgent {
    public static void startAgent() {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        try {
            ObjectName forkJoinName = new ObjectName("com.enode:name=ForkJoinPool");
            server.registerMBean(new ForkJoinPool(), forkJoinName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
