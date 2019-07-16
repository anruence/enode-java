package com.microsoft.conference.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microsoft.conference.common.dataobject.ConferenceDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConferenceMapper extends BaseMapper<ConferenceDO> {

}
