package com.qianzhui.enode.common.remoting;

import com.qianzhui.enode.common.remoting.exception.RemotingCommandException;

public interface CommandCustomHeader {
    void checkFields() throws RemotingCommandException;
}
