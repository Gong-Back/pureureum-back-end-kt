package gongback.pureureum.application.dto

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    UNAUTHORIZED(HttpStatus.FORBIDDEN, "인증되지 않은 요청입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송할 수 없습니다"),
    SMS_SENDING_OVER_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, "더 이상 SMS를 보낼 수 없습니다");

    override fun toString(): String {
        return "HttpStatus: $httpStatus, Message: $message"
    }
}
