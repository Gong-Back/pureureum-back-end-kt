package gongback.pureureum.presentation.api

import gongback.pureureum.application.dto.TokenRes
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletResponse

private const val ACCESS_TOKEN = "accessToken"
private const val REFRESH_TOKEN = "refreshToken"

class CookieProvider {

    companion object {
        fun addTokenToSecureCookie(tokenRes: TokenRes, response: HttpServletResponse) {
            val accessTokenCookie = Cookie(ACCESS_TOKEN, tokenRes.accessToken)
            accessTokenCookie.isHttpOnly = true
            response.addCookie(accessTokenCookie)

            val refreshTokenCookie = Cookie(REFRESH_TOKEN, tokenRes.refreshToken)
            refreshTokenCookie.isHttpOnly = true
            response.addCookie(refreshTokenCookie)
        }
    }
}
