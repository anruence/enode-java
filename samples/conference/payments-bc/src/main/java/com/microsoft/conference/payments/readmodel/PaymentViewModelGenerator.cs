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

namespace Payments.ReadModel
{
    [Component]
    public class PaymentViewModelGenerator :
        IMessageHandler<PaymentInitiated>,
        IMessageHandler<PaymentCompleted>,
        IMessageHandler<PaymentRejected>
    {
        public Task<AsyncTaskResult> HandleAsync(PaymentInitiated evnt)
        {
            return TryTransactionAsync((connection, transaction) =>
            {
                var tasks = new List<Task>();
                tasks.Add(connection.InsertAsync(new
                {
                    Id = evnt.AggregateRootId,
                    OrderId = evnt.OrderId,
                    State = (int)PaymentState.Initiated,
                    Description = evnt.Description,
                    TotalAmount = evnt.TotalAmount,
                    Version = evnt.Version
                }, ConfigSettings.PaymentTable, transaction));
                foreach (var item in evnt.Items)
                {
                    tasks.Add(connection.InsertAsync(new
                    {
                        Id = item.Id,
                        PaymentId = evnt.AggregateRootId,
                        Description = item.Description,
                        Amount = item.Amount
                    }, ConfigSettings.PaymentItemTable, transaction));
                }
                return tasks;
            });
        }
        public Task<AsyncTaskResult> HandleAsync(PaymentCompleted evnt)
        {
            return TryUpdateRecordAsync(connection =>
            {
                return connection.UpdateAsync(new
                {
                    State = (int)PaymentState.Completed,
                    Version = evnt.Version
                }, new
                {
                    Id = evnt.AggregateRootId,
                    Version = evnt.Version - 1
                }, ConfigSettings.PaymentTable);
            });
        }
        public Task<AsyncTaskResult> HandleAsync(PaymentRejected evnt)
        {
            return TryUpdateRecordAsync(connection =>
            {
                return connection.UpdateAsync(new
                {
                    State = (int)PaymentState.Rejected,
                    Version = evnt.Version
                }, new
                {
                    Id = evnt.AggregateRootId,
                    Version = evnt.Version - 1
                }, ConfigSettings.PaymentTable);
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
