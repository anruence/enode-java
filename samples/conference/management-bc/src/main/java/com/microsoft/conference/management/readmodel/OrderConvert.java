package com.microsoft.conference.management.readmodel;

import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderConvert {
    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    OrderDTO toDTO(OrderDO orderDO);

    AttendeeDTO toDTO(OrderSeatAssignmentDO assignmentDO);
}
