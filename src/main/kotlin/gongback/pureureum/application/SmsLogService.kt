package gongback.pureureum.application

import gongback.pureureum.domain.sms.SmsLog
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class SmsLogService(
    private val smsLogRepository: SmsLogRepository
) {
    @Transactional
    fun save(receiver: String) {
        smsLogRepository.save(SmsLog(receiver = receiver))
    }

    fun getTotalSize(): Long {
        return smsLogRepository.getTotalSize()
    }

    fun isCertificated(phoneNumber: String): Boolean {
        return smsLogRepository.getLastSmsLog(phoneNumber).isSuccess
    }

    @Transactional
    fun completeCertification(phoneNumber: String) {
        smsLogRepository.getLastSmsLog(phoneNumber).completeCertification()
    }
}
