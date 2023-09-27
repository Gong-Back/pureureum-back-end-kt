package gongback.pureureum.application.dto

import gongback.pureureum.domain.dashboard.DashboardMember
import gongback.pureureum.domain.dashboard.DashboardMemberRole

data class DashboardMemberRes(
    val id: Long,
    val name: String,
    val role: DashboardMemberRole,
    val profileUrl: String
) {
    companion object {
        fun fromDashboardMember(dashboardMember: DashboardMember, profileUrl: String): DashboardMemberRes =
            DashboardMemberRes(
                dashboardMember.user.id,
                dashboardMember.user.name,
                dashboardMember.userProjectRole,
                profileUrl
            )
    }
}
