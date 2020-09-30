package com.microsoft.conference.management.domain.models;

import com.microsoft.conference.common.Linq;
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
import com.microsoft.conference.management.domain.publishableexceptions.SeatInsufficientException;
import org.enodeframework.common.exception.EnodeRuntimeException;
import org.enodeframework.domain.AggregateRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conference extends AggregateRoot<String> {
    private ConferenceInfo conferenceInfo;
    private List<SeatType> seatTypes;
    private Map<String, List<ReservationItem>> reservationsMap;
    private boolean isPublished;

    public Conference(String id, ConferenceInfo info) {
        super(id);
        applyEvent(new ConferenceCreated(info));
    }

    public void update(ConferenceEditableInfo info) {
        applyEvent(new ConferenceUpdated(info));
    }

    public void publish() {
        if (isPublished) {
            throw new RuntimeException("Conference already published.");
        }
        applyEvent(new ConferencePublished());
    }

    public void unpublish() {
        if (!isPublished) {
            throw new RuntimeException("Conference already unpublished.");
        }
        applyEvent(new ConferenceUnpublished());
    }

    public void addSeat(SeatTypeInfo seatTypeInfo, int quantity) {
        applyEvent(new SeatTypeAdded("", seatTypeInfo, quantity));
    }

    public void updateSeat(String seatTypeId, SeatTypeInfo seatTypeInfo, int quantity) {
        SeatType seatType = Linq.single(seatTypes, x -> x.id.equals(seatTypeId));
        if (seatType == null) {
            throw new EnodeRuntimeException("Seat type not exist.");
        }
        applyEvent(new SeatTypeUpdated(seatTypeId, seatTypeInfo));
        if (seatType.quantity != quantity) {
            int totalReservationQuantity = getTotalReservationQuantity(seatType.id);
            if (quantity < totalReservationQuantity) {
                throw new RuntimeException(String.format("Quantity cannot be small than total reservation quantity:%s", totalReservationQuantity));
            }
            applyEvent(new SeatTypeQuantityChanged(seatTypeId, quantity, quantity - totalReservationQuantity));
        }
    }

    public void removeSeat(String seatTypeId) {
        if (isPublished) {
            throw new RuntimeException("Can't delete seat type from a conference that has been published");
        }
        if (hasReservation(seatTypeId)) {
            throw new RuntimeException("The seat type has reservation, cannot be remove.");
        }
        applyEvent(new SeatTypeRemoved(seatTypeId));
    }

    public void makeReservation(String reservationId, List<ReservationItem> reservationItems) {
        if (!isPublished) {
            throw new RuntimeException("Can't make reservation to the conference which is not published.");
        }
        if (reservationsMap.containsKey(reservationId)) {
            throw new RuntimeException(String.format("Duplicated reservation, reservationId:{}", reservationId));
        }
        if (reservationItems == null || reservationItems.size() == 0) {
            throw new RuntimeException(String.format("Reservation items can't be null or empty, reservationId:{}", reservationId));
        }
        List<SeatAvailableQuantity> seatAvailableQuantities = new ArrayList<>();
        for (ReservationItem reservationItem : reservationItems) {
            if (reservationItem.quantity <= 0) {
                throw new RuntimeException(String.format("Quantity must be bigger than than zero, reservationId:{}, seatTypeId:{}", reservationId, reservationItem.seatTypeId));
            }
            SeatType seatType = seatTypes.stream().filter(x -> x.id == reservationItem.seatTypeId).findFirst().orElse(null);
            if (seatType == null) {
                throw new EnodeRuntimeException(String.format("Seat type '{}' not exist.", reservationItem.seatTypeId));
            }
            int availableQuantity = seatType.quantity - getTotalReservationQuantity(seatType.id);
            if (availableQuantity < reservationItem.quantity) {
                throw new SeatInsufficientException(id, reservationId);
            }
            seatAvailableQuantities.add(new SeatAvailableQuantity(seatType.id, availableQuantity - reservationItem.quantity));
        }
        applyEvent(new SeatsReserved(reservationId, reservationItems, seatAvailableQuantities));
    }

    public void commitReservation(String reservationId) {
        if (reservationsMap.containsKey(reservationId)) {
            List<ReservationItem> reservationItems = reservationsMap.get(reservationId);
            List<SeatQuantity> seatQuantities = new ArrayList<>();
            for (ReservationItem reservationItem : reservationItems) {
                SeatType seatType = Linq.single(seatTypes, (x -> x.id == reservationItem.seatTypeId));
                seatQuantities.add(new SeatQuantity(seatType.id, seatType.quantity - reservationItem.quantity));
            }
            applyEvent(new SeatsReservationCommitted(reservationId, seatQuantities));
        }
    }

    public void cancelReservation(String reservationId) {
        if (reservationsMap.containsKey(reservationId)) {
            List<ReservationItem> reservationItems = reservationsMap.get(reservationId);
            List<SeatAvailableQuantity> seatAvailableQuantities = new ArrayList<>();
            for (ReservationItem reservationItem : reservationItems) {
                SeatType seatType = Linq.single(seatTypes, (x -> x.id == reservationItem.seatTypeId));
                int availableQuantity = seatType.quantity - getTotalReservationQuantity(seatType.id);
                seatAvailableQuantities.add(new SeatAvailableQuantity(seatType.id, availableQuantity + reservationItem.quantity));
            }
            applyEvent(new SeatsReservationCancelled(reservationId, seatAvailableQuantities));
        }
    }

    private boolean hasReservation(String seatTypeId) {
        return reservationsMap.values().stream().anyMatch(x -> x.stream().anyMatch(y -> y.seatTypeId == seatTypeId));
    }

    private int getTotalReservationQuantity(String seatTypeId) {
        int totalReservationQuantity = 0;
        for (List<ReservationItem> reservation : reservationsMap.values()) {
            ReservationItem reservationItem = Linq.singleOrDefault(reservation, x -> x.seatTypeId == seatTypeId);
            if (reservationItem != null) {
                totalReservationQuantity += reservationItem.quantity;
            }
        }
        return totalReservationQuantity;
    }

    private void handle(ConferenceCreated evnt) {
        id = evnt.getAggregateRootId();
        conferenceInfo = evnt.info;
        seatTypes = new ArrayList<>();
        reservationsMap = new HashMap<>();
        isPublished = false;
    }

    private void handle(ConferenceUpdated evnt) {
        ConferenceEditableInfo editableInfo = evnt.info;
        conferenceInfo = new ConferenceInfo(
                conferenceInfo.accessCode,
                conferenceInfo.owner,
                conferenceInfo.slug,
                editableInfo.name,
                editableInfo.description,
                editableInfo.location,
                editableInfo.tagline,
                editableInfo.twitterSearch,
                editableInfo.startDate,
                editableInfo.endDate);
    }

    private void handle(ConferencePublished evnt) {
        isPublished = true;
    }

    private void handle(ConferenceUnpublished evnt) {
        isPublished = false;
    }

    private void handle(SeatTypeAdded evnt) {
        SeatType seatType = new SeatType(evnt.seatTypeId, evnt.seatTypeInfo);
        seatType.quantity = evnt.quantity;
        seatTypes.add(seatType);
    }

    private void handle(SeatTypeUpdated evnt) {
        Linq.single(seatTypes, x -> x.id == evnt.seatTypeId).seatTypeInfo = evnt.seatTypeInfo;
    }

    private void handle(SeatTypeQuantityChanged evnt) {
        Linq.single(seatTypes, x -> x.id == evnt.seatTypeId).quantity = evnt.quantity;
    }

    private void handle(SeatTypeRemoved evnt) {
        // remove 指定的seatTypeId
        seatTypes.remove(Linq.single(seatTypes, x -> x.id.equals(evnt.seatTypeId)));
    }

    private void handle(SeatsReserved evnt) {
        reservationsMap.put(evnt.reservationId, evnt.reservationItems);
    }

    private void handle(SeatsReservationCommitted evnt) {
        reservationsMap.remove(evnt.reservationId);
        for (SeatQuantity seatQuantity : evnt.seatQuantities) {
            Linq.single(seatTypes, (x -> x.id == seatQuantity.seatTypeId)).quantity = seatQuantity.quantity;
        }
    }

    private void handle(SeatsReservationCancelled evnt) {
        reservationsMap.remove(evnt.ReservationId);
    }
}