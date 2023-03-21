package gongback.pureureum.application

import gongback.pureureum.application.dto.RegisterUserReq
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByEmail
import gongback.pureureum.domain.user.existsByPhoneNumber
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val smsLogService: SmsLogService,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {
    fun register(registerUserReq: RegisterUserReq) {
        checkDuplicatedUser(registerUserReq.email, registerUserReq.phoneNumber)
        existsSuccessPhoneNumber(registerUserReq.phoneNumber)
        userRepository.save(registerUserReq.toEntityByEncodedPassword(bCryptPasswordEncoder.encode(registerUserReq.password)))
    }

    fun checkDuplicatedUser(email: String, phoneNumber: String) {
        checkDuplicatedEmail(email)
        checkDuplicatedPhoneNumber(phoneNumber)
    }

    fun checkDuplicatedEmail(email: String) {
        check(!userRepository.existsByEmail(email)) { "이미 가입된 이메일입니다." }
    }

    fun checkDuplicatedPhoneNumber(phoneNumber: String) {
        check(!userRepository.existsByPhoneNumber(phoneNumber)) { "이미 가입된 전화번호입니다." }
    }

    private fun existsSuccessPhoneNumber(phoneNumber: String) {
        check(smsLogService.isCertification(phoneNumber)) { "본인 인증되지 않은 정보입니다." }
    }
}
