package gongback.pureureum.domain.project

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

/**
 * amount: 금액
 * refundInstruction: 환불 정책
 * depositInformation: 입금 계좌 정보
 */
@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(name = "uk_project_id", columnNames = ["project_id"])
    ]
)
class ProjectPayment(
    @Column(nullable = false)
    val amount: Int = 0,

    @Column(nullable = true, length = 500)
    val refundInstruction: String? = null,

    @Column(nullable = true, length = 500)
    val depositInformation: String? = null
) : BaseEntity()
