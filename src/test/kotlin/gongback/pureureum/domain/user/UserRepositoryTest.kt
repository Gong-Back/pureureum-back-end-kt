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

    context("이메일 중복확인") {
        val user = createUser()
        userRepository.save(user)

        expect("이메일이 중복된다") {
            val result = userRepository.existsByEmail(user.email)
            result shouldBe true
        }

        expect("이메일이 중복되지 않는다.") {
            val result = userRepository.existsByEmail("otherEmail")
            result shouldBe false
        }
    }
})
