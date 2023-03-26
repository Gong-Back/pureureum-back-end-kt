package gongback.pureureum.presentation.api

import gongback.pureureum.application.OAuth2Service
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.AuthenticationInfo
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.dto.SocialEmailDto
import gongback.pureureum.application.dto.SocialRegisterUserReq
import gongback.pureureum.application.dto.TempSocialAuthDto
import gongback.pureureum.support.security.Tokens.Companion.REFRESH_TOKEN_HEADER
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/oauth")
class OAuth2RestController(
    private val oAuth2Service: OAuth2Service,
    private val userAuthenticationService: UserAuthenticationService
) {
    @PostMapping("/login/kakao")
    fun kakaoLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getKakaoUserInfo(authenticationInfo)
        return login(oAuthUserInfo, response)
    }

    @PostMapping("/login/google")
    fun googleLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getGoogleUserInfo(authenticationInfo)
        return login(oAuthUserInfo, response)
    }

    @PostMapping("/login/naver")
    fun naverLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getNaverUserInfo(authenticationInfo)
        return login(oAuthUserInfo, response)
    }

    @GetMapping("/temp/{email}")
    fun register(
        @PathVariable("email") email: String
    ): ResponseEntity<ApiResponse<TempSocialAuthDto>> {
        val tempSocialAuth = userAuthenticationService.getTempSocialAuth(email)
        return ResponseEntity.ok().body(ApiResponse.ok(tempSocialAuth))
    }

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid socialRegisterUserReq: SocialRegisterUserReq,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Unit>> {
        userAuthenticationService.registerBySocialReq(socialRegisterUserReq)
        setToken(response, socialRegisterUserReq.email)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    private fun login(
        oAuthUserInfo: OAuthUserInfo,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val code = userAuthenticationService.socialLogin(oAuthUserInfo)
        when (code) {
            ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH -> {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                        ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH.code,
                        ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH.message,
                        SocialEmailDto(oAuthUserInfo.clientEmail)
                    )
                )
            }

            ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS -> {
                val userAccountDto = userAuthenticationService.getUserAccountDto(oAuthUserInfo.phoneNumber)
                return ResponseEntity.badRequest().body(
                    ApiResponse.error(
                        ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.code,
                        ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.message,
                        userAccountDto
                    )
                )
            }

            else -> {
                setToken(response, oAuthUserInfo.clientEmail)
                return ResponseEntity.ok().build()
            }
        }
    }

    private fun setToken(
        response: HttpServletResponse,
        email: String
    ) {
        response.setHeader(
            HttpHeaders.AUTHORIZATION,
            userAuthenticationService.generateAccessTokenByEmail(email)
        )
        response.setHeader(
            REFRESH_TOKEN_HEADER,
            userAuthenticationService.generateRefreshTokenByEmail(email)
        )
    }
}