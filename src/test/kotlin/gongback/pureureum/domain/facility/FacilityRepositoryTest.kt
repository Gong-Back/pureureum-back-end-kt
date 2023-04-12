package gongback.pureureum.domain.facility

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createCertificationDoc
import support.createFacility
import support.createUser
import support.test.BaseTests.RepositoryTest
import java.util.Collections

@RepositoryTest
class FacilityRepositoryTest(
    private val facilityRepository: FacilityRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("사용자별 카테고리에 따른 시설 조회") {
        val user = createUser()
        val facility = createFacility(user = user, progress = FacilityProgress.APPROVED)
        facilityRepository.save(facility)

        expect("시설이 존재한다") {
            val result = facilityRepository.getApprovedByCategory(FacilityCategory.YOUTH_FARMING, user.id)
            result shouldBe listOf(facility)
        }

        expect("시설이 존재하지 않는다") {
            val result = facilityRepository.getApprovedByCategory(FacilityCategory.FARMING_HEALING, user.id)
            result shouldBe Collections.emptyList()
        }
    }

    context("사용자별 시설 조회") {
        val user = createUser()
        val facility = createFacility(user = user, certificationDoc = Collections.emptyList())
        facilityRepository.save(facility)

        expect("시설이 존재한다") {
            val result = facilityRepository.getByUserId(user.id)
            result shouldBe listOf(facility)
        }

        expect("시설이 존재하지 않는다") {
            val result = facilityRepository.getByUserId(2L)
            result shouldBe Collections.emptyList()
        }
    }

    context("인증 서류 아이디에 따른 인증 서류 파일 키 조회") {
        val certificationDoc = createCertificationDoc()
        val facility = createFacility(certificationDoc = listOf(createCertificationDoc()).toMutableList())
        val docId = 1L

        facilityRepository.save(facility)

        expect("인증 서류가 존재한다") {
            val result = facilityRepository.getDocFileKeyByDocId(docId)
            result shouldBe certificationDoc.fileKey
        }

        expect("인증 서류가 존재하지 않는다") {
            shouldThrow<IllegalArgumentException> { facilityRepository.getDocFileKeyByDocId(2L) }
        }
    }
})
