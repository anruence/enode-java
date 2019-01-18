package com.enode.infrastructure.impl;

import com.enode.infrastructure.ITimeProvider;

import java.util.Date;

public class DefaultTimeProvider implements ITimeProvider {
    @Override
    public Date getCurrentTime() {
        return new Date();
    }
}
