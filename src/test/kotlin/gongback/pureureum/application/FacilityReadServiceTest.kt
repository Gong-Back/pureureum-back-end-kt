package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityProgress
import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getAllNotApprovedByCategory
import gongback.pureureum.domain.facility.getApprovedByCategoryAndUserId
import gongback.pureureum.domain.facility.getByUserId
import gongback.pureureum.domain.facility.getDocFileKeyByDocId
import gongback.pureureum.domain.facility.getFacilityById
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import support.FACILITY_CATEGORY
import support.createFacility
import support.createFacilityRes
import support.createFacilityResWithProgress
import support.createFacilityWithDocIds
import support.createUser

class FacilityReadServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val userRepository = mockk<UserRepository>()
    val facilityRepository = mockk<FacilityRepository>()
    val facilityReadService = FacilityReadService(fileService, userRepository, facilityRepository)

    Given("사용자 이메일과 카테고리") {
        val user = createUser()
        val email = user.email
        val category = FACILITY_CATEGORY
        val facilities = listOf(createFacility())
        val facilityRes = listOf(createFacilityRes(id = 0L))

        When("올바른 카테고리라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.getApprovedByCategoryAndUserId(category, user.id) } returns facilities

            Then("사용자의 카테고리별 시설 정보를 반환한다.") {
                facilityReadService.getApprovedFacilityByCategory(email, category) shouldBe facilityRes
            }
        }
    }

    Given("사용자 이메일") {
        val user = createUser()
        val email = user.email
        val facilities = listOf(createFacility())
        val facilityResWithProgress = listOf(createFacilityResWithProgress(id = 0L))

        When("올바른 사용자 정보라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.getByUserId(user.id) } returns facilities

            Then("사용자의 시설 정보를 반환한다.") {
                facilityReadService.getAllFacilities(email) shouldBe facilityResWithProgress
            }
        }
    }

    Given("인증 서류 아이디") {
        val docId = 1L
        val fileKey = "sampleKey"
        val fileUrl = "sampleURL"

        When("올바른 인증 서류 아이디라면") {
            every { facilityRepository.getDocFileKeyByDocId(any(), any()) } returns fileKey
            every { fileService.getFileUrl(any()) } returns fileUrl

            Then("인증 서류의 다운로드 URL을 반환한다") {
                facilityReadService.getCertificationDocDownloadPath(1L, docId) shouldBe fileUrl
            }
        }
    }

    Given("시설 카테고리") {
        val facilities = listOf(createFacility(progress = FacilityProgress.NOT_APPROVED))
        val facilitiesRes = listOf(createFacilityRes())

        When("올바른 카테고리라면") {
            every { facilityRepository.getAllNotApprovedByCategory(any()) } returns facilities

            Then("해당하는 카테고리의 승인받지 않은 시설 리스트를 반환한다") {
                facilityReadService.getNotApprovedFacilitiesByCategory(FACILITY_CATEGORY) shouldBe facilitiesRes
            }
        }
    }

    Given("시설 아이디") {
        val facility = createFacility()
        val facilityWithDocIds = createFacilityWithDocIds()

        When("올바른 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설 정보를 반환한다") {
                facilityReadService.getFacilityById(1L) shouldBe facilityWithDocIds
            }
        }

        When("올바르지 않은 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } throws IllegalArgumentException("시설 정보가 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { facilityReadService.getFacilityById(2L) }
            }
        }
    }
})
