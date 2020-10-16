package com.microsoft.conference.registration.controller;

import com.microsoft.conference.common.ActionResult;
import com.microsoft.conference.registration.readmodel.service.OrderQueryService;
import com.microsoft.conference.registration.readmodel.service.OrderSeatAssignmentVO;
import com.microsoft.conference.registration.readmodel.service.OrderVO;
import org.enodeframework.commanding.ICommand;
import org.enodeframework.commanding.ICommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class OrderController {

    private ICommandService commandService;

    private OrderQueryService orderQueryService;

    private ActionResult View(Object... objects) {
        return new ActionResult<>(objects);
    }

    @GetMapping("orders/{id}")
    public ActionResult display(String orderId) {
        OrderVO order = orderQueryService.findOrder(orderId);
        return View(order);
    }

    public ActionResult assignSeats(String orderId) {
        List<OrderSeatAssignmentVO> assignments = orderQueryService.findOrderSeatAssignments(orderId);
        return View(assignments);
    }

    public ActionResult assignSeats(String orderId, List<OrderSeatAssignmentVO> seatAssignments) {
        if (!seatAssignments.stream().findAny().isPresent()) {
        }

//            var assignmentsId = seatAssignments[0].AssignmentsId;
//            var unassignedCommands = seatAssignments
//                .Where(x => string.IsNullOrWhiteSpace(x.AttendeeEmail))
//                .Select(x => (ICommand)new UnassignSeat(assignmentsId) { Position = x.Position });
//            var assignedCommands = seatAssignments
//                .Where(x => !string.IsNullOrWhiteSpace(x.AttendeeEmail))
//                .Select(x => new AssignSeat(assignmentsId)
//                {
//                    Position = x.Position,
//                    PersonalInfo = new PersonalInfo
//                    {
//                        Email = x.AttendeeEmail,
//                        FirstName = x.AttendeeFirstName,
//                        LastName = x.AttendeeLastName
//                    }
//                });
//
//            var commands = assignedCommands.Union(unassignedCommands).ToList();
//            foreach (var command in commands)
//            {
//                SendCommandAsync(command);
//            }
        return View();

    }

    public ActionResult find(String email, String accessCode) {
        String orderId = orderQueryService.locateOrder(email, accessCode);
        return View();
    }

    private CompletableFuture<Void> sendCommandAsync(ICommand command) {
        return commandService.sendAsync(command);
    }
}