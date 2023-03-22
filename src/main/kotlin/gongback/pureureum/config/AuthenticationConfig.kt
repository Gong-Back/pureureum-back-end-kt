package gongback.pureureum.config

import gongback.pureureum.security.LoginUserResolver
import gongback.pureureum.security.RefreshTokenResolver
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class AuthenticationConfig(
    private val loginUserResolver: LoginUserResolver,
    private val refreshTokenResolver: RefreshTokenResolver
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loginUserResolver)
        resolvers.add(refreshTokenResolver)
    }
}
