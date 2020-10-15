package com.microsoft.conference.registration.readmodel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderLineDO;
import com.microsoft.conference.common.mapper.OrderLineMapper;
import com.microsoft.conference.common.mapper.OrderMapper;
import com.microsoft.conference.registration.domain.order.event.OrderClosed;
import com.microsoft.conference.registration.domain.order.event.OrderExpired;
import com.microsoft.conference.registration.domain.order.event.OrderPaymentConfirmed;
import com.microsoft.conference.registration.domain.order.event.OrderPlaced;
import com.microsoft.conference.registration.domain.order.event.OrderRegistrantAssigned;
import com.microsoft.conference.registration.domain.order.event.OrderReservationConfirmed;
import com.microsoft.conference.registration.domain.order.event.OrderSuccessed;
import com.microsoft.conference.registration.domain.order.model.OrderStatus;
import org.enodeframework.annotation.Event;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 */
@Event
public class OrderViewModelGenerator {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLineMapper orderLineMapper;

    public void handleAsync(OrderPlaced evnt) {

        //插入订单主记录
        OrderDO orderDO = OrderConvert.INSTANCE.toDO(evnt);
        orderDO.setStatus(OrderStatus.Placed.getStatus());
        orderMapper.insert(orderDO);

        //插入订单明细
        evnt.getOrderTotal().getOrderLines().forEach(orderLine -> {
            OrderLineDO orderLineDO = OrderConvert.INSTANCE.toDO(evnt, orderLine);
            orderLineMapper.insert(orderLineDO);
        });

    }


    public void handleAsync(OrderRegistrantAssigned evnt) {
        OrderDO orderDO = OrderConvert.INSTANCE.toDO(evnt);
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }

    public void handleAsync(OrderReservationConfirmed evnt) {
        OrderDO orderDO = new OrderDO();
        orderDO.setStatus(evnt.getOrderStatus().getStatus());
        orderDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }

    public void handleAsync(OrderPaymentConfirmed evnt) {
        OrderDO orderDO = new OrderDO();
        orderDO.setStatus(evnt.getOrderStatus().getStatus());
        orderDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }

    public void handleAsync(OrderExpired evnt) {
        OrderDO orderDO = new OrderDO();
        orderDO.setStatus(OrderStatus.Expired.getStatus());
        orderDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }

    public void handleAsync(OrderClosed evnt) {
        OrderDO orderDO = new OrderDO();
        orderDO.setStatus(OrderStatus.Closed.getStatus());
        orderDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }

    public void handleAsync(OrderSuccessed evnt) {
        OrderDO orderDO = new OrderDO();
        orderDO.setStatus(OrderStatus.Success.getStatus());
        orderDO.setVersion(evnt.getVersion());
        LambdaUpdateWrapper<OrderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(OrderDO::getOrderId, evnt.getAggregateRootId());
        wrapper.eq(OrderDO::getVersion, evnt.getVersion() - 1);
        orderMapper.update(orderDO, wrapper);
    }
}
