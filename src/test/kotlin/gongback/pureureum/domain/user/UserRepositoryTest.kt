package gongback.pureureum.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createUser
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
            val result = userRepository.existsEmailOrNickname(user.email)
            result shouldBe true
        }

        expect("존재하지 않는다.") {
            val result = userRepository.existsEmailOrNickname("otherEmail")
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

    context("회원 조회") {
        val user = createUser()
        userRepository.save(user)

        expect("이메일 조회 성공") {
            val result = userRepository.getUserByEmail(user.email)
            result.email shouldBe user.email
        }

        expect("이메일 조회 실패") {
            shouldThrow<IllegalArgumentException> { userRepository.getUserByEmail("otherEmail") }
        }

        expect("전화번호 조회 성공") {
            val result = userRepository.getUserByPhoneNumber(user.phoneNumber)
            result.phoneNumber shouldBe user.phoneNumber
        }

        expect("전화번호 조회 실패") {
            shouldThrow<IllegalArgumentException> { userRepository.getUserByPhoneNumber("otherEmail") }
        }
    }

    context("닉네임 중복 확인") {
        val user = createUser()
        userRepository.save(user)

        expect("존재한다.") {
            val result = userRepository.existsNickname(user.nickname)
            result shouldBe true
        }

        expect("존재하지 않는다.") {
            val result = userRepository.existsNickname("otherNickname")
            result shouldBe false
        }
    }
})
