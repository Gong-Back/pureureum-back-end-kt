package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.CERTIFICATION_DOC_FILE_KEY
import support.CERTIFICATION_DOC_ORIGINAL_FILE_NAME
import support.CERTIFICATION_DOC_TYPE
import support.FACILITY_PROGRESS_APPROVED
import support.createCertificationDocDto
import support.createFacility
import support.createFacilityReq
import support.createMockCertificationDoc
import support.createUser

class FacilityWriteServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val fileService = mockk<FileService>()
    val facilityWriteService = FacilityWriteService(userRepository, facilityRepository, fileService)

    Given("사용자 이메일과 시설 정보") {
        val user = createUser()

        When("올바른 사용자 정보와 시설 정보가 들어왔다면") {
            val email = user.email
            val facility = createFacility()
            val facilityReq = createFacilityReq()

            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.save(any()) } returns facility

            Then("시설 정보를 저장하고, 저장된 시설 아이디를 반환한다") {
                facilityWriteService.registerFacility(email, facilityReq) shouldBe facility.id
            }
        }
    }

    Given("인증 서류 파일들") {
        val certificationDocFiles = listOf(createMockCertificationDoc())

        val contentType = CERTIFICATION_DOC_TYPE
        val originalFileName = CERTIFICATION_DOC_ORIGINAL_FILE_NAME
        val fileKey = CERTIFICATION_DOC_FILE_KEY

        val certificationDocDto = createCertificationDocDto(fileKey, contentType, originalFileName)

        When("올바른 인증 서류 정보와 시설 엔티티 정보라면") {
            every { fileService.validateImageType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { fileService.uploadFile(any(), any()) } returns fileKey

            Then("파일을 업로드하고, 시설 정보 엔티티를 반환한다") {
                facilityWriteService.uploadCertificationDocs(certificationDocFiles) shouldBe listOf(certificationDocDto)
            }
        }

        When("인증 서류에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            every { fileService.validateImageType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.uploadCertificationDocs(certificationDocFiles)
                }
            }
        }

        When("인증 서류에 대한 파일 이름이 올바르지 않을 경우") {
            every { fileService.validateImageType(any()) } returns contentType
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.uploadCertificationDocs(certificationDocFiles)
                }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            every { fileService.validateImageType(any()) } returns contentType
            every { fileService.validateFileName(any()) } returns originalFileName
            every { fileService.uploadFile(any(), any()) } throws S3Exception()

            Then("예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.uploadCertificationDocs(certificationDocFiles)
                }
            }
        }
    }

    Given("시설 아이디와 인증 서류 엔티티 정보") {
        val facility = createFacility()
        val facilityCertificationDocDto = createCertificationDocDto()
        val certificationDocDtos = listOf(facilityCertificationDocDto)
        val certificationDocs = listOf(facilityCertificationDocDto.toEntity())

        When("올바른 시설 아이디와 인증 서류 엔티티 정보가 들어온다면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("인증 서류 정보를 저장한다") {
                shouldNotThrowAnyUnit { facilityWriteService.saveFacilityFiles(facility.id, certificationDocDtos) }
                facility.certificationDoc shouldBe certificationDocs
            }
        }
    }

    Given("저장된 시설 아이디") {
        val facility = createFacility()

        When("올바른 시설 아이디가 들어왔다면") {
            every { facilityRepository.deleteById(any()) } just runs

            Then("시설 정보를 삭제한다") {
                shouldNotThrowAnyUnit { facilityWriteService.deleteFacility(facility.id) }
            }
        }
    }

    Given("시설 아이디와 진행 상태") {
        val facility = createFacility()

        When("올바른 시설 아이디와 진행 상태라면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설의 진행 상태가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityWriteService.updateFacilityProgress(facility.id, FACILITY_PROGRESS_APPROVED) }
                facility.progress shouldBe FACILITY_PROGRESS_APPROVED
            }
        }
    }

    Given("시설 아이디 리스트와 진행 상태") {
        val facility1 = createFacility()
        val facility2 = createFacility()

        When("올바른 시설 아이디 리스트와 진행 상태라면") {
            every { facilityRepository.updateProgressByIds(any(), any()) } just runs

            Then("여러 시설의 진행 상태가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityWriteService.updateFacilitiesProgress(listOf(facility1.id, facility2.id), FACILITY_PROGRESS_APPROVED) }
            }
        }
    }
})
