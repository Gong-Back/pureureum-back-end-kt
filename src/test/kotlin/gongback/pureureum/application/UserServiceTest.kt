package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
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
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.EMAIL
import support.createMockProfileFile
import support.createProfile
import support.createUser
import support.createUserInfoReq

class UserServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val smsLogRepository = mockk<SmsLogRepository>()
    val uploadService = mockk<UploadService>()
    val userService = UserService(uploadService, userRepository, smsLogRepository)

    Given("회원 이메일") {
        val user = createUser()
        val email = user.email
        When("이메일의 회원이 있을 경우") {
            every { userRepository.getUserByEmail(email) } returns user

            Then("회원 정보를 반환한다.") {
                userService.getUserByEmail(email).email shouldBe email
            }
        }

        When("이메일의 회원이 없을 경우") {
            every { userRepository.getUserByEmail(email) } throws IllegalArgumentException()

            Then("성공한다.") {
                shouldThrow<IllegalArgumentException> { userService.getUserByEmail(email) }
            }
        }
    }

    Given("사용자와 사용자 정보") {
        val user = createUser()
        val userInfoReq = createUserInfoReq()

        When("이미 존재하는 핸드폰 정보라면") {
            every { userRepository.getReferenceById(any()) } returns user
            every {
                userRepository.existsByPhoneNumber(any())
            } throws IllegalArgumentException("이미 가입된 전화번호입니다")
            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }
        When("인증되지 않은 핸드폰 정보라면") {
            every { userRepository.getReferenceById(any()) } returns user
            every {
                smsLogRepository.getLastSmsLog(any())
            } throws IllegalArgumentException("본인 인증되지 않은 정보입니다")
            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }
        When("이미 존재하는 닉네임이라면") {
            every { userRepository.getReferenceById(any()) } returns user
            every { userRepository.existsNickname(any()) } returns true
            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { userService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }
        When("존재하지 않으면서 인증된 핸드폰 정보이거나, 올바른 비밀번호이거나, 올바른 닉네임이라면") {
            every { userRepository.existsByPhoneNumber(any()) } returns false
            every { smsLogRepository.getLastSmsLog(any()).isSuccess } returns true
            every { smsLogRepository.deleteByReceiver(any()) } just runs
            every { userRepository.existsNickname(any()) } returns false
            every { userRepository.getUserByEmail(any()) } returns user
            Then("사용자 정보를 업데이트한다") {
                shouldNotThrowAnyUnit { userService.updateUserInfo(EMAIL, userInfoReq) }
            }
        }
    }

    Given("사용자와 프로필 이미지 정보") {
        val user = createUser()
        val profile = createProfile()
        val fileDto = FileDto(
            profile.fileKey,
            profile.contentType,
            profile.originalFileName
        )

        When("원본 파일 이름이 존재하지 않는다면") {
            val file = createMockProfileFile(originalFileName = null)
            every { uploadService.validateFileName(file) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { userService.updatedProfile(user.email, file) }
            }
        }

        When("원본 파일 이름이 비어있다면") {
            val file = createMockProfileFile(originalFileName = "")
            every { uploadService.validateFileName(file) } throws IllegalArgumentException("원본 파일 이름이 비어있습니다")
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { userService.updatedProfile(user.email, file) }
            }
        }

        When("파일 형식이 비어있다면") {
            val file = createMockProfileFile(contentType = null)
            every { uploadService.validateFileName(file) } returns file.name
            every { uploadService.validateContentType(file) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { userService.updatedProfile(user.email, file) }
            }
        }

        When("이미지 형식의 파일이 아니라면") {
            val file = createMockProfileFile(contentType = "text/html")
            every { uploadService.validateFileName(file) } returns file.name
            every { uploadService.validateContentType(file) } throws IllegalArgumentException("이미지 형식의 파일만 가능합니다")

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { userService.updatedProfile(user.email, file) }
            }
        }

        When("사용자의 기존 프로필 이미지가 별도로 설정한 파일이라면") {
            val file = createMockProfileFile()
            every { userRepository.getUserByEmail(any()) } returns user
            every { uploadService.validateFileName(file) } returns fileDto.fileKey
            every { uploadService.validateContentType(file) } returns file.contentType!!
            every { uploadService.deleteFile(any()) } just runs
            every { uploadService.uploadFile(any(), any(), any()) } returns fileDto.fileKey
            every { userRepository.save(any()) } returns user

            Then("기존의 파일을 제거한 후 정보를 업데이트한다.") {
                shouldNotThrowAnyUnit { userService.updatedProfile(EMAIL, file) }
            }
        }
        When("사용자가 프로필을 설정하지 않았을 경우") {
            Then("아무 작업도 하지 않는다.") {
                shouldNotThrowAnyUnit { userService.updatedProfile(EMAIL, null) }
            }
        }
    }
})
