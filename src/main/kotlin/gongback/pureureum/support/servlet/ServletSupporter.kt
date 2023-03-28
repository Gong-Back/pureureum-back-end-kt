package gongback.pureureum.support.servlet

import jakarta.servlet.http.HttpServletRequest

class ServletSupporter {
    companion object {
        fun getRequestIp(request: HttpServletRequest): String {
            val headers = listOf(
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
            )
            for (header in headers) {
                val ip = request.getHeader(header)
                if (ip.isNotBlank()) {
                    return ip
                }
            }
            return request.remoteAddr
        }
    }
}
