package gongback.pureureum.domain.sms

import gongback.pureureum.application.createSmsLog
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class SmsLogRepositoryTest(
    private val smsLogRepository: SmsLogRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("마지막 인증 기록 조회") {
        val receiver = "010-0000-0000"
        val smsLog = createSmsLog(receiver = receiver, isSuccess = false)
        smsLogRepository.save(smsLog)

        expect("마지막 인증 기록이 존재한다.") {
            val result = smsLogRepository.getLastSmsLog(receiver)
            result.receiver shouldBe receiver
        }
    }
})
