package gongback.pureureum.application

import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsSendResponse

interface SmsService {
    fun sendSmsCertification(phoneNumberReq: PhoneNumberReq): SmsSendResponse

    fun completeCertification(phoneNumberReq: PhoneNumberReq)
}
