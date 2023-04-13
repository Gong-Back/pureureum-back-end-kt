package support.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.security.JwtNotExistsException
import gongback.pureureum.security.JwtNotValidException
import gongback.pureureum.security.JwtTokenProvider
import gongback.pureureum.security.LoginEmail
import gongback.pureureum.security.LoginEmailResolver
import io.mockk.every
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.cookies.ResponseCookiesSnippet
import org.springframework.restdocs.headers.RequestHeadersSnippet
import org.springframework.restdocs.headers.ResponseHeadersSnippet
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.restdocs.payload.RequestFieldsSnippet
import org.springframework.restdocs.payload.ResponseFieldsSnippet
import org.springframework.restdocs.request.PathParametersSnippet
import org.springframework.restdocs.request.RequestPartsSnippet
import org.springframework.restdocs.snippet.Attributes.Attribute
import org.springframework.test.web.servlet.MockHttpServletRequestDsl
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultHandlersDsl
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.MultiValueMap
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.filter.CharacterEncodingFilter
import support.ACCESS_TOKEN
import support.EMAIL
import support.NOT_VALID_ACCESS_TOKEN
import support.NOT_VALID_REFRESH_TOKEN
import support.REFRESH_TOKEN
import support.test.BaseTests.TestEnvironment

@TestEnvironment
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
abstract class ControllerTestHelper {
    lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var loginEmailResolver: LoginEmailResolver

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    lateinit var objectMapper: ObjectMapper

    protected val LENGTH = "length"
    protected val EXAMPLE = "example"

    @BeforeEach
    internal fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentationContextProvider: RestDocumentationContextProvider
    ) {
        objectMapper = jacksonObjectMapper().apply { registerModule(JavaTimeModule()) }

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .addFilter<DefaultMockMvcBuilder>(CharacterEncodingFilter("UTF-8", true))
            .alwaysDo<DefaultMockMvcBuilder>(MockMvcResultHandlers.print())
            .apply<DefaultMockMvcBuilder>(
                MockMvcRestDocumentation.documentationConfiguration(restDocumentationContextProvider)
                    .operationPreprocessors()
                    .withRequestDefaults(Preprocessors.prettyPrint())
                    .withResponseDefaults(Preprocessors.prettyPrint())
            )
            .build()

        loginEmailResolver.also {
            slot<MethodParameter>().also { slot ->
                every { it.supportsParameter(capture(slot)) } answers {
                    slot.captured.hasParameterAnnotation(LoginEmail::class.java)
                }
            }
            slot<NativeWebRequest>().also { slot ->
                every { it.resolveArgument(any(), any(), capture(slot), any()) } answers {
                    val accessToken =
                        slot.captured.getHeader(HttpHeaders.AUTHORIZATION) ?: throw JwtNotExistsException()
                    val tokenFormat = accessToken.split(" ")
                    if (tokenFormat[0] != "Bearer" || tokenFormat[1] == NOT_VALID_ACCESS_TOKEN) {
                        throw JwtNotValidException()
                    }

                    EMAIL
                }
            }
        }

        every { jwtTokenProvider.isValidToken(REFRESH_TOKEN) } returns true
        every { jwtTokenProvider.isValidToken(ACCESS_TOKEN) } returns true
        every { jwtTokenProvider.isValidToken(NOT_VALID_REFRESH_TOKEN) } returns false
        every { jwtTokenProvider.isValidToken(NOT_VALID_ACCESS_TOKEN) } returns false
        every { jwtTokenProvider.getSubject(REFRESH_TOKEN) } returns EMAIL
        every { jwtTokenProvider.createRefreshToken(EMAIL) } returns REFRESH_TOKEN
        every { jwtTokenProvider.createToken(EMAIL) } returns ACCESS_TOKEN
    }

    fun objectToString(value: Any): String = objectMapper.writeValueAsString(value)

    fun MockHttpServletRequestDsl.jsonContent(value: Any) {
        content = objectToString(value)
        contentType = MediaType.APPLICATION_JSON
    }

    fun MockHttpServletRequestDsl.params(data: MultiValueMap<String, String>) {
        params = data
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, responseFieldsSnippet: ResponseFieldsSnippet) {
        return handle(document("{class-name}/$value", responseFieldsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, requestPartsSnippet: RequestPartsSnippet) {
        return handle(document("{class-name}/$value", requestPartsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestHeadersSnippet: RequestHeadersSnippet,
        requestPartsSnippet: RequestPartsSnippet
    ) {
        return handle(document("{class-name}/$value", requestHeadersSnippet, requestPartsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestHeadersSnippet: RequestHeadersSnippet
    ) {
        return handle(document("{class-name}/$value", requestHeadersSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, requestFieldsSnippet: RequestFieldsSnippet) {
        return handle(document("{class-name}/$value", requestFieldsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestHeadersSnippet: RequestHeadersSnippet,
        responseHeadersSnippet: ResponseHeadersSnippet,
        responseCookieSnippet: ResponseCookiesSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ) {
        return handle(
            document(
                "{class-name}/$value",
                requestHeadersSnippet,
                responseHeadersSnippet,
                responseCookieSnippet,
                responseFieldsSnippet
            )
        )
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestFieldsSnippet: RequestFieldsSnippet,
        responseHeadersSnippet: ResponseHeadersSnippet,
        responseCookieSnippet: ResponseCookiesSnippet
    ) {
        return handle(
            document(
                "{class-name}/$value",
                requestFieldsSnippet,
                responseHeadersSnippet,
                responseCookieSnippet
            )
        )
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestFieldsSnippet: RequestFieldsSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ) {
        return handle(document("{class-name}/$value", requestFieldsSnippet, responseFieldsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        responseHeadersSnippet: ResponseHeadersSnippet,
        responseCookiesSnippet: ResponseCookiesSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ) {
        return handle(
            document(
                "{class-name}/$value",
                responseHeadersSnippet,
                responseCookiesSnippet,
                responseFieldsSnippet
            )
        )
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestHeadersSnippet: RequestHeadersSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ) {
        return handle(document("{class-name}/$value", requestHeadersSnippet, responseFieldsSnippet))
    }

    fun createPathDocument(
        value: Any,
        pathParametersSnippet: PathParametersSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ): RestDocumentationResultHandler {
        return document("{class-name}/$value", pathParametersSnippet, responseFieldsSnippet)
    }

    companion object {
        fun field(key: String, value: String): Attribute {
            return Attribute(key, value)
        }
    }
}
