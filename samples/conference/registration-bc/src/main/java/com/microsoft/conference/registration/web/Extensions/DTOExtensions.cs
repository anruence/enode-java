using System.Linq;
using Conference.Common;
using Registration.Commands.Orders;
using Registration.ReadModel;
using Registration.Web.Models;

namespace Registration.Web.Extensions
{
    public static class DTOExtensions
    {
        public static PlaceOrder ToPlaceOrderCommand(this OrderViewModel model, ConferenceAlias conferenceAlias, IConferenceQueryService conferenceQueryService)
        {
            var seatTypes = conferenceQueryService.GetPublishedSeatTypes(conferenceAlias.Id);
            var command = new PlaceOrder();
            command.AggregateRootId = GuidUtil.NewSequentialId();
            command.ConferenceId = conferenceAlias.Id;
            command.Seats = model.Seats.Where(x => x.Quantity > 0).Select(x =>
            {
                var seat = seatTypes.Single(y => y.Id == x.SeatType);
                return new SeatInfo
                {
                    SeatType = x.SeatType,
                    Quantity = x.Quantity,
                    SeatName = seat.Name,
                    UnitPrice = seat.Price
                };
            }).ToList();
            return command;
        }
    }
}