package gongback.pureureum.domain.dashboard

import org.springframework.data.jpa.repository.JpaRepository

fun DashboardRepository.getDashboardById(id: Long): Dashboard =
    findDashboardById(id) ?: throw NoSuchElementException("요청하신 대시보드 정보를 찾을 수 없습니다")

interface DashboardRepository : JpaRepository<Dashboard, Long> {
    fun findDashboardById(id: Long): Dashboard?
}
