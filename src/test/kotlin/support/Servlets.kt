package support

import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockHttpServletRequestDsl

fun MockHttpServletRequestDsl.token(token: String) {
    header(HttpHeaders.AUTHORIZATION, bearerToken(token))
}

private fun bearerToken(token: String): String = "Bearer $token"
