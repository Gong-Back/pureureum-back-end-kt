package gongback.pureureum.application

import gongback.pureureum.application.dto.DashboardMemberRes
import gongback.pureureum.application.dto.ErrorCode
import gongback.pureureum.domain.dashboard.Dashboard
import gongback.pureureum.domain.dashboard.DashboardRepository
import gongback.pureureum.domain.dashboard.getDashboardById
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DashboardReadService(
    private val fileService: FileService,
    private val dashboardRepository: DashboardRepository
) {
    fun getDashboardMembers(dashboardId: Long, userEmail: String): List<DashboardMemberRes> {
        val dashboard: Dashboard = dashboardRepository.getDashboardById(dashboardId)
        val dashboardMembers = dashboard.members
        if (dashboardMembers.all { it.user.email != userEmail }) {
            throw PureureumException(message = "대시보드에 참여하지 않은 사용자입니다", errorCode = ErrorCode.FORBIDDEN)
        }
        return dashboardMembers.map {
            DashboardMemberRes.fromDashboardMember(it, fileService.getFileUrl(it.user.profile.fileKey))
        }
    }
}
