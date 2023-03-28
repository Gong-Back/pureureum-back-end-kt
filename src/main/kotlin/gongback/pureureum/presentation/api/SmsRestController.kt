package gongback.pureureum.presentation.api

import gongback.pureureum.application.SmsService
import gongback.pureureum.application.UserAuthenticationService
import gongback.pureureum.application.dto.ErrorCode
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
    private val smsService: SmsService,
    private val userAuthenticationService: UserAuthenticationService
) {
    @PostMapping("/send/certification")
    fun sendSmsCertification(
        @RequestBody @Valid phoneNumberReq: PhoneNumberReq
    ): ResponseEntity<ApiResponse<Any>> {
        try {
            userAuthenticationService.checkDuplicatedPhoneNumber(phoneNumberReq.phoneNumber)
            val smsSendResponse = smsService.sendSmsCertification(phoneNumberReq)
            return ResponseEntity.ok().body(ApiResponse.ok(smsSendResponse))
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(
                    ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.code,
                    ErrorCode.REQUEST_RESOURCE_ALREADY_EXISTS.message,
                    userAuthenticationService.getUserAccountDto(phoneNumberReq.phoneNumber)
                )
            )
        }
    }

    @PostMapping("/complete/certification")
    fun completeSmsCertification(
        @RequestBody @Valid phoneNumberReq: PhoneNumberReq
    ): ResponseEntity<ApiResponse<SmsSendResponse>> {
        smsService.completeCertification(phoneNumberReq)
        return ResponseEntity.ok().build()
    }
}
