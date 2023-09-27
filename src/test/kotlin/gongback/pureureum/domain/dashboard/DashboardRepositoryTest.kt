package gongback.pureureum.domain.dashboard

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode
import io.kotest.matchers.shouldBe
import support.createDashboard
import support.test.BaseTests.RepositoryTest

@RepositoryTest
class DashboardRepositoryTest(
    private val dashboardRepository: DashboardRepository
) : ExpectSpec({
    extensions(SpringTestExtension(SpringTestLifecycleMode.Root))

    context("대시보드 아이디에 따른 존재 여부 확인") {
        val dashboard = createDashboard()
        dashboardRepository.save(dashboard)

        expect("대시보드가 존재한다") {
            val result = dashboardRepository.getDashboardById(dashboard.id)
            result shouldBe dashboard
        }

        expect("대시보드가 존재하지 않는다") {
            shouldThrow<NoSuchElementException> {
                dashboardRepository.getDashboardById(100L)
            }
        }
    }
})
