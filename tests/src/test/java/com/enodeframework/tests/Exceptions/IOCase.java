package com.enodeframework.tests.Exceptions;

import com.enodeframework.common.exception.IORuntimeException;
import org.junit.Test;

public class IOCase {

    @Test
    public void testEx() {
        try {
            throw new IORuntimeException("AsyncCommandIOException" + 1);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            if (throwable instanceof IORuntimeException) {
                System.out.println("cause instance return true");
            }
            if (e instanceof IORuntimeException) {
                System.out.println("io");
            } else {
                System.out.println("mio");
            }
        }
    }
}
