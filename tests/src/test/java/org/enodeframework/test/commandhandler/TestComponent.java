package org.enodeframework.test.commandhandler;

import org.springframework.stereotype.Component;

@Component
public class TestComponent {

    public String sayHello() {
        return "hello, world";
    }
}
