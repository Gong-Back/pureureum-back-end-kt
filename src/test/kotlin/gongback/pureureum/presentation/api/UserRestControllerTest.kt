package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.UserService
import gongback.pureureum.domain.user.Gender
import gongback.pureureum.domain.user.Password
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import support.PHONE_NUMBER
import support.REFRESH_HEADER_NAME
import support.accessToken
import support.createAccessToken
import support.createLocalDate
import support.createRefreshToken
import support.createUser
import support.createUserInfoRes
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

private fun createUserInfoReq(
    password: Password = support.PASSWORD,
    phoneNumber: String = PHONE_NUMBER,
    nickName: String = EMAIL
): Map<String, Any> {
    return mapOf(
        "password" to password,
        "phoneNumber" to phoneNumber,
        "nickname" to nickName
    )
}

@WebMvcTest(UserRestController::class)
class UserRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var userService: UserService

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

    @Test
    fun `사용자 정보 조회 성공`() {
        val userInfo = createUserInfoRes(createUser())

        mockMvc.get("/api/v1/users/me") {
            accessToken(createAccessToken())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(userInfo) }
        }.andDo {
            createDocument(
                "get-user-info-success",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.email").description("아이디"),
                    fieldWithPath("data.phoneNumber").description("핸드폰 번호"),
                    fieldWithPath("data.name").description("이름"),
                    fieldWithPath("data.nickname").description("닉네임"),
                    fieldWithPath("data.gender").description("성별"),
                    fieldWithPath("data.birthday").description("생년월일"),
                    fieldWithPath("data.profileId").description("프로필 이미지 아이디")
                )
            )
        }
    }

    @Test
    fun `회원 정보 수정 성공`() {
        every { userService.updateUserInfo(any(), any()) } just runs

        mockMvc.post("/api/v1/users/update/info") {
            accessToken(createAccessToken())
            jsonContent(createUserInfoReq())
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "update-user-info-success",
                requestFields(
                    fieldWithPath("password").description("비밀번호").optional(),
                    fieldWithPath("phoneNumber").description("전화번호")
                        .attributes(Attributes.Attribute(LENGTH, "13")).optional(),
                    fieldWithPath("nickname").description("닉네임 (공백 X)")
                        .attributes(Attributes.Attribute(LENGTH, "2~30")).optional()
                )
            )
        }
    }

    @Test
    fun `회원 정보 수정 실패 - 중복된 닉네임이 들어왔을 때`() {
        every { userService.updateUserInfo(any(), any()) } throws IllegalArgumentException("이미 존재하는 닉네임입니다")

        mockMvc.post("/api/v1/users/update/info") {
            accessToken(createAccessToken())
            jsonContent(mapOf("nickname" to "duplicatedName"))
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "update-user-info-fail-duplicate-nickname",
                requestFields(
                    fieldWithPath("nickname").description("중복된 닉네임")
                ),
                responseFields(
                    fieldWithPath("code").description("오류 코드"),
                    fieldWithPath("messages").description("오류 메시지"),
                    fieldWithPath("data").description("응답 데이터")
                )
            )
        }
    }

    @Test
    fun `회원 정보 수정 실패 - 형식에 맞지 않은 정보일 때`() {
        val userInfoReq = createUserInfoReq(
            password = support.PASSWORD,
            phoneNumber = "00000000000",
            nickName = "abcdeabcdeabcdeabcdeabcdeabcdea"
        )

        mockMvc.post("/api/v1/users/update/info") {
            jsonContent(userInfoReq)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "update-user-info-fail",
                requestFields(
                    fieldWithPath("password").description("비밀번호"),
                    fieldWithPath("nickname").description("형식에 맞지 않는 닉네임"),
                    fieldWithPath("phoneNumber").description("형식에 맞지 않는 전화번호")
                ),
                responseFields(
                    fieldWithPath("code").description("오류 코드"),
                    fieldWithPath("messages").description("오류 메시지"),
                    fieldWithPath("data").description("응답 데이터")
                )
            )
        }
    }

    @Test
    fun `프로필 이미지 업데이트 성공`() {
        every { userService.updateProfile(any(), any()) } just runs

        val profile = MockMultipartFile(
            "profile",
            "default_profile.png",
            "image/png",
            "sample".toByteArray()
        )

        mockMvc.multipart("/api/v1/users/update/profile") {
            accessToken(createAccessToken())
            file(profile)
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "update-profile-success",
                requestParts(
                    partWithName("profile")
                        .description("프로필 이미지 파일(image/*), 기본 이미지로 변경 시 단순 요청")
                        .optional()
                )
            )
        }
    }
}
