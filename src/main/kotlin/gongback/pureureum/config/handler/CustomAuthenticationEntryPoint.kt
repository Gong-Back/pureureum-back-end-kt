package gongback.pureureum.config.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gongback.pureureum.application.dto.ErrorCode.UNAUTHORIZED
import gongback.pureureum.presentation.api.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(
    private val logger: KLogger = KotlinLogging.logger {}
) : AuthenticationEntryPoint {
    override fun commence(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        authException: AuthenticationException?
    ) {
        logger.error { "[AuthenticationEntryPoint] ${request?.requestURI}" }

        val code = UNAUTHORIZED.httpStatus.value()
        response?.status = code
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(response?.outputStream, ApiResponse.error(code, UNAUTHORIZED.message))
    }
}
