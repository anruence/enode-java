using System;
using System.Collections.Generic;
using System.Data;
using System.Data.SqlClient;
using System.Threading.Tasks;
using Conference.Common;
using ECommon.Components;
using ECommon.Dapper;
using ECommon.IO;
using ENode.Infrastructure;
using Registration.SeatAssigning;

namespace Registration.Handlers
{
    [Component]
    public class OrderSeatAssignmentsViewModelGenerator :
        IMessageHandler<OrderSeatAssignmentsCreated>,
        IMessageHandler<SeatAssigned>,
        IMessageHandler<SeatUnassigned>
    {
        public Task<AsyncTaskResult> HandleAsync(OrderSeatAssignmentsCreated evnt)
        {
            return TryTransactionAsync((connection, transaction) =>
            {
                var tasks = new List<Task>();

                foreach (var assignment in evnt.Assignments)
                {
                    tasks.Add(connection.InsertAsync(new
                    {
                        AssignmentsId = evnt.AggregateRootId,
                        OrderId = evnt.OrderId,
                        Position = assignment.Position,
                        SeatTypeId = assignment.Seat.SeatTypeId,
                        SeatTypeName = assignment.Seat.SeatTypeName
                    }, ConfigSettings.OrderSeatAssignmentsTable, transaction));
                }

                return tasks;
            });
        }
        public Task<AsyncTaskResult> HandleAsync(SeatAssigned evnt)
        {
            return TryUpdateRecordAsync(connection =>
            {
                return connection.UpdateAsync(new
                {
                    AttendeeFirstName = evnt.Attendee.FirstName,
                    AttendeeLastName = evnt.Attendee.LastName,
                    AttendeeEmail = evnt.Attendee.Email
                }, new
                {
                    AssignmentsId = evnt.AggregateRootId,
                    Position = evnt.Position
                }, ConfigSettings.OrderSeatAssignmentsTable);
            });
        }
        public Task<AsyncTaskResult> HandleAsync(SeatUnassigned evnt)
        {
            return TryUpdateRecordAsync(connection =>
            {
                return connection.UpdateAsync(new
                {
                    AttendeeFirstName = string.Empty,
                    AttendeeLastName = string.Empty,
                    AttendeeEmail = string.Empty
                }, new
                {
                    AssignmentsId = evnt.AggregateRootId,
                    Position = evnt.Position
                }, ConfigSettings.OrderSeatAssignmentsTable);
            });
        }

        private async Task<AsyncTaskResult> TryUpdateRecordAsync(Func<IDbConnection, Task<int>> action)
        {
            using (var connection = GetConnection())
            {
                await action(connection);
                return AsyncTaskResult.Success;
            }
        }
        private async Task<AsyncTaskResult> TryTransactionAsync(Func<IDbConnection, IDbTransaction, IEnumerable<Task>> actions)
        {
            using (var connection = GetConnection())
            {
                await connection.OpenAsync().ConfigureAwait(false);
                var transaction = await Task.Run<SqlTransaction>(() => connection.BeginTransaction()).ConfigureAwait(false);
                try
                {
                    await Task.WhenAll(actions(connection, transaction)).ConfigureAwait(false);
                    await Task.Run(() => transaction.Commit()).ConfigureAwait(false);
                    return AsyncTaskResult.Success;
                }
                catch
                {
                    transaction.Rollback();
                    throw;
                }
            }
        }
        private SqlConnection GetConnection()
        {
            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
        }
    }
}
