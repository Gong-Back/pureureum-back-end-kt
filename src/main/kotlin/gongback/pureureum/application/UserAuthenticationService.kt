package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.LoginReq
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.application.dto.SocialRegisterUserReq
import gongback.pureureum.application.dto.TempSocialAuthDto
import gongback.pureureum.application.dto.TokenRes
import gongback.pureureum.application.dto.UserAccountDto
import gongback.pureureum.domain.sms.SmsLog
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import gongback.pureureum.domain.social.TempSocialAuthRepository
import gongback.pureureum.domain.social.getTempByEmail
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.domain.user.existsEmail
import gongback.pureureum.domain.user.existsEmailOrNickname
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.domain.user.getUserByPhoneNumber
import gongback.pureureum.security.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserAuthenticationService(
    private val userRepository: UserRepository,
    private val tempSocialAuthRepository: TempSocialAuthRepository,
    private val smsLogRepository: SmsLogRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun validateAuthentication(loginReq: LoginReq) {
        val findUser = userRepository.getUserByEmail(loginReq.email)
        findUser.authenticate(loginReq.password)
    }

    fun generateAccessTokenByEmail(email: String) = jwtTokenProvider.createToken(email)

    fun generateRefreshTokenByEmail(email: String) = jwtTokenProvider.createRefreshToken(email)

    fun reissueToken(bearerToken: String): TokenRes {
        val token = jwtTokenProvider.extractToken(bearerToken)
        val email = jwtTokenProvider.getSubject(token)
        return TokenRes(jwtTokenProvider.createToken(email), jwtTokenProvider.createRefreshToken(email))
    }

    @Transactional
    fun register(registerUserReq: RegisterUserReq) {
        checkDuplicatedUser(registerUserReq.email, registerUserReq.phoneNumber)
        validateCertifiedPhoneNumber(registerUserReq.phoneNumber)
        userRepository.save(registerUserReq.toEntity())
    }

    @Transactional
    fun registerBySocialInfo(oAuthUserInfo: OAuthUserInfo) {
        userRepository.save(oAuthUserInfo.toUser())
    }

    @Transactional
    fun registerBySocialReq(socialRegisterUserReq: SocialRegisterUserReq) {
        validateCertifiedPhoneNumber(socialRegisterUserReq.phoneNumber)
        userRepository.save(socialRegisterUserReq.toUser())
        tempSocialAuthRepository.deleteByEmail(socialRegisterUserReq.email)
    }

    fun checkDuplicatedUser(email: String, phoneNumber: String) {
        checkDuplicatedEmailOrNickname(email)
        checkDuplicatedPhoneNumber(phoneNumber)
    }

    fun getUserAccountDto(phoneNumber: String): UserAccountDto {
        val findUser = userRepository.getUserByPhoneNumber(phoneNumber)
        return UserAccountDto(findUser.email, findUser.socialType)
    }

    fun checkDuplicatedEmailOrNickname(email: String) {
        require(!userRepository.existsEmailOrNickname(email)) { "이미 가입된 사용자입니다" }
    }

    fun checkDuplicatedPhoneNumber(phoneNumber: String) {
        require(!existsPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다" }
    }

    @Transactional
    fun saveTempSocialInfo(oAuthUserInfo: OAuthUserInfo) {
        if (oAuthUserInfo.phoneNumber.isNotBlank()) {
            smsLogRepository.save(SmsLog(oAuthUserInfo.phoneNumber))
        }
        tempSocialAuthRepository.save(oAuthUserInfo.toTempSocialAuth())
    }

    fun getTempSocialAuth(email: String): TempSocialAuthDto {
        val tempSocialAuth = tempSocialAuthRepository.getTempByEmail(email)
        return TempSocialAuthDto.fromTempSocialAuth(tempSocialAuth)
    }

    fun socialLogin(oAuthUserInfo: OAuthUserInfo): ErrorCode {
        if (existsUserByEmail(oAuthUserInfo.clientEmail)) {
            return ErrorCode.OK
        }

        if (oAuthUserInfo.phoneNumber.isNotEmpty() && existsPhoneNumber(oAuthUserInfo.phoneNumber)) {
            return ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS
        }

        if (oAuthUserInfo.isValid()) {
            registerBySocialInfo(oAuthUserInfo)
            return ErrorCode.OK
        }

        if (!tempSocialAuthRepository.existsByEmail(oAuthUserInfo.clientEmail)) {
            saveTempSocialInfo(oAuthUserInfo)
        }
        return ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH
    }

    fun validateCertifiedPhoneNumber(phoneNumber: String) {
        require(smsLogRepository.getLastSmsLog(phoneNumber).isSuccess) { "본인 인증되지 않은 정보입니다" }
    }

    fun deleteByPhoneNumber(phoneNumber: String) {
        smsLogRepository.deleteByReceiver(phoneNumber)
    }

    private fun existsPhoneNumber(phoneNumber: String) = userRepository.existsByPhoneNumber(phoneNumber)

    private fun existsUserByEmail(email: String) = userRepository.existsEmail(email)
}
