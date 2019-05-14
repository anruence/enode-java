package com.enode.commanding;

import com.enode.common.IEnum;

public enum CommandAddResult implements IEnum {

    Success(1),
    DuplicateCommand(2);

    private int status;

    CommandAddResult(int status) {
        this.status = status;
    }

    @Override
    public int status() {
        return this.status;
    }
}
