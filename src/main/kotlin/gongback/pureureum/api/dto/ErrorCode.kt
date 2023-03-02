package gongback.pureureum.api.dto

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val httpStatus: HttpStatus,
    val message: String
) {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED!"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN!")
}
