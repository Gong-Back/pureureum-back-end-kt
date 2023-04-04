package gongback.pureureum.application.dto

import com.fasterxml.jackson.annotation.JsonProperty
import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.domain.social.TempSocialAuth
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.Profile
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserGender
import gongback.pureureum.domain.user.UserRole
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Length
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AuthenticationInfo(
    @field:NotEmpty
    val code: String,
    @field:NotEmpty
    val redirectUrl: String
)

data class OAuthToken(
    @JsonProperty("token_type")
    val tokenType: String = "",
    @JsonProperty("access_token")
    val accessToken: String = ""
)

open class OAuthUserInfo {
    open val name: String = ""
    open val clientEmail: String = ""
    open val birthday: String = ""
    open val phoneNumber: String = ""
    open val gender: UserGender? = null
    open val socialType: SocialType? = null
    open val profile: Profile? = null

    fun isValid(): Boolean =
        name.isNotBlank() && clientEmail.isNotBlank() && birthday.isNotBlank() && phoneNumber.isNotBlank() && gender != null

    fun toUser(): User {
        require(isValid()) { "정보가 올바르지 않습니다" }
        return User(
            email = clientEmail,
            phoneNumber = phoneNumber,
            name = name,
            nickname = clientEmail,
            gender = gender!!,
            birthday = LocalDate.parse(birthday, DateTimeFormatter.ISO_DATE),
            password = Password(clientEmail),
            userRole = UserRole.ROLE_USER,
            socialType = socialType!!
        )
    }

    fun toTempSocialAuth(): TempSocialAuth {
        return TempSocialAuth(
            name = name,
            email = clientEmail,
            birthday = birthday,
            phoneNumber = phoneNumber,
            gender = gender,
            socialType = socialType
        )
    }
}

data class KakaoUserInfoRes(
    val id: String = "",
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount = KakaoAccount()
) : OAuthUserInfo() {
    data class KakaoAccount(
        val name: String = "",
        val email: String = "",
        val birthyear: String = "",
        val birthday: String = "",
        val gender: String = "",
        @JsonProperty("phone_number")
        val phoneNumber: String = ""
    )

    override val name: String = kakaoAccount.name
    override val clientEmail: String = "kakao_$id"
    override val birthday: String = birthdayFormat()
    override val phoneNumber: String = kakaoAccount.phoneNumber
    override val gender: UserGender? = genderFormat()
    override val socialType: SocialType = SocialType.KAKAO

    fun birthdayFormat(): String {
        if (kakaoAccount.birthyear.isEmpty() || kakaoAccount.birthday.isEmpty()) {
            return ""
        }

        val stringBuilder = StringBuilder()
        stringBuilder.append(kakaoAccount.birthyear).append("-").append(kakaoAccount.birthday)
        stringBuilder.insert(7, "-")
        return stringBuilder.toString()
    }

    fun genderFormat(): UserGender? =
        if (kakaoAccount.gender.isEmpty()) null else UserGender.valueOf(kakaoAccount.gender.uppercase())
}

data class NaverUserInfoRes(
    val response: Response = Response()
) : OAuthUserInfo() {
    data class Response(
        val id: String = "",
        val name: String = "",
        val email: String = "",
        val birthyear: String = "",
        val birthday: String = "",
        val gender: String = "",
        val mobile: String = ""
    )

    override val name: String = response.name
    override val clientEmail: String = "naver_${response.email.substring(0, response.email.indexOf("@"))}"
    override val birthday: String = birthdayFormat()
    override val phoneNumber: String = response.mobile
    override val gender: UserGender? = genderFormat()
    override val socialType: SocialType = SocialType.NAVER

    fun birthdayFormat(): String {
        if (response.birthyear.isEmpty() || response.birthday.isEmpty()) {
            return ""
        }

        return StringBuilder().append(response.birthyear).append("-").append(response.birthday).toString()
    }

    fun genderFormat(): UserGender? =
        if (response.gender.isEmpty()) null else if (response.gender == "F") UserGender.FEMALE else UserGender.MALE
}

data class GoogleUserInfoRes(
    val id: String = "",
    override val name: String = "",
    val email: String = ""
) : OAuthUserInfo() {
    override val clientEmail: String = "google_${email.substring(0, email.indexOf("@"))}"
    override val socialType: SocialType = SocialType.GOOGLE
}

data class SocialRegisterUserReq(
    @field:Length(min = 8)
    val email: String,
    @field:Length(max = 20)
    val name: String,
    @field:Past
    val birthday: LocalDate,
    @field:Pattern(regexp = "010-\\d{4}-\\d{4}", message = "올바른 형식의 전화번호여야 합니다")
    val phoneNumber: String,
    val gender: UserGender,
    val socialType: SocialType
) {
    fun toUser(): User {
        return User(
            email = email,
            phoneNumber = phoneNumber,
            name = name,
            nickname = email,
            gender = gender,
            birthday = birthday,
            password = Password(email),
            userRole = UserRole.ROLE_USER,
            socialType = socialType
        )
    }
}

data class SocialEmailDto(
    val email: String
)

data class TempSocialAuthDto(
    val email: String,
    val name: String? = null,
    val birthday: String? = null,
    val phoneNumber: String? = null,
    val gender: UserGender? = null,
    val socialType: SocialType? = null
)
