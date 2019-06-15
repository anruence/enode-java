using Conference.Common;
using ConferenceManagement.Commands;
using ConferenceManagement.ReadModel;
using ConferenceManagement.Web.Models;

namespace ConferenceManagement.Web.Extensions
{
    public static class DTOExtensions
    {
        public static ConferenceInfo ToViewModel(this ConferenceDTO dto)
        {
            if (dto == null) return null;

            var model = new ConferenceInfo();
            model.Id = dto.Id;
            model.Name = dto.Name;
            model.Description = dto.Description;
            model.Location = dto.Location;
            model.Tagline = dto.Tagline;
            model.TwitterSearch = dto.TwitterSearch;
            model.StartDate = dto.StartDate;
            model.EndDate = dto.EndDate;
            model.AccessCode = dto.AccessCode;
            model.OwnerName = dto.OwnerName;
            model.OwnerEmail = dto.OwnerEmail;
            model.Slug = dto.Slug;
            model.IsPublished = dto.IsPublished;
            model.WasEverPublished = dto.WasEverPublished;
            return model;
        }
        public static CreateConference ToCreateConferenceCommand(this ConferenceInfo model)
        {
            var command = new CreateConference();
            command.AggregateRootId = GuidUtil.NewSequentialId();
            command.Name = model.Name;
            command.Description = model.Description;
            command.Location = model.Location;
            command.Tagline = model.Tagline;
            command.TwitterSearch = model.TwitterSearch;
            command.StartDate = model.StartDate;
            command.EndDate = model.EndDate;
            command.AccessCode = model.AccessCode;
            command.OwnerName = model.OwnerName;
            command.OwnerEmail = model.OwnerEmail;
            command.Slug = model.Slug;
            return command;
        }
        public static UpdateConference ToUpdateConferenceCommand(this EditableConferenceInfo model, ConferenceInfo original)
        {
            var command = new UpdateConference();
            command.AggregateRootId = original.Id;
            command.Name = model.Name;
            command.Description = model.Description;
            command.Location = model.Location;
            command.Tagline = model.Tagline;
            command.TwitterSearch = model.TwitterSearch;
            command.StartDate = model.StartDate;
            command.EndDate = model.EndDate;
            return command;
        }

        public static SeatType ToViewModel(this SeatTypeDTO dto)
        {
            if (dto == null) return null;

            var model = new SeatType();
            model.Id = dto.Id;
            model.Name = dto.Name;
            model.Description = dto.Description;
            model.Price = dto.Price;
            model.Quantity = dto.Quantity;
            return model;
        }
        public static AddSeatType ToAddSeatTypeCommand(this SeatType model, ConferenceInfo conference)
        {
            var command = new AddSeatType(conference.Id);
            command.Name = model.Name;
            command.Description = model.Description;
            command.Price = model.Price;
            command.Quantity = model.Quantity;
            return command;
        }
        public static UpdateSeatType ToUpdateSeatTypeCommand(this SeatType model, ConferenceInfo conference)
        {
            var command = new UpdateSeatType(conference.Id);
            command.SeatTypeId = model.Id;
            command.Name = model.Name;
            command.Description = model.Description;
            command.Price = model.Price;
            command.Quantity = model.Quantity;
            return command;
        }
    }
}