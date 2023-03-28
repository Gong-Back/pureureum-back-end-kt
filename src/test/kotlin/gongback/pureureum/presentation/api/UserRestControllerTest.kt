package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.SmsLogService
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.domain.user.Gender
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.web.servlet.post
import support.REFRESH_HEADER_NAME
import support.createAccessToken
import support.createLocalDate
import support.createRefreshToken
import support.refreshToken
import support.test.ControllerTestHelper
import java.time.LocalDate

private const val EMAIL = "test@test.com"
private const val PASSWORD = "password"

private fun createRegisterUserRequest(
    name: String = "회원",
    email: String = EMAIL,
    phoneNumber: String = "010-0000-0000",
    gender: Gender = Gender.MALE,
    birthday: LocalDate = createLocalDate(1998, 12, 28),
    password: String = PASSWORD
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
    email: String = EMAIL,
    password: String = PASSWORD
): Map<String, String> {
    return mapOf("email" to email, "password" to password)
}

@WebMvcTest(UserRestController::class)
class UserRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var smsLogService: SmsLogService

    @MockkBean
    private lateinit var userAuthenticationService: UserAuthenticationService

    @Test
    fun `로그인 성공`() {
        val accessToken = createAccessToken()
        val refreshToken = createRefreshToken()

        every { userAuthenticationService.validateAuthentication(any()) } just runs
        every { userAuthenticationService.generateAccessTokenByEmail(any()) } returns accessToken
        every { userAuthenticationService.generateRefreshTokenByEmail(any()) } returns refreshToken

        mockMvc.post("/api/v1/users/login") {
            jsonContent(createLoginReq())
        }.andExpect {
            status { isOk() }
            header {
                string(HttpHeaders.AUTHORIZATION, accessToken)
                string(REFRESH_HEADER_NAME, refreshToken)
            }
        }.andDo {
            createDocument(
                "user-login-success",
                requestFields(
                    fieldWithPath("email").description("아이디").attributes(Attributes.Attribute(LENGTH, "8-15")),
                    fieldWithPath("password").description("비밀번호")
                ),
                responseHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token"),
                    headerWithName(REFRESH_HEADER_NAME).description("Refresh Token")
                )
            )
        }
    }

    @Test
    fun `로그인 실패 - 이메일이 유효하지 않을 때`() {
        every { userAuthenticationService.validateAuthentication(any()) } throws IllegalArgumentException("요청하신 사용자 정보를 찾을 수 없습니다")

        mockMvc.post("/api/v1/users/login") {
            jsonContent(createLoginReq())
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "user-login-fail-not-valid-email",
                requestFields(
                    fieldWithPath("email").description("유효하지 않은 이메일"),
                    fieldWithPath("password").description("비밀번호")
                )
            )
        }
    }

    @Test
    fun `로그인 실패 - 비밀번호가 유효하지 않을 때`() {
        every { userAuthenticationService.validateAuthentication(any()) } throws IllegalArgumentException("비밀번호가 일치하지 않습니다")

        mockMvc.post("/api/v1/users/login") {
            jsonContent(createLoginReq())
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "user-login-fail-not-valid-password",
                requestFields(
                    fieldWithPath("email").description("아이디").attributes(Attributes.Attribute(LENGTH, "8-15")),
                    fieldWithPath("password").description("유효하지 않은 비밀번호")
                )
            )
        }
    }

    @Test
    fun `회원가입 성공`() {
        every { userAuthenticationService.register(any()) } just Runs

        mockMvc.post("/api/v1/users/register") {
            jsonContent(createRegisterUserRequest())
        }.andExpect {
            status { isCreated() }
        }.andDo {
            createDocument(
                "user-register-success",
                requestFields(
                    fieldWithPath("email").description("아이디").attributes(Attributes.Attribute(LENGTH, "8-15")),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("이름").attributes(Attributes.Attribute(LENGTH, "max 20")),
                    fieldWithPath("phoneNumber").description("전화번호").attributes(Attributes.Attribute(LENGTH, "13")),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생년월일")
                )
            )
        }
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
        }.andDo {
            createDocument(
                "user-register-fail",
                requestFields(
                    fieldWithPath("email").description("형식에 맞지 않는 아이디"),
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("name").description("형식에 맞지 않는 이름"),
                    fieldWithPath("phoneNumber").description("형식에 맞지 않는 전화번호"),
                    fieldWithPath("gender").description("성별"),
                    fieldWithPath("birthday").description("생년월일")
                )
            )
        }
    }

    @Test
    fun `이메일 중복 확인 성공`() {
        every { userAuthenticationService.checkDuplicatedEmailOrNickname(any()) } just Runs

        mockMvc.post("/api/v1/users/validate/email") {
            jsonContent(mapOf("email" to "testEmail123"))
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "user-checkEmail-success",
                requestFields(
                    fieldWithPath("email").description("아이디").attributes(Attributes.Attribute(LENGTH, "8-15"))
                )
            )
        }
    }

    @Test
    fun `이메일 중복 확인 실패`() {
        every { userAuthenticationService.checkDuplicatedEmailOrNickname(any()) } throws IllegalStateException("이미 가입된 이메일입니다")

        mockMvc.post("/api/v1/users/validate/email") {
            jsonContent(mapOf("email" to "testEmail123"))
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "user-checkEmail-fail",
                requestFields(
                    fieldWithPath("email").description("이미 가입된 아이디")
                )
            )
        }
    }

    @Test
    fun `토큰 재발급 성공`() {
        val accessToken = createAccessToken()
        val refreshToken = createRefreshToken()

        every { userAuthenticationService.generateTokenByRefreshToken(any()) } returns accessToken

        mockMvc.post("/api/v1/users/reissue/token") {
            refreshToken(refreshToken)
        }.andExpect {
            status { isOk() }
            header { string(HttpHeaders.AUTHORIZATION, accessToken) }
        }.andDo {
            createDocument(
                "reissue-access-token-success",
                requestHeaders(
                    headerWithName("RefreshToken").description("Refresh Token")
                ),
                responseHeaders(
                    headerWithName(HttpHeaders.AUTHORIZATION).description("Access Token")
                )
            )
        }
    }

    @Test
    fun `토큰 재발급 실패 - 토큰이 없을 때`() {
        mockMvc.post("/api/v1/users/reissue/token") {
        }.andExpect {
            status { isUnauthorized() }
        }.andDo { createDocument("reissue-access-token-fail") }
    }
}
