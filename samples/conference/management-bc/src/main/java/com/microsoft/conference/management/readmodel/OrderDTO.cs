using System;
using System.Collections.Generic;

namespace ConferenceManagement.ReadModel
{
    public class OrderDTO
    {
        private IList<AttendeeDTO> _attendees = new List<AttendeeDTO>();

        public Guid OrderId { get; set; }
        public Guid ConferenceId { get; set; }
        public string AccessCode { get; set; }
        public string RegistrantFirstName { get; set; }
        public string RegistrantLastName { get; set; }
        public string RegistrantEmail { get; set; }
        public decimal TotalAmount { get; set; }
        public int Status { get; set; }
        public IList<AttendeeDTO> GetAttendees()
        {
            return _attendees;
        }
        public void SetAttendees(IList<AttendeeDTO> attendees)
        {
            _attendees = attendees;
        }
        public string GetStatusText()
        {
            return ((OrderStatus)Status).ToString();
        }

        enum OrderStatus
        {
            Placed = 1,                //订单已生成
            ReservationSuccess = 2,    //位置预定已成功（下单已成功）
            ReservationFailed = 3,     //位置预定已失败（下单失败）
            PaymentSuccess = 4,        //付款已成功
            PaymentRejected = 5,       //付款已拒绝
            Expired = 6,               //订单已过期
            Success = 7,               //交易已成功
            Closed = 8                 //订单已关闭
        }
    }
    public class AttendeeDTO
    {
        public int Position { get; set; }
        public string SeatTypeName { get; set; }
        public string AttendeeFirstName { get; set; }
        public string AttendeeLastName { get; set; }
        public string AttendeeEmail { get; set; }
    }
}
