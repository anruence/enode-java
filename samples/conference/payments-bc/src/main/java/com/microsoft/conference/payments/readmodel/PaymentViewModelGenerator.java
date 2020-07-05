package com.microsoft.conference.payments.readmodel;

import com.microsoft.conference.payments.domain.events.PaymentCompleted;
import com.microsoft.conference.payments.domain.events.PaymentInitiated;
import com.microsoft.conference.payments.domain.events.PaymentRejected;

public class PaymentViewModelGenerator {
    public void HandleAsync(PaymentInitiated evnt) {
//            return TryTransactionAsync((connection, transaction) =>
//            {
//                var tasks = new List<Task>();
//                tasks.add(connection.InsertAsync(new
//                {
//                    Id = evnt.getAggregateRootId(),
//                    OrderId = evnt.OrderId,
//                    State = (int)PaymentState.Initiated,
//                    Description = evnt.Description,
//                    TotalAmount = evnt.TotalAmount,
//                    Version = evnt.Version
//                }, ConfigSettings.PaymentTable, transaction));
//                for (var item in evnt.Items)
//                {
//                    tasks.add(connection.InsertAsync(new
//                    {
//                        Id = item.Id,
//                        PaymentId = evnt.getAggregateRootId(),
//                        Description = item.Description,
//                        Amount = item.Amount
//                    }, ConfigSettings.PaymentItemTable, transaction));
//                }
//                return tasks;
//            });
    }

    public void HandleAsync(PaymentCompleted evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    State = (int)PaymentState.Completed,
//                    Version = evnt.Version
//                }, new
//                {
//                    Id = evnt.getAggregateRootId(),
//                    Version = evnt.Version - 1
//                }, ConfigSettings.PaymentTable);
//            });
    }

    public void HandleAsync(PaymentRejected evnt) {
//            return TryUpdateRecordAsync(connection =>
//            {
//                return connection.UpdateAsync(new
//                {
//                    State = (int)PaymentState.Rejected,
//                    Version = evnt.Version
//                }, new
//                {
//                    Id = evnt.getAggregateRootId(),
//                    Version = evnt.Version - 1
//                }, ConfigSettings.PaymentTable);
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
