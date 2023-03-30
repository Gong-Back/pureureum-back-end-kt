package gongback.pureureum.domain.file

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createProfile
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class ProfileRepositoryTest(
    private val profileRepository: ProfileRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("S3 파일 키 조회") {
        val profile = createProfile()
        profileRepository.save(profile)

        expect("파일 아이디에 해당하는 파일 키를 조회한다") {
            val fileKey = profileRepository.getFileKey(profile.id)
            fileKey shouldBe profile.fileKey
        }

        expect("파일 키가 존재하지 않으면 예외가 발생한다") {
            shouldThrow<IllegalArgumentException> {
                profileRepository.getFileKey(2L)
            }
        }
    }
})
