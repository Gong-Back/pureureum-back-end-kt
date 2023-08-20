package gongback.pureureum.application.dto

import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserGender
import gongback.pureureum.domain.user.UserRole
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import org.hibernate.validator.constraints.Length

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

    val gender: UserGender,

    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String,

    @field:Past
    val birthday: LocalDate
) {
    fun toEntity(): User {
        return User(
            email = email,
            phoneNumber = phoneNumber,
            name = name,
            nickname = email,
            gender = gender,
            birthday = birthday,
            password = password,
            userRole = UserRole.ROLE_USER,
            socialType = SocialType.PUREUREUM
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
    val gender: UserGender,
    val birthday: LocalDate,
    val profileUrl: String
) {
    companion object {
        fun toUserWithProfileUrl(user: User, profileUrl: String): UserInfoRes {
            return UserInfoRes(
                user.email,
                user.phoneNumber,
                user.name,
                user.nickname,
                user.userGender,
                user.birthday,
                profileUrl = profileUrl
            )
        }
    }
}

data class EmailReq(
    @field:Length(min = 8, max = 15, message = "올바른 형식의 아이디여야 합니다.")
    val email: String
)

data class UserAccountDto(
    val email: String,
    val socialType: SocialType
)
