package com.enodeframework.tests.guava;

import com.enodeframework.common.io.Task;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class CacheMap {

    public static Logger log = LoggerFactory.getLogger(CacheMap.class);

    @Test
    public void cache() {
        RemovalListener<String, String> listener = notification -> {
            if (notification.wasEvicted()) {
                RemovalCause cause = notification.getCause();
                log.info("remove cause is :{}", cause.toString());
                log.info("key:{}.value:{}", notification.getKey(), notification.getValue());
            }
        };
        Cache<String, String> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(300, TimeUnit.MILLISECONDS)
                .removalListener(listener)
                .build();
        cache.put("s1", "a");
        cache.put("s2", "b");
        cache.put("s3", "c");
        cache.put("s4", "d");
        cache.cleanUp();
    }
}
