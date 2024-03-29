package gongback.pureureum.domain.sms

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity

@Entity
class SmsLog(
    @Column
    val receiver: String,

    isSuccess: Boolean = false
) : BaseEntity() {
    final var isSuccess: Boolean = isSuccess
        protected set

    fun completeCertification() {
        this.isSuccess = true
    }
}
