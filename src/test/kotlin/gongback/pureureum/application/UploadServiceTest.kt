package gongback.pureureum.application

import gongback.pureureum.application.util.NameGenerator
import gongback.pureureum.support.enum.FileType
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.PROFILE_SERVER_FILE_NAME
import support.PROFILE_URL
import support.createMockFile
import support.createProfile

class UploadServiceTest : BehaviorSpec({
    val storageService = mockk<StorageService>()
    val uploadService = UploadService(
        storageService,
        object : NameGenerator {
            override fun generate(): String {
                return "server_default_profile"
            }
        }
    )

    Given("파일과 파일 타입, 서버 저장 파일 이름") {
        val file = createMockFile()
        val profile = createProfile()
        val fileKey = profile.fileKey
        val fileType = FileType.PROFILE
        When("정상적인 프로필 이미지 파일이라면") {
            every { storageService.uploadFile(any(), any(), any()) } returns fileKey
            Then("업로드를 진행한다") {
                uploadService.uploadFile(file, fileType, PROFILE_SERVER_FILE_NAME) shouldBe fileKey
            }
        }
    }

    Given("파일 키") {
        val profile = createProfile()
        val fileUrl = PROFILE_URL
        When("정상적인 파일 키라면") {
            every { storageService.getUrl(any()) } returns fileUrl
            every { storageService.deleteFile(any()) } just runs

            Then("파일 URL을 반환한다") {
                uploadService.getFileUrl(profile.fileKey) shouldBe fileUrl
            }
            Then("파일을 제거한다") {
                shouldNotThrowAnyUnit { uploadService.deleteFile(profile.fileKey) }
            }
        }
    }
})
