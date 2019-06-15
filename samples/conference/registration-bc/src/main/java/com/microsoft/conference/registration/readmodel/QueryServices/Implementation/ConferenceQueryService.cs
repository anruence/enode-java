using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using Conference.Common;
using ECommon.Components;
using ECommon.Dapper;

namespace Registration.ReadModel.Implementation
{
    [Component]
    public class ConferenceQueryService : IConferenceQueryService
    {
        public ConferenceDetails GetConferenceDetails(string slug)
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<ConferenceDetails>(new { Slug = slug }, ConfigSettings.ConferenceTable).SingleOrDefault();
            }
        }
        public ConferenceAlias GetConferenceAlias(string slug)
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<ConferenceAlias>(new { Slug = slug }, ConfigSettings.ConferenceTable).SingleOrDefault();
            }
        }
        public IList<ConferenceAlias> GetPublishedConferences()
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<ConferenceAlias>(new { IsPublished = 1 }, ConfigSettings.ConferenceTable).ToList();
            }
        }
        public IList<SeatType> GetPublishedSeatTypes(Guid conferenceId)
        {
            using (var connection = GetConnection())
            {
                return connection.QueryList<SeatType>(new { ConferenceId = conferenceId }, ConfigSettings.SeatTypeTable).ToList();
            }
        }
        public IList<SeatTypeName> GetSeatTypeNames(IEnumerable<Guid> seatTypes)
        {
            var distinctIds = seatTypes.Distinct().ToArray();
            if (distinctIds.Length == 0)
            {
                return new List<SeatTypeName>();
            }

            using (var connection = GetConnection())
            {
                var result = new List<SeatTypeName>();
                foreach (var seatId in distinctIds)
                {
                    var seat = connection.QueryList<SeatType>(new { Id = seatId }, ConfigSettings.SeatTypeTable).SingleOrDefault();
                    if (seat != null)
                    {
                        result.Add(new SeatTypeName { Id = seat.Id, Name = seat.Name });
                    }
                }
                return result;
            }
        }

        private IDbConnection GetConnection()
        {
            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
        }
    }
}