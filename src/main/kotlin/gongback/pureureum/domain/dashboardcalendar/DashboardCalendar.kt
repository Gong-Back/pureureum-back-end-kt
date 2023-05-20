package gongback.pureureum.domain.dashboardcalendar

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "dashboard_calendar")
class DashboardCalendar(
    @Column(nullable = false)
    val userId: Long,

    @Column(nullable = false, name = "start_date")
    val startDate: LocalDateTime,

    @Column(nullable = false, name = "end_date")
    val endDate: LocalDateTime,

    @Column(length = 30, nullable = false)
    val content: String
) : BaseEntity()
