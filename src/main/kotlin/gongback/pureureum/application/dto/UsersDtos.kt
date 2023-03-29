package gongback.pureureum.application.dto

import gongback.pureureum.domain.file.Profile
import gongback.pureureum.domain.user.Gender
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.Role
import gongback.pureureum.domain.user.SocialType
import gongback.pureureum.domain.user.User
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

data class LoginReq(
    @field:Length(min = 8, max = 15)
    val email: String,

    val password: Password
)

data class RegisterUserReq(
    @field:Length(min = 8)
    val email: String,

    val password: Password,

    @field:Length(max = 20)
    val name: String,

    val gender: Gender,
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String,

    @field:Past
    val birthday: LocalDate
) {
    fun toEntity(profile: Profile): User {
        return User(
            email = email,
            phoneNumber = phoneNumber,
            name = name,
            nickname = email,
            gender = gender,
            birthday = birthday,
            password = password,
            role = Role.ROLE_USER,
            socialType = SocialType.PUREUREUM,
            profile = profile
        )
    }
}

data class UserInfoReq(
    val password: Password?,

    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String?,

    @field:Length(min = 2, max = 30, message = "닉네임은 2~30글자여야 합니다")
    val nickname: String?
)

data class UserInfoRes(
    val email: String,
    val phoneNumber: String,
    val name: String,
    val nickname: String,
    val gender: Gender,
    val birthday: LocalDate,
    val profileId: Long
)

data class EmailReq(
    @field:Length(min = 8, max = 15, message = "올바른 형식의 아이디여야 합니다.")
    val email: String
)

data class UserAccountDto(
    val email: String,
    val socialType: SocialType
)
