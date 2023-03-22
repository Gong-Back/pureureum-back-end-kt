package gongback.pureureum.application.dto

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: Int,
    val message: String
) {
    UNAUTHORIZED(HttpStatus.FORBIDDEN, 401, "인증되지 않은 요청입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.FORBIDDEN, 405, "잘못된 HTTP METHOD입니다"),
    REQUEST_RESOURCE_NOT_VALID(HttpStatus.BAD_REQUEST, 410, "요청 자원이 유효하지 않습니다"),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 510, "SMS 전송할 수 없습니다"),
    SMS_OVER_SENDING_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, 511, "더 이상 SMS를 보낼 수 없습니다");

    override fun toString(): String {
        return "HttpStatus: $httpStatus, Message: $message"
    }
}
