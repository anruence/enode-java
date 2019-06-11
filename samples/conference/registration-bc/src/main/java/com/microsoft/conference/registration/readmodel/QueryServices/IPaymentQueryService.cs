using System;

namespace Registration.ReadModel
{
    public interface IPaymentQueryService
    {
        Payment FindPayment(Guid paymentId);
    }
}