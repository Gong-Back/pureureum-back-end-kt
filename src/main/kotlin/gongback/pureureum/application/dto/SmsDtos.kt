package gongback.pureureum.application.dto

import jakarta.validation.constraints.Pattern

data class PhoneNumberReq(
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String
) {
    val receiver: String
        get() = phoneNumber.replace("-", "")
}

/**
 * subject(제목): LMS, MMS에서만 사용 가능
 */
data class NaverSendMessageDto(
    val type: SmsType = SmsType.SMS,
    val contentType: SmsContentType = SmsContentType.COMM,
    val from: String,
    val subject: String? = null,
    val content: String,
    val messages: List<MessageDto>,
    val reserveTime: String? = null
)

/**
 * to: 받는 사람 번호(- 제외)
 */
data class MessageDto(
    val to: String,
    val subject: String? = null,
    val content: String? = null
)

data class SmsSendResponse(
    val certificationNumber: String
)

enum class SmsType {
    SMS, LMS, MMS
}

enum class SmsContentType {
    COMM, AD
}
