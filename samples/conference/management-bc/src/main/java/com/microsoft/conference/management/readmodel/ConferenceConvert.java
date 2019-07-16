package com.microsoft.conference.management.readmodel;

import com.microsoft.conference.common.dataobject.ConferenceDO;
import com.microsoft.conference.common.dataobject.ConferenceSlugIndexDO;
import com.microsoft.conference.common.dataobject.SeatTypeDO;
import com.microsoft.conference.management.domain.events.ConferenceCreated;
import com.microsoft.conference.management.domain.events.ConferenceUpdated;
import com.microsoft.conference.management.domain.models.ConferenceEditableInfo;
import com.microsoft.conference.management.domain.models.ConferenceInfo;
import com.microsoft.conference.management.domain.models.ConferenceSlugIndex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ConferenceConvert {
    ConferenceConvert INSTANCE = Mappers.getMapper(ConferenceConvert.class);

    @Mapping(source = "evnt.version", target = "version")
    @Mapping(source = "evnt.sequence", target = "eventSequence")
    @Mapping(source = "evnt.aggregateRootId", target = "conferenceId")
    @Mapping(target = "id", ignore = true)
    ConferenceDO toDO(ConferenceCreated evnt, ConferenceInfo info);

    ConferenceDTO toDTO(ConferenceDO conferenceDO);

    ConferenceSlugIndex toDTO(ConferenceSlugIndexDO slugIndexDO);

    SeatTypeDTO toDTO(SeatTypeDO conferenceDO);

    @Mapping(source = "evnt.version", target = "version")
    @Mapping(source = "evnt.sequence", target = "eventSequence")
    @Mapping(source = "evnt.aggregateRootId", target = "conferenceId")
    @Mapping(target = "id", ignore = true)
    ConferenceDO toDO(ConferenceUpdated evnt, ConferenceEditableInfo info);
}
