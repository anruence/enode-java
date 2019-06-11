using System;
using System.Collections.Generic;
using System.Linq;
using ENode.Domain;

namespace ConferenceManagement
{
    public class Conference : AggregateRoot<Guid>
    {
        private ConferenceInfo _info;
        private IList<SeatType> _seatTypes;
        private IDictionary<Guid, IEnumerable<ReservationItem>> _reservations;
        private bool _isPublished;

        public Conference(Guid id, ConferenceInfo info) : base(id)
        {
            ApplyEvent(new ConferenceCreated(info));
        }

        public void Update(ConferenceEditableInfo info)
        {
            ApplyEvent(new ConferenceUpdated(info));
        }
        public void Publish()
        {
            if (_isPublished)
            {
                throw new Exception("Conference already published.");
            }
            ApplyEvent(new ConferencePublished());
        }
        public void Unpublish()
        {
            if (!_isPublished)
            {
                throw new Exception("Conference already unpublished.");
            }
            ApplyEvent(new ConferenceUnpublished());
        }
        public void AddSeat(SeatTypeInfo seatTypeInfo, int quantity)
        {
            ApplyEvent(new SeatTypeAdded(Guid.NewGuid(), seatTypeInfo, quantity));
        }
        public void UpdateSeat(Guid seatTypeId, SeatTypeInfo seatTypeInfo, int quantity)
        {
            var seatType = _seatTypes.SingleOrDefault(x => x.Id == seatTypeId);
            if (seatType == null)
            {
                throw new Exception("Seat type not exist.");
            }
            ApplyEvent(new SeatTypeUpdated(seatTypeId, seatTypeInfo));

            if (seatType.Quantity != quantity)
            {
                var totalReservationQuantity = GetTotalReservationQuantity(seatType.Id);
                if (quantity < totalReservationQuantity)
                {
                    throw new Exception(string.Format("Quantity cannot be small than total reservation quantity:{0}", totalReservationQuantity));
                }
                ApplyEvent(new SeatTypeQuantityChanged(seatTypeId, quantity, quantity - totalReservationQuantity));
            }
        }
        public void RemoveSeat(Guid seatTypeId)
        {
            if (_isPublished)
            {
                throw new Exception("Can't delete seat type from a conference that has been published");
            }
            if (HasReservation(seatTypeId))
            {
                throw new Exception("The seat type has reservation, cannot be remove.");
            }
            ApplyEvent(new SeatTypeRemoved(seatTypeId));
        }
        public void MakeReservation(Guid reservationId, IEnumerable<ReservationItem> reservationItems)
        {
            if (!_isPublished)
            {
                throw new Exception("Can't make reservation to the conference which is not published.");
            }
            if (_reservations.ContainsKey(reservationId))
            {
                throw new Exception(string.Format("Duplicated reservation, reservationId:{0}", reservationId));
            }
            if (reservationItems == null || reservationItems.Count() == 0)
            {
                throw new Exception(string.Format("Reservation items can't be null or empty, reservationId:{0}", reservationId));
            }

            var seatAvailableQuantities = new List<SeatAvailableQuantity>();
            foreach (var reservationItem in reservationItems)
            {
                if (reservationItem.Quantity <= 0)
                {
                    throw new Exception(string.Format("Quantity must be bigger than than zero, reservationId:{0}, seatTypeId:{1}", reservationId, reservationItem.SeatTypeId));
                }
                var seatType = _seatTypes.SingleOrDefault(x => x.Id == reservationItem.SeatTypeId);
                if (seatType == null)
                {
                    throw new ArgumentOutOfRangeException(string.Format("Seat type '{0}' not exist.", reservationItem.SeatTypeId));
                }
                var availableQuantity = seatType.Quantity - GetTotalReservationQuantity(seatType.Id);
                if (availableQuantity < reservationItem.Quantity)
                {
                    throw new SeatInsufficientException(_id, reservationId);
                }
                seatAvailableQuantities.Add(new SeatAvailableQuantity(seatType.Id, availableQuantity - reservationItem.Quantity));
            }
            ApplyEvent(new SeatsReserved(reservationId, reservationItems, seatAvailableQuantities));
        }
        public void CommitReservation(Guid reservationId)
        {
            IEnumerable<ReservationItem> reservationItems;
            if (_reservations.TryGetValue(reservationId, out reservationItems))
            {
                var seatQuantities = new List<SeatQuantity>();
                foreach (var reservationItem in reservationItems)
                {
                    var seatType = _seatTypes.Single(x => x.Id == reservationItem.SeatTypeId);
                    seatQuantities.Add(new SeatQuantity(seatType.Id, seatType.Quantity - reservationItem.Quantity));
                }
                ApplyEvent(new SeatsReservationCommitted(reservationId, seatQuantities));
            }
        }
        public void CancelReservation(Guid reservationId)
        {
            IEnumerable<ReservationItem> reservationItems;
            if (_reservations.TryGetValue(reservationId, out reservationItems))
            {
                var seatAvailableQuantities = new List<SeatAvailableQuantity>();
                foreach (var reservationItem in reservationItems)
                {
                    var seatType = _seatTypes.Single(x => x.Id == reservationItem.SeatTypeId);
                    var availableQuantity = seatType.Quantity - GetTotalReservationQuantity(seatType.Id);
                    seatAvailableQuantities.Add(new SeatAvailableQuantity(seatType.Id, availableQuantity + reservationItem.Quantity));
                }
                ApplyEvent(new SeatsReservationCancelled(reservationId, seatAvailableQuantities));
            }
        }

        private bool HasReservation(Guid seatTypeId)
        {
            return _reservations.Any(x => x.Value.Any(y => y.SeatTypeId == seatTypeId));
        }
        private int GetTotalReservationQuantity(Guid seatTypeId)
        {
            var totalReservationQuantity = 0;
            foreach (var reservation in _reservations)
            {
                var reservationItem = reservation.Value.SingleOrDefault(x => x.SeatTypeId == seatTypeId);
                if (reservationItem != null)
                {
                    totalReservationQuantity += reservationItem.Quantity;
                }
            }
            return totalReservationQuantity;
        }

        #region Event Handle Methods

        private void Handle(ConferenceCreated evnt)
        {
            _id = evnt.AggregateRootId;
            _info = evnt.Info;
            _seatTypes = new List<SeatType>();
            _reservations = new Dictionary<Guid, IEnumerable<ReservationItem>>();
            _isPublished = false;
        }
        private void Handle(ConferenceUpdated evnt)
        {
            var editableInfo = evnt.Info;
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
        private void Handle(ConferencePublished evnt)
        {
            _isPublished = true;
        }
        private void Handle(ConferenceUnpublished evnt)
        {
            _isPublished = false;
        }
        private void Handle(SeatTypeAdded evnt)
        {
            _seatTypes.Add(new SeatType(evnt.SeatTypeId, evnt.SeatTypeInfo) { Quantity = evnt.Quantity });
        }
        private void Handle(SeatTypeUpdated evnt)
        {
            _seatTypes.Single(x => x.Id == evnt.SeatTypeId).Info = evnt.SeatTypeInfo;
        }
        private void Handle(SeatTypeQuantityChanged evnt)
        {
            _seatTypes.Single(x => x.Id == evnt.SeatTypeId).Quantity = evnt.Quantity;
        }
        private void Handle(SeatTypeRemoved evnt)
        {
            _seatTypes.Remove(_seatTypes.Single(x => x.Id == evnt.SeatTypeId));
        }
        private void Handle(SeatsReserved evnt)
        {
            _reservations.Add(evnt.ReservationId, evnt.ReservationItems.ToList());
        }
        private void Handle(SeatsReservationCommitted evnt)
        {
            _reservations.Remove(evnt.ReservationId);
            foreach (var seatQuantity in evnt.SeatQuantities)
            {
                _seatTypes.Single(x => x.Id == seatQuantity.SeatTypeId).Quantity = seatQuantity.Quantity;
            }
        }
        private void Handle(SeatsReservationCancelled evnt)
        {
            _reservations.Remove(evnt.ReservationId);
        }

        #endregion
    }
}
