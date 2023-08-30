package gongback.pureureum.application.dto

data class TokenRes(
    val accessToken: String,
    val refreshToken: String
)

data class AccessTokenRes(
    val accessToken: String
)
