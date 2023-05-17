package gongback.pureureum.presentation.api

import gongback.pureureum.application.dto.ErrorCode
import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val code: Int,
    val messages: List<String>? = null,
    val data: T? = null
) {
    companion object {
        fun error(code: Int, message: String?): ApiResponse<Unit> =
            ApiResponse(code = code, messages = message?.let { listOf(message) })

        fun error(code: Int, messages: List<String>): ApiResponse<Unit> =
            ApiResponse(code = code, messages = messages)

        fun <T> error(errorCode: ErrorCode, data: T): ApiResponse<T> =
            ApiResponse(code = errorCode.code, messages = listOf(errorCode.message), data)

        fun error(errorCode: ErrorCode): ApiResponse<Unit> =
            ApiResponse(code = errorCode.code, messages = listOf(errorCode.message))

        fun <T> error(code: Int, message: String, data: T): ApiResponse<T> =
            ApiResponse(code = code, messages = listOf(message), data)

        fun <T> ok(): ApiResponse<T> = ApiResponse(code = ErrorCode.OK.code)
        fun <T> ok(data: T?): ApiResponse<T> = ApiResponse(code = ErrorCode.OK.code, data = data)
        fun <T> created(data: T?): ApiResponse<T> =
            ApiResponse(code = HttpStatus.CREATED.value(), data = data)
    }
}
