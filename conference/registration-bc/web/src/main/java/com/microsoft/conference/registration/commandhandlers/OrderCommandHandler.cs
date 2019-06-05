using System.Linq;
using System.Threading.Tasks;
using ECommon.Components;
using ENode.Commanding;
using Registration.Commands.Orders;
using Registration.Orders;

namespace Registration.CommandHandlers
{
    [Component]
    public class OrderCommandHandler :
        ICommandHandler<PlaceOrder>,
        ICommandHandler<AssignRegistrantDetails>,
        ICommandHandler<ConfirmReservation>,
        ICommandHandler<ConfirmPayment>,
        ICommandHandler<MarkAsSuccess>,
        ICommandHandler<CloseOrder>
    {
        private readonly IPricingService _pricingService;

        public OrderCommandHandler(IPricingService pricingService)
        {
            _pricingService = pricingService;
        }

        public Task HandleAsync(ICommandContext context, PlaceOrder command)
        {
            return context.AddAsync(new Order(
                command.AggregateRootId,
                command.ConferenceId,
                command.Seats.Select(x => new SeatQuantity(new SeatType(x.SeatType, x.SeatName, x.UnitPrice), x.Quantity)),
                _pricingService));
        }
        public async Task HandleAsync(ICommandContext context, AssignRegistrantDetails command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            order.AssignRegistrant(command.FirstName, command.LastName, command.Email);
        }
        public async Task HandleAsync(ICommandContext context, ConfirmReservation command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            order.ConfirmReservation(command.IsReservationSuccess);
        }
        public async Task HandleAsync(ICommandContext context, ConfirmPayment command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            order.ConfirmPayment(command.IsPaymentSuccess);
        }
        public async Task HandleAsync(ICommandContext context, MarkAsSuccess command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            order.MarkAsSuccess();
        }
        public async Task HandleAsync(ICommandContext context, CloseOrder command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            order.Close();
        }
    }
}
