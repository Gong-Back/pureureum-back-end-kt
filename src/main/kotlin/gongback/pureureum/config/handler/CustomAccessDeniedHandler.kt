package gongback.pureureum.config.handler

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import gongback.pureureum.api.dto.ApiResponse
import gongback.pureureum.api.dto.ErrorCode.FORBIDDEN
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogger
import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(
    private val logger: KLogger = KotlinLogging.logger {}
) : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        accessDeniedException: AccessDeniedException?
    ) {
        logger.error { "[AccessDeniedHandler] = ${request?.requestURI}" }

        response?.status = FORBIDDEN.httpStatus.value()
        response?.contentType = MediaType.APPLICATION_JSON_VALUE
        jacksonObjectMapper().writeValue(response?.outputStream, ApiResponse.error(FORBIDDEN.message))
    }
}
