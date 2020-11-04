package com.microsoft.conference.registration.controller;

import com.google.common.base.Strings;
import com.microsoft.conference.common.ActionResult;
import com.microsoft.conference.common.ErrCode;
import com.microsoft.conference.common.registration.commands.PersonalInfo;
import com.microsoft.conference.common.registration.commands.seatassignment.AssignSeat;
import com.microsoft.conference.common.registration.commands.seatassignment.UnassignSeat;
import com.microsoft.conference.registration.readmodel.service.OrderQueryService;
import com.microsoft.conference.registration.readmodel.service.OrderSeatAssignmentVO;
import com.microsoft.conference.registration.readmodel.service.OrderVO;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class OrderController {

    @Autowired
    private ICommandService commandService;

    @Autowired
    private OrderQueryService orderQueryService;

    @GetMapping("orders/{id}")
    public ActionResult<OrderVO> display(@PathVariable("id") String orderId) {
        OrderVO order = orderQueryService.findOrder(orderId);
        return view(order);
    }

    @GetMapping("orders")
    public ActionResult<String> find(String email, String accessCode) {
        String orderId = orderQueryService.locateOrder(email, accessCode);
        return view(orderId);
    }

    @GetMapping("seatassignments/{id}")
    public ActionResult<OrderSeatsViewModel> assignSeats(@PathVariable("id") String orderId) {
        List<OrderSeatAssignmentVO> assignments = orderQueryService.findOrderSeatAssignments(orderId);
        OrderSeatsViewModel viewModel = new OrderSeatsViewModel();
        viewModel.setOrderId(orderId);
        viewModel.setSeatAssignments(assignments);
        return view(viewModel);
    }

    @PostMapping("seatassignments")
    public ActionResult<String> assignSeats(OrderSeatsViewModel viewModel) {
        List<OrderSeatAssignmentVO> seatAssignments = viewModel.getSeatAssignments();
        if (seatAssignments.isEmpty()) {
            return ActionResult.error(ErrCode.SYSTEM_ERROR, "" + viewModel.getOrderId());
        }
        String assignmentsId = seatAssignments.get(0).getAssignmentsId();
        List<ICommand> unassignedCommands = seatAssignments.stream().filter(x -> Strings.isNullOrEmpty(x.getAttendeeEmail()))
                .map(y -> {
                    UnassignSeat unassignSeat = new UnassignSeat(assignmentsId);
                    unassignSeat.setPosition(y.getPosition());
                    return (ICommand) unassignSeat;
                }).collect(Collectors.toList());
        List<ICommand> assignedCommands = seatAssignments.stream().filter(x -> !Strings.isNullOrEmpty(x.getAttendeeEmail()))
                .map(y -> {
                    AssignSeat unassignSeat = new AssignSeat(assignmentsId);
                    unassignSeat.setPosition(y.getPosition());
                    PersonalInfo personalInfo = new PersonalInfo();
                    personalInfo.setEmail(y.getAttendeeEmail());
                    personalInfo.setFirstName(y.getAttendeeFirstName());
                    personalInfo.setLastName(y.getAttendeeLastName());
                    unassignSeat.setPersonalInfo(personalInfo);
                    return (ICommand) unassignSeat;
                }).collect(Collectors.toList());
        assignedCommands.addAll(unassignedCommands);
        for (ICommand command : assignedCommands) {
            sendCommandAsync(command);
        }
        return ActionResult.empty();
    }


    private <T> ActionResult<T> view(T objects) {
        return new ActionResult<>(objects);
    }

    private void sendCommandAsync(ICommand command) {
        commandService.sendAsync(command);
    }
}