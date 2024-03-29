package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.UserReadService
import gongback.pureureum.application.UserWriteService
import gongback.pureureum.application.dto.TokenRes
import gongback.pureureum.domain.user.Password
import gongback.pureureum.domain.user.UserGender
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.restdocs.cookies.CookieDocumentation.responseCookies
import org.springframework.restdocs.cookies.CookieDocumentation.cookieWithName
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.partWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParts
import org.springframework.restdocs.snippet.Attributes
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.multipart
import org.springframework.test.web.servlet.post
import support.ACCESS_TOKEN
import support.PHONE_NUMBER
import support.REFRESH_TOKEN
import support.TOKEN_TYPE
import support.createAccessToken
import support.createLocalDate
import support.createMockProfileFile
import support.createProfileDto
import support.createRefreshToken
import support.createUser
import support.createUserInfoRes
import support.test.ControllerTestHelper
import support.token

private const val EMAIL = "test@test.com"
private const val PASSWORD = "password"

private fun createRegisterUserRequest(
    name: String = "회원",
    email: String = EMAIL,
    phoneNumber: String = "010-0000-0000",
    userGender: UserGender = UserGender.MALE,
    birthday: LocalDate = createLocalDate(1998, 12, 28),
    password: String = PASSWORD
): Map<String, Any> {
    return mapOf(
        "name" to name,
        "email" to email,
        "phoneNumber" to phoneNumber,
        "gender" to userGender,
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
    private lateinit var userReadService: UserReadService

    @MockkBean
    private lateinit var userWriteService: UserWriteService

    @MockkBean
    private lateinit var userAuthenticationService: UserAuthenticationService

    @Test
    fun `로그인 성공`() {
        val tokenRes = TokenRes(createAccessToken(), createRefreshToken())

        every { userAuthenticationService.validateAuthentication(any()) } just runs
        every { userAuthenticationService.getTokenRes(any()) } returns tokenRes

        mockMvc.post("/api/v1/users/login") {
            jsonContent(createLoginReq())
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "user-login-success",
                requestFields(
                    fieldWithPath("email").description("아이디").attributes(Attributes.Attribute(LENGTH, "8-15")),
                    fieldWithPath("password").description("비밀번호")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.accessToken").description("액세스 토큰")
                ),
                responseCookies(
                    cookieWithName("refreshToken").description("refresh token")
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
            status { isNoContent() }
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
        every { userAuthenticationService.checkDuplicatedEmailOrNickname(any()) } throws IllegalArgumentException("이미 가입된 이메일입니다")

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
    fun `Token 재발급 - 성공`() {
        val tokenRes = TokenRes(ACCESS_TOKEN, REFRESH_TOKEN)
        every { userAuthenticationService.reissueToken(any()) } returns tokenRes

        mockMvc.post("/api/v1/users/reissue-token") {
            header(HttpHeaders.AUTHORIZATION, TOKEN_TYPE + REFRESH_TOKEN)
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "token-reissue-success",
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.accessToken").description("액세스 토큰")
                ),
                responseCookies(
                    cookieWithName("refreshToken").description("refresh token")
                )
            )
        }
    }

    @Test
    fun `사용자 정보 조회 성공`() {
        val userInfo = createUserInfoRes(createUser())

        every { userReadService.getUserInfoWithProfileUrl(any()) } returns userInfo

        mockMvc.get("/api/v1/users/me") {
            token(createAccessToken())
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
                    fieldWithPath("data.profileUrl").description("signed 프로필 주소")
                )
            )
        }
    }

    @Test
    fun `회원 정보 수정 성공`() {
        every { userWriteService.updateUserInfo(any(), any()) } just runs

        mockMvc.post("/api/v1/users/update/info") {
            token(createAccessToken())
            jsonContent(createUserInfoReq())
        }.andExpect {
            status { isNoContent() }
        }.andDo {
            createDocument(
                "update-user-info-success",
                requestFields(
                    fieldWithPath("password").description("비밀번호").optional(),
                    fieldWithPath("phoneNumber").description("전화번호")
                        .attributes(Attributes.Attribute(LENGTH, "13")).optional(),
                    fieldWithPath("nickname").description("닉네임")
                        .attributes(Attributes.Attribute(LENGTH, "2~30")).optional()
                )
            )
        }
    }

    @Test
    fun `회원 정보 수정 실패 - 중복된 닉네임이 들어왔을 때`() {
        every { userWriteService.updateUserInfo(any(), any()) } throws IllegalArgumentException("이미 존재하는 닉네임입니다")

        mockMvc.post("/api/v1/users/update/info") {
            token(createAccessToken())
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
        val profileDto = createProfileDto()
        every { userWriteService.uploadProfileImage(any(), any()) } returns profileDto
        every { userWriteService.updateProfile(any(), any()) } just runs

        val profileImage = createMockProfileFile()

        mockMvc.multipart("/api/v1/users/update/profile") {
            token(createAccessToken())
            file(profileImage)
        }.andExpect {
            status { isNoContent() }
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

    @Test
    fun `프로필 이미지 업데이트 실패 - 원본 파일 이름이 비어있을 경우`() {
        every { userWriteService.uploadProfileImage(any(), any()) } throws IllegalArgumentException("원본 파일 이름이 비어있습니다")
        val profile = createMockProfileFile(
            originalFileName = ""
        )

        mockMvc.multipart("/api/v1/users/update/profile") {
            token(createAccessToken())
            file(profile)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "update-profile-original-file-name-empty-fail",
                requestParts(
                    partWithName("profile")
                        .description("형식에 맞지 않은 이미지 정보")
                )
            )
        }
    }

    @Test
    fun `프로필 이미지 업데이트 실패 - 파일 형식이 이미지가 아닐 경우`() {
        every { userWriteService.uploadProfileImage(any(), any()) } throws IllegalArgumentException("이미지 형식의 파일만 가능합니다")
        val profile = createMockProfileFile(
            contentType = "text/html"
        )

        mockMvc.multipart("/api/v1/users/update/profile") {
            token(createAccessToken())
            file(profile)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "update-profile-file-type-not-image-fail",
                requestParts(
                    partWithName("profile")
                        .description("형식에 맞지 않은 이미지 정보")
                )
            )
        }
    }
}
