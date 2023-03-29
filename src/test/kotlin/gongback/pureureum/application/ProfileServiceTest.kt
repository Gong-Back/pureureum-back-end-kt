package gongback.pureureum.application

import gongback.pureureum.application.dto.FileDto
import gongback.pureureum.domain.file.FileType
import gongback.pureureum.domain.file.ProfileRepository
import gongback.pureureum.domain.file.getFileKey
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.createMockFile
import support.createProfile

class ProfileServiceTest : BehaviorSpec({
    val uploadService = mockk<UploadService>()
    val profileRepository = mockk<ProfileRepository>()
    val profileService = ProfileService(uploadService, profileRepository)

    Given("파일과 파일 타입") {
        val profile = createProfile()
        val serverFileName = profile.serverFileName
        val fileKey = profile.fileKey
        val fileDto = FileDto(
            profile.fileKey,
            profile.contentType,
            profile.originalFileName,
            serverFileName
        )

        When("원본 파일 이름이 비어있다면") {
            val file = createMockFile(originalFileName = "")
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { profileService.uploadFile(file, FileType.PROFILE) }
            }
        }
        When("이미지 형식의 파일이 아니라면") {
            val file = createMockFile(contentType = "text/html")
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { profileService.uploadFile(file, FileType.PROFILE) }
            }
        }
        When("원본 파일 이름이 존재하면서 이미지 형식의 파일이라면") {
            val file = createMockFile()
            every { uploadService.createServerFileName(any()) } returns serverFileName
            every { uploadService.uploadFile(any(), any(), any()) } returns fileKey
            Then("파일 정보를 반환한다.") {
                profileService.uploadFile(file, FileType.PROFILE) shouldBe fileDto
            }
        }
    }

    Given("파일 아이디") {
        val profile = createProfile()
        val profileId = profile.id
        val fileKey = profile.fileKey
        When("프로필 이미지 아이디가 유효하지 않다면") {
            every { profileRepository.getFileKey(any()) } throws IllegalArgumentException("프로필 이미지 정보가 존재하지 않습니다")
            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> { profileService.getFileUrl(profileId) }
            }
        }
        When("프로필 이미지 아이디가 유효하다면") {
            every { profileRepository.getFileKey(any()) } returns fileKey
            every { uploadService.getFileUrl(any()) } returns fileKey
            every { profileRepository.getReferenceById(any()) } returns profile
            Then("프로필 이미지 URL을 반환한다.") {
                profileService.getFileUrl(profileId) shouldBe fileKey
            }
            Then("프로필 이미지 정보를 반환한다.") {
                profileService.get(profileId) shouldBe profile
            }
        }
    }

    Given("프로필 이미지") {
        val profile = createProfile()
        When("파일 업로드에 성공하면") {
            every { profileRepository.save(any()) } returns profile

            Then("저장한다") {
                shouldNotThrowAnyUnit { profileService.save(profile) }
            }
        }
    }

    Given("파일 아이디와 파일 키") {
        val profile = createProfile()
        When("파일을 제거하면") {
            every { profileRepository.deleteById(any()) } just runs
            every { uploadService.deleteFile(any()) } just runs
        }
        Then("프로필 이미지 정보를 제거한다") {
            shouldNotThrowAnyUnit { profileService.save(profile) }
        }
    }
})
