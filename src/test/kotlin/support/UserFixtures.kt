package support

import gongback.pureureum.application.dto.KakaoUserInfoRes
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.application.dto.UserAccountDto
import gongback.pureureum.domain.user.Gender
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.Role
import gongback.pureureum.domain.user.SocialType
import gongback.pureureum.domain.user.User
import java.time.LocalDate

const val NAME: String = "회원"
const val EMAIL: String = "testEmail"
const val PHONE_NUMBER: String = "010-0000-0000"
val GENDER: Gender = Gender.MALE
val BIRTHDAY: LocalDate = createLocalDate(1998, 12, 28)
val PASSWORD: Password = Password("passwordTest")
val SOCIAL_TYPE_PUREUREUM: SocialType = SocialType.PUREUREUM

fun createUser(
    name: String = NAME,
    email: String = EMAIL,
    phoneNumber: String = PHONE_NUMBER,
    gender: Gender = GENDER,
    birthday: LocalDate = BIRTHDAY,
    password: Password = PASSWORD,
    id: Long = 0L
): User {
    return User(name, email, phoneNumber, email, gender, birthday, password, Role.ROLE_USER, SocialType.PUREUREUM, id)
}

fun createRegisterReq(
    email: String = EMAIL,
    password: Password = PASSWORD,
    name: String = NAME,
    gender: Gender = GENDER,
    phoneNumber: String = PHONE_NUMBER,
    birthday: LocalDate = BIRTHDAY
): RegisterUserReq {
    return RegisterUserReq(email, password, name, gender, phoneNumber, birthday)
}

fun createUserAccountDto(
    email: String = EMAIL,
    socialType: SocialType = SOCIAL_TYPE_PUREUREUM
): UserAccountDto {
    return UserAccountDto(email, socialType)
}

fun createKakaoUserInfo(
    name: String = NAME,
    birthyear: String = "1998",
    birthday: String = "1228",
    phoneNumber: String = PHONE_NUMBER,
    gender: String = "male",
    socialType: SocialType = SocialType.KAKAO
): OAuthUserInfo {
    return KakaoUserInfoRes(
        "2129419241",
        KakaoUserInfoRes.KakaoAccount(name, "kakao_2129419241", birthyear, birthday, gender, phoneNumber)
    )
}
