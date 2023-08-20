package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.OAuth2Service
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.AuthenticationInfo
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.SocialRegisterUserReq
import gongback.pureureum.application.dto.TempSocialAuthDto
import gongback.pureureum.application.dto.TokenRes
import gongback.pureureum.domain.social.SocialTempGender
import gongback.pureureum.domain.social.SocialType
import gongback.pureureum.domain.user.UserGender
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import java.time.LocalDate
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.snippet.Attributes.Attribute
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import support.BIRTHDAY
import support.NAME
import support.PHONE_NUMBER
import support.UserGENDER
import support.createAccessToken
import support.createKakaoUserInfo
import support.createRefreshToken
import support.createUserAccountDto
import support.test.ControllerTestHelper

fun createAuthenticationInfo(
    code: String = "AuthenticationCode",
    redirectUrl: String = "localhost:3000/oauth2/redirect"
): AuthenticationInfo {
    return AuthenticationInfo(code, redirectUrl)
}

fun createTempSocialAuthDto(
    email: String = "naver_testUser",
    name: String? = null,
    birthday: String? = null,
    phoneNumber: String? = null,
    userGender: SocialTempGender? = null,
    socialType: SocialType = SocialType.NAVER
): TempSocialAuthDto {
    return TempSocialAuthDto(email, name, birthday, phoneNumber, userGender, socialType)
}

fun createSocialRegisterUserReq(
    email: String = "naver_testUser",
    name: String = NAME,
    birthday: LocalDate = BIRTHDAY,
    phoneNumber: String = PHONE_NUMBER,
    userGender: UserGender = UserGENDER,
    socialType: SocialType = SocialType.NAVER
): SocialRegisterUserReq {
    return SocialRegisterUserReq(email, name, birthday, phoneNumber, userGender, socialType)
}

@WebMvcTest(OAuth2RestController::class)
class OAuth2RestControllerTest : ControllerTestHelper() {
    @MockkBean
    lateinit var oAuth2Service: OAuth2Service

    @MockkBean
    lateinit var userAuthenticationService: UserAuthenticationService

    @Test
    fun `OAuth 로그인 성공`() {
        val oAuth2UserInfo = createKakaoUserInfo()
        val tokenRes = TokenRes(createAccessToken(), createRefreshToken())

        every { oAuth2Service.getKakaoUserInfo(any()) } returns oAuth2UserInfo
        every { userAuthenticationService.socialLogin(any()) } returns ErrorCode.OK
        every { userAuthenticationService.getTokenRes(any()) } returns tokenRes

        mockMvc.post("/api/v1/oauth/login/kakao") {
            jsonContent(createAuthenticationInfo())
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument(
                "oAuth-login-success",
                requestFields(
                    fieldWithPath("code").description("인가 코드"),
                    fieldWithPath("redirectUrl").description("리다이렉트 주소")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.accessToken").description("AccessToken"),
                    fieldWithPath("data.refreshToken").description("RefreshToken")
                )
            )
        }
    }

    @Test
    fun `OAuth 로그인 실패 - 충분하지 않은 정보`() {
        val oAuth2UserInfo = createKakaoUserInfo(name = "", birthyear = "", birthday = "", phoneNumber = "")
        every { oAuth2Service.getKakaoUserInfo(any()) } returns oAuth2UserInfo
        every { userAuthenticationService.socialLogin(any()) } returns ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH

        mockMvc.post("/api/v1/oauth/login/kakao") {
            jsonContent(createAuthenticationInfo())
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "oAuth-login-fail-not-enough-info",
                requestFields(
                    fieldWithPath("code").description("인가 코드"),
                    fieldWithPath("redirectUrl").description("리다이렉트 주소")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.email").description("소셜 아이디").attributes(Attribute(LENGTH, "min 8"))
                )
            )
        }
    }

    @Test
    fun `OAuth 로그인 실패 - 이미 존재하는 사용자일 경우`() {
        val oAuth2UserInfo = createKakaoUserInfo()
        val userAccountDto = createUserAccountDto()
        every { oAuth2Service.getKakaoUserInfo(any()) } returns oAuth2UserInfo
        every { userAuthenticationService.socialLogin(any()) } returns ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS
        every { userAuthenticationService.getUserAccountDto(any()) } returns userAccountDto

        mockMvc.post("/api/v1/oauth/login/kakao") {
            jsonContent(createAuthenticationInfo())
        }.andExpect {
            status { isConflict() }
        }.andDo {
            createDocument(
                "oAuth-login-fail-exists-user",
                requestFields(
                    fieldWithPath("code").description("인가 코드"),
                    fieldWithPath("redirectUrl").description("리다이렉트 주소")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.email").description("사용자 이메일").attributes(Attribute(LENGTH, "8-15")),
                    fieldWithPath("data.socialType").description("사용자 가입 방법")
                )
            )
        }
    }

    @Test
    fun `소셜 사용자 임시 저장 정보 조회 성공`() {
        val tempSocialAuthDto = createTempSocialAuthDto()
        every { userAuthenticationService.getTempSocialAuth(any()) } returns tempSocialAuthDto

        this.mockMvc.perform(get("/api/v1/oauth/temp/{email}", tempSocialAuthDto.email))
            .andExpect(status().isOk)
            .andDo(
                createPathDocument(
                    "temp-user-info-search-success",
                    pathParameters(
                        parameterWithName("email").description("OAuth 로그인 응답 코드 412때, 받은 email")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("messages").description("응답 메시지"),
                        fieldWithPath("data.email").description("저장되어 있는 소셜 사용자 이메일"),
                        fieldWithPath("data.name").description("저장 되어 있는 소셜 사용자 이름"),
                        fieldWithPath("data.birthday").description("저장 되어 있는 소셜 사용자 생일"),
                        fieldWithPath("data.phoneNumber").description("저장 되어 있는 소셜 사용자 전화번호"),
                        fieldWithPath("data.gender").description("저장 되어 있는 소셜 사용자 성별"),
                        fieldWithPath("data.socialType").description("저장 되어 있는 소셜 사용자 로그인 타입")
                    )
                )
            )
    }

    @Test
    fun `소셜 사용자 임시 저장 정보 조회 실패 - 저장되어 있지 않은 이메일`() {
        val tempSocialAuthDto = createTempSocialAuthDto()
        every { userAuthenticationService.getTempSocialAuth(any()) } throws IllegalArgumentException("요청하신 임시 소셜 사용자 정보를 찾을 수 없습니다")

        this.mockMvc.perform(get("/api/v1/oauth/temp/{email}", tempSocialAuthDto.email))
            .andExpect(status().isBadRequest)
            .andDo(
                createPathDocument(
                    "temp-user-info-search-fail-not-exists-email",
                    pathParameters(
                        parameterWithName("email").description("임시 저장되지 않은 이메일")
                    ),
                    responseFields(
                        fieldWithPath("code").description("응답 코드"),
                        fieldWithPath("messages").description("응답 메시지"),
                        fieldWithPath("data").description("응답 데이터")
                    )
                )
            )
    }

    @Test
    fun `OAuth 회원 회원가입 성공`() {
        val socialRegisterUserReq = createSocialRegisterUserReq()
        val tokenRes = TokenRes(createAccessToken(), createRefreshToken())

        every { userAuthenticationService.registerBySocialReq(any()) } just runs
        every { userAuthenticationService.getTokenRes(any()) } returns tokenRes

        mockMvc.post("/api/v1/oauth/register") {
            jsonContent(socialRegisterUserReq)
        }.andExpect {
            status { isCreated() }
        }.andDo {
            createDocument(
                "oAuth-register-success",
                requestFields(
                    fieldWithPath("email").description("소셜 회원 아이디").attributes(Attribute(LENGTH, "min 8")),
                    fieldWithPath("name").description("소셜 회원 이름").attributes(Attribute(LENGTH, "max 20")),
                    fieldWithPath("birthday").description("소셜 회원 생일"),
                    fieldWithPath("phoneNumber").description("소셜 회원 전화번호").attributes(Attribute(LENGTH, "13")),
                    fieldWithPath("gender").description("소셜 회원 성별"),
                    fieldWithPath("socialType").description("소셜 회원 로그인 타입")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.accessToken").description("AccessToken"),
                    fieldWithPath("data.refreshToken").description("RefreshToken")
                )
            )
        }
    }

    @Test
    fun `OAuth 회원 회원가입 실패 - 인증 받지 않은 전화번호`() {
        val socialRegisterUserReq = createSocialRegisterUserReq()
        every { userAuthenticationService.registerBySocialReq(any()) } throws IllegalArgumentException("본인 인증되지 않은 정보입니다")

        mockMvc.post("/api/v1/oauth/register") {
            jsonContent(socialRegisterUserReq)
        }.andExpect {
            status { isBadRequest() }
        }.andDo {
            createDocument(
                "oAuth-register-fail-invalid-phoneNumber",
                requestFields(
                    fieldWithPath("email").description("소셜 회원 아이디").attributes(Attribute(LENGTH, "min 8")),
                    fieldWithPath("name").description("소셜 회원 이름").attributes(Attribute(LENGTH, "max 20")),
                    fieldWithPath("birthday").description("소셜 회원 생일"),
                    fieldWithPath("phoneNumber").description("인증 받지 않은 전화번호").attributes(Attribute(LENGTH, "13")),
                    fieldWithPath("gender").description("소셜 회원 성별"),
                    fieldWithPath("socialType").description("소셜 회원 로그인 타입")
                ),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data").description("응답 데이터")
                )
            )
        }
    }
}
