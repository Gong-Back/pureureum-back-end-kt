package support

import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockHttpServletRequestDsl

fun MockHttpServletRequestDsl.accessToken(token: String) {
    header(HttpHeaders.AUTHORIZATION, bearerToken(token))
}

fun MockHttpServletRequestDsl.refreshToken(token: String) {
    header(REFRESH_HEADER_NAME, bearerToken(token))
}

private fun bearerToken(token: String): String = "Bearer $token"
