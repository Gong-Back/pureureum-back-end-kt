package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.SmsSendException
import gongback.pureureum.application.SmsService
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.SmsSendResponse
import gongback.pureureum.infra.sms.SmsOverRequestException
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.snippet.Attributes.Attribute
import org.springframework.test.web.servlet.post
import support.createUserAccountDto
import support.test.ControllerTestHelper

fun createPhoneNumber(
    phoneNumber: String = "010-0000-0000"
): Map<String, Any> {
    return mapOf("phoneNumber" to phoneNumber)
}

@WebMvcTest(SmsRestController::class)
class SmsRestControllerTest : ControllerTestHelper() {
    @MockkBean
    private lateinit var smsService: SmsService

    @MockkBean
    private lateinit var userAuthenticationService: UserAuthenticationService

    @Test
    fun `전화번호 인증 전송 성공`() {
        val smsSendResponse = SmsSendResponse("000000")
        every { userAuthenticationService.checkDuplicatedPhoneNumber(any()) } just runs
        every { smsService.sendSmsCertification(any()) } returns smsSendResponse

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.ok(smsSendResponse) }
        }.andDo {
            createDocument(
                "sms-send-success",
                requestFields(fieldWithPath("phoneNumber").description("전화번호").attributes(Attribute(LENGTH, "13"))),
                responseFields(
                    fieldWithPath("code").description("응답 코드"),
                    fieldWithPath("messages").description("응답 메시지"),
                    fieldWithPath("data.certificationNumber").description("인증번호").attributes(Attribute(LENGTH, "6"))
                )
            )
        }
    }

    @Test
    fun `전화번호 인증 전송 실패 - 이미 존재하는 전화번호`() {
        val userAccountDto = createUserAccountDto()
        every { userAuthenticationService.checkDuplicatedPhoneNumber(any()) } throws IllegalArgumentException("이미 가입된 전화번호입니다")
        every { userAuthenticationService.getUserAccountDto(any()) } returns userAccountDto

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { isConflict() }
            content {
                ApiResponse.error(
                    ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.code,
                    ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.message,
                    userAccountDto
                )
            }
        }.andDo {
            createDocument(
                "sms-send-fail-exists-user",
                requestFields(fieldWithPath("phoneNumber").description("이미 가입된 전화번호")),
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
    fun `전화번호 인증 전송 실패 - 서버 오류`() {
        every { userAuthenticationService.checkDuplicatedPhoneNumber(any()) } just runs
        every { smsService.sendSmsCertification(any()) } throws SmsSendException()

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { is5xxServerError() }
            content { ApiResponse.error(ErrorCode.SMS_SEND_FAILED.code, ErrorCode.SMS_SEND_FAILED.message) }
        }.andDo {
            createDocument(
                "sms-send-fail-server",
                requestFields(fieldWithPath("phoneNumber").description("전화번호").attributes(Attribute(LENGTH, "13")))
            )
        }
    }

    @Test
    fun `전화번호 인증 전송 실패 - 50건 초과`() {
        every { userAuthenticationService.checkDuplicatedPhoneNumber(any()) } just runs
        every { smsService.sendSmsCertification(any()) } throws SmsOverRequestException()

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { is5xxServerError() }
            content {
                ApiResponse.error(
                    ErrorCode.SMS_OVER_SENDING_REQUEST.code,
                    ErrorCode.SMS_OVER_SENDING_REQUEST.message
                )
            }
        }.andDo {
            createDocument(
                "sms-send-fail-client",
                requestFields(fieldWithPath("phoneNumber").description("전화번호").attributes(Attribute(LENGTH, "13")))
            )
        }
    }

    @Test
    fun `전화번호 인증 완료`() {
        every { smsService.completeCertification(any()) } just runs

        mockMvc.post("/api/v1/sms/complete/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { isNoContent() }
        }.andDo {
            createDocument(
                "sms-complete-success",
                requestFields(fieldWithPath("phoneNumber").description("전화번호").attributes(Attribute(LENGTH, "13")))
            )
        }
    }

    @Test
    fun `전화번호 인증 실패 - 기록이 없을 때`() {
        val req = createPhoneNumber()

        val errorMessage = " receiver: ${req.get("phoneNumber")}"
        every { smsService.completeCertification(any()) } throws IllegalArgumentException("본인 인증 요청을 하지 않은 사용자입니다, $errorMessage")

        mockMvc.post("/api/v1/sms/complete/certification") {
            jsonContent(req)
        }.andExpect {
            status { isBadRequest() }
            content { ApiResponse.error(ErrorCode.REQUEST_RESOURCE_NOT_VALID.code, errorMessage) }
        }.andDo {
            createDocument(
                "sms-complete-fail",
                requestFields(fieldWithPath("phoneNumber").description("로그에 없는 전화번호"))
            )
        }
    }
}
