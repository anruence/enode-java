package com.microsoft.conference.management.readmodel;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.common.dataobject.OrderDO;
import com.microsoft.conference.common.dataobject.OrderSeatAssignmentDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.common.mapper.ConferenceMapper;
import com.microsoft.conference.common.mapper.OrderMapper;
import com.microsoft.conference.common.mapper.OrderSeatAssignmentMapper;
import com.microsoft.conference.common.mapper.SeatTypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConferenceQueryService {

    @Autowired
    private ConferenceMapper conferenceMapper;
    @Autowired
    private SeatTypeMapper seatTypeMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderSeatAssignmentMapper orderSeatAssignmentMapper;

    public ConferenceDTO findConference(String slug) {
        LambdaQueryWrapper<ConferenceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConferenceDO::getSlug, slug);
        ConferenceDO conferenceDO = conferenceMapper.selectOne(queryWrapper);
        return ConferenceConvert.INSTANCE.toDTO(conferenceDO);
    }

    public ConferenceDTO findConference(String email, String accessCode) {
        LambdaQueryWrapper<ConferenceDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ConferenceDO::getOwnerEmail, email);
        queryWrapper.eq(ConferenceDO::getAccessCode, accessCode);
        ConferenceDO conferenceDO = conferenceMapper.selectOne(queryWrapper);
        return ConferenceConvert.INSTANCE.toDTO(conferenceDO);
    }

    public List<SeatTypeDTO> findSeatTypes(String conferenceId) {
        LambdaQueryWrapper<SeatTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeatTypeDO::getConferenceId, conferenceId);
        List<SeatTypeDO> seatTypeDOS = seatTypeMapper.selectList(queryWrapper);
        Optional.ofNullable(seatTypeDOS).orElse(new ArrayList<>())
                .stream().map(x -> ConferenceConvert.INSTANCE.toDTO(x))
                .collect(Collectors.toList());
        return new ArrayList<>();
    }

    public SeatTypeDTO findSeatType(String seatTypeId) {
        LambdaQueryWrapper<SeatTypeDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeatTypeDO::getSeatTypeId, seatTypeId);
        SeatTypeDO seatTypeDO = seatTypeMapper.selectOne(queryWrapper);
        return ConferenceConvert.INSTANCE.toDTO(seatTypeDO);
    }

    public List<OrderDTO> findOrders(String conferenceId) {
        LambdaQueryWrapper<OrderDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDO::getConferenceId, conferenceId);
        List<OrderDO> orderDOS = orderMapper.selectList(queryWrapper);
        return Optional.ofNullable(orderDOS).orElse(new ArrayList<>()).stream().map(orderDO -> {
            LambdaQueryWrapper<OrderSeatAssignmentDO> wrapper = new LambdaQueryWrapper<>();
            List<OrderSeatAssignmentDO> seatAssignmentDOS = orderSeatAssignmentMapper.selectList(wrapper);
            OrderDTO orderDTO = OrderConvert.INSTANCE.toDTO(orderDO);
            orderDTO.setAttendees(Optional.ofNullable(seatAssignmentDOS).orElse(new ArrayList<>()).stream()
                    .map(OrderConvert.INSTANCE::toDTO)
                    .collect(Collectors.toList()));
            return orderDTO;
        }).collect(Collectors.toList());
    }
}
