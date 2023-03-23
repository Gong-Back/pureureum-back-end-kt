package gongback.pureureum.presentation.api

import org.springframework.http.HttpStatus

data class ApiResponse<T>(
    val code: Int,
    val messages: List<String>? = null,
    val data: T? = null
) {
    companion object {
        fun error(code: Int, message: String): ApiResponse<Unit> =
            ApiResponse(code = code, messages = listOf(message))

        fun error(code: Int, messages: List<String>): ApiResponse<Unit> =
            ApiResponse(code = code, messages = messages)

        fun <T> ok(data: T?): ApiResponse<T> = ApiResponse(code = HttpStatus.OK.value(), data = data)
        fun <T> created(data: T?): ApiResponse<T> =
            ApiResponse(code = HttpStatus.CREATED.value(), data = data)
    }
}
