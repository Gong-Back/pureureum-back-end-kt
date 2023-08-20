package gongback.pureureum.application

import gongback.pureureum.application.util.FileNameGenerator
import gongback.pureureum.support.constant.FileType
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.createFileInfo
import support.createMockCertificationDoc

class FileServiceTest : BehaviorSpec({
    val storageService = mockk<StorageService>()
    val fileNameGenerator = mockk<FileNameGenerator>()
    val fileService = FileService(storageService, fileNameGenerator)

    Given("파일 정보, 파일 타입") {
        val fileInfo = createFileInfo(createMockCertificationDoc())
        val serverFilePath = "facility/certification/test.png"

        When("올바른 파일에 대한 정보라면") {
            every { fileNameGenerator.generate() } returns "test.png"
            every { storageService.uploadFile(any(), any(), any()) } returns serverFilePath

            Then("파일을 업로드한다") {
                fileService.uploadFile(fileInfo, FileType.FACILITY_CERTIFICATION) shouldBe serverFilePath
            }
        }
    }

    Given("파일 키") {
        val serverFilePath = "facility/certification/test.png"
        val fileUrl = "test-pureureum-upload.com/facility/certification/test.png"

        When("조회 시 올바른 파일 키가 주어졌다면") {
            every { storageService.getUrl(any()) } returns fileUrl

            Then("파일에 대한 URL을 반환한다") {
                fileService.getFileUrl(serverFilePath) shouldBe fileUrl
            }
        }

        When("삭제 시 올바른 파일 키가 주어졌다면") {
            every { storageService.deleteFile(any()) } just runs

            Then("파일을 제거한다") {
                shouldNotThrowAnyUnit { fileService.deleteFile(serverFilePath) }
            }
        }
    }

    Given("파일 이름") {
        When("올바른 파일 이름이라면") {
            val fileName = "good-file-name"

            Then("올바른 파일 이름을 반환한다") {
                fileService.validateFileName(fileName) shouldBe fileName
            }
        }

        When("파일 이름이 null이라면") {
            val fileName = null

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { fileService.validateFileName(fileName) }
            }
        }

        When("파일 이름이 공백이라면") {
            val fileName = ""

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { fileService.validateFileName(fileName) }
            }
        }
    }

    Given("파일 컨텐츠 타입") {
        When("올바른 이미지 컨텐츠 타입이라면") {
            val fileContentType = "image/png"

            Then("올바른 파일 컨텐츠 타입을 반환한다") {
                fileService.getImageType(fileContentType) shouldBe fileContentType
            }
        }

        When("파일 컨텐츠 타입이 null이라면") {
            val fileContentType = null

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { fileService.getImageType(fileContentType) }
            }

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { fileService.getAnyContentType(fileContentType) }
            }
        }

        When("image로 시작하지 않는 컨텐츠 타입이라면") {
            val fileContentType = "application/pdf"

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { fileService.getImageType(fileContentType) }
            }
        }
    }
})
