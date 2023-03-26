package gongback.pureureum.application

import gongback.pureureum.application.dto.AuthenticationInfo
import gongback.pureureum.application.dto.GoogleUserInfoRes
import gongback.pureureum.application.dto.KakaoUserInfoRes
import gongback.pureureum.application.dto.NaverUserInfoRes
import gongback.pureureum.application.dto.OAuthToken
import gongback.pureureum.application.dto.OAuthUserInfo
import gongback.pureureum.application.properties.GoogleProperties
import gongback.pureureum.application.properties.KakaoProperties
import gongback.pureureum.application.properties.NaverProperties
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@Service
class OAuth2Service(
    private val webClient: WebClient,
    private val kakaoProperties: KakaoProperties,
    private val googleProperties: GoogleProperties,
    private val naverProperties: NaverProperties
) {
    fun getKakaoUserInfo(authenticationInfo: AuthenticationInfo): OAuthUserInfo {
        val tokenParams = getKakaoTokenParams(authenticationInfo.code, authenticationInfo.redirectUrl)
        val token = requestToken(kakaoProperties.kakaoTokenUrl, tokenParams).accessToken
        return requestUserInfo(kakaoProperties.kakaoUserInfoUrl, token, KakaoUserInfoRes::class.java)
    }

    fun getGoogleUserInfo(authenticationInfo: AuthenticationInfo): OAuthUserInfo {
        val tokenParams = getGoogleTokenParams(authenticationInfo.code, authenticationInfo.redirectUrl)
        val token = requestToken(googleProperties.googleTokenUrl, tokenParams).accessToken
        return requestUserInfo(googleProperties.googleUserInfoUrl, token, GoogleUserInfoRes::class.java)
    }

    fun getNaverUserInfo(authenticationInfo: AuthenticationInfo): OAuthUserInfo {
        val tokenParams = getNaverTokenParams(authenticationInfo.code)
        val token = requestToken(naverProperties.naverTokenUrl, tokenParams).accessToken
        return requestUserInfo(naverProperties.naverUserInfoUrl, token, NaverUserInfoRes::class.java)
    }

    private fun isValidAuthenticationToken(code: String) {
        if (code.isEmpty()) {
            throw OAuthAuthenticationException()
        }
    }

    private fun getKakaoTokenParams(code: String, redirectUrl: String): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", kakaoProperties.apiKey)
            add("redirect_uri", redirectUrl)
            add("code", code)
            add("client_secret", kakaoProperties.secretKey)
        }
    }

    private fun getGoogleTokenParams(code: String, redirectUrl: String): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", googleProperties.apiKey)
            add("redirect_uri", redirectUrl)
            add("code", code)
            add("client_secret", googleProperties.secretKey)
        }
    }

    private fun getNaverTokenParams(code: String): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("grant_type", "authorization_code")
            add("client_id", naverProperties.apiKey)
            add("code", code)
            add("state", naverProperties.state)
            add("client_secret", naverProperties.secretKey)
        }
    }

    fun bearerToken(token: String) = "Bearer $token"

    private fun requestToken(uri: String, params: MultiValueMap<String, String>): OAuthToken {
        return webClient
            .post()
            .uri(uri)
            .body(BodyInserters.fromFormData(params))
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError || httpStatusCode.is5xxServerError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java).map { _ -> OAuthAuthenticationException() }
            }
            .bodyToMono(OAuthToken::class.java)
            .block() ?: throw OAuthAuthenticationException()
    }

    private fun <R> requestUserInfo(uri: String, token: String, responseType: Class<R>): R {
        return webClient
            .get()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError || httpStatusCode.is5xxServerError }) { clientResponse ->
                clientResponse.bodyToMono(String::class.java).map { _ -> OAuthAuthenticationException() }
            }
            .bodyToMono(responseType)
            .block() ?: throw OAuthAuthenticationException()
    }
}
