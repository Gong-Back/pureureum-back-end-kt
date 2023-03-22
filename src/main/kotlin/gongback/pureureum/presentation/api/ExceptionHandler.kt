package gongback.pureureum.presentation.api

import gongback.pureureum.application.PureureumException
import gongback.pureureum.security.JwtException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[MethodArgumentNotValidException] ${ex.messages()}")
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.messages()))
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[HttpRequestMethodNotSupportedException] ${ex.messages()}")
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(ex.messages()))
    }

    private fun MethodArgumentNotValidException.messages(): String {
        return bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage.orEmpty()}" }
    }

    private fun HttpRequestMethodNotSupportedException.messages(): String {
        return "${body.title}: ${body.detail}"
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[IllegalStateException] ${ex.message}")
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.message))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[IllegalArgumentException] ${ex.message}")
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.message))
    }

    @ExceptionHandler(PureureumException::class)
    fun handlePureureumException(ex: PureureumException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[PureureumException] ${ex.message}")
        return ResponseEntity.status(ex.errorCode.httpStatus).body(ApiResponse.error(ex.message))
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(ex: JwtException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[Exception] ", ex)
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[Exception] ", ex)
        return ResponseEntity.internalServerError().body(ApiResponse.error(ex.message))
    }
}
