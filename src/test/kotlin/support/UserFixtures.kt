package support

import gongback.pureureum.application.dto.KakaoUserInfoRes
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.application.dto.UserAccountDto
import gongback.pureureum.application.dto.UserInfoReq
import gongback.pureureum.application.dto.UserInfoRes
import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserGender
import gongback.pureureum.domain.user.UserRole
import java.time.LocalDate

const val NAME: String = "회원"
const val EMAIL: String = "testEmail"
const val PHONE_NUMBER: String = "010-0000-0000"
val UserGENDER: UserGender = UserGender.MALE
val BIRTHDAY: LocalDate = createLocalDate(1998, 12, 28)
val PASSWORD: Password = Password("passwordTest")
val SOCIAL_TYPE_PUREUREUM: SocialType = SocialType.PUREUREUM

fun createUser(
    name: String = NAME,
    email: String = EMAIL,
    phoneNumber: String = PHONE_NUMBER,
    userGender: UserGender = UserGENDER,
    birthday: LocalDate = BIRTHDAY,
    password: Password = PASSWORD,
): User {
    return User(
        email, phoneNumber, name, email, userGender, birthday, password, UserRole.ROLE_USER,
        SocialType.PUREUREUM
    )
}

fun createRegisterReq(
    email: String = EMAIL,
    password: Password = PASSWORD,
    name: String = NAME,
    userGender: UserGender = UserGENDER,
    phoneNumber: String = PHONE_NUMBER,
    birthday: LocalDate = BIRTHDAY
): RegisterUserReq {
    return RegisterUserReq(email, password, name, userGender, phoneNumber, birthday)
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
    gender: String = "male"
): OAuthUserInfo {
    return KakaoUserInfoRes(
        "2129419241",
        KakaoUserInfoRes.KakaoAccount(name, "kakao_2129419241", birthyear, birthday, gender, phoneNumber)
    )
}

fun createUserInfoReq(
    password: Password = PASSWORD,
    phoneNumber: String = PHONE_NUMBER,
    nickname: String = EMAIL
): UserInfoReq {
    return UserInfoReq(password, phoneNumber, nickname)
}
fun createUserInfoRes(
    user: User
): UserInfoRes {
    return UserInfoRes(
        user.email,
        user.phoneNumber,
        user.name,
        user.nickname,
        user.userGender,
        user.birthday,
        "signedProfileUrl"
    )
}
