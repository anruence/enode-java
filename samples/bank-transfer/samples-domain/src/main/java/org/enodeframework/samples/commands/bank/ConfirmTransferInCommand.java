package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认转入
 */
public class ConfirmTransferInCommand extends Command {
    public ConfirmTransferInCommand() {
    }

    public ConfirmTransferInCommand(String transactionId) {
        super(transactionId);
    }
}
