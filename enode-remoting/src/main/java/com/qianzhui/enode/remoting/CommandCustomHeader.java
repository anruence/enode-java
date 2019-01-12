package com.qianzhui.enode.remoting;

import com.qianzhui.enode.remoting.exception.RemotingCommandException;

public interface CommandCustomHeader {
    void checkFields() throws RemotingCommandException;
}
