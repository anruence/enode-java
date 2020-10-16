package com.microsoft.conference.registration.readmodel;

import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderLineDO;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.registration.domain.order.event.OrderPlaced;
import com.microsoft.conference.registration.domain.order.event.OrderRegistrantAssigned;
import com.microsoft.conference.registration.domain.order.model.OrderLine;
import com.microsoft.conference.registration.domain.seatassigning.event.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.seatassigning.model.Attendee;
import com.microsoft.conference.registration.domain.seatassigning.model.SeatAssignment;
import com.microsoft.conference.registration.readmodel.service.OrderVO;
import com.microsoft.conference.registration.readmodel.service.OrderLineVO;
import com.microsoft.conference.registration.readmodel.service.OrderSeatAssignmentVO;
import com.microsoft.conference.registration.readmodel.service.SeatTypeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderConvert {
    OrderConvert INSTANCE = Mappers.getMapper(OrderConvert.class);

    @Mapping(source = "evnt.aggregateRootId", target = "assignmentId")
    @Mapping(source = "evnt.orderId", target = "orderId")
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "assignment.attendee.firstName", target = "attendeeFirstName")
    @Mapping(source = "assignment.attendee.lastName", target = "attendeeLastName")
    @Mapping(source = "assignment.attendee.email", target = "attendeeEmail")
    OrderSeatAssignmentDO toSeatAssignment(OrderSeatAssignmentsCreated evnt, SeatAssignment assignment, com.microsoft.conference.registration.domain.SeatType seatType);

    @Mapping(source = "firstName", target = "attendeeFirstName")
    @Mapping(source = "lastName", target = "attendeeLastName")
    @Mapping(source = "email", target = "attendeeEmail")
    OrderSeatAssignmentDO toSeatAssignment(Attendee assigned);

    OrderSeatAssignmentVO toSeatAssignment(OrderSeatAssignmentDO assignmentDO);

    SeatTypeVO toDO(SeatTypeDO seatTypeDO);

    OrderVO toOrder(OrderDO orderDO);

    @Mapping(source = "evnt.aggregateRootId", target = "orderId")
    @Mapping(source = "evnt.accessCode", target = "accessCode")
    @Mapping(source = "evnt.reservationExpirationDate", target = "reservationExpirationDate")
    @Mapping(source = "evnt.orderTotal.total", target = "totalAmount")
    @Mapping(source = "evnt.version", target = "version")
    @Mapping(ignore = true, target = "id")
    OrderDO toDO(OrderPlaced evnt);

    @Mapping(source = "evnt.aggregateRootId", target = "orderId")
    @Mapping(source = "line.seatQuantity.seatType.unitPrice", target = "unitPrice")
    @Mapping(source = "line.seatQuantity.seatType.seatTypeId", target = "seatTypeId")
    @Mapping(source = "line.seatQuantity.seatType.seatTypeName", target = "seatTypeName")
    @Mapping(source = "line.seatQuantity.quantity", target = "quantity")
    @Mapping(source = "line.lineTotal", target = "lineTotal")
    @Mapping(target = "id", ignore = true)
    OrderLineDO toDO(OrderPlaced evnt, OrderLine line);


    @Mapping(source = "evnt.registrant.firstName", target = "registrantFirstName")
    @Mapping(source = "evnt.registrant.lastName", target = "registrantLastName")
    @Mapping(source = "evnt.registrant.email", target = "registrantEmail")
    @Mapping(source = "evnt.version", target = "version")
    @Mapping(target = "id", ignore = true)
    OrderDO toDO(OrderRegistrantAssigned evnt);

    OrderLineVO toOrderLine(OrderLineDO lineDO);
}
