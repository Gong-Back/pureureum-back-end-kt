package gongback.pureureum.security

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
class JwtProperties(
    val key: String = "defaultdefaultdefaultdefaultdefault",
    val expiredTimeMs: Long = 0L,
    val refreshExpiredTimeMs: Long = 0L
)
