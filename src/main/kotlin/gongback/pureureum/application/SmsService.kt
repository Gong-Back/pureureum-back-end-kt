package gongback.pureureum.application

import gongback.pureureum.application.dto.PhoneNumberReq
import gongback.pureureum.application.dto.SmsRequestDto
import gongback.pureureum.application.dto.SmsSendResponse
import gongback.pureureum.domain.sms.SmsLog
import gongback.pureureum.domain.sms.SmsLogRepository
import gongback.pureureum.domain.sms.getLastSmsLog
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

private const val CERTIFICATION_NUMBER_SIZE: Int = 6
private const val FIRST_DAY_OF_MONTH = 1

@Service
@Transactional(readOnly = true)
class SmsService(
    private val smsLogRepository: SmsLogRepository,
    private val smsSender: SmsSender
) {
    fun sendSmsCertification(phoneNumberReq: PhoneNumberReq): SmsSendResponse {
        val certificationNumber = generateCertificationNumber()
        val totalSizeOfMonth = getTotalSizeOfMonth()
        smsLogRepository.save(SmsLog(phoneNumberReq.phoneNumber))
        smsSender.send(SmsRequestDto(phoneNumberReq.receiver, certificationNumber, totalSizeOfMonth))
        return SmsSendResponse(certificationNumber = certificationNumber)
    }

    @Transactional
    fun completeCertification(phoneNumberReq: PhoneNumberReq) {
        smsLogRepository.getLastSmsLog(phoneNumberReq.phoneNumber).completeCertification()
    }

    private fun generateCertificationNumber(): String {
        return (FIRST_DAY_OF_MONTH..CERTIFICATION_NUMBER_SIZE).joinToString("") { (0..9).random().toString() }
    }

    private fun getTotalSizeOfMonth(): Long {
        val now = LocalDate.now()
        val startDate = LocalDate.of(now.year, now.month, FIRST_DAY_OF_MONTH)
        val endDate = LocalDate.of(now.year, now.month, getDaysByMonth(now.year, now.monthValue))

        return smsLogRepository.getTotalSizeByDate(startDate, endDate)
    }

    private fun getDaysByMonth(year: Int, month: Int): Int = when (month) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        2 -> if (isLeapYear(year)) 29 else 28
        else -> 30
    }

    private fun isLeapYear(year: Int): Boolean {
        return year and 3 == 0 && (year % 100 != 0 || year % 400 == 0)
    }
}
