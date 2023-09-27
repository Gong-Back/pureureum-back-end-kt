package gongback.pureureum.application

import gongback.pureureum.domain.dashboard.DashboardMember
import gongback.pureureum.domain.dashboard.DashboardMemberRole
import gongback.pureureum.domain.dashboard.DashboardRepository
import gongback.pureureum.domain.dashboard.getDashboardById
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import support.PROFILE_URL
import support.createDashboard
import support.createDashboardMember
import support.createUser

class DashboardReadServiceTest : BehaviorSpec({
    val fileService = mockk<FileService>()
    val dashboardRepository = mockk<DashboardRepository>()
    val dashboardReadService = DashboardReadService(fileService, dashboardRepository)

    Given("대시보드 아이디, 사용자 이메일") {
        val user = createUser(email = "user")
        val dashboardManager = createDashboardMember(createUser(email = "manager"), DashboardMemberRole.MANAGER)

        When("대시보드에 참여한 사용자가 대시보드 사용자 목록을 조회하는 경우") {
            val dashboardMembers = listOf(dashboardManager, DashboardMember(user, DashboardMemberRole.MEMBER))
            val dashboard = createDashboard(members = dashboardMembers)
            every { dashboardRepository.getDashboardById(any()) } returns dashboard
            every { fileService.getFileUrl(any()) } returns PROFILE_URL

            Then("정상적으로 사용자 목록을 반환한다") {
                dashboardReadService.getDashboardMembers(1L, user.email).size shouldBe 2
            }
        }

        When("대시보드에 참여하지 않은 사용자가 대시보드 사용자 목록을 조회하는 경우") {
            val dashboardMembers = listOf(dashboardManager)
            val dashboard = createDashboard(members = dashboardMembers)
            every { dashboardRepository.getDashboardById(any()) } returns dashboard

            Then("예외가 발생한다") {
                shouldThrow<PureureumException> {
                    dashboardReadService.getDashboardMembers(1L, user.email)
                }
            }
        }
    }
})
