package gongback.pureureum.security

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe

private const val KEY: String = "testKeytestKeytestKeytestKeytestKeytestKeytestKeytestKey"
private const val EXPIRED_TIME_MS: Long = 10000L
private const val REFRESH_EXPIRED_TIME_MS: Long = 100000L
private const val NEGATIVE_TIME_MS: Long = -10L
private const val NOT_VALID_TOKEN: String = ""

class JwtTokenProviderTest : StringSpec({
    val jwtProperties = JwtProperties(KEY, EXPIRED_TIME_MS, REFRESH_EXPIRED_TIME_MS)

    "엑세스 토큰을 생성하고, 올바른 payload가 반환되는지 확인한다" {
        val jwtTokenProvider = JwtTokenProvider(jwtProperties)
        val email: String = "testEmail1"

        val token = jwtTokenProvider.createToken(email)
        jwtTokenProvider.getSubject(token) shouldBe email
    }

    "리프레쉬 토큰을 생성하고, 올바른 payload가 반환되는지 확인한다" {
        val jwtTokenProvider = JwtTokenProvider(jwtProperties)
        val email: String = "testEmail1"

        val refreshToken = jwtTokenProvider.createRefreshToken(email)
        jwtTokenProvider.getSubject(refreshToken) shouldBe email
    }

    "유효 시간이 지난 토큰의 유효성 검사 시 false 리턴" {
        val jwtTokenProvider = JwtTokenProvider(JwtProperties(KEY, NEGATIVE_TIME_MS, REFRESH_EXPIRED_TIME_MS))
        val email: String = "testEmail1"

        val refreshToken = jwtTokenProvider.createToken(email)
        jwtTokenProvider.isValidToken(refreshToken) shouldBe false
    }

    "올바르지 않은 토큰의 유효성 검사 시 JwtNotValidException 발생" {
        val jwtTokenProvider = JwtTokenProvider(jwtProperties)

        jwtTokenProvider.isValidToken(NOT_VALID_TOKEN).shouldBeFalse()
    }
})
