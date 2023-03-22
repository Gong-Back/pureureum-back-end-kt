package gongback.pureureum.domain.sms

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class SmsLog(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    val receiver: String,

    isSuccess: Boolean = false,

    val createDate: LocalDateTime = LocalDateTime.now()
) {
    var isSuccess: Boolean = isSuccess
        private set

    fun completeCertification() {
        this.isSuccess = true
    }
}
