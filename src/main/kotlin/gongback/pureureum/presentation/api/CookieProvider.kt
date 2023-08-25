package gongback.pureureum.presentation.api

import gongback.pureureum.application.dto.TokenRes
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie

private const val ACCESS_TOKEN_COOKIE_KEY = "accessToken"
private const val REFRESH_TOKEN_COOKIE_KEY = "refreshToken"
private const val NONE = "None"

class CookieProvider {

    companion object {
        fun addTokenToSecureCookie(tokenRes: TokenRes, response: HttpServletResponse) {
            val accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_KEY, tokenRes.accessToken)
                .sameSite(NONE)
                .httpOnly(true)
                .secure(true)
                .build()
                .toString()
            response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie)

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
