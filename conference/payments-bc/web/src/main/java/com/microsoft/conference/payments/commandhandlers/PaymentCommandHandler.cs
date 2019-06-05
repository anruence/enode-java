using System.Linq;
using System.Threading.Tasks;
using ECommon.Components;
using ENode.Commanding;
using Payments.Commands;

namespace Payments.CommandHandlers
{
    [Component]
    public class PaymentCommandHandler :
        ICommandHandler<CreatePayment>,
        ICommandHandler<CompletePayment>,
        ICommandHandler<CancelPayment>
    {
        public Task HandleAsync(ICommandContext context, CreatePayment command)
        {
            return context.AddAsync(new Payment(
                command.AggregateRootId,
                command.OrderId,
                command.ConferenceId,
                command.Description,
                command.TotalAmount,
                command.Lines.Select(x => new PaymentItem(x.Description, x.Amount)).ToList()));
        }
        public async Task HandleAsync(ICommandContext context, CompletePayment command)
        {
            var payment = await context.GetAsync<Payment>(command.AggregateRootId);
            payment.Complete();
        }
        public async Task HandleAsync(ICommandContext context, CancelPayment command)
        {
            var payment = await context.GetAsync<Payment>(command.AggregateRootId);
            payment.Cancel();
        }
    }
}
