package gongback.pureureum.domain.sms

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

fun SmsLogRepository.getLastSmsLog(receiver: String): SmsLog =
    findFirstByReceiverOrderByCreatedDateDesc(receiver)
        ?: throw IllegalArgumentException("본인 인증 요청을 하지 않은 사용자입니다, receiver: $receiver")
interface SmsLogRepository : JpaRepository<SmsLog, Long> {
    @Query("select count(sl) from SmsLog sl")
    fun getTotalSize(): Long

    fun findFirstByReceiverOrderByCreatedDateDesc(receiver: String): SmsLog?

    @Modifying
    fun deleteByReceiver(receiver: String)
}
