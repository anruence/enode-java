package com.enode.commanding;

import com.enode.common.extensions.IEnum;

public enum CommandAddResult implements IEnum {

    Success(1),
    DuplicateCommand(2);

    CommandAddResult(int status) {
        _status = status;
    }

    private int _status;

    @Override
    public int status() {
        return _status;
    }
}
