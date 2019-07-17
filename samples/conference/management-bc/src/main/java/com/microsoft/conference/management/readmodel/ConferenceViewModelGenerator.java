package com.microsoft.conference.management.readmodel;

import com.enodeframework.annotation.Event;
import com.enodeframework.common.io.AsyncTaskResult;
import com.microsoft.conference.management.domain.Events.ConferenceCreated;
import com.microsoft.conference.management.domain.Events.ConferencePublished;
import com.microsoft.conference.management.domain.Events.ConferenceUnpublished;
import com.microsoft.conference.management.domain.Events.ConferenceUpdated;
import com.microsoft.conference.management.domain.Events.SeatTypeAdded;
import com.microsoft.conference.management.domain.Events.SeatTypeQuantityChanged;
import com.microsoft.conference.management.domain.Events.SeatTypeRemoved;
import com.microsoft.conference.management.domain.Events.SeatTypeUpdated;
import com.microsoft.conference.management.domain.Events.SeatsReservationCancelled;
import com.microsoft.conference.management.domain.Events.SeatsReservationCommitted;
import com.microsoft.conference.management.domain.Events.SeatsReserved;

/**
 * IMessageHandler<ConferenceCreated>,
 * IMessageHandler<ConferenceUpdated>,
 * IMessageHandler<ConferencePublished>,
 * IMessageHandler<ConferenceUnpublished>,
 * IMessageHandler<SeatTypeadded>,
 * IMessageHandler<SeatTypeUpdated>,
 * IMessageHandler<SeatTypeQuantityChanged>,
 * IMessageHandler<SeatTypeRemoved>,
 * IMessageHandler<SeatsReserved>,
 * IMessageHandler<SeatsReservationCommitted>,
 * IMessageHandler<SeatsReservationCancelled>
 */

@Event
public class ConferenceViewModelGenerator {

    public AsyncTaskResult HandleAsync(ConferenceCreated evnt) {
//        return TryInsertRecordAsync(connection = >
//                {
//                        var info = evnt.Info;
//        return connection.InsertAsync(new
//        {
//            Id = evnt.getAggregateRootId(),
//                    AccessCode = info.AccessCode,
//                    OwnerName = info.Owner.Name,
//                    OwnerEmail = info.Owner.Email,
//                    Slug = info.Slug,
//                    Name = info.Name,
//                    Description = info.Description,
//                    Location = info.Location,
//                    Tagline = info.Tagline,
//                    TwitterSearch = info.TwitterSearch,
//                    StartDate = info.StartDate,
//                    EndDate = info.EndDate,
//                    IsPublished = 0,
//                    Version = evnt.Version,
//                    EventSequence = evnt.Sequence
//        },ConfigSettings.ConferenceTable);
//            });
        return AsyncTaskResult.Success;
    }

    public AsyncTaskResult HandleAsync(ConferenceUpdated evnt) {
//        return TryUpdateRecordAsync(connection = >
//                {
//                        var info = evnt.Info;
//        return connection.UpdateAsync(new
//        {
//            Name = info.Name,
//                    Description = info.Description,
//                    Location = info.Location,
//                    Tagline = info.Tagline,
//                    TwitterSearch = info.TwitterSearch,
//                    StartDate = info.StartDate,
//                    EndDate = info.EndDate,
//                    IsPublished = 0,
//                    Version = evnt.Version,
//                    EventSequence = evnt.Sequence
//        },new
//        {
//            Id = evnt.getAggregateRootId(),
//                    Version = evnt.Version - 1
//        },ConfigSettings.ConferenceTable);
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(ConferencePublished evnt) {
//        return TryUpdateRecordAsync(connection = >
//                {
//        return connection.UpdateAsync(new
//        {
//            IsPublished = 1,
//                    Version = evnt.Version,
//                    EventSequence = evnt.Sequence
//        },new
//        {
//            Id = evnt.getAggregateRootId(),
//                    Version = evnt.Version - 1
//        },ConfigSettings.ConferenceTable);
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(ConferenceUnpublished evnt) {
//        return TryUpdateRecordAsync(connection = >
//                {
//        return connection.UpdateAsync(new
//        {
//            IsPublished = 0,
//                    Version = evnt.Version,
//                    EventSequence = evnt.Sequence
//        },new
//        {
//            Id = evnt.getAggregateRootId(),
//                    Version = evnt.Version - 1
//        },ConfigSettings.ConferenceTable);
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatTypeAdded evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//        if (effectedRows == 1) {
//            await connection.InsertAsync(new
//            {
//                Id = evnt.SeatTypeId,
//                        Name = evnt.SeatTypeInfo.Name,
//                        Description = evnt.SeatTypeInfo.Description,
//                        Quantity = evnt.Quantity,
//                        AvailableQuantity = evnt.Quantity,
//                        Price = evnt.SeatTypeInfo.Price,
//                        ConferenceId = evnt.getAggregateRootId(),
//            },ConfigSettings.SeatTypeTable, transaction);
//        }
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatTypeUpdated evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//
//        if (effectedRows == 1) {
//            await connection.UpdateAsync(new
//            {
//                Name = evnt.SeatTypeInfo.Name,
//                        Description = evnt.SeatTypeInfo.Description,
//                        Price = evnt.SeatTypeInfo.Price
//            },new
//            {
//                ConferenceId = evnt.getAggregateRootId(),
//                        Id = evnt.SeatTypeId
//            },ConfigSettings.SeatTypeTable, transaction);
//        }
//            });

        return null;
    }

    public AsyncTaskResult HandleAsync(SeatTypeQuantityChanged evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//
//        if (effectedRows == 1) {
//            await connection.UpdateAsync(new
//            {
//                Quantity = evnt.Quantity,
//                        AvailableQuantity = evnt.AvailableQuantity
//            },new
//            {
//                ConferenceId = evnt.getAggregateRootId(),
//                        Id = evnt.SeatTypeId
//            },ConfigSettings.SeatTypeTable, transaction);
//        }
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatTypeRemoved evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//        if (effectedRows == 1) {
//            await connection.DeleteAsync(new
//            {
//                ConferenceId = evnt.getAggregateRootId(),
//                        Id = evnt.SeatTypeId
//            },ConfigSettings.SeatTypeTable, transaction);
//        }
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatsReserved evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//
//        if (effectedRows == 1) {
//            var tasks = new List<Task>();
//
//            //插入预定记录
//            for (var reservationItem in evnt.ReservationItems)
//            {
//                tasks.add(connection.InsertAsync(new
//                {
//                    ConferenceId = evnt.getAggregateRootId(),
//                            ReservationId = evnt.ReservationId,
//                            SeatTypeId = reservationItem.SeatTypeId,
//                            Quantity = reservationItem.Quantity
//                },ConfigSettings.ReservationItemsTable, transaction));
//            }
//
//            //更新位置的可用数量
//            for (var seatAvailableQuantity in evnt.SeatAvailableQuantities)
//            {
//                tasks.add(connection.UpdateAsync(new
//                {
//                    AvailableQuantity = seatAvailableQuantity.AvailableQuantity
//                },new
//                {
//                    ConferenceId = evnt.getAggregateRootId(),
//                            Id = seatAvailableQuantity.SeatTypeId
//                },ConfigSettings.SeatTypeTable, transaction));
//            }
//
//            await Task.WhenAll(tasks);
//        }
//            });

        return null;
    }

    public AsyncTaskResult HandleAsync(SeatsReservationCommitted evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//
//        if (effectedRows == 1) {
//            var tasks = new List<Task>();
//
//            //删除预定记录
//            tasks.add(connection.DeleteAsync(new
//            {
//                ConferenceId = evnt.getAggregateRootId(),
//                        ReservationId = evnt.ReservationId
//            },ConfigSettings.ReservationItemsTable, transaction));
//
//            //更新位置的数量
//            for (var seatQuantity in evnt.SeatQuantities)
//            {
//                tasks.add(connection.UpdateAsync(new
//                {
//                    Quantity = seatQuantity.Quantity
//                },new
//                {
//                    ConferenceId = evnt.getAggregateRootId(),
//                            Id = seatQuantity.SeatTypeId
//                },ConfigSettings.SeatTypeTable, transaction));
//            }
//
//            await Task.WhenAll(tasks);
//        }
//            });
        return null;
    }

    public AsyncTaskResult HandleAsync(SeatsReservationCancelled evnt) {
//        return TryTransactionAsync(async(connection, transaction) = >
//                {
//                        var effectedRows = await connection.UpdateAsync(new
//                        {
//                                Version = evnt.Version,
//                                EventSequence = evnt.Sequence
//                        }, new
//                        {
//                                Id = evnt.getAggregateRootId(),
//                                Version = evnt.Version - 1
//                        }, ConfigSettings.ConferenceTable, transaction);
//
//        if (effectedRows == 1) {
//            var tasks = new List<Task>();
//
//            //删除预定记录
//            tasks.add(connection.DeleteAsync(new
//            {
//                ConferenceId = evnt.getAggregateRootId(),
//                        ReservationId = evnt.ReservationId
//            },ConfigSettings.ReservationItemsTable, transaction));
//
//            //更新位置的可用数量
//            for (var seatAvailableQuantity in evnt.SeatAvailableQuantities)
//            {
//                tasks.add(connection.UpdateAsync(new
//                {
//                    AvailableQuantity = seatAvailableQuantity.AvailableQuantity
//                },new
//                {
//                    ConferenceId = evnt.getAggregateRootId(),
//                            Id = seatAvailableQuantity.SeatTypeId
//                },ConfigSettings.SeatTypeTable, transaction));
//            }
//
//            await Task.WhenAll(tasks);
//        }
//            });
        return null;
    }

//    private  AsyncTaskResult TryInsertRecordAsync(Func<IDbConnection, Task<long>> action) {
//        try {
//            using(var connection = GetConnection())
//            {
//                await action (connection);
//                return AsyncTaskResult.Success;
//            }
//        } catch (SqlException ex) {
//            if (ex.Number == 2627)  //主键冲突，忽略即可；出现这种情况，是因为同一个消息的重复处理
//            {
//                return AsyncTaskResult.Success;
//            }
//            throw ;
//        }
//    }

//    private async AsyncTaskResult TryUpdateRecordAsync(Func<IDbConnection, Task<int>> action) {
//        using(var connection = GetConnection())
//        {
//            await action (connection);
//            return AsyncTaskResult.Success;
//        }
//    }

//    private async AsyncTaskResult TryTransactionAsync(Func<IDbConnection, IDbTransaction, Task> action) {
//        using(var connection = GetConnection())
//        {
//            await connection.OpenAsync().ConfigureAwait(false);
//            var transaction = await Task.Run<SqlTransaction> (() = > connection.BeginTransaction()).
//            ConfigureAwait(false);
//            try {
//                await action (connection, transaction).ConfigureAwait(false);
//                await Task.Run(() = > transaction.Commit()).ConfigureAwait(false);
//                return AsyncTaskResult.Success;
//            } catch
//            {
//                transaction.Rollback();
//                throw ;
//            }
//        }
//    }
//
//    private async AsyncTaskResult TryTransactionAsync(Func<IDbConnection, IDbTransaction, List<Task>> actions) {
//        using(var connection = GetConnection())
//        {
//            await connection.OpenAsync().ConfigureAwait(false);
//            var transaction = await Task.Run<SqlTransaction> (() = > connection.BeginTransaction()).
//            ConfigureAwait(false);
//            try {
//                await Task.WhenAll(actions(connection, transaction)).ConfigureAwait(false);
//                await Task.Run(() = > transaction.Commit()).ConfigureAwait(false);
//                return AsyncTaskResult.Success;
//            } catch
//            {
//                transaction.Rollback();
//                throw ;
//            }
//        }
//    }
//
//    private SqlConnection GetConnection() {
//        return new SqlConnection(ConfigSettings.ConferenceConnectionString);
//    }

}
