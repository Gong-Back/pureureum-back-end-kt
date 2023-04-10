package gongback.pureureum.security

import gongback.pureureum.support.security.Tokens
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KLogger
import mu.KotlinLogging
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.util.WebUtils

const val JWT_INVALID_CODE = 430
const val JWT_REISSUE_CODE = 431

data class JwtResponse(
    val code: Int
)

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
class JwtExceptionHandler(private val jwtTokenProvider: JwtTokenProvider) {
    private val logger: KLogger = KotlinLogging.logger {}

    @ExceptionHandler(JwtException::class)
    fun handleJwtException(
        ex: JwtException,
        httpServletRequest: HttpServletRequest,
        httpServletResponse: HttpServletResponse
    ): ResponseEntity<Any> {
        logger.error("[JwtException] ", ex)
        val cookie =
            WebUtils.getCookie(httpServletRequest, Tokens.REFRESH_TOKEN_HEADER) ?: return ResponseEntity.status(
                HttpStatus.UNAUTHORIZED
            ).body(JwtResponse(JWT_INVALID_CODE))

        val refreshToken = cookie.value

        if (!jwtTokenProvider.isValidToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(JwtResponse(JWT_INVALID_CODE))
        }

        val payload = jwtTokenProvider.getSubject(refreshToken)
        val refreshTokenCookie = Cookie(Tokens.REFRESH_TOKEN_HEADER, jwtTokenProvider.createRefreshToken(payload))
        refreshTokenCookie.isHttpOnly = true
        httpServletResponse.addCookie(refreshTokenCookie)

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .header(HttpHeaders.AUTHORIZATION, jwtTokenProvider.createToken(payload))
            .body(JwtResponse(JWT_REISSUE_CODE))
    }
}
