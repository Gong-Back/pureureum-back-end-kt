package gongback.pureureum.application

import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.domain.file.FileType
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.PROFILE_URL
import support.createMockFile
import support.createProfile

class UploadServiceTest : BehaviorSpec({
    val amazonS3Service = mockk<AmazonS3Service>()
    val uploadService = UploadService(
        amazonS3Service,
        object : NameGenerator {
            override fun generate(): String {
                return "server_default_profile"
            }
        }
    )

    Given("원본 파일 이름") {
        val profile = createProfile()
        When("정상적인 이름이라면") {
            Then("서버 저장 파일 이름을 반환한다.") {
                uploadService.createServerFileName(profile.originalFileName) shouldBe profile.serverFileName
            }
        }
    }

    Given("파일과 파일 타입, 서버 저장 파일 이름") {
        val file = createMockFile()
        val profile = createProfile()
        val fileKey = profile.fileKey
        val fileType = FileType.PROFILE
        When("정상적인 프로필 이미지 파일이라면") {
            every { amazonS3Service.uploadFile(any(), any(), any()) } returns fileKey
            Then("업로드를 진행한다") {
                uploadService.uploadFile(file, fileType, profile.serverFileName) shouldBe fileKey
            }
        }
    }

    Given("파일 키") {
        val profile = createProfile()
        val fileUrl = PROFILE_URL
        When("정상적인 파일 키라면") {
            every { amazonS3Service.getUrl(any()) } returns fileUrl
            every { amazonS3Service.deleteFile(any()) } just runs

            Then("파일 URL을 반환한다") {
                uploadService.getFileUrl(profile.fileKey) shouldBe fileUrl
            }
            Then("파일을 제거한다") {
                shouldNotThrowAnyUnit { uploadService.deleteFile(profile.fileKey) }
            }
        }
    }
})
