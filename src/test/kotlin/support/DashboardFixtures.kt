package support

import gongback.pureureum.application.dto.DashboardMemberRes
import gongback.pureureum.domain.dashboard.Dashboard
import gongback.pureureum.domain.dashboard.DashboardMember
import gongback.pureureum.domain.dashboard.DashboardMemberRole
import gongback.pureureum.domain.dashboardbulletinboard.DashboardBulletinBoard
import gongback.pureureum.domain.dashboardcalendar.DashboardCalendar
import gongback.pureureum.domain.dashboardgallery.DashboardGallery
import gongback.pureureum.domain.dashboardnotice.DashboardNotice
import gongback.pureureum.domain.user.User

fun createDashboardMembersRes(): List<DashboardMemberRes> {
    return listOf(
        DashboardMemberRes(1, "name1", DashboardMemberRole.MANAGER, "profileKey1"),
        DashboardMemberRes(2, "name2", DashboardMemberRole.MEMBER, "profileKey2")
    )
}

// TODO 임시로 만든 Fixture
fun createDashboard(
    projectId: Long = 0L,
    members: List<DashboardMember> = emptyList(),
    bulletinBoards: List<DashboardBulletinBoard> = emptyList(),
    bulletinNotices: List<DashboardNotice> = emptyList(),
    calendars: List<DashboardCalendar> = emptyList(),
    galleries: List<DashboardGallery> = emptyList()
): Dashboard =
    Dashboard(
        projectId = projectId,
        members = members,
        bulletinBoards = bulletinBoards,
        bulletinNotices = bulletinNotices,
        calendars = calendars,
        galleries = galleries
    )

fun createDashboardMember(
    user: User,
    role: DashboardMemberRole
): DashboardMember =
    DashboardMember(user, role)
