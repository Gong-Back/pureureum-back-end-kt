package gongback.pureureum.application.dto

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "올바르지 않은 접근입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "올바르지 않은 접근입니다."),
    NOT_FOUND(HttpStatus.BAD_REQUEST, "요청하신 정보를 찾을 수 없습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "SMS 전송할 수 없습니다."),
    SMS_SENDING_OVER_REQUEST(HttpStatus.BAD_REQUEST, "더 이상 SMS를 보낼 수 없습니다.");

    override fun toString(): String {
        return "HttpStatus: $httpStatus, Message: $message"
    }
}
