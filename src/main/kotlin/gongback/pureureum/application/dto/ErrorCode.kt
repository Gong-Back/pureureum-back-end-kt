package gongback.pureureum.application.dto

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val code: Int,
    val message: String
) {
    OK(HttpStatus.OK, 200, "Ok"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 401, "인증되지 않은 요청입니다"),
    FORBIDDEN(HttpStatus.FORBIDDEN, 403, "접근 권한이 없습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.BAD_REQUEST, 405, "잘못된 HTTP METHOD입니다"),
    REQUEST_RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, 409, "요청 자원이 이미 존재합니다"),
    REQUEST_RESOURCE_NOT_VALID(HttpStatus.BAD_REQUEST, 410, "요청 자원이 유효하지 않습니다"),
    REQUEST_RESOURCE_NOT_ENOUGH(HttpStatus.BAD_REQUEST, 412, "요청 자원이 충분하지 않습니다"),
    OAUTH_AUTHENTICATION_FAIL(HttpStatus.UNAUTHORIZED, 415, "OAUTH2 인증에 실패했습니다"),
    PROJECT_TOTAL_RECRUITS_FULL(HttpStatus.BAD_REQUEST, 416, "모집 인원이 가득찼습니다."),
    SMS_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 510, "SMS 전송할 수 없습니다"),
    SMS_OVER_SENDING_REQUEST(HttpStatus.INTERNAL_SERVER_ERROR, 511, "더 이상 SMS를 보낼 수 없습니다"),
    S3_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 512, "S3 서버 오류가 발생하였습니다"),
    FILE_HANDLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 513, "파일 처리 중 오류가 발생하였습니다.");

    override fun toString(): String {
        return "HttpStatus: $httpStatus, Message: $message"
    }
}
