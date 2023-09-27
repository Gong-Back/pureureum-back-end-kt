package gongback.pureureum.domain.dashboard

import gongback.pureureum.domain.user.User
import gongback.pureureum.support.domain.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard_member")
class DashboardMember(

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    val userProjectRole: DashboardMemberRole
) : BaseEntity()
