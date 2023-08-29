package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import support.CERTIFICATION_DOC_FILE_KEY
import support.CERTIFICATION_DOC_NAME
import support.CERTIFICATION_DOC_TYPE
import support.FACILITY_PROGRESS
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

        When("유효한 파일 정보가 들어왔다면") {
            val email = user.email
            val facility = createFacility()
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc())

            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.save(any()) } returns facility

            Then("시설 정보를 등록한다.") {
                shouldNotThrowAnyUnit { facilityWriteService.registerFacility(email, facilityReq, certificationDoc) }
            }
        }
    }

    Given("저장된 시설 아이디, 인증 서류 파일들") {
        val facility = createFacility()
        val certificationDoc = listOf(createMockCertificationDoc())
        val facilityId = facility.id

        When("올바른 인증 서류 정보와 시설 엔티티 정보라면") {
            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { fileService.uploadFile(any(), any()) } returns CERTIFICATION_DOC_FILE_KEY
            every { facilityRepository.getFacilityById(any()) } returns facility

            facilityWriteService.saveFacilityFiles(facilityId, certificationDoc)

            Then("시설에 대한 인증 서류 정보를 저장한다") {
                verify(exactly = 0) { facilityRepository.deleteById(facilityId) }
            }
        }

        When("인증 서류에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")
            every { facilityRepository.deleteById(any()) } just runs

            Then("저장되었던 시설에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.saveFacilityFiles(facilityId, certificationDoc)
                }
                verify(exactly = 1) { facilityRepository.deleteById(facilityId) }
            }
        }

        When("인증 서류에 대한 파일 이름이 올바르지 않을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
            every { facilityRepository.deleteById(any()) } just runs

            Then("저장되었던 시설에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.saveFacilityFiles(facilityId, certificationDoc)
                }
                verify(exactly = 1) { facilityRepository.deleteById(facilityId) }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { fileService.uploadFile(any(), any()) } throws S3Exception()
            every { facilityRepository.deleteById(any()) } just runs

            Then("저장되었던 시설에 대한 정보를 제거하고, 예외가 발생한다") {
                shouldThrow<FileHandlingException> {
                    facilityWriteService.saveFacilityFiles(facilityId, certificationDoc)
                }
                verify(exactly = 1) { facilityRepository.deleteById(facilityId) }
            }
        }
    }

    Given("시설 아이디와 진행 상태") {
        val facility = createFacility()

        When("올바른 시설 아이디와 진행 상태라면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설 정보가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityWriteService.updateFacilityProgress(1L, FACILITY_PROGRESS) }
            }
        }

        When("올바르지 않은 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } throws IllegalArgumentException("시설 정보가 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { facilityWriteService.updateFacilityProgress(1L, FACILITY_PROGRESS) }
            }
        }
    }

    Given("시설 아이디 리스트와 진행 상태") {
        When("올바른 시설 아이디 리스트와 진행 상태라면") {
            every { facilityRepository.updateProgressByIds(any(), any()) } just runs

            Then("시설 정보가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityWriteService.updateFacilitiesProgress(listOf(1L, 2L), FACILITY_PROGRESS) }
            }
        }
    }
})
