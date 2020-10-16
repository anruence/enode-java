package com.microsoft.conference.registration.readmodel;

import com.microsoft.conference.common.registration.commands.order.PlaceOrder;
import com.microsoft.conference.registration.readmodel.service.ConferenceAlias;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RegistrationConvert {
    RegistrationConvert INSTANCE = Mappers.getMapper(RegistrationConvert.class);

    PlaceOrder toPlaceOrderCommand(ConferenceAlias alias);

}
