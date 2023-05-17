package gongback.pureureum.domain.dashboard

import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_member", indexes = [Index(name = "idx_dashboard_member_user_id", columnList = "user_id")])
class DashboardMember(

    @Column(nullable = false, name = "user_id")
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val userProjectRole: DashboardMemberRole
) : BaseEntity()
