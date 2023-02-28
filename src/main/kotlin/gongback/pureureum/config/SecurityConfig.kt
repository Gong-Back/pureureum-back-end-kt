package gongback.pureureum.config

import gongback.pureureum.config.handler.CustomAccessDeniedHandler
import gongback.pureureum.config.handler.CustomAuthenticationEntryPoint
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler
) {
    private val permitPatterns: List<String> = listOf("/api/v1/auth/**", "/api/docs", "/favicon.ico")

    @Bean
    fun springSecurity(http: HttpSecurity): SecurityFilterChain = http
        .csrf { it.disable() }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .logout { it.disable() }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it
                .requestMatchers(*permitPatterns.toTypedArray()).permitAll()
                .anyRequest().authenticated()
        }
        .exceptionHandling {
            it
                .authenticationEntryPoint(customAuthenticationEntryPoint)
                .accessDeniedHandler(customAccessDeniedHandler)
        }
        .build()
}
