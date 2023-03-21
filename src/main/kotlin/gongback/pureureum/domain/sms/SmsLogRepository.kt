package gongback.pureureum.domain.sms

import gongback.pureureum.application.dto.ErrorCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

fun SmsLogRepository.getLastSmsLog(receiver: String): SmsLog =
    findFirstByReceiverOrderByCreateDateDesc(receiver)
        ?: throw IllegalArgumentException(ErrorCode.NOT_FOUND.message + " receiver: $receiver")

interface SmsLogRepository : JpaRepository<SmsLog, Long> {
    @Query("select count(sl) from SmsLog sl")
    fun getTotalSize(): Int

    fun findFirstByReceiverOrderByCreateDateDesc(receiver: String): SmsLog?
}
