package com.qianzhui.enode.infrastructure.impl;

import com.qianzhui.enode.infrastructure.ITimeProvider;

import java.util.Date;

public class DefaultTimeProvider implements ITimeProvider {
    @Override
    public Date getCurrentTime() {
        return new Date();
    }
}
