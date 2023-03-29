package support.test

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.security.JwtNotExistsException
import gongback.pureureum.security.LoginUser
import gongback.pureureum.security.LoginUserResolver
import gongback.pureureum.security.RefreshToken
import gongback.pureureum.security.RefreshTokenResolver
import io.mockk.every
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
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
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.filter.CharacterEncodingFilter
import support.REFRESH_HEADER_NAME
import support.createAccessToken
import support.createUser
import support.test.BaseTests.TestEnvironment

@TestEnvironment
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension::class)
abstract class ControllerTestHelper {
    lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var loginUserResolver: LoginUserResolver

    @MockkBean
    private lateinit var refreshTokenResolver: RefreshTokenResolver

    @Autowired
    lateinit var objectMapper: ObjectMapper

    protected val LENGTH = "length"

    @BeforeEach
    internal fun setUp(
        webApplicationContext: WebApplicationContext,
        restDocumentationContextProvider: RestDocumentationContextProvider
    ) {
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

        loginUserResolver.also {
            slot<MethodParameter>().also { slot ->
                every { it.supportsParameter(capture(slot)) } answers {
                    slot.captured.hasParameterAnnotation(LoginUser::class.java)
                }
            }
            slot<NativeWebRequest>().also { slot ->
                every { it.resolveArgument(any(), any(), capture(slot), any()) } answers {
                    val hasToken = slot.captured.getHeader(HttpHeaders.AUTHORIZATION)?.startsWith("Bearer", true)
                    if (hasToken != true) {
                        throw JwtNotExistsException()
                    }
                    createUser()
                }
            }
        }

        refreshTokenResolver.also {
            slot<MethodParameter>().also { slot ->
                every { it.supportsParameter(capture(slot)) } answers {
                    slot.captured.hasParameterAnnotation(RefreshToken::class.java)
                }
            }
            slot<NativeWebRequest>().also { slot ->
                every { it.resolveArgument(any(), any(), capture(slot), any()) } answers {
                    val hasToken = slot.captured.getHeader(REFRESH_HEADER_NAME)?.startsWith("Bearer", true)
                    if (hasToken != true) {
                        throw JwtNotExistsException()
                    }
                    createAccessToken()
                }
            }
        }
    }

    fun MockHttpServletRequestDsl.jsonContent(value: Any) {
        content = objectMapper.writeValueAsString(value)
        contentType = MediaType.APPLICATION_JSON
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any) {
        return handle(document("{class-name}/$value"))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, requestHeadersSnippet: RequestHeadersSnippet) {
        return handle(document("{class-name}/$value", requestHeadersSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestHeadersSnippet: RequestHeadersSnippet,
        responseHeadersSnippet: ResponseHeadersSnippet
    ) {
        return handle(document("{class-name}/$value", requestHeadersSnippet, responseHeadersSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, responseFieldsSnippet: ResponseFieldsSnippet) {
        return handle(document("{class-name}/$value", responseFieldsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, requestPartsSnippet: RequestPartsSnippet) {
        return handle(document("{class-name}/$value", requestPartsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(value: Any, requestFieldsSnippet: RequestFieldsSnippet) {
        return handle(document("{class-name}/$value", requestFieldsSnippet))
    }

    fun MockMvcResultHandlersDsl.createDocument(
        value: Any,
        requestFieldsSnippet: RequestFieldsSnippet,
        responseHeadersSnippet: ResponseHeadersSnippet
    ) {
        return handle(document("{class-name}/$value", requestFieldsSnippet, responseHeadersSnippet))
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
        pathParametersSnippet: PathParametersSnippet,
        responseFieldsSnippet: ResponseFieldsSnippet
    ) {
        return handle(document("{class-name}/$value", pathParametersSnippet, responseFieldsSnippet))
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
