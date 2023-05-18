package gongback.pureureum.application

import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsSendResponse
import gongback.pureureum.domain.sms.SmsLog
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.chrono.IsoChronology

@Service
@Transactional(readOnly = true)
class SmsService(
    private val smsLogRepository: SmsLogRepository,
    private val smsSender: SmsSender
) {

    private val numberSize: Int = 6;

    fun sendSmsCertification(phoneNumberReq: PhoneNumberReq): SmsSendResponse {
        val certificationNumber = generateCertificationNumber()
        val totalSizeOfMonth = getTotalSizeOfMonth()
        smsLogRepository.save(SmsLog(phoneNumberReq.phoneNumber))
        smsSender.send(phoneNumberReq.receiver, certificationNumber, totalSizeOfMonth)
        return SmsSendResponse(certificationNumber = certificationNumber)
    }

    @Transactional
    fun completeCertification(phoneNumberReq: PhoneNumberReq) {
        smsLogRepository.getLastSmsLog(phoneNumberReq.phoneNumber).completeCertification()
    }

    private fun generateCertificationNumber(): String {
        val randomCertificationNumber = StringBuilder()
        var size = numberSize
        while (size-- > 0) {
            randomCertificationNumber.append((0..9).random())
        }
        return randomCertificationNumber.toString()
    }

    private fun getTotalSizeOfMonth(): Long {
        val now = LocalDate.now()
        val startDate = LocalDate.of(now.year, now.month, 1)
        val endDate = LocalDate.of(now.year, now.month, getDaysByMonth(now.year.toLong(), now.monthValue))

        return smsLogRepository.getTotalSizeByDate(startDate, endDate)
    }

    private fun getDaysByMonth(year: Long, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        2 -> if (IsoChronology.INSTANCE.isLeapYear(year)) 29 else 28
        else -> 30
    }
}
