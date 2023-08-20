package gongback.pureureum.application

import gongback.pureureum.domain.user.UserRepository
import gongback.pureureum.domain.user.getUserByEmail
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import support.createUser

class UserReadServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val userRepository = mockk<UserRepository>()
    val userReadService = UserReadService(fileService, userRepository)

    Given("회원 이메일") {
        val user = createUser()
        val email = user.email
        When("이메일의 회원이 있을 경우") {
            every { userRepository.getUserByEmail(email) } returns user

            Then("회원 정보를 반환한다.") {
                userReadService.getUserByEmail(email).email shouldBe email
            }
        }

        When("이메일의 회원이 없을 경우") {
            every { userRepository.getUserByEmail(email) } throws IllegalArgumentException()

            Then("성공한다.") {
                shouldThrow<IllegalArgumentException> { userReadService.getUserByEmail(email) }
            }
        }
    }
})
