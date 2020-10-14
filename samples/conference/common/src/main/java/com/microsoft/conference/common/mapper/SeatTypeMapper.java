package com.microsoft.conference.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeatTypeMapper extends BaseMapper<SeatTypeDO> {

}
