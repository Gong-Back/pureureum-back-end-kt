package gongback.pureureum.application.dto

import gongback.pureureum.domain.user.Gender
import gongback.pureureum.domain.user.Role
import gongback.pureureum.domain.user.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import java.time.LocalDate

data class LoginReq(
    @field:Email
    val email: String,
    val password: String
)

data class RegisterUserReq(
    @field:Length(min = 8, max = 15)
    val email: String,

    @field:Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9])(?=.*[!@#$%^&*])[a-zA-Z0-9!@#$%^&*]{8,15}$",
        message = "올바른 형식의 비밀번호여야 합니다"
    )
    val password: String,

    @field:Length(max = 20)
    val name: String,

    val gender: Gender,

    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String,

    @field:Past
    val birthday: LocalDate
) {
    fun toEntityByEncodedPassword(encodedPassword: String): User {
        return User(
            email = email,
            phoneNumber = phoneNumber,
            name = name,
            gender = gender,
            birthday = birthday,
            password = encodedPassword,
            role = Role.ROLE_USER
        )
    }
}

data class EmailReq(
    @field:Length(min = 8, max = 15, message = "올바른 형식의 아이디여야 합니다.")
    val email: String
)
