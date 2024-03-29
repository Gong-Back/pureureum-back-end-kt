package gongback.pureureum.presentation.api

import gongback.pureureum.application.OAuth2Service
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.AccessTokenRes
import gongback.pureureum.application.dto.AuthenticationInfo
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.dto.SocialEmailDto
import gongback.pureureum.application.dto.SocialRegisterUserReq
import gongback.pureureum.application.dto.TempSocialAuthDto
import gongback.pureureum.presentation.api.CookieProvider.Companion.addRefreshTokenToCookie
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/oauth")
class OAuth2RestController(
    private val oAuth2Service: OAuth2Service,
    private val userAuthenticationService: UserAuthenticationService
) {
    @PostMapping("/login/kakao")
    fun kakaoLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getKakaoUserInfo(authenticationInfo)
        return login(oAuthUserInfo, servletResponse)
    }

    @PostMapping("/login/google")
    fun googleLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getGoogleUserInfo(authenticationInfo)
        return login(oAuthUserInfo, servletResponse)
    }

    @PostMapping("/login/naver")
    fun naverLoginProcess(
        @RequestBody @Valid authenticationInfo: AuthenticationInfo,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        val oAuthUserInfo = oAuth2Service.getNaverUserInfo(authenticationInfo)
        return login(oAuthUserInfo, servletResponse)
    }

    @GetMapping("/temp/{email}")
    fun tempRegister(
        @PathVariable("email") email: String
    ): ResponseEntity<ApiResponse<TempSocialAuthDto>> {
        val tempSocialAuth = userAuthenticationService.getTempSocialAuth(email)
        return ResponseEntity.ok().body(ApiResponse.ok(tempSocialAuth))
    }

    @PostMapping("/register")
    fun register(
        @RequestBody @Valid socialRegisterUserReq: SocialRegisterUserReq,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ApiResponse<AccessTokenRes>> {
        userAuthenticationService.registerBySocialReq(socialRegisterUserReq)
        val tokenRes = userAuthenticationService.getTokenRes(socialRegisterUserReq.email)
        addRefreshTokenToCookie(tokenRes, servletResponse)
        val accessTokenRes = AccessTokenRes(tokenRes.accessToken)

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.ok(accessTokenRes))
    }

    private fun login(
        oAuthUserInfo: OAuthUserInfo,
        servletResponse: HttpServletResponse
    ): ResponseEntity<ApiResponse<Any>> {
        return when (val code = userAuthenticationService.socialLogin(oAuthUserInfo)) {
            ErrorCode.REQUEST_RESOURCE_NOT_ENOUGH -> {
                ResponseEntity.badRequest().body(
                    ApiResponse.error(
                        code,
                        SocialEmailDto(oAuthUserInfo.clientEmail)
                    )
                )
            }

            ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS -> {
                val userAccountDto = userAuthenticationService.getUserAccountDto(oAuthUserInfo.phoneNumber)
                ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ApiResponse.error(
                        code,
                        userAccountDto
                    )
                )
            }

            else -> {
                val tokenRes = userAuthenticationService.getTokenRes(oAuthUserInfo.clientEmail)
                addRefreshTokenToCookie(tokenRes, servletResponse)
                val accessTokenRes = AccessTokenRes(tokenRes.accessToken)
                return ResponseEntity.ok(ApiResponse.ok(accessTokenRes))
            }
        }
    }
}
