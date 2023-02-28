package gongback.pureureum.api

import gongback.pureureum.api.dto.ApiResponse.Companion.success
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.post
import support.test.ControllerTestHelper

@WebMvcTest(AuthController::class)
class AuthControllerTest : ControllerTestHelper() {
    @Test
    fun `로그인 성공`() {
        val response = "Success!"

        mockMvc.post("/api/v1/auth/login") {
            jsonContent(createUser())
        }.andExpect {
            status { isOk() }
            content { success(response) }
        }.andDo { createDocument("user-login-success") }
    }
}

private fun createUser(
    email: String = "test@test.com",
    password: String = "password"
): Map<String, String> {
    return mapOf("email" to email, "password" to password)
}
