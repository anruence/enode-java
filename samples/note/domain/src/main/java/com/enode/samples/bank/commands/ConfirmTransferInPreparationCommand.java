﻿package com.enode.samples.bank.commands;

import com.enode.commanding.Command;


/// <summary>确认预转入
/// </summary>
public class ConfirmTransferInPreparationCommand extends Command {
    public ConfirmTransferInPreparationCommand() {
    }

    public ConfirmTransferInPreparationCommand(String transactionId) {
        super(transactionId);
    }
}

