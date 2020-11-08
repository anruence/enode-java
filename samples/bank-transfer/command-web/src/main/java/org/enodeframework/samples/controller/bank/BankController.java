package org.enodeframework.samples.controller.bank;

import org.enodeframework.commanding.ICommandService;
import org.enodeframework.common.utilities.ObjectId;
import org.enodeframework.samples.commands.bank.CreateAccountCommand;
import org.enodeframework.samples.commands.bank.StartDepositTransactionCommand;
import org.enodeframework.samples.commands.bank.StartTransferTransactionCommand;
import org.enodeframework.samples.domain.bank.transfertransaction.TransferTransactionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/bank")
public class BankController {
    @Autowired
    private ICommandService commandService;

    @RequestMapping("deposit")
    public String deposit() {
        String account1 = ObjectId.generateNewStringId();
        String account2 = ObjectId.generateNewStringId();
        String account3 = "INVALID-" + ObjectId.generateNewStringId();
        //创建两个银行账户
        commandService.executeAsync(new CreateAccountCommand(account1, "雪华")).join();
        commandService.executeAsync(new CreateAccountCommand(account2, "凯锋")).join();
        //每个账户都存入1000元
        commandService.sendAsync(new StartDepositTransactionCommand(ObjectId.generateNewStringId(), account1, 1000)).join();
        commandService.sendAsync(new StartDepositTransactionCommand(ObjectId.generateNewStringId(), account2, 1000)).join();

        //账户1向账户3转账300元，交易会失败，因为账户3不存在
        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account1, account3, 300D))).join();
        //账户1向账户2转账1200元，交易会失败，因为余额不足
        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account1, account2, 1200D))).join();
        //账户2向账户1转账500元，交易成功
        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account2, account1, 500D))).join();
        return "success";
    }

    @RequestMapping("transfer")
    public String transfer() {

        return "success";
    }

    @RequestMapping("perf")
    public String perf(@RequestParam("count") int totalCount) throws Exception {
        long start = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(totalCount);
        for (int i = 0; i < totalCount; i++) {
            try {
                deposit();
            } finally {
                latch.countDown();
            }
        }
        latch.await();
        long end = System.currentTimeMillis();
        return String.valueOf(end - start);
    }
}
