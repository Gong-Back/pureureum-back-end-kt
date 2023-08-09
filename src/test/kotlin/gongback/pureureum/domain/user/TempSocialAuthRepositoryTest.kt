package gongback.pureureum.domain.user

import gongback.pureureum.domain.social.SocialTempGender
import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.domain.social.TempSocialAuth
import gongback.pureureum.domain.social.TempSocialAuthRepository
import gongback.pureureum.domain.social.getTempByEmail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.test.BaseTests.RepositoryTest

fun createTempSocialAuth(
    email: String = "naver_testUser",
    name: String = "회원",
    birthday: String = "1998-12-28",
    phoneNumber: String = "010-1234-5678",
    userGender: SocialTempGender = SocialTempGender.MALE,
    socialType: SocialType = SocialType.NAVER
): TempSocialAuth {
    return TempSocialAuth(
        email = email,
        name = name,
        birthday = birthday,
        phoneNumber = phoneNumber,
        gender = userGender,
        socialType = socialType
    )
}

@RepositoryTest
class TempSocialAuthRepositoryTest(
    private val tempSocialAuthRepository: TempSocialAuthRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("이메일 존재 확인") {
        val tempSocialAuth = createTempSocialAuth()
        tempSocialAuthRepository.save(tempSocialAuth)

        expect("존재한다.") {
            val result = tempSocialAuthRepository.existsByEmail(tempSocialAuth.email)
            result shouldBe true
        }

        expect("존재하지 않는다.") {
            val result = tempSocialAuthRepository.existsByEmail("otherEmail")
            result shouldBe false
        }
    }

    context("소셜 임시 정보 조회") {
        val tempSocialAuth = createTempSocialAuth()
        tempSocialAuthRepository.save(tempSocialAuth)

        expect("이메일 조회 성공") {
            val result = tempSocialAuthRepository.getTempByEmail(tempSocialAuth.email)
            result.email shouldBe result.email
        }

        expect("이메일 조회 실패") {
            shouldThrow<NoSuchElementException> { tempSocialAuthRepository.getTempByEmail("otherEmail") }
        }
    }
})
