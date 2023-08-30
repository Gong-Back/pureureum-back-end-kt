package gongback.pureureum.presentation.api

import gongback.pureureum.application.dto.TokenRes
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie

private const val REFRESH_TOKEN_COOKIE_KEY = "refreshToken"
private const val NONE = "None"

class CookieProvider {

    companion object {
        fun addRefreshTokenToCookie(tokenRes: TokenRes, response: HttpServletResponse) {
            val refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_KEY, tokenRes.refreshToken)
                .sameSite(NONE)
                .httpOnly(true)
                .secure(true)
                .build()
                .toString()
            response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie)
        }
    }
}
