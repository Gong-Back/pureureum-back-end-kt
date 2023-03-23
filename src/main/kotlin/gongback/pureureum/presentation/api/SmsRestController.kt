package gongback.pureureum.presentation.api

import gongback.pureureum.application.SmsService
import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsSendResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/sms")
class SmsRestController(
    private val smsService: SmsService
) {
    @PostMapping("/send/certification")
    fun sendSmsCertification(
        @RequestBody @Valid phoneNumberReq: PhoneNumberReq
    ): ResponseEntity<ApiResponse<SmsSendResponse>> {
        val smsSendResponse = smsService.sendSmsCertification(phoneNumberReq)
        return ResponseEntity.ok().body(ApiResponse.ok(smsSendResponse))
    }

    @PostMapping("/complete/certification")
    fun completeSmsCertification(
        @RequestBody @Valid phoneNumberReq: PhoneNumberReq
    ): ResponseEntity<ApiResponse<SmsSendResponse>> {
        smsService.completeCertification(phoneNumberReq)
        return ResponseEntity.ok().build()
    }
}
