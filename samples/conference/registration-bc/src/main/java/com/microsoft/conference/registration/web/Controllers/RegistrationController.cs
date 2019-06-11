using System;
using System.Linq;
using System.Threading.Tasks;
using System.Web.Mvc;
using Conference.Common;
using ECommon.IO;
using ENode.Commanding;
using Payments.Commands;
using Registration.Commands;
using Registration.Commands.Orders;
using Registration.ReadModel;
using Registration.Web.Extensions;
using Registration.Web.Models;
using Registration.Web.Utils;
using OrderStatus = Registration.Orders.OrderStatus;

namespace Registration.Web.Controllers
{
    public class RegistrationController : ConferenceTenantController
    {
        public const string ThirdPartyProcessorPayment = "thirdParty";
        private static readonly TimeSpan DraftOrderWaitTimeout = TimeSpan.FromSeconds(5);
        private static readonly TimeSpan DraftOrderPollInterval = TimeSpan.FromMilliseconds(750);
        private static readonly TimeSpan PricedOrderWaitTimeout = TimeSpan.FromSeconds(5);
        private static readonly TimeSpan PricedOrderPollInterval = TimeSpan.FromMilliseconds(750);

        private readonly ICommandService _commandService;

        public RegistrationController(ICommandService commandService, IConferenceQueryService conferenceQueryService, IOrderQueryService orderQueryService)
            : base(conferenceQueryService, orderQueryService)
        {
            _commandService = commandService;
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult StartRegistration()
        {
            return View(CreateViewModel());
        }

        [HttpPost]
        public async Task<ActionResult> StartRegistration(OrderViewModel model)
        {
            if (!ModelState.IsValid)
            {
                return View(CreateViewModel());
            }

            var command = model.ToPlaceOrderCommand(this.ConferenceAlias, ConferenceQueryService);
            if (!command.Seats.Any())
            {
                ModelState.AddModelError("ConferenceCode", "You must reservation at least one seat.");
                return View(CreateViewModel());
            }

            var result = await SendCommandAsync(command);

            if (!result.IsSuccess())
            {
                ModelState.AddModelError("ConferenceCode", result.GetErrorMessage());
                return View(CreateViewModel());
            }

            return RedirectToAction("SpecifyRegistrantAndPaymentDetails", new { conferenceCode = this.ConferenceCode, orderId = command.AggregateRootId });
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult SpecifyRegistrantAndPaymentDetails(Guid orderId)
        {
            var order = this.WaitUntilReservationCompleted(orderId).Result;
            if (order == null)
            {
                return View("PricedOrderUnknown");
            }

            if (order.Status == (int)OrderStatus.ReservationSuccess)
            {
                return View(new RegistrationViewModel
                {
                    RegistrantDetails = new RegistrantDetails { OrderId = order.OrderId },
                    Order = order
                });
            }
            else
            {
                return View("ReservationFailed");
            }
        }

        [HttpPost]
        public ActionResult SpecifyRegistrantAndPaymentDetails(Guid orderId, RegistrantDetails model, string paymentType)
        {
            if (!ModelState.IsValid)
            {
                return SpecifyRegistrantAndPaymentDetails(orderId);
            }

            SendCommandAsync(new AssignRegistrantDetails(orderId)
            {
                FirstName = model.FirstName,
                LastName = model.LastName,
                Email = model.Email
            });

            return this.StartPayment(orderId);
        }

        [HttpPost]
        public ActionResult StartPayment(Guid orderId)
        {
            var order = this.OrderQueryService.FindOrder(orderId);

            if (order == null)
            {
                return View("ReservationUnknown");
            }
            if (order.Status == (int)OrderStatus.PaymentSuccess || order.Status == (int)OrderStatus.Success)
            {
                return View("ShowCompletedOrder");
            }
            if (order.ReservationExpirationDate.HasValue && order.ReservationExpirationDate < DateTime.UtcNow)
            {
                return RedirectToAction("ShowExpiredOrder", new { conferenceCode = this.ConferenceAlias.Slug, orderId = orderId });
            }
            if (order.IsFreeOfCharge())
            {
                return CompleteRegistrationWithoutPayment(orderId);
            }

            return CompleteRegistrationWithThirdPartyProcessorPayment(order);
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult ShowRejectedOrder(Guid orderId)
        {
            return View();
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult ShowExpiredOrder(Guid orderId)
        {
            return View();
        }

        [HttpGet]
        [OutputCache(Duration = 0, NoStore = true)]
        public ActionResult ThankYou(Guid orderId)
        {
            return View(this.OrderQueryService.FindOrder(orderId));
        }

        private ActionResult CompleteRegistrationWithThirdPartyProcessorPayment(Order order)
        {
            var paymentCommand = CreatePaymentCommand(order);

            SendCommandAsync(paymentCommand);

            var paymentAcceptedUrl = this.Url.Action("ThankYou", new { conferenceCode = this.ConferenceAlias.Slug, order.OrderId });
            var paymentRejectedUrl = this.Url.Action("ShowRejectedOrder", new { conferenceCode = this.ConferenceAlias.Slug, orderId = order.OrderId });

            return RedirectToAction(
                "ThirdPartyProcessorPayment",
                "Payment",
                new
                {
                    conferenceCode = this.ConferenceAlias.Slug,
                    paymentId = paymentCommand.AggregateRootId,
                    paymentAcceptedUrl,
                    paymentRejectedUrl
                });
        }
        private CreatePayment CreatePaymentCommand(Order order)
        {
            var description = "Payment for the order of " + this.ConferenceAlias.Name;
            var paymentCommand = new CreatePayment
            {
                AggregateRootId = GuidUtil.NewSequentialId(),
                ConferenceId = this.ConferenceAlias.Id,
                OrderId = order.OrderId,
                Description = description,
                TotalAmount = order.TotalAmount,
                Lines = order.GetLines().Select(x => new PaymentLine { Id = x.SeatTypeId, Description = x.SeatTypeName, Amount = x.LineTotal })
            };

            return paymentCommand;
        }
        private ActionResult CompleteRegistrationWithoutPayment(Guid orderId)
        {
            SendCommandAsync(new MarkAsSuccess(orderId));
            return RedirectToAction("ThankYou", new { conferenceCode = this.ConferenceAlias.Slug, orderId });
        }
        private OrderViewModel CreateViewModel()
        {
            var seatTypes = this.ConferenceQueryService.GetPublishedSeatTypes(this.ConferenceAlias.Id);
            var viewModel = new OrderViewModel
            {
                ConferenceId = this.ConferenceAlias.Id,
                ConferenceCode = this.ConferenceAlias.Slug,
                ConferenceName = this.ConferenceAlias.Name,
                Items = seatTypes.Select(x => new OrderItemViewModel
                {
                    SeatType = x,
                    MaxSelectionQuantity = Math.Max(Math.Min(x.AvailableQuantity, 20), 0)
                }).ToList()
            };

            return viewModel;
        }

        /// <summary>轮训订单状态，直到订单的库存预扣操作完成
        /// </summary>
        /// <param name="orderId"></param>
        /// <returns></returns>
        private Task<Order> WaitUntilReservationCompleted(Guid orderId)
        {
            return TimerTaskFactory.StartNew<Order>(
                    () => this.OrderQueryService.FindOrder(orderId),
                    x => x != null && (x.Status == (int)OrderStatus.ReservationSuccess || x.Status == (int)OrderStatus.ReservationFailed),
                    PricedOrderPollInterval,
                    PricedOrderWaitTimeout);
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
