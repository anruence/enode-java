package org.enodeframework.samples.commands.bank;

import org.enodeframework.commanding.Command;

/**
 * 确认转出
 */
public class ConfirmTransferOutCommand extends Command {
    public ConfirmTransferOutCommand() {
    }

    public ConfirmTransferOutCommand(String transactionId) {
        super(transactionId);
    }
}
