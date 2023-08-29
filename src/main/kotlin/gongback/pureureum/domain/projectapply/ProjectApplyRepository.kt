package gongback.pureureum.domain.projectapply

import gongback.pureureum.domain.project.Project
import org.springframework.data.jpa.repository.JpaRepository

fun ProjectApplyRepository.existsByProjectAndUserId(project: Project, userId: Long): Boolean =
    findByProjectAndUserId(project, userId) != null

interface ProjectApplyRepository : JpaRepository<ProjectApply, Long> {
    fun findByProjectAndUserId(project: Project, userId: Long): ProjectApply?
}
