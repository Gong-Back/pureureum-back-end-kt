package gongback.pureureum.config.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gongback.pureureum.api.dto.ApiResponse
import gongback.pureureum.api.dto.ErrorCode.UNAUTHORIZED
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
        logger.error { "[AuthenticationEntryPoint] = ${request?.requestURI}" }

        response?.status = UNAUTHORIZED.httpStatus.value()
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(response?.outputStream, ApiResponse.error(UNAUTHORIZED.message))
    }
}
