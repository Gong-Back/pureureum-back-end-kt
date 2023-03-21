package gongback.pureureum.application

import gongback.pureureum.application.dto.RegisterReq
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByEmail
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
    fun register(registerReq: RegisterReq) {
        checkDuplicatedEmail(registerReq.email)
        existsSuccessPhoneNumber(registerReq.phoneNumber)
        userRepository.save(registerReq.toEntityByEncodedPassword(bCryptPasswordEncoder.encode(registerReq.password)))
    }

    fun checkDuplicatedEmail(email: String) {
        check(!userRepository.existsByEmail(email)) { "이미 가입된 이메일입니다." }
    }

    private fun existsSuccessPhoneNumber(phoneNumber: String) {
        check(smsLogService.isCertification(phoneNumber)) { "본인 인증되지 않은 정보입니다." }
    }
}
