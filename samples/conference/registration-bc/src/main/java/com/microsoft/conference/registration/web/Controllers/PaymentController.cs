using System;
using System.Threading;
using System.Web.Mvc;
using ENode.Commanding;
using Payments.Commands;
using Registration.ReadModel;

namespace Registration.Web.Controllers
{
    public class PaymentController : Controller
    {
        private const int WaitTimeoutInSeconds = 5;

        private readonly ICommandService _commandService;
        private readonly IPaymentQueryService _paymentQueryService;

        public PaymentController(ICommandService commandService, IPaymentQueryService paymentQueryService)
        {
            this._commandService = commandService;
            this._paymentQueryService = paymentQueryService;
        }

        public ActionResult ThirdPartyProcessorPayment(string conferenceCode, Guid paymentId, string paymentAcceptedUrl, string paymentRejectedUrl)
        {
            var returnUrl = Url.Action("ThirdPartyProcessorPaymentAccepted", new { conferenceCode, paymentId, paymentAcceptedUrl });
            var cancelReturnUrl = Url.Action("ThirdPartyProcessorPaymentRejected", new { conferenceCode, paymentId, paymentRejectedUrl });

            var paymentDTO = this.WaitUntilAvailable(paymentId);
            if (paymentDTO == null)
            {
                return this.View("WaitForPayment");
            }

            var paymentProcessorUrl = this.Url.Action("Pay", "ThirdPartyProcessorPayment", new
            {
                area = "ThirdPartyProcessor",
                itemName = paymentDTO.Description,
                itemAmount = paymentDTO.TotalAmount,
                returnUrl,
                cancelReturnUrl
            });

            // redirect to external site
            return this.Redirect(paymentProcessorUrl);
        }

        public ActionResult ThirdPartyProcessorPaymentAccepted(string conferenceCode, Guid paymentId, string paymentAcceptedUrl)
        {
            this._commandService.SendAsync(new CompletePayment { AggregateRootId = paymentId });

            return this.Redirect(paymentAcceptedUrl);
        }

        public ActionResult ThirdPartyProcessorPaymentRejected(string conferenceCode, Guid paymentId, string paymentRejectedUrl)
        {
            this._commandService.SendAsync(new CancelPayment { AggregateRootId = paymentId });

            return this.Redirect(paymentRejectedUrl);
        }

        private Payment WaitUntilAvailable(Guid paymentId)
        {
            var deadline = DateTime.Now.AddSeconds(WaitTimeoutInSeconds);

            while (DateTime.Now < deadline)
            {
                var paymentDTO = this._paymentQueryService.FindPayment(paymentId);
                if (paymentDTO != null)
                {
                    return paymentDTO;
                }

                Thread.Sleep(500);
            }

            return null;
        }
    }
}
