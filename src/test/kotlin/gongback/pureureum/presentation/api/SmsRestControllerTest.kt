package gongback.pureureum.presentation.api

import com.ninjasquad.springmockk.MockkBean
import gongback.pureureum.application.SmsSendException
import gongback.pureureum.application.SmsService
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.application.dto.SmsSendResponse
import io.mockk.every
import io.mockk.just
import io.mockk.runs
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.post
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

    @Test
    fun `전화번호 인증 전송 성공`() {
        val smsSendResponse = SmsSendResponse("000000")
        every { smsService.sendSmsCertification(any()) } returns smsSendResponse

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { isOk() }
            content { ApiResponse.success(smsSendResponse) }
        }.andDo {
            createDocument("sms-send-success")
        }
    }

    @Test
    fun `전화번호 인증 전송 실패 - 서버 오류`() {
        every { smsService.sendSmsCertification(any()) } throws SmsSendException()

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { is5xxServerError() }
            content { ApiResponse.error(ErrorCode.SMS_SEND_FAILED.message) }
        }.andDo {
            createDocument("sms-send-fail-server")
        }
    }

    @Test
    fun `전화번호 인증 전송 실패 - 50건 초과`() {
        every { smsService.sendSmsCertification(any()) } throws SmsSendException()

        mockMvc.post("/api/v1/sms/send/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { is5xxServerError() }
            content { ApiResponse.error(ErrorCode.SMS_SENDING_OVER_REQUEST.message) }
        }.andDo {
            createDocument("sms-send-fail-client")
        }
    }

    @Test
    fun `전화번호 인증 완료`() {
        every { smsService.completeCertification(any()) } just runs

        mockMvc.post("/api/v1/sms/complete/certification") {
            jsonContent(createPhoneNumber())
        }.andExpect {
            status { isOk() }
        }.andDo {
            createDocument("sms-complete-success")
        }
    }

    @Test
    fun `전화번호 인증 실패 - 기록이 없을 때`() {
        val req = createPhoneNumber()

        val errorMessage: String = " receiver: ${req.get("phoneNumber")}"
        every { smsService.completeCertification(any()) } throws IllegalArgumentException(errorMessage)

        mockMvc.post("/api/v1/sms/complete/certification") {
            jsonContent(req)
        }.andExpect {
            status { isBadRequest() }
            content { ApiResponse.error(errorMessage) }
        }.andDo {
            createDocument("sms-complete-fail")
        }
    }
}
