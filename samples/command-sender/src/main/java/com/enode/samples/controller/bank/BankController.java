package com.enode.samples.controller.bank;

import com.enode.common.logging.ENodeLogger;
import com.enode.common.utilities.ObjectId;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank")
public class BankController {

    private Logger logger = ENodeLogger.getLog();

    @RequestMapping("deposit")
    public Object deposit(@RequestParam("data") String data) throws Exception {

        logger.info("ENode started...");

        String account1 = ObjectId.generateNewStringId();
        String account2 = ObjectId.generateNewStringId();
        String account3 = "INVALID-" + ObjectId.generateNewStringId();

//        //创建两个银行账户
//        commandService.executeAsync(new CreateAccountCommand(account1, "雪华"), CommandReturnType.EventHandled).get();
//
//        commandService.executeAsync(new CreateAccountCommand(account2, "凯锋"), CommandReturnType.EventHandled).get();
//
//        //每个账户都存入1000元
//        commandService.sendAsync(new StartDepositTransactionCommand(ObjectId.generateNewStringId(), account1, 1000)).get();
//
//        commandService.sendAsync(new StartDepositTransactionCommand(ObjectId.generateNewStringId(), account2, 1000)).get();
//
//        //账户1向账户3转账300元，交易会失败，因为账户3不存在
//        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account1, account3, 300D))).get();
//
//        //账户1向账户2转账1200元，交易会失败，因为余额不足
//        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account1, account2, 1200D))).get();
//
//        //账户2向账户1转账500元，交易成功
//        commandService.sendAsync(new StartTransferTransactionCommand(ObjectId.generateNewStringId(), new TransferTransactionInfo(account2, account1, 500D))).get();

        Thread.sleep(200);
        logger.info("Press Enter to exit...");
        return "success";
    }

    @RequestMapping("transfer")
    public Object transfer(@RequestParam("data") String data) {
        return "success";
    }
}
