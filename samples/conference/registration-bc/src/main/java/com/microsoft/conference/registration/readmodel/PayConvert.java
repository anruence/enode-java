package com.microsoft.conference.registration.readmodel;

import com.microsoft.conference.common.dataobject.PaymentDO;
import com.microsoft.conference.registration.readmodel.service.PaymentVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PayConvert {
    PayConvert INSTANCE = Mappers.getMapper(PayConvert.class);

    PaymentVO toPayment(PaymentDO paymentDO);
}
