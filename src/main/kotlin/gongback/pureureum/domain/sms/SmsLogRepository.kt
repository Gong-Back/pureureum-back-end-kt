package gongback.pureureum.domain.sms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

fun SmsLogRepository.getLastSmsLog(receiver: String): SmsLog =
    findFirstByReceiverOrderByCreateDateDesc(receiver)
        ?: throw IllegalArgumentException("요청하신 정보를 찾을 수 없습니다, receiver: $receiver")

interface SmsLogRepository : JpaRepository<SmsLog, Long> {
    @Query("select count(sl) from SmsLog sl")
    fun getTotalSize(): Long

    fun findFirstByReceiverOrderByCreateDateDesc(receiver: String): SmsLog?

    @Modifying
    fun deleteByReceiver(receiver: String)
}
