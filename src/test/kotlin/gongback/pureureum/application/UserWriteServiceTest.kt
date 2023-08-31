package gongback.pureureum.application

import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.existsByPhoneNumber
import gongback.pureureum.domain.user.existsNickname
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import support.EMAIL
import support.PROFILE_CONTENT_TYPE
import support.PROFILE_KEY
import support.PROFILE_ORIGINAL_FILE_NAME
import support.createMockProfileFile
import support.createProfile
import support.createProfileDto
import support.createUser
import support.createUserInfoReq

class UserWriteServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val userRepository = mockk<UserRepository>()
    val smsLogRepository = mockk<SmsLogRepository>()
    val userWriteService = UserWriteService(fileService, userRepository, smsLogRepository)

    Given("사용자 이메일과 사용자 정보") {
        val user = createUser()
        val userInfoReq = createUserInfoReq()

        When("이미 존재하는 핸드폰 정보라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { userRepository.existsByPhoneNumber(any()) } throws IllegalArgumentException("이미 가입된 전화번호입니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userWriteService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }

        When("인증되지 않은 핸드폰 정보라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { smsLogRepository.getLastSmsLog(any()) } throws IllegalArgumentException("본인 인증되지 않은 정보입니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userWriteService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }

        When("이미 존재하는 닉네임이라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { userRepository.existsNickname(any()) } returns true

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userWriteService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }

        When("존재하지 않으면서 인증된 핸드폰 정보이거나, 올바른 비밀번호이거나, 올바른 닉네임이라면") {
            every { userRepository.existsByPhoneNumber(any()) } returns false
            every { smsLogRepository.getLastSmsLog(any()).isSuccess } returns true
            every { smsLogRepository.deleteByReceiver(any()) } just runs
            every { userRepository.existsNickname(any()) } returns false
            every { userRepository.getUserByEmail(any()) } returns user

            Then("사용자 정보를 업데이트한다") {
                shouldNotThrowAnyUnit { userWriteService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }
    }

    Given("사용자 이메일과 프로필 이미지 파일") {
        val user = createUser()
        val profileDto = createProfileDto()
        val profileImage = createMockProfileFile()

        val contentType = PROFILE_CONTENT_TYPE
        val originalFileName = PROFILE_ORIGINAL_FILE_NAME
        val profileKey = PROFILE_KEY

        When("기존의 사용자 프로필 이미지가 기본 이미지라면") {
            clearMocks(fileService)

            every { fileService.validateImageType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { userRepository.getUserByEmail(any()) } returns user
            every { fileService.uploadFile(any(), any()) } returns profileKey

            Then("S3에 저장된 기존 이미지를 제거하지 않고, 이미지 업로드 후 프로필 엔티티를 생성한다") {
                verify(exactly = 0) {
                    fileService.deleteFile(any())
                }
                userWriteService.uploadProfileImage(user.email, profileImage) shouldBe profileDto
            }
        }

        When("기존의 사용자 프로필 이미지가 기본 이미지가 아니라면") {
            clearMocks(fileService)
            val newUser = createUser()
            val customProfile = createProfile(originalFileName = "new-profile.png")
            newUser.updateProfile(customProfile)

            every { fileService.validateImageType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { userRepository.getUserByEmail(any()) } returns newUser
            every { fileService.deleteFile(any()) } just runs
            every { fileService.uploadFile(any(), any()) } returns profileKey

            Then("S3에 저장된 기존 이미지를 제거하고, 이미지 업로드 후 프로필 엔티티를 생성한다") {
                userWriteService.uploadProfileImage(user.email, profileImage) shouldBe profileDto
                verify(exactly = 1) {
                    fileService.deleteFile(any())
                }
            }
        }
    }

    Given("사용자 이메일, 프로필 엔티티") {
        val user = createUser()
        val profileDto = createProfileDto()
        val newProfile = profileDto.toEntity()

        When("올바른 사용자 정보와 프로필 엔티티가 들어왔다면") {
            every { userRepository.getUserByEmail(any()) } returns user

            Then("사용자의 프로필 이미지 정보를 업데이트한다") {
                shouldNotThrowAnyUnit { userWriteService.updateProfile(user.email, profileDto) }
                user.profile shouldBe newProfile
            }
        }
    }
})
