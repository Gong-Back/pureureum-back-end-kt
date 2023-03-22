package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.UserService
import gongback.pureureum.domain.user.Gender
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.post
import support.createLocalDate
import support.test.ControllerTestHelper
import java.time.LocalDate

private fun createRegisterUserRequest(
    name: String = "회원",
    email: String = "testEmail123",
    phoneNumber: String = "010-0000-0000",
    gender: Gender = Gender.MALE,
    birthday: LocalDate = createLocalDate(1998, 12, 28),
    password: String = "passwordTest0!"
): Map<String, Any> {
    return mapOf(
        "name" to name,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "gender" to gender,
        "birthday" to birthday,
        "password" to password
    )
}

private fun createLoginReq(
    email: String = "test@test.com",
    password: String = "password"
): Map<String, String> {
    return mapOf("email" to email, "password" to password)
}

@WebMvcTest(UserRestController::class)
class UserRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var userService: UserService

    @Test
    fun `로그인 성공`() {
        val response = "Success!"

        mockMvc.post("/api/v1/users/login") {
            jsonContent(createLoginReq())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.success(response) }
        }.andDo { createDocument("user-login-success") }
    }

    @Test
    fun `회원가입 성공`() {
        every { userService.register(any()) } just Runs

        mockMvc.post("/api/v1/users/register") {
            jsonContent(createRegisterUserRequest())
        }.andExpect {
            status { isCreated() }
        }.andDo { createDocument("user-register-success") }
    }

    @Test
    fun `회원가입 실패`() {
        val registerUserReq = createRegisterUserRequest(
            name = "테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트테스트",
            password = "password",
            email = "email",
            phoneNumber = "00000000000"
        )

        mockMvc.post("/api/v1/users/register") {
            jsonContent(registerUserReq)
        }.andExpect {
            status { isBadRequest() }
        }.andDo { createDocument("user-register-fail") }
    }

    @Test
    fun `이메일 중복 확인 성공`() {
        every { userService.checkDuplicatedEmail(any()) } just Runs

        mockMvc.post("/api/v1/users/validate/email") {
            jsonContent(mapOf("email" to "testEmail123"))
        }.andExpect {
            status { isOk() }
        }.andDo { createDocument("user-checkEmail-success") }
    }

    @Test
    fun `이메일 중복 확인 실패`() {
        every { userService.checkDuplicatedEmail(any()) } throws IllegalStateException("이미 가입된 이메일입니다.")

        mockMvc.post("/api/v1/users/validate/email") {
            jsonContent(mapOf("email" to "testEmail123"))
        }.andExpect {
            status { isBadRequest() }
        }.andDo { createDocument("user-checkEmail-fail") }
    }
}
