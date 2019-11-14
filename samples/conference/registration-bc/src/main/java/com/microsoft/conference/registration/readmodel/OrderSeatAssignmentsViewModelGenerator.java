package com.microsoft.conference.registration.readmodel;

import com.microsoft.conference.registration.domain.SeatAssigning.Events.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.SeatAssigning.Events.SeatAssigned;
import com.microsoft.conference.registration.domain.SeatAssigning.Events.SeatUnassigned;

public class OrderSeatAssignmentsViewModelGenerator {
    public void HandleAsync(OrderSeatAssignmentsCreated evnt) {
//            return TryTransactionAsync((connection, transaction) =>
//            {
//                var tasks = new List<Task>();
//
//                for (var assignment in evnt.Assignments)
//                {
//                    tasks.add(connection.InsertAsync(new
//                    {
//                        AssignmentsId = evnt.getAggregateRootId(),
//                        OrderId = evnt.OrderId,
//                        Position = assignment.Position,
//                        SeatTypeId = assignment.Seat.SeatTypeId,
//                        SeatTypeName = assignment.Seat.SeatTypeName
//                    }, ConfigSettings.OrderSeatAssignmentsTable, transaction));
//                }
//
//                return tasks;
//            });

    }

    public void HandleAsync(SeatAssigned evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    AttendeeFirstName = evnt.Attendee.FirstName,
//                    AttendeeLastName = evnt.Attendee.LastName,
//                    AttendeeEmail = evnt.Attendee.Email
//                }, new
//                {
//                    AssignmentsId = evnt.getAggregateRootId(),
//                    Position = evnt.Position
//                }, ConfigSettings.OrderSeatAssignmentsTable);
//            });

    }

    public void HandleAsync(SeatUnassigned evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    AttendeeFirstName = String.Empty,
//                    AttendeeLastName = String.Empty,
//                    AttendeeEmail = String.Empty
//                }, new
//                {
//                    AssignmentsId = evnt.getAggregateRootId(),
//                    Position = evnt.Position
//                }, ConfigSettings.OrderSeatAssignmentsTable);
//            });

    }
//        private async void TryUpdateRecordAsync(Func<IDbConnection, Task<int>> action)
//        {
//            using (var connection = GetConnection())
//            {
//                await action(connection);
//                return void.Success;
//            }
//        }
//        private async void TryTransactionAsync(Func<IDbConnection, IDbTransaction, List<Task>> actions)
//        {
//            using (var connection = GetConnection())
//            {
//                await connection.OpenAsync().ConfigureAwait(false);
//                var transaction = await Task.Run<SqlTransaction>(() => connection.BeginTransaction()).ConfigureAwait(false);
//                try
//                {
//                    await Task.WhenAll(actions(connection, transaction)).ConfigureAwait(false);
//                    await Task.Run(() => transaction.Commit()).ConfigureAwait(false);
//                    return void.Success;
//                }
//                catch
//                {
//                    transaction.Rollback();
//                    throw;
//                }
//            }
//        }
//        private SqlConnection GetConnection()
//        {
//            return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//        }
}
