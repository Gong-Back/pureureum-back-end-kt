package gongback.pureureum.application

import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByEmail
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.security.JwtTokenProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
import support.createRegisterReq
import support.createUser

class UserAuthenticationServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val smsLogService = mockk<SmsLogService>()
    val jwtTokenProvider = mockk<JwtTokenProvider>()

    val userAuthenticationService = UserAuthenticationService(userRepository, smsLogService, jwtTokenProvider)

    Given("회원가입 정보") {
        val registerReq = createRegisterReq()
        When("이미 존재하는 이메일이면") {
            every { userRepository.existsByEmail(registerReq.email) } returns true
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    userAuthenticationService.register(registerReq)
                }
            }
        }

        When("이미 존재하는 전화번호이면") {
            every { userRepository.existsByPhoneNumber(registerReq.phoneNumber) } returns true
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    userAuthenticationService.register(registerReq)
                }
            }
        }

        When("존재하지 않는 이메일이면서, 인증하지 않은 전화번호일 경우") {
            every { userRepository.existsByEmail(registerReq.email) } returns false
            every { smsLogService.isCertificated(registerReq.phoneNumber) } returns false

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    userAuthenticationService.register(registerReq)
                }
            }
        }

        When("존재하지 않는 이메일, 전화번호이면서, 본인 인증한 전화번호인 경우") {
            every { userRepository.existsByEmail(registerReq.email) } returns false
            every { userRepository.existsByPhoneNumber(registerReq.phoneNumber) } returns false
            every { smsLogService.isCertificated(registerReq.phoneNumber) } returns true
            every { userRepository.save(any()) } returns createUser()

            Then("성공한다.") {
                userAuthenticationService.register(registerReq)
            }
        }
    }
})
