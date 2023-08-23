package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.event.FacilityCreateEvent
import gongback.pureureum.domain.facility.getFacilityById
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
import support.createFacility
import support.createFileReq
import support.createMockCertificationDoc

class FacilityEventListenerHandlerTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val facilityRepository = mockk<FacilityRepository>()
    val facilityEventListenerHandler = FacilityEventListenerHandler(fileService, facilityRepository)

    Given("시설 생성 이벤트") {
        val facility = createFacility()
        val certificationDoc = listOf(createMockCertificationDoc())
        val facilityCreateEvent = FacilityCreateEvent(facility.id, createFileReq(certificationDoc))

        When("올바른 인증 서류 정보와 시설 엔티티 정보라면") {
            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { fileService.uploadFile(any(), any()) } returns CERTIFICATION_DOC_FILE_KEY
            every { facilityRepository.getFacilityById(any()) } returns facility

            facilityEventListenerHandler.handleFacilityCreate(facilityCreateEvent)

            Then("시설에 대한 인증 서류 정보를 저장한다") {
                verify(exactly = 0) { facilityRepository.deleteById(facilityCreateEvent.facilityId) }
            }
        }

        When("인증 서류에 대한 올바르지 않은 컨텐츠 타입이 들어왔을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")
            every { facilityRepository.deleteById(any()) } just runs

            facilityEventListenerHandler.handleFacilityCreate(facilityCreateEvent)

            Then("저장되었던 시설에 대한 정보를 제거한다") {
                verify(exactly = 1) { facilityRepository.deleteById(facilityCreateEvent.facilityId) }
            }
        }

        When("인증 서류에 대한 파일 이름이 올바르지 않을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")
            every { facilityRepository.deleteById(any()) } just runs

            facilityEventListenerHandler.handleFacilityCreate(facilityCreateEvent)

            Then("저장되었던 시설에 대한 정보를 제거한다") {
                verify(exactly = 1) { facilityRepository.deleteById(facilityCreateEvent.facilityId) }
            }
        }

        When("파일 업로드 도중 실패했을 경우") {
            clearMocks(fileService, facilityRepository)

            every { fileService.validateImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { fileService.uploadFile(any(), any()) } throws S3Exception()
            every { facilityRepository.deleteById(any()) } just runs

            facilityEventListenerHandler.handleFacilityCreate(facilityCreateEvent)

            Then("저장되었던 시설에 대한 정보를 제거한다") {
                verify(exactly = 1) { facilityRepository.deleteById(facilityCreateEvent.facilityId) }
            }
        }
    }
})
