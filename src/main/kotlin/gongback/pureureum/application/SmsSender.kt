package gongback.pureureum.application

import gongback.pureureum.application.dto.SmsRequestDto

interface SmsSender {
    fun send(smsRequestDto: SmsRequestDto)
}
