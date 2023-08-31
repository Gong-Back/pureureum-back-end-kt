package gongback.pureureum.domain.projectapply

import org.springframework.data.jpa.repository.JpaRepository

fun ProjectApplyRepository.existsByProjectIdAndUserId(projectId: Long, userId: Long): Boolean =
    findByProjectIdAndUserId(projectId, userId) != null

interface ProjectApplyRepository : JpaRepository<ProjectApply, Long> {
    fun findByProjectIdAndUserId(projectId: Long, userId: Long): ProjectApply?
}
