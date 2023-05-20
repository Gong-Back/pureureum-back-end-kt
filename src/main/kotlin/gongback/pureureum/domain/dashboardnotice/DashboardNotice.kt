package gongback.pureureum.domain.dashboardnotice

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Lob
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_notice")
class DashboardNotice(
    @Column(nullable = false)
    val userId: Long,

    @Column(length = 50, nullable = false)
    val title: String,

    @Lob
    @Column(nullable = false)
    val content: String
) : BaseEntity()
