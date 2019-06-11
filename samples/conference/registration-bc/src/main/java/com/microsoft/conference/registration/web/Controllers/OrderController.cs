using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Mvc;
using Conference.Common;
using ECommon.IO;
using ENode.Commanding;
using Registration.Commands;
using Registration.Commands.SeatAssignments;
using Registration.ReadModel;
using Registration.Web.Models;

namespace Registration.Web.Controllers
{
    public class OrderController : ConferenceTenantController
    {
        private readonly ICommandService _commandService;

        public OrderController(ICommandService commandService, IConferenceQueryService conferenceQueryService, IOrderQueryService orderQueryService)
            : base(conferenceQueryService, orderQueryService)
        {
            _commandService = commandService;
        }

        [HttpGet]
        public ActionResult Display(Guid orderId)
        {
            var order = OrderQueryService.FindOrder(orderId);
            if (order == null)
            {
                return RedirectToAction("Find", new { conferenceCode = this.ConferenceCode });
            }
            return View(order);
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public RedirectToRouteResult AssignSeatsForOrder(Guid orderId)
        {
            var order = OrderQueryService.FindOrder(orderId);
            if (order == null)
            {
                return RedirectToAction("Display", new { orderId });
            }

            return RedirectToAction("AssignSeats", new { assignmentsId = order.OrderId });
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult AssignSeats(Guid orderId)
        {
            var assignments = OrderQueryService.FindOrderSeatAssignments(orderId);
            if (assignments == null)
            {
                return RedirectToAction("Find", new { conferenceCode = this.ConferenceCode });
            }
            return View(new OrderSeatsViewModel { OrderId = orderId, SeatAssignments = assignments });
        }

        [HttpPost]
        public ActionResult AssignSeats(Guid orderId, List<OrderSeatAssignment> seatAssignments)
        {
            if (!seatAssignments.Any())
            {
                return RedirectToAction("Display", new { orderId = orderId });
            }

            var assignmentsId = seatAssignments[0].AssignmentsId;
            var unassignedCommands = seatAssignments
                .Where(x => string.IsNullOrWhiteSpace(x.AttendeeEmail))
                .Select(x => (ICommand)new UnassignSeat(assignmentsId) { Position = x.Position });
            var assignedCommands = seatAssignments
                .Where(x => !string.IsNullOrWhiteSpace(x.AttendeeEmail))
                .Select(x => new AssignSeat(assignmentsId)
                {
                    Position = x.Position,
                    PersonalInfo = new PersonalInfo
                    {
                        Email = x.AttendeeEmail,
                        FirstName = x.AttendeeFirstName,
                        LastName = x.AttendeeLastName
                    }
                });

            var commands = assignedCommands.Union(unassignedCommands).ToList();
            foreach (var command in commands)
            {
                SendCommandAsync(command);
            }

            return RedirectToAction("Display", new { orderId = orderId });
        }

        [HttpGet]
        public ActionResult Find()
        {
            return View();
        }

        [HttpPost]
        public ActionResult Find(string email, string accessCode)
        {
            var orderId = OrderQueryService.LocateOrder(email, accessCode);

            if (!orderId.HasValue)
            {
                // TODO: 404?
                return RedirectToAction("Find", new { conferenceCode = this.ConferenceCode });
            }

            return RedirectToAction("Display", new { conferenceCode = this.ConferenceCode, orderId = orderId.Value });
        }

        /// <summary>异步发送给定的命令
        /// </summary>
        /// <param name="command"></param>
        /// <param name="millisecondsDelay"></param>
        /// <returns></returns>
        private Task<AsyncTaskResult> SendCommandAsync(ICommand command, int millisecondsDelay = 5000)
        {
            return _commandService.SendAsync(command).TimeoutAfter(millisecondsDelay);
        }
    }
}