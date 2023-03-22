package gongback.pureureum.security

import gongback.pureureum.application.UserService
import gongback.pureureum.domain.user.User
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

private const val BEARER = "Bearer"

@Component
class LoginUserResolver(
    private val userService: UserService,
    private val jwtTokenProvider: JwtTokenProvider
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(LoginUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): User {
        val token = jwtTokenProvider.extractAccessToken(webRequest)

        val userEmail = jwtTokenProvider.getSubject(token)

        return userService.getUserByEmail(userEmail)
    }
}
