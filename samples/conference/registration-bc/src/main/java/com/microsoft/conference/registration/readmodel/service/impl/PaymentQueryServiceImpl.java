package com.microsoft.conference.registration.readmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.conference.common.dataobject.PaymentDO;
import com.microsoft.conference.common.mapper.PaymentMapper;
import com.microsoft.conference.registration.readmodel.PayConvert;
import com.microsoft.conference.registration.readmodel.service.PaymentQueryService;
import com.microsoft.conference.registration.readmodel.service.PaymentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public PaymentVO findPayment(String paymentId) {
        LambdaQueryWrapper<PaymentDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentDO::getPaymentId, paymentId);
        PaymentDO paymentDO = paymentMapper.selectOne(queryWrapper);
        return PayConvert.INSTANCE.toPayment(paymentDO);
    }
}
