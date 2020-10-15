package com.microsoft.conference.registration.readmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.conference.common.ListUtils;
import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderLineDO;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import com.microsoft.conference.common.mapper.OrderLineMapper;
import com.microsoft.conference.common.mapper.OrderMapper;
import com.microsoft.conference.common.mapper.OrderSeatAssignmentMapper;
import com.microsoft.conference.registration.readmodel.OrderConvert;
import com.microsoft.conference.registration.readmodel.service.IOrderQueryService;
import com.microsoft.conference.registration.readmodel.service.Order;
import com.microsoft.conference.registration.readmodel.service.OrderSeatAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderQueryService implements IOrderQueryService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLineMapper orderLineMapper;

    @Autowired
    private OrderSeatAssignmentMapper orderSeatAssignmentMapper;

    @Override
    public Order findOrder(String orderId) {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getOrderId, orderId);
        OrderDO orderDO = orderMapper.selectOne(wrapper);
        Order order = OrderConvert.INSTANCE.toOrder(orderDO);
        if (order == null) {
            return null;
        }
        LambdaQueryWrapper<OrderLineDO> lineQuery = new LambdaQueryWrapper<>();
        lineQuery.eq(OrderLineDO::getOrderId, orderId);
        List<OrderLineDO> orderLineDOS = orderLineMapper.selectList(lineQuery);
        order.setOrderLines(ListUtils.map(orderLineDOS, OrderConvert.INSTANCE::toOrderLine));
        return order;
    }

    @Override
    public String locateOrder(String email, String accessCode) {
        LambdaQueryWrapper<OrderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderDO::getRegistrantEmail, email);
        wrapper.eq(OrderDO::getAccessCode, accessCode);
        OrderDO orderDO = orderMapper.selectOne(wrapper);
        if (orderDO == null) {
            return "";
        }
        return orderDO.getOrderId();
    }

    @Override
    public List<OrderSeatAssignment> findOrderSeatAssignments(String orderId) {
        LambdaQueryWrapper<OrderSeatAssignmentDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderSeatAssignmentDO::getOrderId, orderId);
        List<OrderSeatAssignmentDO> orderSeatAssignmentDOS = orderSeatAssignmentMapper.selectList(wrapper);
        return ListUtils.map(orderSeatAssignmentDOS, OrderConvert.INSTANCE::toSeatAssignment);
    }
}