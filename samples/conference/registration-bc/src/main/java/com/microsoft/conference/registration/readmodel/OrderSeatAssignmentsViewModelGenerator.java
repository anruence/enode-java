package com.microsoft.conference.registration.readmodel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import com.microsoft.conference.common.mapper.OrderSeatAssignmentMapper;
import com.microsoft.conference.registration.domain.seatassigning.event.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.seatassigning.event.SeatAssigned;
import com.microsoft.conference.registration.domain.seatassigning.event.SeatUnassigned;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.springframework.beans.factory.annotation.Autowired;

@Event
public class OrderSeatAssignmentsViewModelGenerator {

    @Autowired
    private OrderSeatAssignmentMapper orderSeatAssignmentMapper;

    @Subscribe
    public void handleAsync(OrderSeatAssignmentsCreated evnt) {
        evnt.getSeatAssignments().forEach(seatAssignment -> {
            OrderSeatAssignmentDO seatAssignmentDO = OrderConvert.INSTANCE.toSeatAssignment(evnt, seatAssignment, seatAssignment.getSeatType());
            orderSeatAssignmentMapper.insert(seatAssignmentDO);
        });
    }

    @Subscribe
    public void handleAsync(SeatAssigned evnt) {
        OrderSeatAssignmentDO seatAssignmentDO = OrderConvert.INSTANCE.toSeatAssignment(evnt.getAttendee());
        LambdaUpdateWrapper<OrderSeatAssignmentDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderSeatAssignmentDO::getAssignmentId, evnt.getAggregateRootId());
        updateWrapper.eq(OrderSeatAssignmentDO::getPosition, evnt.getPosition());
        orderSeatAssignmentMapper.update(seatAssignmentDO, updateWrapper);
    }

    @Subscribe
    public void handleAsync(SeatUnassigned evnt) {
        OrderSeatAssignmentDO seatAssignmentDO = new OrderSeatAssignmentDO();
        seatAssignmentDO.setAttendeeEmail("");
        seatAssignmentDO.setAttendeeFirstName("");
        seatAssignmentDO.setAttendeeLastName("");
        LambdaUpdateWrapper<OrderSeatAssignmentDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderSeatAssignmentDO::getAssignmentId, evnt.getAggregateRootId());
        updateWrapper.eq(OrderSeatAssignmentDO::getPosition, evnt.getPosition());
        orderSeatAssignmentMapper.update(seatAssignmentDO, updateWrapper);
    }
}
