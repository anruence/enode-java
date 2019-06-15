package com.enodeframework.samples.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TestService {

    public static Logger logger = LoggerFactory.getLogger(TestService.class);

    public void sayHello() {
        logger.info("hello");
    }

}
