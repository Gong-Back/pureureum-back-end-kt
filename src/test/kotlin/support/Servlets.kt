package support

import jakarta.servlet.http.Cookie
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockHttpServletRequestDsl

fun MockHttpServletRequestDsl.accessToken(token: String) {
    header(HttpHeaders.AUTHORIZATION, bearerToken(token))
}

fun MockHttpServletRequestDsl.refreshToken(token: String) {
    val refreshTokenCookie = Cookie(REFRESH_COOKIE_NAME, token)
    refreshTokenCookie.isHttpOnly = true
    cookie(refreshTokenCookie)
}

private fun bearerToken(token: String): String = "Bearer $token"
