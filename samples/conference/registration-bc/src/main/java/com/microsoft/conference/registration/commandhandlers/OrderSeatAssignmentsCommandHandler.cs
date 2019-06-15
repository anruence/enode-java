using System.Threading.Tasks;
using ECommon.Components;
using ENode.Commanding;
using Registration.Commands.SeatAssignments;
using Registration.Orders;
using Registration.SeatAssigning;

namespace Registration.CommandHandlers
{
    [Component]
    public class OrderSeatAssignmentsCommandHandler :
        ICommandHandler<CreateSeatAssignments>,
        ICommandHandler<UnassignSeat>,
        ICommandHandler<AssignSeat>
    {
        public async Task HandleAsync(ICommandContext context, CreateSeatAssignments command)
        {
            var order = await context.GetAsync<Order>(command.AggregateRootId);
            OrderSeatAssignments orderSeatAssignments = order.CreateSeatAssignments();
            context.Add(orderSeatAssignments);
        }
        public async Task HandleAsync(ICommandContext context, AssignSeat command)
        {
            var orderSeatAssignments = await context.GetAsync<OrderSeatAssignments>(command.AggregateRootId);
            orderSeatAssignments.AssignSeat(command.Position, new Attendee(
                command.PersonalInfo.FirstName,
                command.PersonalInfo.LastName,
                command.PersonalInfo.Email));
        }
        public async Task HandleAsync(ICommandContext context, UnassignSeat command)
        {
            var orderSeatAssignments = await context.GetAsync<OrderSeatAssignments>(command.AggregateRootId);
            orderSeatAssignments.UnassignSeat(command.Position);
        }
    }
}
