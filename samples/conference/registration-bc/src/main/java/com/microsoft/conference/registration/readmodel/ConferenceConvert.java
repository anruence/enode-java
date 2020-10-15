package com.microsoft.conference.registration.readmodel;

import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.registration.readmodel.service.ConferenceAlias;
import com.microsoft.conference.registration.readmodel.service.ConferenceDetails;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConferenceConvert {
    ConferenceConvert INSTANCE = Mappers.getMapper(ConferenceConvert.class);

    ConferenceDetails toDetail(ConferenceDO evnt);

    ConferenceAlias toAlias(ConferenceDO evnt);
}
