package gongback.pureureum.application

import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class SmsLogServiceTest : BehaviorSpec({
    val smsLogRepository = mockk<SmsLogRepository>()

    val smsLogService = SmsLogService(smsLogRepository)

    Given("수신자 번호") {
        val receiver = "010-0000-0000"

        When("Sms 전송을 성공하면") {
            val smsLog = createSmsLog(receiver, false)
            every { smsLogRepository.save(any()) } returns smsLog

            Then("sms 정보를 저장한다.") {
                smsLogService.save(receiver)
            }
        }

        When("인증이 완료된 사람이면") {
            val smsLog = createSmsLog(receiver, true)
            every { smsLogRepository.getLastSmsLog(any()) } returns smsLog

            Then("True를 반환한다.") {
                smsLogService.isCertificated(receiver) shouldBe true
            }
        }

        When("인증이 완료되지 않은 사람이면") {
            val smsLog = createSmsLog(receiver, false)
            every { smsLogRepository.getLastSmsLog(any()) } returns smsLog

            Then("False를 반환한다.") {
                smsLogService.isCertificated(receiver) shouldBe false
            }
        }
    }
})
