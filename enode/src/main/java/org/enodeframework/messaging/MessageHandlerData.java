package org.enodeframework.messaging;

import org.enodeframework.infrastructure.IObjectProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerData<T extends IObjectProxy> {
    public List<T> allHandlers = new ArrayList<>();
    public List<T> listHandlers = new ArrayList<>();
    public List<T> queuedHandlers = new ArrayList<>();
}
