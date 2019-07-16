package com.microsoft.conference.management.domain.Models;

import com.enodeframework.domain.AggregateRoot;
import com.enodeframework.infrastructure.WrappedRuntimeException;
import com.microsoft.conference.Linq;
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
import com.microsoft.conference.management.domain.PublishableExceptions.SeatInsufficientException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conference extends AggregateRoot<String> {
    private ConferenceInfo _info;
    private List<SeatType> _seatTypes;
    private Map<String, List<ReservationItem>> _reservations;
    private boolean _isPublished;

    public Conference(String id, ConferenceInfo info) {
        super(id);
        applyEvent(new ConferenceCreated(info));
    }

    public void Update(ConferenceEditableInfo info) {
        applyEvent(new ConferenceUpdated(info));
    }

    public void Publish() {
        if (_isPublished) {
            throw new RuntimeException("Conference already published.");
        }
        applyEvent(new ConferencePublished());
    }

    public void Unpublish() {
        if (!_isPublished) {
            throw new RuntimeException("Conference already unpublished.");
        }
        applyEvent(new ConferenceUnpublished());
    }

    public void addSeat(SeatTypeInfo seatTypeInfo, int quantity) {
        applyEvent(new SeatTypeAdded("", seatTypeInfo, quantity));
    }

    public void UpdateSeat(String seatTypeId, SeatTypeInfo seatTypeInfo, int quantity) {
        SeatType seatType = Linq.Single(_seatTypes, x -> x.Id.equals(seatTypeId));
        if (seatType == null) {
            throw new WrappedRuntimeException("Seat type not exist.");
        }
        applyEvent(new SeatTypeUpdated(seatTypeId, seatTypeInfo));

        if (seatType.Quantity != quantity) {
            int totalReservationQuantity = GetTotalReservationQuantity(seatType.Id);
            if (quantity < totalReservationQuantity) {
                throw new RuntimeException(String.format("Quantity cannot be small than total reservation quantity:%s", totalReservationQuantity));
            }
            applyEvent(new SeatTypeQuantityChanged(seatTypeId, quantity, quantity - totalReservationQuantity));
        }
    }

    public void removeSeat(String seatTypeId) {
        if (_isPublished) {
            throw new RuntimeException("Can't delete seat type from a conference that has been published");
        }
        if (HasReservation(seatTypeId)) {
            throw new RuntimeException("The seat type has reservation, cannot be remove.");
        }
        applyEvent(new SeatTypeRemoved(seatTypeId));
    }

    public void MakeReservation(String reservationId, List<ReservationItem> reservationItems) {
        if (!_isPublished) {
            throw new RuntimeException("Can't make reservation to the conference which is not published.");
        }
        if (_reservations.containsKey(reservationId)) {
            throw new RuntimeException(String.format("Duplicated reservation, reservationId:{}", reservationId));
        }
        if (reservationItems == null || reservationItems.size() == 0) {
            throw new RuntimeException(String.format("Reservation items can't be null or empty, reservationId:{}", reservationId));
        }

        List<SeatAvailableQuantity> seatAvailableQuantities = new ArrayList<>();
        for (ReservationItem reservationItem : reservationItems) {
            if (reservationItem.Quantity <= 0) {
                throw new RuntimeException(String.format("Quantity must be bigger than than zero, reservationId:{}, seatTypeId:{}", reservationId, reservationItem.SeatTypeId));
            }
            SeatType seatType = _seatTypes.stream().filter(x -> x.Id == reservationItem.SeatTypeId).findFirst().orElse(null);
            if (seatType == null) {
                throw new WrappedRuntimeException(String.format("Seat type '{}' not exist.", reservationItem.SeatTypeId));
            }
            int availableQuantity = seatType.Quantity - GetTotalReservationQuantity(seatType.Id);
            if (availableQuantity < reservationItem.Quantity) {
                throw new SeatInsufficientException(id, reservationId);
            }
            seatAvailableQuantities.add(new SeatAvailableQuantity(seatType.Id, availableQuantity - reservationItem.Quantity));
        }
        applyEvent(new SeatsReserved(reservationId, reservationItems, seatAvailableQuantities));
    }

    public void CommitReservation(String reservationId) {
        if (_reservations.containsKey(reservationId)) {

            List<ReservationItem> reservationItems = _reservations.get(reservationId);
            List<SeatQuantity> seatQuantities = new ArrayList<>();
            for (ReservationItem reservationItem : reservationItems) {
                SeatType seatType = Linq.Single(_seatTypes, (x -> x.Id == reservationItem.SeatTypeId));
                seatQuantities.add(new SeatQuantity(seatType.Id, seatType.Quantity - reservationItem.Quantity));
            }
            applyEvent(new SeatsReservationCommitted(reservationId, seatQuantities));
        }
    }

    public void CancelReservation(String reservationId) {

        if (_reservations.containsKey(reservationId)) {

            List<ReservationItem> reservationItems = _reservations.get(reservationId);
            List<SeatAvailableQuantity> seatAvailableQuantities = new ArrayList<>();
            for (ReservationItem reservationItem : reservationItems) {
                SeatType seatType = Linq.Single(_seatTypes, (x -> x.Id == reservationItem.SeatTypeId));
                int availableQuantity = seatType.Quantity - GetTotalReservationQuantity(seatType.Id);
                seatAvailableQuantities.add(new SeatAvailableQuantity(seatType.Id, availableQuantity + reservationItem.Quantity));
            }
            applyEvent(new SeatsReservationCancelled(reservationId, seatAvailableQuantities));
        }
    }

    private boolean HasReservation(String seatTypeId) {
        return _reservations.values().stream().anyMatch(x -> x.stream().anyMatch(y -> y.SeatTypeId == seatTypeId));
    }

    private int GetTotalReservationQuantity(String seatTypeId) {
        int totalReservationQuantity = 0;
        for (List<ReservationItem> reservation : _reservations.values()) {
            ReservationItem reservationItem = Linq.SingleOrDefault(reservation, x -> x.SeatTypeId == seatTypeId);
            if (reservationItem != null) {
                totalReservationQuantity += reservationItem.Quantity;
            }
        }
        return totalReservationQuantity;
    }


    private void Handle(ConferenceCreated evnt) {
        id = evnt.aggregateRootId();
        _info = evnt.Info;
        _seatTypes = new ArrayList<>();
        _reservations = new HashMap<>();
        _isPublished = false;
    }

    private void Handle(ConferenceUpdated evnt) {
        ConferenceEditableInfo editableInfo = evnt.Info;
        _info = new ConferenceInfo(
                _info.AccessCode,
                _info.Owner,
                _info.Slug,
                editableInfo.Name,
                editableInfo.Description,
                editableInfo.Location,
                editableInfo.Tagline,
                editableInfo.TwitterSearch,
                editableInfo.StartDate,
                editableInfo.EndDate);
    }

    private void Handle(ConferencePublished evnt) {
        _isPublished = true;
    }

    private void Handle(ConferenceUnpublished evnt) {
        _isPublished = false;
    }

    private void Handle(SeatTypeAdded evnt) {
        SeatType seatType = new SeatType(evnt.SeatTypeId, evnt.SeatTypeInfo);
        seatType.Quantity = evnt.Quantity;
        _seatTypes.add(seatType);
    }

    private void Handle(SeatTypeUpdated evnt) {
        Linq.Single(_seatTypes, x -> x.Id == evnt.SeatTypeId).Info = evnt.SeatTypeInfo;
    }

    private void Handle(SeatTypeQuantityChanged evnt) {
        Linq.Single(_seatTypes, x -> x.Id == evnt.SeatTypeId).Quantity = evnt.Quantity;
    }

    private void Handle(SeatTypeRemoved evnt) {
        // remove 指定的seatTypeId
        _seatTypes.remove(Linq.Single(_seatTypes, x -> x.Id.equals(evnt.SeatTypeId)));
    }

    private void Handle(SeatsReserved evnt) {
        _reservations.put(evnt.ReservationId, evnt.ReservationItems);
    }

    private void Handle(SeatsReservationCommitted evnt) {
        _reservations.remove(evnt.ReservationId);
        for (SeatQuantity seatQuantity : evnt.SeatQuantities) {
            Linq.Single(_seatTypes, (x -> x.Id == seatQuantity.SeatTypeId)).Quantity = seatQuantity.Quantity;
        }
    }

    private void Handle(SeatsReservationCancelled evnt) {
        _reservations.remove(evnt.ReservationId);
    }

}