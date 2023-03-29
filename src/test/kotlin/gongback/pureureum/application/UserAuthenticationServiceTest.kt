package gongback.pureureum.application

import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.domain.file.ProfileRepository
import gongback.pureureum.domain.user.TempSocialAuthRepository
import gongback.pureureum.domain.user.User
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.domain.user.existsEmail
import gongback.pureureum.domain.user.existsEmailOrNickname
import gongback.pureureum.security.JwtTokenProvider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import support.createKakaoUserInfo
import support.createProfile
import support.createRegisterReq
import support.createUser

class UserAuthenticationServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val smsLogService = mockk<SmsLogService>()
    val tempSocialAuthRepository = mockk<TempSocialAuthRepository>()
    val jwtTokenProvider = mockk<JwtTokenProvider>()
    val profileRepository = mockk<ProfileRepository>()
    val profileService = mockk<ProfileService>()

    val userAuthenticationService =
        UserAuthenticationService(
            userRepository,
            tempSocialAuthRepository,
            smsLogService,
            profileService,
            jwtTokenProvider
        )

    Given("회원가입 정보") {
        val registerReq = createRegisterReq()
        val profile = createProfile()

        When("이미 존재하는 이메일이거나 닉네임이라면") {
            every { userRepository.existsEmailOrNickname(registerReq.email) } returns true
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

        When("이미 존재하지 않은 이메일이거나 닉네임이면서, 인증하지 않은 전화번호일 경우") {
            every { userRepository.existsEmailOrNickname(registerReq.email) } returns false
            every { smsLogService.isCertificated(registerReq.phoneNumber) } returns false

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    userAuthenticationService.register(registerReq)
                }
            }
        }

        When("이미 존재하지 않은 이메일이거나 닉네임이면서, 전화번호이면서, 본인 인증한 전화번호인 경우") {
            every { userRepository.existsEmailOrNickname(registerReq.email) } returns false
            every { userRepository.existsByPhoneNumber(registerReq.phoneNumber) } returns false
            every { smsLogService.isCertificated(registerReq.phoneNumber) } returns true
            every { profileRepository.getReferenceById(any()) } returns profile
            every { profileService.get(any()) } returns profile
            every { userRepository.save(any()) } returns createUser()

            Then("성공한다.") {
                userAuthenticationService.register(registerReq)
            }
        }
    }

    Given("소셜 로그인 사용자 정보") {
        val oAuthUserInfo = createKakaoUserInfo()
        val oAuthUserInfoWithEmpty = createKakaoUserInfo("", "", "", "", "")
        When("이미 존재하는 이메일이거나 닉네임인 경우") {
            every { userRepository.existsEmail(any()) } returns true

            Then("ErrorCode OK 반환") {
                userAuthenticationService.socialLogin(oAuthUserInfo) shouldBe ErrorCode.OK
            }
        }

        When("이미 존재하는 전화번호인 경우") {
            every { userRepository.existsEmail(any()) } returns false
            every { userRepository.existsByPhoneNumber(any()) } returns true

            Then("ErrorCode REQUEST_RESOURCE_ALREADY_EXISTS 반환") {
                userAuthenticationService.socialLogin(oAuthUserInfo) shouldBe ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS
            }
        }

        When("모든 정보가 다 있는 경우") {
            every { userRepository.existsEmail(any()) } returns false
            every { userRepository.existsByPhoneNumber(any()) } returns false
            every { userRepository.save(any()) } returns mockk<User>()

            Then("ErrorCode OK 반환") {
                userAuthenticationService.socialLogin(oAuthUserInfo) shouldBe ErrorCode.OK
            }
        }

        When("필요한 정보가 부족한 경우") {
            every { userRepository.existsEmail(any()) } returns false
            every { userRepository.existsByPhoneNumber(any()) } returns false
            every { tempSocialAuthRepository.existsByEmail(any()) } returns true

            Then("ErrorCode OK 반환") {
                userAuthenticationService.socialLogin(oAuthUserInfoWithEmpty) shouldBe ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH
            }
        }
    }
})
