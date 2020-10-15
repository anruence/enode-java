package com.microsoft.conference.common.dataobject;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

@TableName(value = "reservation_item")
@Getter
@Setter
public class ReservationItemDO {
    private Long id;
    private String conferenceId;
    private String reservationId;
    private String seatTypeId;
    private Integer quantity;
}
