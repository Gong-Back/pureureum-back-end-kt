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
import io.kotest.assertions.throwables.shouldNotThrowAnyUnit
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import support.CERTIFICATION_DOC_FILE_KEY
import support.CERTIFICATION_DOC_NAME
import support.CERTIFICATION_DOC_TYPE
import support.FACILITY_CATEGORY
import support.FACILITY_PROGRESS
import support.createFacility
import support.createFacilityReq
import support.createFacilityRes
import support.createFacilityResWithProgress
import support.createFacilityWithDocIds
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
            every { uploadService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 존재하지 않습니다")

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
            every { uploadService.validateFileName(any()) } throws IllegalArgumentException("원본 파일 이름이 비어있습니다")

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
            every { uploadService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { uploadService.getImageType(any()) } throws IllegalArgumentException("파일 형식이 유효하지 않습니다")

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
            every { uploadService.validateFileName(any()) } returns CERTIFICATION_DOC_NAME
            every { uploadService.getImageType(any()) } returns CERTIFICATION_DOC_TYPE
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

        When("올바른 카테고리라면") {
            every { userRepository.getUserByEmail(any()) } returns user
            every { facilityRepository.getApprovedByCategoryAndUserId(category, user.id) } returns facilities
            every { uploadService.validateFileName(any()) } returns "OriginalFilename"
            every { uploadService.getImageType(any()) } returns "image/png"

            Then("사용자의 카테고리별 시설 정보를 반환한다.") {
                facilityService.getApprovedFacilityByCategory(email, category.name) shouldBe facilityRes
            }
        }

        When("올바르지 않은 카테고리라면") {
            val invalidCategory = "invalidCategory"

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { facilityService.getApprovedFacilityByCategory(email, invalidCategory) }
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
            every { uploadService.validateFileName(any()) } returns "OriginalFilename"
            every { uploadService.getImageType(any()) } returns "image/png"

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
            every { facilityRepository.getDocFileKeyByDocId(any(), any()) } returns fileKey
            every { uploadService.getFileUrl(any()) } returns fileUrl
            every { uploadService.validateFileName(any()) } returns "OriginalFilename"
            every { uploadService.getImageType(any()) } returns "image/png"

            Then("인증 서류의 다운로드 URL을 반환한다") {
                facilityService.getCertificationDocDownloadPath(1L, docId) shouldBe fileUrl
            }
        }
    }

    Given("시설 카테고리") {
        val facilities = listOf(createFacility(progress = FacilityProgress.NOT_APPROVED))
        val facilitiesRes = listOf(createFacilityRes())

        When("올바른 카테고리라면") {
            every { facilityRepository.getAllNotApprovedByCategory(any()) } returns facilities

            Then("해당하는 카테고리의 승인받지 않은 시설 리스트를 반환한다") {
                facilityService.getNotApprovedFacilitiesByCategory(FACILITY_CATEGORY.name) shouldBe facilitiesRes
            }
        }

        When("올바르지 않은 카테고리라면") {
            val invalidCategory = "invalidCategory"

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> { facilityService.getNotApprovedFacilitiesByCategory(invalidCategory) }
            }
        }
    }

    Given("시설 아이디") {
        val facility = createFacility()
        val facilityWithDocIds = createFacilityWithDocIds()

        When("올바른 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설 정보를 반환한다") {
                facilityService.getFacilityById(1L) shouldBe facilityWithDocIds
            }
        }

        When("올바르지 않은 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } throws IllegalArgumentException("시설 정보가 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { facilityService.getFacilityById(2L) }
            }
        }
    }

    Given("시설 아이디와 진행 상태") {
        val facility = createFacility()

        When("올바른 시설 아이디와 진행 상태라면") {
            every { facilityRepository.getFacilityById(any()) } returns facility

            Then("시설 정보가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityService.updateFacilityProgress(1L, FACILITY_PROGRESS) }
            }
        }

        When("올바르지 않은 시설 아이디라면") {
            every { facilityRepository.getFacilityById(any()) } throws IllegalArgumentException("시설 정보가 존재하지 않습니다")

            Then("예외가 발생한다") {
                shouldThrow<IllegalArgumentException> { facilityService.updateFacilityProgress(1L, FACILITY_PROGRESS) }
            }
        }
    }

    Given("시설 아이디 리스트와 진행 상태") {
        When("올바른 시설 아이디 리스트와 진행 상태라면") {
            every { facilityRepository.updateProgressByIds(any(), any()) } just runs

            Then("시설 정보가 업데이트된다") {
                shouldNotThrowAnyUnit { facilityService.updateFacilitiesProgress(listOf(1L, 2L), FACILITY_PROGRESS) }
            }
        }
    }
})
