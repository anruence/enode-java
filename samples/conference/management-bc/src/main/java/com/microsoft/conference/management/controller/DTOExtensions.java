package com.microsoft.conference.management.controller;

import com.microsoft.conference.common.management.commands.AddSeatType;
import com.microsoft.conference.common.management.commands.CreateConference;
import com.microsoft.conference.common.management.commands.UpdateConference;
import com.microsoft.conference.common.management.commands.UpdateSeatType;
import com.microsoft.conference.management.domain.models.SeatType;
import com.microsoft.conference.management.readmodel.ConferenceDTO;
import com.microsoft.conference.management.readmodel.SeatTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface DTOExtensions {

    DTOExtensions INSTANCE = Mappers.getMapper(DTOExtensions.class);

    ConferenceInfo ToViewModel(ConferenceDTO dto);

    CreateConference ToCreateConferenceCommand(ConferenceInfo model);

    @Mapping(source = "model.name", target = "name")
    @Mapping(source = "model.description", target = "description")
    UpdateConference ToUpdateConferenceCommand(EditableConferenceInfo model);

    SeatType ToViewModel(SeatTypeDTO dto);

    @Mapping(source = "model.id", target = "id")
    AddSeatType ToAddSeatTypeCommand(SeatType model, ConferenceInfo conference);

    @Mapping(source = "model.id", target = "id")
    UpdateSeatType ToUpdateSeatTypeCommand(SeatType model, ConferenceInfo conference);
}