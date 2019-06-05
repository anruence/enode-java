using System;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using Conference.Common;
using ConferenceManagement.Domain.Models;
using ConferenceManagement.Domain.Repositories;
using ECommon.Components;
using ECommon.Dapper;

namespace ConferenceManagement.Repositories.Dapper
{
    [Component]
    public class ConferenceSlugIndexRepository : IConferenceSlugIndexRepository
    {
        public void Add(ConferenceSlugIndex index)
        {
            using (var connection = GetConnection())
            {
                connection.Insert(new
                {
                    IndexId = index.IndexId,
                    ConferenceId = index.ConferenceId,
                    Slug = index.Slug
                }, ConfigSettings.ConferenceSlugIndexTable);
            }
        }
        public ConferenceSlugIndex FindSlugIndex(string slug)
        {
            using (var connection = GetConnection())
            {
                var record = connection.QueryList(new { Slug = slug }, ConfigSettings.ConferenceSlugIndexTable).SingleOrDefault();
                if (record != null)
                {
                    var indexId = record.IndexId as string;
                    var conferenceId = (Guid)record.ConferenceId;
                    return new ConferenceSlugIndex(indexId, conferenceId, slug);
                }
                return null;
            }
        }

        private IDbConnection GetConnection()
        {
            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
        }
    }
}
