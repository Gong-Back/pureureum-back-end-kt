package gongback.pureureum.application

import gongback.pureureum.application.dto.LoginReq
import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByEmail
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.domain.user.getUserByEmail
import gongback.pureureum.security.JwtTokenProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserAuthenticationService(
    private val userRepository: UserRepository,
    private val smsLogService: SmsLogService,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun validateAuthentication(loginReq: LoginReq) {
        val findUser = userRepository.getUserByEmail(loginReq.email)
        findUser.authenticate(loginReq.password)
    }

    fun generateAccessTokenByEmail(email: String) = jwtTokenProvider.createToken(email)

    fun generateRefreshTokenByEmail(email: String) = jwtTokenProvider.createRefreshToken(email)

    fun generateTokenByRefreshToken(refreshToken: String): String {
        val userEmail = jwtTokenProvider.getSubject(refreshToken)
        return jwtTokenProvider.createToken(userEmail)
    }

    @Transactional
    fun register(registerUserReq: RegisterUserReq) {
        checkDuplicatedUser(registerUserReq.email, registerUserReq.phoneNumber)
        existsSuccessPhoneNumber(registerUserReq.phoneNumber)
        userRepository.save(registerUserReq.toEntity())
    }

    fun checkDuplicatedUser(email: String, phoneNumber: String) {
        checkDuplicatedEmail(email)
        checkDuplicatedPhoneNumber(phoneNumber)
    }

    fun checkDuplicatedEmail(email: String) {
        require(!userRepository.existsByEmail(email)) { "이미 가입된 이메일입니다." }
    }

    fun checkDuplicatedPhoneNumber(phoneNumber: String) {
        require(!userRepository.existsByPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다." }
    }

    private fun existsSuccessPhoneNumber(phoneNumber: String) {
        require(smsLogService.isCertificated(phoneNumber)) { "본인 인증되지 않은 정보입니다." }
    }
}
