package gongback.pureureum.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest
import java.util.*
import javax.crypto.SecretKey

private const val BEARER = "Bearer"
private const val REFRESH_TOKEN = "RefreshToken"

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    val key: SecretKey = Keys.hmacShaKeyFor(jwtProperties.key.toByteArray())

    fun createToken(payload: String): String {
        val claims: Claims = Jwts.claims().setSubject(payload)
        val now = Date()
        val expiration = Date(now.time + jwtProperties.expiredTimeMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key)
            .compact()
    }

    fun createRefreshToken(payload: String): String {
        val claims: Claims = Jwts.claims().setSubject(payload)
        val now = Date()
        val expiration = Date(now.time + jwtProperties.refreshExpiredTimeMs)

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(expiration)
            .signWith(key)
            .compact()
    }

    fun isValidToken(token: String): Boolean = try {
        getClaimsJws(token)
        true
    } catch (e: ExpiredJwtException) {
        throw JwtExpiredException()
    } catch (e: JwtException) {
        false
    } catch (e: IllegalArgumentException) {
        false
    }

    fun getSubject(token: String): String = getClaimsJws(token)
        .body
        .subject

    private fun getClaimsJws(token: String) = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)

    fun extractAccessToken(request: WebRequest): String {
        val accessToken = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw JwtNotExistsException()
        return extractToken(accessToken)
    }

    fun extractRefreshToken(request: WebRequest): String {
        val refreshToken = request.getHeader(REFRESH_TOKEN) ?: throw JwtNotExistsException()
        return extractToken(refreshToken)
    }

    private fun extractToken(authentication: String): String {
        val (tokenType, token) = splitToTokenFormat(authentication)
        if (tokenType != BEARER || !isValidToken(token)) {
            throw JwtNotValidException()
        }
        return token
    }

    private fun splitToTokenFormat(authorization: String): Pair<String, String> = try {
        val tokenFormat = authorization.split(" ")
        tokenFormat[0] to tokenFormat[1]
    } catch (e: IndexOutOfBoundsException) {
        throw JwtNotValidException()
    }
}
