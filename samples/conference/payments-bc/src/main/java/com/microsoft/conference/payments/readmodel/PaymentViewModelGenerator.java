package com.microsoft.conference.payments.readmodel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.microsoft.conference.common.dataobject.PaymentDO;
import com.microsoft.conference.common.dataobject.PaymentItemDO;
import com.microsoft.conference.common.mapper.PaymentItemMapper;
import com.microsoft.conference.common.mapper.PaymentMapper;
import com.microsoft.conference.payments.domain.event.PaymentCompleted;
import com.microsoft.conference.payments.domain.event.PaymentInitiated;
import com.microsoft.conference.payments.domain.event.PaymentRejected;
import com.microsoft.conference.payments.domain.model.PaymentItem;
import com.microsoft.conference.payments.domain.model.PaymentState;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

@Event
public class PaymentViewModelGenerator {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private PaymentItemMapper paymentItemMapper;

    @Subscribe
    public void handleAsync(PaymentInitiated evnt) {
        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setState(PaymentState.Initiated);
        paymentDO.setDescription(evnt.getDescription());
        paymentDO.setPaymentId(evnt.getAggregateRootId());
        paymentDO.setOrderId(evnt.getOrderId());
        paymentDO.setVersion(evnt.getVersion());
        paymentDO.setTotalAmount(evnt.getTotalAmount());
        paymentMapper.insert(paymentDO);

        for (PaymentItem paymentItem : evnt.getPaymentItems()) {
            PaymentItemDO paymentItemDO = new PaymentItemDO();
            paymentItemDO.setAmount(paymentItem.getAmount());
            paymentItemDO.setDescription(paymentItem.getDescription());
            paymentItemDO.setPaymentId(evnt.getAggregateRootId());
            paymentItemDO.setPaymentItemId(paymentItem.getId());
            paymentItemMapper.insert(paymentItemDO);
        }
    }

    @Subscribe
    public void handleAsync(PaymentCompleted evnt) {
        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setState(PaymentState.Completed);
        paymentDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<PaymentDO> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(PaymentDO::getPaymentId, evnt.getAggregateRootId());
        updateWrapper.eq(PaymentDO::getVersion, evnt.getVersion() - 1);
        paymentMapper.update(paymentDO, updateWrapper);
    }

    @Subscribe
    public void handleAsync(PaymentRejected evnt) {
        PaymentDO paymentDO = new PaymentDO();
        paymentDO.setState(PaymentState.Rejected);
        paymentDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<PaymentDO> updateWrapper = new LambdaUpdateWrapper();
        updateWrapper.eq(PaymentDO::getPaymentId, evnt.getAggregateRootId());
        updateWrapper.eq(PaymentDO::getVersion, evnt.getVersion() - 1);
        paymentMapper.update(paymentDO, updateWrapper);
    }
}
