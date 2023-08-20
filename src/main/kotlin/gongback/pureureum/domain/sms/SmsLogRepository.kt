package gongback.pureureum.domain.sms

import java.time.LocalDate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

fun SmsLogRepository.getLastSmsLog(receiver: String): SmsLog =
    findFirstByReceiverOrderByIdDesc(receiver)
        ?: throw NoSuchElementException("본인 인증 요청을 하지 않은 사용자입니다, receiver: $receiver")

interface SmsLogRepository : JpaRepository<SmsLog, Long> {
    @Query("select count(sl) from SmsLog sl where sl.createdDate between :startDate and :endDate")
    fun getTotalSizeByDate(@Param("startDate") startDate: LocalDate, @Param("endDate") endDate: LocalDate): Long

    fun findFirstByReceiverOrderByIdDesc(receiver: String): SmsLog?

    @Modifying
    fun deleteByReceiver(receiver: String)
}
