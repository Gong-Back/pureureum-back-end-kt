package gongback.pureureum.application

import gongback.pureureum.domain.facility.FacilityRepository
import gongback.pureureum.domain.facility.getApprovedByCategory
import gongback.pureureum.domain.facility.getByUserId
import gongback.pureureum.domain.facility.getDocFileKeyByDocId
import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import support.CERTIFICATION_DOC_FILE_KEY
import support.FACILITY_CATEGORY
import support.createFacility
import support.createFacilityReq
import support.createFacilityRes
import support.createFacilityResWithProgress
import support.createMockCertificationDoc
import support.createUser

class FacilityServiceTest : BehaviorSpec({
    val facilityRepository = mockk<FacilityRepository>()
    val userRepository = mockk<UserRepository>()
    val uploadService = mockk<UploadService>()
    val facilityService = FacilityService(facilityRepository, userRepository, uploadService)

    Given("사용자 이메일과 시설 정보") {
        val user = createUser()

        When("원본 파일 이름이 존재하지 않는다면") {
            val email = user.email
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc(originalFileName = null))
            every { userRepository.getUserByEmail(any()) } returns user

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    facilityService.registerFacility(
                        email,
                        facilityReq,
                        certificationDoc
                    )
                }
            }
        }

        When("원본 파일 이름이 비어있다면") {
            val email = user.email
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc(originalFileName = ""))

            every { userRepository.getUserByEmail(any()) } returns user

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    facilityService.registerFacility(
                        email,
                        facilityReq,
                        certificationDoc
                    )
                }
            }
        }

        When("파일 형식이 유효하지 않다면") {
            val email = user.email
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc(contentType = null))

            every { userRepository.getUserByEmail(any()) } returns user

            Then("예외가 발생한다.") {
                shouldThrow<IllegalArgumentException> {
                    facilityService.registerFacility(
                        email,
                        facilityReq,
                        certificationDoc
                    )
                }
            }
        }

        When("원본 파일 이름이 존재하면서 유효한 파일이라면") {
            val email = user.email
            val facility = createFacility()
            val facilityReq = createFacilityReq()
            val certificationDoc = listOf(createMockCertificationDoc())
            val fileKey = CERTIFICATION_DOC_FILE_KEY

            every { userRepository.getUserByEmail(any()) } returns user
            every { uploadService.uploadFile(any(), any(), any()) } returns fileKey
            every { facilityRepository.save(any()) } returns facility

            Then("시설 정보를 등록한다.") {
                shouldNotThrowAnyUnit { facilityService.registerFacility(email, facilityReq, certificationDoc) }
            }
        }
    }

    Given("사용자 이메일과 카테고리") {
        val user = createUser()
        val email = user.email
        val category = FACILITY_CATEGORY
        val facilities = listOf(createFacility())
        val facilityRes = listOf(createFacilityRes(id = 0L))

        When("올바르지 않은 카테고리 정보라면") {
            val invalidCategory = "invalidCategory"

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { facilityService.getApprovedFacilityByCategory(email, invalidCategory) }
            }
        }

        When("올바른 카테고리 정보라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.getApprovedByCategory(category, user.id) } returns facilities

            Then("사용자의 카테고리별 시설 정보를 반환한다.") {
                facilityService.getApprovedFacilityByCategory(email, category.name) shouldBe facilityRes
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
                facilityService.getAllFacilities(email) shouldBe facilityResWithProgress
            }
        }
    }

    Given("인증 서류 아이디") {
        val docId = 1L
        val fileKey = "sampleKey"
        val fileUrl = "sampleURL"

        When("올바른 인증 서류 아이디라면") {
            every { facilityRepository.getDocFileKeyByDocId(any()) } returns fileKey
            every { uploadService.getFileUrl(any()) } returns fileUrl

            Then("인증 서류의 다운로드 URL을 반환한다") {
                facilityService.getCertificationDocDownloadPath(docId) shouldBe fileUrl
            }
        }
    }
})
