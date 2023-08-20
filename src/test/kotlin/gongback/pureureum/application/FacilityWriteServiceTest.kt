package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.spyk
import org.springframework.context.ApplicationEventPublisher
import support.FACILITY_PROGRESS
import support.createFacility
import support.createFacilityReq
import support.createMockCertificationDoc
import support.createUser

class FacilityWriteServiceTest : BehaviorSpec({
    val userRepository = mockk<UserRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val applicationEventPublisher = spyk<ApplicationEventPublisher>()
    val facilityWriteService = FacilityWriteService(userRepository, facilityRepository, applicationEventPublisher)

    Given("사용자 이메일과 시설 정보") {
        val user = createUser()

        When("유효한 파일 정보가 들어왔다면") {
            val email = user.email
            val facility = createFacility()
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc())

            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.save(any()) } returns facility
            every { applicationEventPublisher.publishEvent(any()) } just runs

            Then("시설 정보를 등록한다.") {
                shouldNotThrowAnyUnit { facilityWriteService.registerFacility(email, facilityReq, certificationDoc) }
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
