package com.enodeframework.messaging;

import com.enodeframework.infrastructure.IObjectProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class MessageHandlerData<T extends IObjectProxy> {
    public List<T> AllHandlers = new ArrayList<>();
    public List<T> ListHandlers = new ArrayList<>();
    public List<T> QueuedHandlers = new ArrayList<>();
}
