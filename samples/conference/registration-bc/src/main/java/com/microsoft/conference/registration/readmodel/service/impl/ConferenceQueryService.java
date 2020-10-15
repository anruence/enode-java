package com.microsoft.conference.registration.readmodel.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.microsoft.conference.common.ListUtils;
import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.common.mapper.ConferenceMapper;
import com.microsoft.conference.common.mapper.SeatTypeMapper;
import com.microsoft.conference.registration.readmodel.ConferenceConvert;
import com.microsoft.conference.registration.readmodel.OrderConvert;
import com.microsoft.conference.registration.readmodel.service.ConferenceAlias;
import com.microsoft.conference.registration.readmodel.service.ConferenceDetails;
import com.microsoft.conference.registration.readmodel.service.IConferenceQueryService;
import com.microsoft.conference.registration.readmodel.service.SeatTypeName;
import com.microsoft.conference.registration.readmodel.service.SeatTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ConferenceQueryService implements IConferenceQueryService {

    @Autowired
    private ConferenceMapper conferenceMapper;

    @Autowired
    private SeatTypeMapper seatTypeMapper;

    @Override
    public ConferenceDetails getConferenceDetails(String slug) {
        LambdaQueryWrapper<ConferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConferenceDO::getSlug, slug);
        return ConferenceConvert.INSTANCE.toDetail(conferenceMapper.selectOne(wrapper));
    }

    @Override
    public ConferenceAlias getConferenceAlias(String slug) {
        LambdaQueryWrapper<ConferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConferenceDO::getSlug, slug);
        return ConferenceConvert.INSTANCE.toAlias(conferenceMapper.selectOne(wrapper));
    }

    @Override
    public List<ConferenceAlias> getPublishedConferences() {
        LambdaQueryWrapper<ConferenceDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ConferenceDO::getIsPublished, (byte) 1);
        return ListUtils.map(conferenceMapper.selectList(wrapper), ConferenceConvert.INSTANCE::toAlias);
    }

    @Override
    public List<SeatTypeVO> getPublishedSeatTypes(String conferenceId) {
        LambdaQueryWrapper<SeatTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SeatTypeDO::getConferenceId, conferenceId);
        return ListUtils.map(seatTypeMapper.selectList(wrapper), OrderConvert.INSTANCE::toDO);
    }

    @Override
    public List<SeatTypeName> getSeatTypeNames(List<String> seatTypes) {
        LambdaQueryWrapper<SeatTypeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(SeatTypeDO::getSeatTypeId, seatTypes);
        return Optional.ofNullable(seatTypeMapper.selectList(wrapper)).orElse(new ArrayList<>())
                .stream().map(seatTypeDO -> {
                    SeatTypeName seatTypeName = new SeatTypeName();
                    seatTypeName.setId(seatTypeDO.getSeatTypeId());
                    seatTypeName.setName(seatTypeDO.getName());
                    return seatTypeName;
                }).collect(Collectors.toList());
    }
}