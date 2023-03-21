package gongback.pureureum.application

import gongback.pureureum.domain.sms.SmsLog

const val RECEIVER = "010-0000-0000"

fun createSmsLog(
    receiver: String = RECEIVER,
    isSuccess: Boolean = true
): SmsLog {
    return SmsLog(receiver = receiver, isSuccess = isSuccess)
}
