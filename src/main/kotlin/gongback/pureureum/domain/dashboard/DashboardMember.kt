package gongback.pureureum.domain.dashboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_member")
class DashboardMember(

    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val userProjectRole: DashboardMemberRole
) : BaseEntity()
