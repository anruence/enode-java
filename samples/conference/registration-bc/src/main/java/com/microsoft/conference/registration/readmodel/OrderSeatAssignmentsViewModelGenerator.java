import com.enodeframework.common.io.AsyncTaskResult;
import com.microsoft.conference.registration.domain.SeatAssigning.Events.OrderSeatAssignmentsCreated;
import com.microsoft.conference.registration.domain.SeatAssigning.Events.SeatAssigned;
import com.microsoft.conference.registration.domain.SeatAssigning.Events.SeatUnassigned;

public class OrderSeatAssignmentsViewModelGenerator {
    public AsyncTaskResult HandleAsync(OrderSeatAssignmentsCreated evnt) {
//            return TryTransactionAsync((connection, transaction) =>
//            {
//                var tasks = new List<Task>();
//
//                for (var assignment in evnt.Assignments)
//                {
//                    tasks.add(connection.InsertAsync(new
//                    {
//                        AssignmentsId = evnt.aggregateRootId(),
//                        OrderId = evnt.OrderId,
//                        Position = assignment.Position,
//                        SeatTypeId = assignment.Seat.SeatTypeId,
//                        SeatTypeName = assignment.Seat.SeatTypeName
//                    }, ConfigSettings.OrderSeatAssignmentsTable, transaction));
//                }
//
//                return tasks;
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatAssigned evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    AttendeeFirstName = evnt.Attendee.FirstName,
//                    AttendeeLastName = evnt.Attendee.LastName,
//                    AttendeeEmail = evnt.Attendee.Email
//                }, new
//                {
//                    AssignmentsId = evnt.aggregateRootId(),
//                    Position = evnt.Position
//                }, ConfigSettings.OrderSeatAssignmentsTable);
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatUnassigned evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    AttendeeFirstName = String.Empty,
//                    AttendeeLastName = String.Empty,
//                    AttendeeEmail = String.Empty
//                }, new
//                {
//                    AssignmentsId = evnt.aggregateRootId(),
//                    Position = evnt.Position
//                }, ConfigSettings.OrderSeatAssignmentsTable);
//            });
        return null;
    }

//        private async AsyncTaskResult TryUpdateRecordAsync(Func<IDbConnection, Task<int>> action)
//        {
//            using (var connection = GetConnection())
//            {
//                await action(connection);
//                return AsyncTaskResult.Success;
//            }
//        }
//        private async AsyncTaskResult TryTransactionAsync(Func<IDbConnection, IDbTransaction, List<Task>> actions)
//        {
//            using (var connection = GetConnection())
//            {
//                await connection.OpenAsync().ConfigureAwait(false);
//                var transaction = await Task.Run<SqlTransaction>(() => connection.BeginTransaction()).ConfigureAwait(false);
//                try
//                {
//                    await Task.WhenAll(actions(connection, transaction)).ConfigureAwait(false);
//                    await Task.Run(() => transaction.Commit()).ConfigureAwait(false);
//                    return AsyncTaskResult.Success;
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
