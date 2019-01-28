package com.enode.samples.controller.bank;

import com.enode.commanding.ICommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bank")
public class BankController {

    @Autowired
    private ICommandService commandService;

    @RequestMapping("deposit")
    public Object deposit(@RequestParam("data") String data) {
        return "success";
    }

    @RequestMapping("transfer")
    public Object transfer(@RequestParam("data") String data) {
        return "success";
    }
}
