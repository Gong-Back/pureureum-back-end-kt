package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.event.FacilityCreateEvent
import gongback.pureureum.domain.facility.getFacilityById
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.mockk
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
            every { fileService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { fileService.getImageType(any()) } returns CERTIFICATION_DOC_TYPE
            every { fileService.uploadFile(any(), any()) } returns CERTIFICATION_DOC_FILE_KEY
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설에 대한 인증 서류 정보를 저장한다") {
                shouldNotThrowAnyUnit { facilityEventListenerHandler.handleFacilityCreate(facilityCreateEvent) }
            }
        }
    }
})
