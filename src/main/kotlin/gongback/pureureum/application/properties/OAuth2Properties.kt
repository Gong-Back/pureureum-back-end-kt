package gongback.pureureum.application.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.kakao")
class KakaoProperties(
    val apiKey: String = "",
    val secretKey: String = "",
    val kakaoTokenUrl: String = "",
    val kakaoUserInfoUrl: String = ""
)

@ConfigurationProperties(prefix = "oauth2.google")
class GoogleProperties(
    val apiKey: String = "",
    val secretKey: String = "",
    val googleTokenUrl: String = "",
    val googleUserInfoUrl: String = ""
)

@ConfigurationProperties(prefix = "oauth2.naver")
class NaverProperties(
    val state: String = "",
    val apiKey: String = "",
    val secretKey: String = "",
    val naverTokenUrl: String = "",
    val naverUserInfoUrl: String = ""
)
