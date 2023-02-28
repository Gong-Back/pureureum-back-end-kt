package gongback.pureureum.api.dto

import jakarta.validation.constraints.Email

data class LoginReq(
    @field:Email
    val email: String,
    val password: String
)
