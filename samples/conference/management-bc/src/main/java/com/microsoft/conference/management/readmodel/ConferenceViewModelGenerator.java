package com.microsoft.conference.management.readmodel;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.common.dataobject.ReservationItemDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.common.mapper.ConferenceMapper;
import com.microsoft.conference.common.mapper.ReservationItemMapper;
import com.microsoft.conference.common.mapper.SeatTypeMapper;
import com.microsoft.conference.management.domain.events.ConferenceCreated;
import com.microsoft.conference.management.domain.events.ConferencePublished;
import com.microsoft.conference.management.domain.events.ConferenceUnpublished;
import com.microsoft.conference.management.domain.events.ConferenceUpdated;
import com.microsoft.conference.management.domain.events.SeatTypeAdded;
import com.microsoft.conference.management.domain.events.SeatTypeQuantityChanged;
import com.microsoft.conference.management.domain.events.SeatTypeRemoved;
import com.microsoft.conference.management.domain.events.SeatTypeUpdated;
import com.microsoft.conference.management.domain.events.SeatsReservationCancelled;
import com.microsoft.conference.management.domain.events.SeatsReservationCommitted;
import com.microsoft.conference.management.domain.events.SeatsReserved;
import com.microsoft.conference.management.domain.models.ConferenceInfo;
import com.microsoft.conference.management.domain.models.ReservationItem;
import com.microsoft.conference.management.domain.models.SeatAvailableQuantity;
import com.microsoft.conference.management.domain.models.SeatQuantity;
import org.enodeframework.annotation.Event;
import org.enodeframework.annotation.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.concurrent.CompletableFuture;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(ConferenceViewModelGenerator.class);

    @Autowired
    private ConferenceMapper conferenceMapper;

    @Autowired
    private SeatTypeMapper seatTypeMapper;

    @Autowired
    private ReservationItemMapper reservationItemMapper;

    @Subscribe
    public void handleAsync(ConferenceCreated evnt) {
        ConferenceDO conferenceDO = ConferenceConvert.INSTANCE.toDO(evnt, evnt.getInfo());
        ConferenceInfo conferenceInfo = evnt.getInfo();
        conferenceDO.setConferenceId(evnt.getAggregateRootId());
        conferenceDO.setOwnerName(conferenceInfo.getOwner().getName());
        conferenceDO.setOwnerEmail(conferenceInfo.getOwner().getEmail());
        try {
            conferenceMapper.insert(conferenceDO);
        } catch (DuplicateKeyException ex) {
            LOGGER.error("insert conference failed", ex);
            //主键冲突，忽略即可；出现这种情况，是因为同一个消息的重复处理
        }
    }

    @Subscribe
    public void handleAsync(ConferenceUpdated evnt) {
        ConferenceDO conferenceDO = ConferenceConvert.INSTANCE.toDO(evnt, evnt.getInfo());
        conferenceDO.setPublished((byte) 0);
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion() - 1);
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        CompletableFuture.runAsync(() -> conferenceMapper.update(conferenceDO, updateWrapper));
    }

    @Subscribe
    public void handleAsync(ConferencePublished evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setPublished((byte) 1);
        conferenceDO.setConferenceId(evnt.getAggregateRootId());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion() - 1);
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        CompletableFuture.runAsync(() -> conferenceMapper.update(conferenceDO, updateWrapper));
    }

    @Subscribe
    public void handleAsync(ConferenceUnpublished evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setPublished((byte) 0);
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        conferenceDO.setConferenceId(evnt.getAggregateRootId());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion() - 1);
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        CompletableFuture.runAsync(() -> conferenceMapper.update(conferenceDO, updateWrapper));
    }

    @Subscribe
    public void handleAsync(SeatTypeAdded evnt) {
        // transaction
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion() - 1);
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            SeatTypeDO seatTypeDO = new SeatTypeDO();
            seatTypeDO.setConferenceId(evnt.getAggregateRootId());
            seatTypeDO.setAvailableQuantity(evnt.getQuantity());
            seatTypeDO.setQuantity(evnt.getQuantity());
            seatTypeDO.setPrice(evnt.getSeatTypeInfo().getPrice());
            seatTypeDO.setName(evnt.getSeatTypeInfo().getName());
            seatTypeDO.setDescription(evnt.getSeatTypeInfo().getDescription());
            seatTypeMapper.insert(seatTypeDO);
        }
    }

    @Subscribe
    public void handleAsync(SeatTypeUpdated evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion() - 1);
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);

        if (effectedRows == 1) {
            SeatTypeDO seatTypeDO = new SeatTypeDO();
            seatTypeDO.setPrice(evnt.getSeatTypeInfo().getPrice());
            seatTypeDO.setName(evnt.getSeatTypeInfo().getName());
            seatTypeDO.setDescription(evnt.getSeatTypeInfo().getDescription());
            LambdaUpdateWrapper<SeatTypeDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SeatTypeDO::getSeatTypeId, evnt.getSeatTypeId());
            wrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
            seatTypeMapper.update(seatTypeDO, wrapper);
        }
    }

    @Subscribe
    public void handleAsync(SeatTypeQuantityChanged evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion());
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        updateWrapper.eq(ConferenceDO::getEventSequence, evnt.getSequence() - 1);
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            SeatTypeDO seatTypeDO = new SeatTypeDO();
            seatTypeDO.setQuantity(evnt.getQuantity());
            seatTypeDO.setAvailableQuantity(evnt.getAvailableQuantity());
            LambdaUpdateWrapper<SeatTypeDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SeatTypeDO::getSeatTypeId, evnt.getSeatTypeId());
            wrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
            seatTypeMapper.update(seatTypeDO, wrapper);
        }
    }

    @Subscribe
    public void handleAsync(SeatTypeRemoved evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion());
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            LambdaUpdateWrapper<SeatTypeDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
            wrapper.eq(SeatTypeDO::getSeatTypeId, evnt.getSeatTypeId());
            seatTypeMapper.delete(wrapper);
        }
    }

    @Subscribe
    public void handleAsync(SeatsReserved evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion());
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            //插入预定记录
            for (ReservationItem reservationItem : evnt.getReservationItems()) {
                ReservationItemDO reservationItemDO = new ReservationItemDO();
                reservationItemDO.setConferenceId(evnt.getAggregateRootId());
                reservationItemDO.setReservationId(evnt.getReservationId());
                reservationItemDO.setQuantity(reservationItem.getQuantity());
                reservationItemDO.setSeatTypeId(reservationItem.getSeatTypeId());
                reservationItemMapper.insert(reservationItemDO);
            }
            //更新位置的可用数量
            for (SeatAvailableQuantity availableQuantity : evnt.getSeatAvailableQuantities()) {
                SeatTypeDO seatTypeDO = new SeatTypeDO();
                seatTypeDO.setAvailableQuantity(availableQuantity.getAvailableQuantity());
                LambdaUpdateWrapper<SeatTypeDO> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(SeatTypeDO::getSeatTypeId, availableQuantity.getSeatTypeId());
                wrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
                seatTypeMapper.update(seatTypeDO, wrapper);
            }
        }
    }

    @Subscribe
    public void handleAsync(SeatsReservationCommitted evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion());
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            LambdaUpdateWrapper<ReservationItemDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ReservationItemDO::getReservationId, evnt.getReservationId());
            wrapper.eq(ReservationItemDO::getConferenceId, evnt.getAggregateRootId());
            //删除预定记录
            reservationItemMapper.delete(wrapper);
            //更新位置的数量
            for (SeatQuantity seatQuantity : evnt.getSeatQuantities()) {
                SeatTypeDO seatTypeDO = new SeatTypeDO();
                seatTypeDO.setQuantity(seatQuantity.getQuantity());
                LambdaUpdateWrapper<SeatTypeDO> seatTypeWrapper = new LambdaUpdateWrapper<>();
                seatTypeWrapper.eq(SeatTypeDO::getSeatTypeId, seatQuantity.getSeatTypeId());
                seatTypeWrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
                seatTypeMapper.update(seatTypeDO, seatTypeWrapper);
            }
        }
    }

    @Subscribe
    public void handleAsync(SeatsReservationCancelled evnt) {
        ConferenceDO conferenceDO = new ConferenceDO();
        conferenceDO.setVersion(evnt.getVersion());
        conferenceDO.setEventSequence(evnt.getSequence());
        LambdaUpdateWrapper<ConferenceDO> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ConferenceDO::getVersion, evnt.getVersion());
        updateWrapper.eq(ConferenceDO::getConferenceId, evnt.getAggregateRootId());
        int effectedRows = conferenceMapper.update(conferenceDO, updateWrapper);
        if (effectedRows == 1) {
            LambdaUpdateWrapper<ReservationItemDO> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(ReservationItemDO::getReservationId, evnt.getReservationId());
            wrapper.eq(ReservationItemDO::getConferenceId, evnt.getAggregateRootId());
            //删除预定记录
            reservationItemMapper.delete(wrapper);
            //更新位置的可用数量
            for (SeatAvailableQuantity seatQuantity : evnt.getSeatAvailableQuantities()) {
                SeatTypeDO seatTypeDO = new SeatTypeDO();
                seatTypeDO.setAvailableQuantity(seatQuantity.getAvailableQuantity());
                LambdaUpdateWrapper<SeatTypeDO> seatTypeWrapper = new LambdaUpdateWrapper<>();
                seatTypeWrapper.eq(SeatTypeDO::getSeatTypeId, seatQuantity.getSeatTypeId());
                seatTypeWrapper.eq(SeatTypeDO::getConferenceId, evnt.getAggregateRootId());
                seatTypeMapper.update(seatTypeDO, seatTypeWrapper);
            }
        }
    }
}
