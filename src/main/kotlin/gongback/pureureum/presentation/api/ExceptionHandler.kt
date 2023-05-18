package gongback.pureureum.presentation.api

import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import gongback.pureureum.application.PureureumException
import gongback.pureureum.application.S3Exception
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.security.JwtException
import gongback.pureureum.security.JwtExpiredException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
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
        return ResponseEntity.status(ErrorCode.REQUEST_RESOURCE_NOT_VALID.httpStatus)
            .body(ApiResponse.error(ErrorCode.REQUEST_RESOURCE_NOT_VALID.code, ex.messages()))
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[HttpMessageNotReadableException] ${ex.message}")
        val errorMessage = when (val cause = ex.cause) {
            is MissingKotlinParameterException -> "${cause.parameter.name} is null"

            else -> "유효하지 않은 요청입니다"
        }
        return ResponseEntity.status(ErrorCode.REQUEST_RESOURCE_NOT_VALID.httpStatus)
            .body(ApiResponse.error(ErrorCode.REQUEST_RESOURCE_NOT_VALID.code, errorMessage))
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        logger.error("[HttpRequestMethodNotSupportedException] ${ex.messages()}")
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED.code, ex.messages()))
    }

    private fun MethodArgumentNotValidException.messages(): List<String> {
        return bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage.orEmpty()}" }
    }

    private fun HttpRequestMethodNotSupportedException.messages(): String {
        return "${body.title}: ${body.detail}"
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[IllegalStateException] ${ex.message}")
        return ResponseEntity.badRequest()
            .body(
                ApiResponse.error(
                    ErrorCode.REQUEST_RESOURCE_NOT_VALID.code,
                    ex.message ?: ErrorCode.REQUEST_RESOURCE_NOT_VALID.message
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[IllegalArgumentException] ${ex.message}")
        return ResponseEntity.badRequest()
            .body(
                ApiResponse.error(
                    ErrorCode.REQUEST_RESOURCE_NOT_VALID.code,
                    ex.message ?: ErrorCode.REQUEST_RESOURCE_NOT_VALID.message
                )
            )
    }

    @ExceptionHandler(PureureumException::class)
    fun handlePureureumException(ex: PureureumException): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[PureureumException] ${ex.message}")
        return ResponseEntity.status(ex.errorCode.httpStatus)
            .body(ApiResponse.error(ex.errorCode.code, ex.message ?: ex.errorCode.message))
    }

    @ExceptionHandler(S3Exception::class)
    fun handleS3Exception(ex: S3Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[S3Exception] ", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(ex.errorCode.code, ex.message ?: ex.errorCode.message))
    }

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(
        ex: JwtException,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.error("[JwtException] ", ex)
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.code, ex.message))
    }

    @ExceptionHandler(JwtExpiredException::class)
    fun handleJwtExpiredException(
        ex: JwtExpiredException,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.error("[JwtExpiredException] ", ex)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.code, ex.message))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ApiResponse<Unit>> {
        logger.error("[Exception] ", ex)
        return ResponseEntity.internalServerError()
            .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex.message ?: ""))
    }
}
