package org.enodeframework.tests.commandhandlers;

import org.springframework.stereotype.Component;

@Component
public class TestComponent {

    public String sayHello() {
        return "hello, world";
    }
}
