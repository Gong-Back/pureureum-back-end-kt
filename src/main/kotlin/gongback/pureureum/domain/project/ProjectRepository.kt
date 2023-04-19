package gongback.pureureum.domain.project

import org.springframework.data.jpa.repository.JpaRepository

fun ProjectRepository.getProjectById(id: Long): Project =
    findProjectById(id) ?: throw IllegalArgumentException("요청하신 프로젝트 정보를 찾을 수 없습니다")

interface ProjectRepository : JpaRepository<Project, Long> {
    fun findProjectById(id: Long): Project?
}
