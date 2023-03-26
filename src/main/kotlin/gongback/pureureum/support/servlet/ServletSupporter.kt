package gongback.pureureum.support.servlet

import jakarta.servlet.http.HttpServletRequest

class ServletSupporter {
    companion object {
        fun getRequestIp(request: HttpServletRequest): String {
            var ip = request.getHeader("X-Forwarded-For")
            if (ip == null) {
                ip = request.getHeader("Proxy-Client-IP")
            }
            if (ip == null) {
                ip = request.getHeader("WL-Proxy-Client-IP")
            }
            if (ip == null) {
                ip = request.getHeader("HTTP_CLIENT_IP")
            }
            if (ip == null) {
                ip = request.getHeader("HTTP_X_FORWARDED_FOR")
            }
            if (ip == null) {
                ip = request.remoteAddr
            }
            return ip
        }
    }
}
