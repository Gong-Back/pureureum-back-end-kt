package gongback.pureureum.domain.user

import gongback.pureureum.application.createUser
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class UserRepositoryTest(
    private val userRepository: UserRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("이메일 중복 확인") {
        val user = createUser()
        userRepository.save(user)

        expect("존재한다.") {
            val result = userRepository.existsByEmail(user.email)
            result shouldBe true
        }

        expect("존재하지 않는다.") {
            val result = userRepository.existsByEmail("otherEmail")
            result shouldBe false
        }
    }

    context("전화번호 중복 확인") {
        val user = createUser()
        userRepository.save(user)

        expect("중복된다") {
            val result = userRepository.existsByPhoneNumber(user.phoneNumber)
            result shouldBe true
        }

        expect("중복되지 않는다.") {
            val result = userRepository.existsByPhoneNumber("010-1234-5678")
            result shouldBe false
        }
    }
})